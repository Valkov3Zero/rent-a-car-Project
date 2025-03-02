package org.example.rentacar.creditCard.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.user.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 16)
    private String cardNumber;

    @Column(nullable = false)
    private String cardholderName;

    @Column(nullable = false)
    private String expirationDate;

    @Column
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL)
    private List<Payment> payments;
}
