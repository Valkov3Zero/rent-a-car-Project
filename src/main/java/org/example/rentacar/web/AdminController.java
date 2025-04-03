package org.example.rentacar.web;

import jakarta.validation.Valid;
import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.model.Status;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.location.model.City;
import org.example.rentacar.location.model.Location;
import org.example.rentacar.location.service.LocationService;
import org.example.rentacar.rental.service.RentalSchedulingService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.CarCreateRequest;
import org.example.rentacar.web.dto.LocationCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize(value = "hasAuthority('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CarService carService;
    private final LocationService locationService;
    private final RentalSchedulingService rentalSchedulingService;


    @Autowired
    public AdminController(UserService userService, CarService carService, LocationService locationService, RentalSchedulingService rentalSchedulingService) {
        this.userService = userService;
        this.carService = carService;
        this.locationService = locationService;
        this.rentalSchedulingService = rentalSchedulingService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User adminUser = userService.getById(authenticationDetails.getUserId());

        if (adminUser.getRole() != UserRole.ADMIN){
            return new ModelAndView("redirect:/");
        }

        List<Car> cars = carService.getAllCars();
        List<Location> allLocations = locationService.getAllLocations();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/dashboard");
        modelAndView.addObject("user", adminUser);
        modelAndView.addObject("cars", cars);
        modelAndView.addObject("locations",allLocations);
        modelAndView.addObject("statuses", Status.values());
        modelAndView.addObject("cities", City.values());

        return modelAndView;
    }
    @PostMapping("/cars/add")
    public String addCar(
            @Valid @ModelAttribute CarCreateRequest carCreateRequest,
            RedirectAttributes redirectAttributes) {

        try {
            // Debug output
            System.out.println("Received car create request: " + carCreateRequest);

            Car newCar = carService.createCar(carCreateRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Car added successfully: " + newCar.getBrand() + " " + newCar.getModel());
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            System.err.println("Domain error adding car: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while adding the car: " + e.getMessage());
            System.err.println("Error adding car: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/cars/delete/{id}")
    public String deleteCar(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {

        try {
            Car car = carService.getCarById(id);
            String carInfo = car.getBrand() + " " + car.getModel();

            carService.deleteCar(id);
            redirectAttributes.addFlashAttribute("successMessage","Car deleted successfully: " + carInfo);
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the car: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/locations/add")
    public String addLocation(@Valid @ModelAttribute LocationCreateRequest locationCreateRequest,
            RedirectAttributes redirectAttributes){
       try {
           Location newLocation = locationService.createLocation(locationCreateRequest);
           redirectAttributes.addFlashAttribute("successMessage",
                   "Location added successfully: " + newLocation.getName());
       }catch (DomainException e) {
           redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
       }catch (Exception e) {
           redirectAttributes.addFlashAttribute("errorMessage",
                   "An error occurred while adding the location: " + e.getMessage());
       }
       return "redirect:/admin/dashboard";
    }

    @PostMapping("/locations/delete/{id}")
    public String deleteLocation(@PathVariable Long id,RedirectAttributes redirectAttributes){
        try {
            Location location = locationService.getLocation(id);
            String locationInfo = location.getName();

            locationService.deleteLocation(id);
            redirectAttributes.addFlashAttribute("successMessage","Location deleted successfully: " + locationInfo);
        }catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "An error occurred while deleting the location: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/process-expired-rentals")
    public String processExpiredRentals(RedirectAttributes redirectAttributes) {
        rentalSchedulingService.manuallyProcessExpiredRentals();
        redirectAttributes.addFlashAttribute("successMessage","Successfully processed expired rentals");
        return "redirect:/admin/dashboard";
    }
}
