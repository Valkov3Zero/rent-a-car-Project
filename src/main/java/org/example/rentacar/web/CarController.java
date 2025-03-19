package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final UserService userService;
    private final CarService carService;

    @Autowired
    public CarController(UserService userService, CarService carService) {
        this.userService = userService;
        this.carService = carService;
    }

    @GetMapping
    public ModelAndView getAllCars(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());

        List<Car> allCars = carService.getAllCars();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cars");
        modelAndView.addObject("user", user);
        modelAndView.addObject("cars", allCars);

        return modelAndView;
    }

    @GetMapping("/{carId}")
    public ModelAndView getCarDetails(@AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                      @PathVariable Long carId,
                                      RedirectAttributes redirectAttributes) {
        try{
            User user = userService.getById(authenticationDetails.getUserId());
            Car car = carService.getCarById(carId);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("car-details");
            modelAndView.addObject("user", user);
            modelAndView.addObject("car", car);
            return modelAndView;
        } catch (DomainException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return new ModelAndView("redirect:/cars");
        }
    }
}
