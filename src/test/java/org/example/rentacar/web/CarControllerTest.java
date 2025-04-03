package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private LocationService locationService;

    private UUID userId;
    private User testUser;
    private AuthenticationDetails authDetails;
    private Car testCar;

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

        testCar = Car.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .description("A comfortable sedan")
                .image("http://example.com/toyota-camry.jpg")
                .build();


        when(locationService.getAllLocations()).thenReturn(Collections.emptyList());
    }

    @Test
    void getAllCars_ShouldReturnCarsPage() throws Exception {

        List<Car> cars = Arrays.asList(
                testCar,
                Car.builder()
                        .id(2L)
                        .brand("Honda")
                        .model("Accord")
                        .year(2022)
                        .licensePlate("XYZ789")
                        .pricePerDay(new BigDecimal("45.00"))
                        .status(Status.AVAILABLE)
                        .build()
        );

        when(userService.getById(userId)).thenReturn(testUser);
        when(carService.getAllCars()).thenReturn(cars);


        mockMvc.perform(get("/cars")
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("cars"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("cars"));

        verify(userService).getById(userId);
        verify(carService).getAllCars();
    }

    @Test
    void getCarDetails_WithValidId_ShouldReturnDetailsPage() throws Exception {
        // Arrange
        when(userService.getById(userId)).thenReturn(testUser);
        when(carService.getCarById(1L)).thenReturn(testCar);


        List<Location> locations = Arrays.asList(
                Location.builder().id(1L).name("Airport").city(City.SOFIA).build(),
                Location.builder().id(2L).name("Downtown").city(City.PLOVDIV).build()
        );
        when(locationService.getAllLocations()).thenReturn(locations);

        mockMvc.perform(get("/cars/{carId}", 1L)
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("car-details"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("car"));

        verify(userService).getById(userId);
        verify(carService).getCarById(1L);
        verify(locationService, atLeastOnce()).getAllLocations();
    }

    @Test
    void getCarDetails_WithInvalidId_ShouldRedirectWithError() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(carService.getCarById(999L)).thenThrow(new DomainException("Car not found"));

        mockMvc.perform(get("/cars/{carId}", 999L)
                        .with(user(authDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).getById(userId);
        verify(carService).getCarById(999L);
    }
}