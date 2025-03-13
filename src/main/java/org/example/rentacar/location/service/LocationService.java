package org.example.rentacar.location.service;

import jakarta.transaction.Transactional;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.repository.LocationRepository;
import org.example.rentacar.web.dto.LocationCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location getLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new DomainException("Location not found"));
    }
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Transactional
    public Location createLocation(LocationCreateRequest locationCreateRequest) {
        Optional<Location> existingLocation = locationRepository.findByNameIgnoreCase(locationCreateRequest.getName());
        if (existingLocation.isPresent()) {
            throw new DomainException("Location with this name already exists");
        }
        Location location = Location.builder()
                .name(locationCreateRequest.getName())
                .address(locationCreateRequest.getAddress())
                .city(locationCreateRequest.getCity())
                .build();

        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        Location location = getLocation(id);

        if ((location.getRentalsStartingHere() != null && !location.getRentalsStartingHere().isEmpty()) ||
                (location.getRentalsEndingHere() != null && !location.getRentalsEndingHere().isEmpty())) {
            throw new DomainException("Cannot delete a location that is associated with rentals");
        }
        locationRepository.delete(location);
    }
}
