package org.example.rentacar.car;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.repository.CarRepository;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.web.dto.CarCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CarIntegrationTest {


    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;


    @Test
    @Transactional
    void createCar_WithValidData_ShouldCreateCar() {

        CarCreateRequest carRequest = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .imageUrl("http://example.com/image.jpg")
                .description("A comfortable sedan")
                .build();

        Car createdCar = carService.createCar(carRequest);

        assertNotNull(createdCar);
        assertNotNull(createdCar.getId());
        assertEquals("Toyota", createdCar.getBrand());
        assertEquals("Camry", createdCar.getModel());
        assertEquals(2023, createdCar.getYear());
        assertEquals("ABC123", createdCar.getLicensePlate());
        assertEquals(new BigDecimal("50.00"), createdCar.getPricePerDay());
        assertEquals(Status.AVAILABLE, createdCar.getStatus());
        assertEquals("http://example.com/image.jpg", createdCar.getImage());
        assertEquals("A comfortable sedan", createdCar.getDescription());


        Optional<Car> savedCar = carRepository.findById(createdCar.getId());
        assertTrue(savedCar.isPresent());
    }

    @Test
    @Transactional
    void createCar_WithDuplicateLicensePlate_ShouldThrowException() {

        CarCreateRequest carRequest1 = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        CarCreateRequest carRequest2 = CarCreateRequest.builder()
                .brand("Honda")
                .model("Accord")
                .year(2022)
                .licensePlate("ABC123") // Same license plate
                .pricePerDay(new BigDecimal("45.00"))
                .status(Status.AVAILABLE)
                .build();

        carService.createCar(carRequest1);

        DomainException exception = assertThrows(DomainException.class, () -> {
            carService.createCar(carRequest2);
        });

        assertEquals("Car with this license plate already exists", exception.getMessage());
        assertEquals(1, carRepository.count());
    }

    @Test
    @Transactional
    void getAllCars_ShouldReturnAllCars() {

        CarCreateRequest car1 = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        CarCreateRequest car2 = CarCreateRequest.builder()
                .brand("Honda")
                .model("Accord")
                .year(2022)
                .licensePlate("XYZ789")
                .pricePerDay(new BigDecimal("45.00"))
                .status(Status.AVAILABLE)
                .build();

        carService.createCar(car1);
        carService.createCar(car2);

        List<Car> allCars = carService.getAllCars();

        assertEquals(2, allCars.size());
        assertTrue(allCars.stream().anyMatch(car -> car.getLicensePlate().equals("ABC123")));
        assertTrue(allCars.stream().anyMatch(car -> car.getLicensePlate().equals("XYZ789")));
    }

    @Test
    @Transactional
    void getCarById_WithValidId_ShouldReturnCar() {

        CarCreateRequest carRequest = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        Car createdCar = carService.createCar(carRequest);

        Car retrievedCar = carService.getCarById(createdCar.getId());

        assertNotNull(retrievedCar);
        assertEquals(createdCar.getId(), retrievedCar.getId());
        assertEquals("Toyota", retrievedCar.getBrand());
        assertEquals("Camry", retrievedCar.getModel());
    }

    @Test
    @Transactional
    void getCarById_WithInvalidId_ShouldThrowException() {

        long nonExistentId = 999L;

        DomainException exception = assertThrows(DomainException.class, () -> {
            carService.getCarById(nonExistentId);
        });

        assertEquals("Car not found", exception.getMessage());
    }

    @Test
    @Transactional
    void updateCarStatus_ShouldUpdateStatus() {

        CarCreateRequest carRequest = CarCreateRequest.builder()
                .brand("Toyota")
                .model("Camry")
                .year(2023)
                .licensePlate("ABC123")
                .pricePerDay(new BigDecimal("50.00"))
                .status(Status.AVAILABLE)
                .build();

        Car createdCar = carService.createCar(carRequest);

        Car updatedCar = carService.updateCarStatus(createdCar.getId(), Status.MAINTENANCE);

        assertEquals(Status.MAINTENANCE, updatedCar.getStatus());

        Car dbCar = carService.getCarById(createdCar.getId());
        assertEquals(Status.MAINTENANCE, dbCar.getStatus());
    }
}
