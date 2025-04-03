package org.example.rentacar.web;

import jakarta.validation.Valid;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.LoginRequest;
import org.example.rentacar.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("register");
        mav.addObject("registerRequest", new RegisterRequest());

        return mav;
    }
    @PostMapping("/register")
    public String processRegister(@Valid RegisterRequest registerRequest,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (DomainException e) {

            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");
        mav.addObject("loginRequest", new LoginRequest());
        if (errorParam != null) {
            mav.addObject("errorMessage", "Incorrect username or password!");
        }
        return mav;
    }
    @GetMapping("/about")
    public String getAboutPage(Model model) {

        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                model.addAttribute("user", principal);
            }
        } catch (Exception e) {

        }
        return "about";
    }

    @GetMapping("/contact")
    public String getContactPage(Model model) {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                model.addAttribute("user", principal);
            }
        } catch (Exception e) {
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContactForm(@RequestParam String name,
                                    @RequestParam String email,
                                    @RequestParam String message,
                                    Model model) {
        // TODO: Implement email sending or message storing functionality

        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                model.addAttribute("user", principal);
            }
        } catch (Exception e) {
        }

        model.addAttribute("successMessage", "Thank you for your message! We will get back to you soon.");
        return "contact";
    }


}
