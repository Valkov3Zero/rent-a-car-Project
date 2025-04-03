package org.example.rentacar.car.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.rentacar.rental.model.Rental;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String image;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false,name = "`year`")
    private int year;

    @Column(nullable = false,unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private BigDecimal pricePerDay;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "car")
    private List<Rental> rentals;
}
