package org.example.rentacar.rental.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RentalSchedulingService {

    private final RentalRepository rentalRepository;
    private final CarService carService;

    @Autowired
    public RentalSchedulingService(RentalRepository rentalRepository, CarService carService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpiredRentals() {

        List<Rental> expiredRentals = findExpiredRentals(LocalDate.now());

        for (Rental rental : expiredRentals) {
            try {
                if (rental.getStatus() == RentalStatus.ACTIVE){
                    rental.setStatus(RentalStatus.COMPLETED);
                }else {
                    rental.setStatus(RentalStatus.CANCELLED);
                }
                rentalRepository.save(rental);

                carService.updateCarStatus(rental.getCar().getId(), Status.AVAILABLE);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }

    private List<Rental> findExpiredRentals(LocalDate currentDate) {
        return rentalRepository.findByStatusInAndEndDateBefore(
                Arrays.asList(
                        RentalStatus.ACTIVE,
                        RentalStatus.RESERVED,
                        RentalStatus.RESERVED_PAID
                ),
                currentDate
        );
    }

    public void manuallyProcessExpiredRentals() {
        processExpiredRentals();
    }
}
