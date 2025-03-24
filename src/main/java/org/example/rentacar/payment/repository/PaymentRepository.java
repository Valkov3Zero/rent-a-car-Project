package org.example.rentacar.payment.repository;

import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.rental.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByRental(Rental rental);
}
