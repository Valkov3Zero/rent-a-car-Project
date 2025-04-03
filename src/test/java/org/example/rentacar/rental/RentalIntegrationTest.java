package org.example.rentacar.rental;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.repository.CarRepository;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.repository.LocationRepository;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.repository.UserRepository;
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
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RentalIntegrationTest {


    @Autowired
    private RentalService rentalService;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    private User testUser;
    private Car testCar;
    private Location pickupLocation;
    private Location dropoffLocation;

    @BeforeEach
    void setup() {

        testUser = User.builder()
                .username("rentaluser")
                .password("password")
                .email("rental@example.com")
                .firstName("Rental")
                .lastName("User")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testCar = Car.builder()
                .brand("Rental Car Brand")
                .model("Rental Model")
                .year(2023)
                .licensePlate("RENT123")
                .pricePerDay(new BigDecimal("60.00"))
                .status(Status.AVAILABLE)
                .build();
        testCar = carRepository.save(testCar);

        pickupLocation = Location.builder()
                .name("Pickup Location")
                .address("Pickup Address")
                .city(City.SOFIA)
                .build();
        pickupLocation = locationRepository.save(pickupLocation);

        dropoffLocation = Location.builder()
                .name("Dropoff Location")
                .address("Dropoff Address")
                .city(City.PLOVDIV)
                .build();
        dropoffLocation = locationRepository.save(dropoffLocation);
    }

    @AfterEach
    void cleanup() {
        rentalRepository.deleteAll();
        carRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void createRental_WithValidData_ShouldCreateRental() {

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(testCar.getId())
                .startDate(startDate)
                .endDate(endDate)
                .pickupLocationId(pickupLocation.getId())
                .dropoffLocationId(dropoffLocation.getId())
                .build();

        Rental createdRental = rentalService.createRental(rentalRequest, testUser.getId());

        assertNotNull(createdRental);
        assertNotNull(createdRental.getId());
        assertEquals(testUser.getId(), createdRental.getUser().getId());
        assertEquals(testCar.getId(), createdRental.getCar().getId());
        assertEquals(startDate, createdRental.getStartDate());
        assertEquals(endDate, createdRental.getEndDate());
        assertEquals(pickupLocation.getId(), createdRental.getPickupLocation().getId());
        assertEquals(dropoffLocation.getId(), createdRental.getDropOffLocation().getId());
        assertEquals(RentalStatus.RESERVED, createdRental.getStatus());

        BigDecimal expectedTotalPrice = testCar.getPricePerDay().multiply(new BigDecimal("4"));
        assertEquals(0, expectedTotalPrice.compareTo(createdRental.getTotalPrice()));

        Car updatedCar = carRepository.findById(testCar.getId()).orElseThrow();
        assertEquals(Status.RENTED, updatedCar.getStatus());
    }

    @Test
    @Transactional
    void createRental_WithUnavailableCar_ShouldThrowException() {

        testCar.setStatus(Status.MAINTENANCE);
        carRepository.save(testCar);

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(testCar.getId())
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .pickupLocationId(pickupLocation.getId())
                .dropoffLocationId(dropoffLocation.getId())
                .build();

        DomainException exception = assertThrows(DomainException.class, () -> {
            rentalService.createRental(rentalRequest, testUser.getId());
        });

        assertEquals("Car is not available", exception.getMessage());
    }

    @Test
    @Transactional
    void getUserRentals_ShouldReturnUserRentals() {

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(testCar.getId())
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .pickupLocationId(pickupLocation.getId())
                .dropoffLocationId(dropoffLocation.getId())
                .build();

        rentalService.createRental(rentalRequest, testUser.getId());

        List<Rental> userRentals = rentalService.getUserRentals(testUser.getId());

        assertEquals(1, userRentals.size());
        assertEquals(testUser.getId(), userRentals.get(0).getUser().getId());
    }

    @Test
    @Transactional
    void updateRentalStatus_ShouldUpdateStatus() {

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(testCar.getId())
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .pickupLocationId(pickupLocation.getId())
                .dropoffLocationId(dropoffLocation.getId())
                .build();

        Rental createdRental = rentalService.createRental(rentalRequest, testUser.getId());

        Rental updatedRental = rentalService.updateRentalStatus(createdRental.getId(), RentalStatus.RESERVED_PAID);

        assertEquals(RentalStatus.RESERVED_PAID, updatedRental.getStatus());

        Rental dbRental = rentalService.getRentalById(createdRental.getId());
        assertEquals(RentalStatus.RESERVED_PAID, dbRental.getStatus());
    }
}
