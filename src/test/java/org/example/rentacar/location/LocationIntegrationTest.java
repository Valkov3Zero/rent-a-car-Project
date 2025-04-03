package org.example.rentacar.location;

import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.repository.LocationRepository;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.web.dto.LocationCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LocationIntegrationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;


    @Test
    @Transactional
    void createLocation_WithValidData_ShouldCreateLocation() {

        LocationCreateRequest locationRequest = LocationCreateRequest.builder()
                .name("Downtown Office")
                .address("123 Main Street")
                .city(City.SOFIA)
                .build();

        Location createdLocation = locationService.createLocation(locationRequest);

        assertNotNull(createdLocation);
        assertNotNull(createdLocation.getId());
        assertEquals("Downtown Office", createdLocation.getName());
        assertEquals("123 Main Street", createdLocation.getAddress());
        assertEquals(City.SOFIA, createdLocation.getCity());

        Optional<Location> savedLocation = locationRepository.findById(createdLocation.getId());
        assertTrue(savedLocation.isPresent());
    }

    @Test
    @Transactional
    void createLocation_WithDuplicateName_ShouldThrowException() {

        LocationCreateRequest locationRequest1 = LocationCreateRequest.builder()
                .name("Downtown Office")
                .address("123 Main Street")
                .city(City.SOFIA)
                .build();

        LocationCreateRequest locationRequest2 = LocationCreateRequest.builder()
                .name("Downtown Office") // Same name
                .address("456 Park Avenue")
                .city(City.PLOVDIV)
                .build();

        locationService.createLocation(locationRequest1);

        DomainException exception = assertThrows(DomainException.class, () -> {
            locationService.createLocation(locationRequest2);
        });

        assertEquals("Location with this name already exists", exception.getMessage());
        assertEquals(1, locationRepository.count());
    }

    @Test
    @Transactional
    void getAllLocations_ShouldReturnAllLocations() {

        LocationCreateRequest location1 = LocationCreateRequest.builder()
                .name("Downtown Office")
                .address("123 Main Street")
                .city(City.SOFIA)
                .build();

        LocationCreateRequest location2 = LocationCreateRequest.builder()
                .name("Airport Branch")
                .address("789 Airport Road")
                .city(City.VARNA)
                .build();

        locationService.createLocation(location1);
        locationService.createLocation(location2);


        List<Location> allLocations = locationService.getAllLocations();


        assertEquals(2, allLocations.size());
        assertTrue(allLocations.stream().anyMatch(location -> location.getName().equals("Downtown Office")));
        assertTrue(allLocations.stream().anyMatch(location -> location.getName().equals("Airport Branch")));
    }

    @Test
    @Transactional
    void getLocation_WithValidId_ShouldReturnLocation() {

        LocationCreateRequest locationRequest = LocationCreateRequest.builder()
                .name("Downtown Office")
                .address("123 Main Street")
                .city(City.SOFIA)
                .build();

        Location createdLocation = locationService.createLocation(locationRequest);

        Location retrievedLocation = locationService.getLocation(createdLocation.getId());

        assertNotNull(retrievedLocation);
        assertEquals(createdLocation.getId(), retrievedLocation.getId());
        assertEquals("Downtown Office", retrievedLocation.getName());
        assertEquals("123 Main Street", retrievedLocation.getAddress());
        assertEquals(City.SOFIA, retrievedLocation.getCity());
    }

    @Test
    @Transactional
    void getLocation_WithInvalidId_ShouldThrowException() {

        long nonExistentId = 999L;

        DomainException exception = assertThrows(DomainException.class, () -> {
            locationService.getLocation(nonExistentId);
        });

        assertEquals("Location not found", exception.getMessage());
    }

    @Test
    @Transactional
    void deleteLocation_WithValidId_ShouldDeleteLocation() {

        LocationCreateRequest locationRequest = LocationCreateRequest.builder()
                .name("Downtown Office")
                .address("123 Main Street")
                .city(City.SOFIA)
                .build();

        Location createdLocation = locationService.createLocation(locationRequest);

        locationService.deleteLocation(createdLocation.getId());

        Optional<Location> deletedLocation = locationRepository.findById(createdLocation.getId());
        assertFalse(deletedLocation.isPresent());
    }
}
