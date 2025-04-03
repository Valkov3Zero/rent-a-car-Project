package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.rental.service.RentalSchedulingService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.CarCreateRequest;
import org.example.rentacar.web.dto.LocationCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private RentalSchedulingService rentalSchedulingService;

    private UUID userId;
    private User adminUser;
    private AuthenticationDetails authDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminUser = User.builder()
                .username("admin")
                .role(UserRole.ADMIN)
                .build();
        adminUser.setId(userId);

        authDetails = new AuthenticationDetails(
                userId, "admin", "password", UserRole.ADMIN, true);
    }

    @Test
    void getAdminDashboard_ShouldReturnDashboardWithData() throws Exception {

        List<Car> cars = Arrays.asList(
                Car.builder().id(1L).brand("Toyota").model("Camry").build(),
                Car.builder().id(2L).brand("Honda").model("Accord").build()
        );

        List<Location> locations = Arrays.asList(
                Location.builder().id(1L).name("Airport").city(City.SOFIA).build(),
                Location.builder().id(2L).name("Downtown").city(City.PLOVDIV).build()
        );

        when(userService.getById(userId)).thenReturn(adminUser);
        when(carService.getAllCars()).thenReturn(cars);
        when(locationService.getAllLocations()).thenReturn(locations);


        mockMvc.perform(get("/admin/dashboard")
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("cities"));

        verify(carService).getAllCars();
        verify(locationService).getAllLocations();
    }

    @Test
    void addCar_WithValidData_ShouldAddCarAndRedirect() throws Exception {

        CarCreateRequest carRequest = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        Car newCar = Car.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .build();

        when(carService.createCar(any(CarCreateRequest.class))).thenReturn(newCar);

        mockMvc.perform(post("/admin/cars/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("carCreateRequest", carRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(carService).createCar(any(CarCreateRequest.class));
    }

    @Test
    void addCar_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        CarCreateRequest carRequest = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        when(carService.createCar(any(CarCreateRequest.class)))
                .thenThrow(new DomainException("Car with this license plate already exists"));

        mockMvc.perform(post("/admin/cars/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("carCreateRequest", carRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(carService).createCar(any(CarCreateRequest.class));
    }

    @Test
    void deleteCar_ShouldDeleteCarAndRedirect() throws Exception {

        Long carId = 1L;
        Car car = Car.builder()
                .id(carId)
                .brand("Toyota")
                .model("Camry")
                .build();

        when(carService.getCarById(carId)).thenReturn(car);
        doNothing().when(carService).deleteCar(carId);

        mockMvc.perform(post("/admin/cars/delete/{id}", carId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(carService).getCarById(carId);
        verify(carService).deleteCar(carId);
    }

    @Test
    void deleteCar_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        Long carId = 1L;

        when(carService.getCarById(carId))
                .thenThrow(new DomainException("Car not found"));

        mockMvc.perform(post("/admin/cars/delete/{id}", carId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(carService).getCarById(carId);
        verify(carService, never()).deleteCar(anyLong());
    }

    @Test
    void addLocation_WithValidData_ShouldAddLocationAndRedirect() throws Exception {

        LocationCreateRequest locationRequest = LocationCreateRequest.builder()
                .name("Airport")
                .address("123 Airport Rd")
                .city(City.SOFIA)
                .build();

        Location newLocation = Location.builder()
                .id(1L)
                .name("Airport")
                .address("123 Airport Rd")
                .city(City.SOFIA)
                .build();

        when(locationService.createLocation(any(LocationCreateRequest.class))).thenReturn(newLocation);

        mockMvc.perform(post("/admin/locations/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("locationCreateRequest", locationRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(locationService).createLocation(any(LocationCreateRequest.class));
    }

    @Test
    void addLocation_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        LocationCreateRequest locationRequest = LocationCreateRequest.builder()
                .name("Airport")
                .address("123 Airport Rd")
                .city(City.SOFIA)
                .build();

        when(locationService.createLocation(any(LocationCreateRequest.class)))
                .thenThrow(new DomainException("Location with this name already exists"));

        mockMvc.perform(post("/admin/locations/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .flashAttr("locationCreateRequest", locationRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(locationService).createLocation(any(LocationCreateRequest.class));
    }

    @Test
    void deleteLocation_ShouldDeleteLocationAndRedirect() throws Exception {

        Long locationId = 1L;
        Location location = Location.builder()
                .id(locationId)
                .name("Airport")
                .build();

        when(locationService.getLocation(locationId)).thenReturn(location);
        doNothing().when(locationService).deleteLocation(locationId);

        mockMvc.perform(post("/admin/locations/delete/{id}", locationId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(locationService).getLocation(locationId);
        verify(locationService).deleteLocation(locationId);
    }

    @Test
    void deleteLocation_WithException_ShouldHandleErrorAndRedirect() throws Exception {

        Long locationId = 1L;

        when(locationService.getLocation(locationId))
                .thenThrow(new DomainException("Location not found"));

        mockMvc.perform(post("/admin/locations/delete/{id}", locationId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(locationService).getLocation(locationId);
        verify(locationService, never()).deleteLocation(anyLong());
    }

    @Test
    void processExpiredRentals_ShouldProcessAndRedirect() throws Exception {

        doNothing().when(rentalSchedulingService).manuallyProcessExpiredRentals();

        mockMvc.perform(post("/admin/process-expired-rentals")
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(rentalSchedulingService).manuallyProcessExpiredRentals();
    }
}