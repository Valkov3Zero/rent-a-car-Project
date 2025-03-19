package org.example.rentacar.rental.repository;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.example.rentacar.rental.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {

    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId " +
            "AND r.status IN ('RESERVED', 'ACTIVE', 'RESERVED_PAID') " +
            "AND ((r.startDate <= :endDate) AND (r.endDate >= :startDate))")
    List<Rental> findOverlappingRentals(
            @Param("cardId") long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Rental> findByUserId(UUID userId);
}
