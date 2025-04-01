package org.example.rentacar.creditCard;


import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.repository.CreditCardRepository;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.user.model.User;
import org.example.rentacar.web.dto.CreditCardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreditCardServiceUTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CreditCardService creditCardService;

    private User testUser;
    private CreditCard existingCard;
    private CreditCardRequest validCardRequest;
    private UUID cardId;

    @BeforeEach
    void setUp() {

        cardId = UUID.randomUUID();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("test@test.com")
                .build();

        existingCard = CreditCard.builder()
                .id(cardId)
                .cardNumber("1234567890123456")
                .cardholderName("Test User")
                .expirationDate("12/25")
                .amount(new BigDecimal("100.00"))
                .user(testUser)
                .build();

        validCardRequest = new CreditCardRequest();
        validCardRequest.setCardNumber("9876543210987654");
        validCardRequest.setCardholderName("Test User");
        validCardRequest.setExpirationDate("10/26");
    }
    @Test
    void addCreditCard_WithValidRequest_ShouldAddCreditCard() {

        when(creditCardRepository.findByUser(testUser)).thenReturn(List.of());

        creditCardService.addCreditCard(validCardRequest, testUser);

        ArgumentCaptor<CreditCard> cardCaptor = ArgumentCaptor.forClass(CreditCard.class);
        verify(creditCardRepository).save(cardCaptor.capture());

        CreditCard savedCard = cardCaptor.getValue();
        assertEquals(validCardRequest.getCardNumber(), savedCard.getCardNumber());
        assertEquals(validCardRequest.getCardholderName(), savedCard.getCardholderName());
        assertEquals(validCardRequest.getExpirationDate(), savedCard.getExpirationDate());
        assertEquals(BigDecimal.ZERO, savedCard.getAmount());
        assertEquals(testUser, savedCard.getUser());
    }

    @Test
    void addCreditCard_WithExistingCardNumber_ShouldThrowException() {

        CreditCard existingCard = CreditCard.builder()
                .cardNumber(validCardRequest.getCardNumber())
                .cardholderName(testUser.getUsername())
                .build();

        when(creditCardRepository.findByUser(testUser)).thenReturn(List.of(existingCard));

        assertThrows(DomainException.class, () -> creditCardService.addCreditCard(validCardRequest, testUser));
        verify(creditCardRepository,never()).save(any());
    }

    @Test
    void addFunds_ShouldUpdateCardBalance() {

        BigDecimal amountToAdd = new BigDecimal("100.00");
        BigDecimal expectedBalance = new BigDecimal("200.00");

        when(creditCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));

        creditCardService.addFunds(cardId, amountToAdd, testUser);

        assertEquals(expectedBalance,existingCard.getAmount());
        verify(creditCardRepository).save(existingCard);
    }
}
