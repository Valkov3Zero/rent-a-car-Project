package org.example.rentacar.car.service;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.repository.CarRepository;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.web.dto.CarCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new DomainException("Car not found"));
    }

    public Car createCar(CarCreateRequest carCreateRequest) {
        Optional<Car> existingCarOpt = carRepository.findByLicensePlate(carCreateRequest.getLicensePlate());
        if (existingCarOpt.isPresent()) {
            throw new DomainException("Car with this license plate already exists");
        }

        Car newCar = Car.builder()
                .brand(carCreateRequest.getBrand())
                .model(carCreateRequest.getModel())
                .year(carCreateRequest.getYear())
                .licensePlate(carCreateRequest.getLicensePlate())
                .pricePerDay(carCreateRequest.getPricePerDay())
                .status(carCreateRequest.getStatus())
                .image(carCreateRequest.getImageURL())
                .description(carCreateRequest.getDescription())
                .build();

        return carRepository.save(newCar);
    }
}
