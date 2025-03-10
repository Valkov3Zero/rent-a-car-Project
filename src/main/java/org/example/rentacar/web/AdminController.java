package org.example.rentacar.web;

import org.example.rentacar.car.model.Car;
import org.example.rentacar.car.service.CarService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
}
