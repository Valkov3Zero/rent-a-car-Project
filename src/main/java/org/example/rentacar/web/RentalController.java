package org.example.rentacar.web;

import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;


    @Autowired
    public RentalController(RentalService rentalService, UserService userService) {
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getUserRentals(@AuthenticationPrincipal AuthenticationDetails authenticationDetails){
        User user = userService.getById(authenticationDetails.getUserId());

        List<Rental> rentals = rentalService.getUserRentals(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("rentals");
        modelAndView.addObject("rentals", rentals);
        modelAndView.addObject("user", user);

        return modelAndView;
    }


}
