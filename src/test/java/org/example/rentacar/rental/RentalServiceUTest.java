package org.example.rentacar.rental;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.RentalCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalServiceUTest {
    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarService carService;

    @Mock
    private UserService userService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private RentalService rentalService;

    private UUID userId;
    private Long carId;
    private Long pickupLocationId;
    private Long dropOffLocationId;
    private UUID rentalId;
    private User testUser;
    private Car testCar;
    private Location pickupLocation;
    private Location dropoffLocation;
    private RentalCreateRequest validRentalRequest;
    private Rental existingRental;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        carId = 1L;
        pickupLocationId = 2L;
        dropOffLocationId = 3L;
        rentalId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testUser")
                .email("test@test.com")
                .build();

        testCar = Car.builder()
                .id(carId)
                .brand("Toyota")
                .model("Supra M4")
                .year(2008)
                .licensePlate("ENVME")
                .pricePerDay(new BigDecimal("142.00"))
                .status(Status.AVAILABLE)
                .build();

        pickupLocation = Location.builder()
                .id(pickupLocationId)
                .name("Pickup Location")
                .address("123 Main Street")
                .build();

        dropoffLocation = Location.builder()
                .id(dropOffLocationId)
                .name("Drop Off Location")
                .address("1235 Drop Street")
                .build();

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(2);

        validRentalRequest = new RentalCreateRequest();
        validRentalRequest.setCarId(carId);
        validRentalRequest.setStartDate(startDate);
        validRentalRequest.setEndDate(endDate);
        validRentalRequest.setPickupLocationId(pickupLocationId);
        validRentalRequest.setDropoffLocationId(dropOffLocationId);

        existingRental = Rental.builder()
                .id(rentalId)
                .car(testCar)
                .user(testUser)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(new BigDecimal("284.00"))
                .status(RentalStatus.RESERVED)
                .pickupLocation(pickupLocation)
                .dropOffLocation(dropoffLocation)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void createRental_WithValidRequest_ShouldCreateRental() {

        when(carService.getCarById(carId)).thenReturn(testCar);
        when(userService.getById(testUser.getId())).thenReturn(testUser);
        when(locationService.getLocation(pickupLocationId)).thenReturn(pickupLocation);
        when(locationService.getLocation(dropOffLocationId)).thenReturn(dropoffLocation);
        when(rentalRepository.findOverlappingRentals(carId, validRentalRequest.getStartDate(), validRentalRequest.getEndDate()))
                .thenReturn(new ArrayList<>());
        when(rentalRepository.save(any(Rental.class))).thenReturn(existingRental);

        Rental createdRental = rentalService.createRental(validRentalRequest,userId);

        assertEquals(existingRental, createdRental);
        verify(carService).updateCarStatus(carId, Status.RENTED);

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());

        Rental capturedRental = rentalCaptor.getValue();
        assertEquals(testCar, capturedRental.getCar());
        assertEquals(testUser, capturedRental.getUser());
        assertEquals(validRentalRequest.getStartDate(), capturedRental.getStartDate());
        assertEquals(validRentalRequest.getEndDate(), capturedRental.getEndDate());
        assertEquals(new BigDecimal("284.00"), capturedRental.getTotalPrice());
        assertEquals(RentalStatus.RESERVED, capturedRental.getStatus());

    }

    @Test
    void createRental_WithUnavailableCar_ShouldThrowException() {
        testCar.setStatus(Status.RENTED);
        when(carService.getCarById(carId)).thenReturn(testCar);
        when(userService.getById(testUser.getId())).thenReturn(testUser);
        when(locationService.getLocation(pickupLocationId)).thenReturn(pickupLocation);
        when(locationService.getLocation(dropOffLocationId)).thenReturn(dropoffLocation);

        assertThrows(DomainException.class, () -> rentalService.createRental(validRentalRequest,userId));
        verify(rentalRepository,never()).save(any(Rental.class));
        verify(carService,never()).updateCarStatus(anyLong(),any(Status.class));
    }

    @Test
    void createRental_WithOverlappingRentals_ShouldThrowException() {

        when(carService.getCarById(carId)).thenReturn(testCar);
        when(userService.getById(testUser.getId())).thenReturn(testUser);
        when(locationService.getLocation(pickupLocationId)).thenReturn(pickupLocation);
        when(locationService.getLocation(dropOffLocationId)).thenReturn(dropoffLocation);
        when(rentalRepository.findOverlappingRentals(carId, validRentalRequest.getStartDate(), validRentalRequest.getEndDate()))
                .thenReturn(List.of(existingRental));

        assertThrows(DomainException.class, () -> rentalService.createRental(validRentalRequest,userId));
        verify(rentalRepository,never()).save(any(Rental.class));
        verify(carService,never()).updateCarStatus(anyLong(),any(Status.class));
    }

    @Test
    void cancelRental_WithValidRental_ShouldCancelRental() {

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(existingRental));

        rentalService.cancelRental(rentalId, userId);

        assertEquals(RentalStatus.CANCELLED, existingRental.getStatus());
        verify(carService).updateCarStatus(carId, Status.AVAILABLE);
        verify(rentalRepository).save(existingRental);
    }
}
