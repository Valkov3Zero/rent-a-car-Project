package org.example.rentacar.creditCard.service;

import jakarta.transaction.Transactional;
import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.repository.CreditCardRepository;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.user.model.User;
import org.example.rentacar.web.dto.CreditCardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    @Autowired
    public CreditCardService(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    public List<CreditCard> getUserCreditCards(User user) {
        return creditCardRepository.findByUser(user);
    }

    @Transactional
    public CreditCard addCreditCard(CreditCardRequest creditCardRequest, User user) {
        boolean cardExists = creditCardRepository.findByUser(user).stream()
                .anyMatch(card -> card.getCardNumber().equals(creditCardRequest.getCardNumber()));
        if (cardExists) {
            throw new DomainException("You already have a card with this number");
        }
        CreditCard newCard = CreditCard.builder()
                .cardNumber(creditCardRequest.getCardNumber())
                .cardholderName(creditCardRequest.getCardholderName())
                .expirationDate(creditCardRequest.getExpirationDate())
                .amount(BigDecimal.ZERO)
                .user(user)
                .build();

        return creditCardRepository.save(newCard);
    }
}
