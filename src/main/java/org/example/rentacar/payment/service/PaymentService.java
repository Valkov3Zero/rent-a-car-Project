package org.example.rentacar.payment.service;

import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.repository.CreditCardRepository;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.payment.repository.PaymentRepository;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.model.RentalStatus;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.user.model.User;
import org.example.rentacar.web.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalService rentalService;
    private final CreditCardRepository creditCardRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, RentalService rentalService, CreditCardService creditCardService, CreditCardRepository creditCardRepository) {
        this.paymentRepository = paymentRepository;
        this.rentalService = rentalService;
        this.creditCardRepository = creditCardRepository;
    }

    public Payment processPayment(PaymentRequest paymentRequest, User user) {

        UUID rentalId = paymentRequest.getRentalId();
        Rental rental = rentalService.getRentalById(rentalId);
        UUID cardId = paymentRequest.getSelectedCardId();
        CreditCard creditCard = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new DomainException("Card not found"));
        boolean paymentSucceeded;

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new DomainException("You can only pay for your own rentals!");
        }
        if (getPaymentByRental(rental).isPresent()){
            throw new DomainException("This rental is already paid!");
        }
        if(creditCard.getAmount().compareTo(rental.getTotalPrice()) < 0){
            throw new DomainException("You do not have enough money!");
        }

        creditCard.setAmount(creditCard.getAmount().subtract(rental.getTotalPrice()));
        creditCardRepository.save(creditCard);

        String cardDetails = "Card Details: " + creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4);
        paymentSucceeded = true;
        if(!paymentSucceeded) {
            throw new DomainException("Payment failed!");
        }
        Payment payment = Payment.builder()
                .paymentDate(LocalDate.now())
                .amount(rental.getTotalPrice())
                .creditCard(creditCard)
                .user(user)
                .rental(rental)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        rentalService.updateRentalStatus(rental.getId(), RentalStatus.RESERVED_PAID);


        return savedPayment;
    }

    public Optional<Payment> getPaymentByRental(Rental rental) {
        return paymentRepository.findByRental(rental);
    }
}
