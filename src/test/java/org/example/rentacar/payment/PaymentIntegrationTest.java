package org.example.rentacar.payment;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.repository.CarRepository;
import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.repository.CreditCardRepository;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.repository.LocationRepository;
import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.payment.repository.PaymentRepository;
import org.example.rentacar.payment.service.PaymentService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.repository.UserRepository;
import org.example.rentacar.web.dto.PaymentRequest;
import org.example.rentacar.web.dto.RentalCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PaymentIntegrationTest {


    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    private User testUser;
    private Car testCar;
    private Location pickupLocation;
    private Location dropoffLocation;
    private Rental testRental;
    private CreditCard testCreditCard;

    @BeforeEach
    void setup() {

        testUser = User.builder()
                .username("paymentuser")
                .password("password")
                .email("payment@example.com")
                .firstName("Payment")
                .lastName("User")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testCreditCard = CreditCard.builder()
                .cardNumber("4111111111111111")
                .cardholderName("Payment User")
                .expirationDate("12/25")
                .amount(new BigDecimal("1000.00"))
                .user(testUser)
                .build();
        testCreditCard = creditCardRepository.save(testCreditCard);

        testCar = Car.builder()
                .brand("Payment Car Brand")
                .model("Payment Model")
                .year(2023)
                .licensePlate("PAY123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();
        testCar = carRepository.save(testCar);

        pickupLocation = Location.builder()
                .name("Payment Pickup")
                .address("Payment Pickup Address")
                .city(City.SOFIA)
                .build();
        pickupLocation = locationRepository.save(pickupLocation);

        dropoffLocation = Location.builder()
                .name("Payment Dropoff")
                .address("Payment Dropoff Address")
                .city(City.PLOVDIV)
                .build();
        dropoffLocation = locationRepository.save(dropoffLocation);

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(testCar.getId())
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .pickupLocationId(pickupLocation.getId())
                .dropoffLocationId(dropoffLocation.getId())
                .build();

        testRental = rentalService.createRental(rentalRequest, testUser.getId());
    }

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
        rentalRepository.deleteAll();
        creditCardRepository.deleteAll();
        carRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void processPayment_WithValidData_ShouldCreatePayment() {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        Payment payment = paymentService.processPayment(paymentRequest, testUser);

        assertNotNull(payment);
        assertNotNull(payment.getId());
        assertEquals(testUser.getId(), payment.getUser().getId());
        assertEquals(testRental.getId(), payment.getRental().getId());
        assertEquals(testCreditCard.getId(), payment.getCreditCard().getId());
        assertEquals(0, payment.getAmount().compareTo(testRental.getTotalPrice()));
        assertEquals(LocalDate.now(), payment.getPaymentDate());

        Rental updatedRental = rentalRepository.findById(testRental.getId()).orElseThrow();
        assertEquals(RentalStatus.RESERVED_PAID, updatedRental.getStatus());

        CreditCard updatedCard = creditCardRepository.findById(testCreditCard.getId()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("1000.00").subtract(testRental.getTotalPrice());
        assertEquals(0, updatedCard.getAmount().compareTo(expectedBalance));
    }

    @Test
    @Transactional
    void processPayment_WithInsufficientFunds_ShouldThrowException() {

        testCreditCard.setAmount(new BigDecimal("10.00")); // Set low balance
        creditCardRepository.save(testCreditCard);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        DomainException exception = assertThrows(DomainException.class, () -> {
            paymentService.processPayment(paymentRequest, testUser);
        });

        assertEquals("You do not have enough money!", exception.getMessage());

        Rental rental = rentalRepository.findById(testRental.getId()).orElseThrow();
        assertEquals(RentalStatus.RESERVED, rental.getStatus());
    }

    @Test
    @Transactional
    void processPayment_ForAlreadyPaidRental_ShouldThrowException() {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        paymentService.processPayment(paymentRequest, testUser);

        DomainException exception = assertThrows(DomainException.class, () -> {
            paymentService.processPayment(paymentRequest, testUser);
        });

        assertEquals("This rental is already paid!", exception.getMessage());
    }

    @Test
    @Transactional
    void processPayment_ForOtherUsersRental_ShouldThrowException() {

        final User otherUser = User.builder()
                .username("otheruser")
                .password("password")
                .email("other@example.com")
                .firstName("Other")
                .lastName("User")
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(otherUser);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        DomainException exception = assertThrows(DomainException.class, () -> {
            paymentService.processPayment(paymentRequest, otherUser);
        });

        assertEquals("You can only pay for your own rentals!", exception.getMessage());
    }

    @Test
    @Transactional
    void getPaymentByRental_ShouldReturnPayment() {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        Payment createdPayment = paymentService.processPayment(paymentRequest, testUser);

        Optional<Payment> foundPayment = paymentService.getPaymentByRental(testRental);

        assertTrue(foundPayment.isPresent());
        assertEquals(createdPayment.getId(), foundPayment.get().getId());
    }
}
