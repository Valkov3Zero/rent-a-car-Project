package org.example.rentacar.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.payment.model.Payment;
import org.example.rentacar.rental.model.Rental;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false,name = "first_name")
    private String firstName;

    @Column(nullable = false,name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Rental> rentals;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<CreditCard> creditCards;

    @OneToMany(mappedBy = "user")
    private List<Payment> payments;

    public boolean isActive;

}
