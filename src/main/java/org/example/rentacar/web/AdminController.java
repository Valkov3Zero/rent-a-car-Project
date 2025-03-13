package org.example.rentacar.web;

import jakarta.validation.Valid;
import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.CarCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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


    @Autowired
    public AdminController(UserService userService, CarService carService) {
        this.userService = userService;
        this.carService = carService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User adminUser = userService.getById(authenticationDetails.getUserId());

        if (adminUser.getRole() != UserRole.ADMIN){
            return new ModelAndView("redirect:/");
        }

        List<Car> cars = carService.getAllCars();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/dashboard");
        modelAndView.addObject("user", adminUser);
        modelAndView.addObject("cars", cars);

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
            e.printStackTrace();
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
}
