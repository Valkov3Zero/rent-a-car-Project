package org.example.rentacar.creditCard.repository;

import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByUser(User user);
}
