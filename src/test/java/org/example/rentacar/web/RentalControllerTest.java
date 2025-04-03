package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.RentalCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalController.class)
public class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalService rentalService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RentalRepository rentalRepository;

    private UUID userId;
    private User testUser;
    private AuthenticationDetails authDetails;
    private Rental testRental;

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
                .status(Status.AVAILABLE)
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
    }

    @Test
    void getUserRentals_ShouldReturnRentalsPage() throws Exception {

        List<Rental> rentals = Arrays.asList(testRental);

        when(userService.getById(userId)).thenReturn(testUser);
        when(rentalService.getUserRentals(userId)).thenReturn(rentals);

        mockMvc.perform(get("/rentals")
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals"))
                .andExpect(model().attributeExists("rentals"))
                .andExpect(model().attributeExists("user"));

        verify(rentalService).getUserRentals(userId);
    }

    @Test
    void createRental_WithValidData_ShouldRedirectToRentals() throws Exception {

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .pickupLocationId(1L)
                .dropoffLocationId(2L)
                .build();

        when(rentalService.createRental(any(RentalCreateRequest.class), eq(userId)))
                .thenReturn(testRental);


        mockMvc.perform(post("/rentals/create")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("rentalCreateRequest", rentalRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(rentalService).createRental(any(RentalCreateRequest.class), eq(userId));
    }

    @Test
    void createRental_WithException_ShouldRedirectToCarDetails() throws Exception {

        RentalCreateRequest rentalRequest = RentalCreateRequest.builder()
                .carId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .pickupLocationId(1L)
                .dropoffLocationId(2L)
                .build();

        when(rentalService.createRental(any(RentalCreateRequest.class), eq(userId)))
                .thenThrow(new DomainException("Car is not available"));

        mockMvc.perform(post("/rentals/create")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("rentalCreateRequest", rentalRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/1"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(rentalService).createRental(any(RentalCreateRequest.class), eq(userId));
    }

    @Test
    void cancelRental_ShouldCancelAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        doNothing().when(rentalService).cancelRental(rentalId, userId);

        mockMvc.perform(post("/rentals/{rentalId}/cancel", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(rentalService).cancelRental(rentalId, userId);
    }

    @Test
    void cancelRental_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        doThrow(new DomainException("Cannot cancel a rental that is already in progress"))
                .when(rentalService).cancelRental(rentalId, userId);


        mockMvc.perform(post("/rentals/{rentalId}/cancel", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(rentalService).cancelRental(rentalId, userId);
    }

    @Test
    void startRental_ShouldStartAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        when(rentalService.startRental(rentalId, userId)).thenReturn(testRental);

        mockMvc.perform(post("/rentals/{rentalId}/start", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(rentalService).startRental(rentalId, userId);
    }

    @Test
    void startRental_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        when(rentalService.startRental(rentalId, userId))
                .thenThrow(new DomainException("Cannot start an unpaid rental"));

        mockMvc.perform(post("/rentals/{rentalId}/start", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(rentalService).startRental(rentalId, userId);
    }

    @Test
    void completeRental_ShouldCompleteAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        when(rentalService.completeRental(rentalId, userId)).thenReturn(testRental);

        mockMvc.perform(post("/rentals/{rentalId}/complete", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(rentalService).completeRental(rentalId, userId);
    }

    @Test
    void completeRental_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        UUID rentalId = testRental.getId();
        when(rentalService.completeRental(rentalId, userId))
                .thenThrow(new DomainException("Cannot complete a rental that is not in progress"));

        mockMvc.perform(post("/rentals/{rentalId}/complete", rentalId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(rentalService).completeRental(rentalId, userId);
    }
}