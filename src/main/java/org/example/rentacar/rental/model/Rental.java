package org.example.rentacar.rental.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.rentacar.car.model.Car;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date",nullable = false)
    private LocalDate endDate;

    @Column(name = "total_price" ,nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;

    @ManyToOne
    private User user;

    @ManyToOne
    private Car car;

    @ManyToOne
    @JoinColumn(name = "pickup_location_id", nullable = false)
    private Location pickupLocation;

    @ManyToOne
    @JoinColumn(name = "dropoff_location_id", nullable = false)
    private Location dropOffLocation;

    @Column
    private LocalDate createdAt;
}
