package org.example.rentacar.rental.service;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.RentalCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final UserService userService;
    private final LocationService locationService;

    @Autowired
    public RentalService(RentalRepository rentalRepository, CarService carService, UserService userService, LocationService locationService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
        this.userService = userService;
        this.locationService = locationService;
    }

    public Rental createRental(RentalCreateRequest rentalCreateRequest, UUID userId) {
        validateDates(rentalCreateRequest.getStartDate(), rentalCreateRequest.getEndDate());

        Car car = carService.getCarById(rentalCreateRequest.getCarId());
        User user = userService.getById(userId);

        Location pickupLocation = locationService.getLocation(rentalCreateRequest.getPickupLocationId());
        Location dropoffLocation = locationService.getLocation(rentalCreateRequest.getDropoffLocationId());

        if (car.getStatus() != Status.AVAILABLE){
            throw new DomainException("Car is not available");
        }
        List<Rental> overlappingRentals = rentalRepository.findOverlappingRentals(car.getId(),
                rentalCreateRequest.getStartDate(),
                rentalCreateRequest.getEndDate());
        if (!overlappingRentals.isEmpty()){
            throw new DomainException("Car is already rented for the selected dates");
        }
        int days = Period.between(rentalCreateRequest.getStartDate(), rentalCreateRequest.getEndDate()).getDays();
        BigDecimal totalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Rental rental = Rental.builder()
                .car(car)
                .user(user)
                .startDate(rentalCreateRequest.getStartDate())
                .endDate(rentalCreateRequest.getEndDate())
                .totalPrice(totalPrice)
                .status(RentalStatus.RESERVED)
                .pickupLocation(pickupLocation)
                .dropOffLocation( dropoffLocation)
                .createdAt(LocalDate.now())
                .build();

        car.setStatus(Status.RENTED);
        carService.updateCarStatus(car.getId(),Status.RENTED);
        return rentalRepository.save(rental);
    }

    public void cancelRental(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(()->new DomainException("Rental not found"));
        if(rental.getStartDate().isBefore(LocalDate.now()) || rental.getStatus() == RentalStatus.ACTIVE){
            throw new DomainException("Cannot cancel an active rental");
        }
        rental.setStatus(RentalStatus.CANCELLED);

        Car car = rental.getCar();
        car.setStatus(Status.AVAILABLE);
        carService.updateCarStatus(car.getId(),Status.AVAILABLE);

        rentalRepository.save(rental);
    }

    public Rental startRental(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new DomainException("Rental not found"));
        if (rental.getStatus() != RentalStatus.RESERVED_PAID){
            throw new DomainException("Only paid rentals can be started");
        }
        if (rental.getStartDate().isAfter(LocalDate.now())) {
            throw new DomainException("Cannot start a rental before its start date");
        }
        rental.setStatus(RentalStatus.ACTIVE);
        return rentalRepository.save(rental);
    }

    public Rental completeRental(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(()->new DomainException("Rental not found"));

        if (rental.getStatus() != RentalStatus.ACTIVE){
            throw new DomainException("Only active rentals can be completed");
        }
        rental.setStatus(RentalStatus.COMPLETED);

        Car car = rental.getCar();
        car.setStatus(Status.AVAILABLE);
        carService.updateCarStatus(car.getId(),Status.AVAILABLE);

        return rentalRepository.save(rental);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (start.isBefore(today)) {
            throw new DomainException("Start date must be today or in the future");
        }
        if (end.isBefore(start)) {
            throw new DomainException("End date must be after start date");
        }
    }

    public Rental updateRentalStatus(UUID rentalId, RentalStatus status) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(()->new DomainException("Rental not found"));
        rental.setStatus(status);
        return rentalRepository.save(rental);
    }

    public List<Rental> getUserRentals(UUID userId) {
        return rentalRepository.findByUserId(userId);
    }

    public Rental getRentalById(UUID rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(() -> new DomainException("Rental not found"));
    }
}
