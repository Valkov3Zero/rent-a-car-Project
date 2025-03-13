package org.example.rentacar.location.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.rentacar.rental.model.Rental;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private City city;

    @OneToMany(mappedBy = "pickupLocation")
    private List<Rental> rentalsStartingHere;

    @OneToMany(mappedBy = "dropOffLocation")
    private List<Rental> rentalsEndingHere;
}
