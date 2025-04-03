package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.payment.service.PaymentService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RentalService rentalService;

    @MockitoBean
    private CreditCardService creditCardService;

    private UUID userId;
    private User testUser;
    private AuthenticationDetails authDetails;
    private Rental testRental;
    private CreditCard testCreditCard;
    private Payment testPayment;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        testUser = User.builder()
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        testUser.setId(userId);

        authDetails = new AuthenticationDetails(
                userId, testUser.getUsername(), "password", UserRole.CUSTOMER, true);

        UUID rentalId = UUID.randomUUID();

        Car car = Car.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .pricePerDay(new BigDecimal("50.00"))
                .build();

        Location pickupLocation = Location.builder()
                .id(1L)
                .name("Airport")
                .city(City.SOFIA)
                .build();

        Location dropoffLocation = Location.builder()
                .id(2L)
                .name("Downtown")
                .city(City.PLOVDIV)
                .build();

        testRental = Rental.builder()
                .id(rentalId)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalPrice(new BigDecimal("150.00"))
                .status(RentalStatus.RESERVED)
                .user(testUser)
                .car(car)
                .pickupLocation(pickupLocation)
                .dropOffLocation(dropoffLocation)
                .build();

        UUID creditCardId = UUID.randomUUID();
        testCreditCard = CreditCard.builder()
                .id(creditCardId)
                .cardNumber("4111111111111111")
                .cardholderName("Test User")
                .expirationDate("12/25")
                .amount(new BigDecimal("1000.00"))
                .user(testUser)
                .build();

        UUID paymentId = UUID.randomUUID();
        testPayment = Payment.builder()
                .id(paymentId)
                .rental(testRental)
                .creditCard(testCreditCard)
                .user(testUser)
                .amount(new BigDecimal("150.00"))
                .paymentDate(LocalDate.now())
                .build();
    }

    @Test
    void showPaymentForm_ShouldDisplayForm() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(rentalService.getRentalById(testRental.getId())).thenReturn(testRental);
        when(paymentService.getPaymentByRental(testRental)).thenReturn(Optional.empty());
        when(creditCardService.getUserCreditCards(testUser)).thenReturn(Arrays.asList(testCreditCard));

        mockMvc.perform(get("/payments/process/{rentalId}", testRental.getId())
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-form"))
                .andExpect(model().attributeExists("paymentRequest"))
                .andExpect(model().attributeExists("creditCards"))
                .andExpect(model().attributeExists("rental"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getById(userId);
        verify(rentalService).getRentalById(testRental.getId());
        verify(paymentService).getPaymentByRental(testRental);
        verify(creditCardService).getUserCreditCards(testUser);
    }

    @Test
    void showPaymentForm_ForAlreadyPaidRental_ShouldRedirect() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(rentalService.getRentalById(testRental.getId())).thenReturn(testRental);
        when(paymentService.getPaymentByRental(testRental)).thenReturn(Optional.of(testPayment));

        mockMvc.perform(get("/payments/process/{rentalId}", testRental.getId())
                        .with(user(authDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(userId);
        verify(rentalService).getRentalById(testRental.getId());
        verify(paymentService).getPaymentByRental(testRental);
        verify(creditCardService, never()).getUserCreditCards(any(User.class));
    }

    @Test
    void showPaymentForm_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(rentalService.getRentalById(testRental.getId()))
                .thenThrow(new DomainException("Rental not found"));

        mockMvc.perform(get("/payments/process/{rentalId}", testRental.getId())
                        .with(user(authDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(userId);
        verify(rentalService).getRentalById(testRental.getId());
        verify(paymentService, never()).getPaymentByRental(any(Rental.class));
    }

    @Test
    void processPayment_WithValidData_ShouldProcessAndRedirect() throws Exception {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        when(userService.getById(userId)).thenReturn(testUser);
        when(paymentService.processPayment(any(PaymentRequest.class), eq(testUser))).thenReturn(testPayment);

        mockMvc.perform(post("/payments/process")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("paymentRequest", paymentRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).getById(userId);
        verify(paymentService).processPayment(any(PaymentRequest.class), eq(testUser));
    }

    @Test
    void processPayment_WithDomainException_ShouldHandleErrorAndRedirect() throws Exception {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        when(userService.getById(userId)).thenReturn(testUser);
        when(paymentService.processPayment(any(PaymentRequest.class), eq(testUser)))
                .thenThrow(new DomainException("Insufficient funds"));

        mockMvc.perform(post("/payments/process")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("paymentRequest", paymentRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(userId);
        verify(paymentService).processPayment(any(PaymentRequest.class), eq(testUser));
    }

    @Test
    void processPayment_WithGenericException_ShouldHandleErrorAndRedirect() throws Exception {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .rentalId(testRental.getId())
                .selectedCardId(testCreditCard.getId())
                .build();

        when(userService.getById(userId)).thenReturn(testUser);
        when(paymentService.processPayment(any(PaymentRequest.class), eq(testUser)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/payments/process")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("paymentRequest", paymentRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(userId);
        verify(paymentService).processPayment(any(PaymentRequest.class), eq(testUser));
    }
}