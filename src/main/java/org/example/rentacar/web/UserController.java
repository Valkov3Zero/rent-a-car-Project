package org.example.rentacar.web;

import jakarta.validation.Valid;
import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.CreditCardRequest;
import org.example.rentacar.web.dto.ProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;
    private final CreditCardService creditCardService;

    @Autowired
    public UserController(UserService userService, CreditCardService creditCardService) {
        this.userService = userService;
        this.creditCardService = creditCardService;
    }

    @GetMapping
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<CreditCard> creditCards = creditCardService.getUserCreditCards(user);

        ModelAndView mav = new ModelAndView();
        mav.setViewName("profile");
        mav.addObject("user", user);
        mav.addObject("creditCards",creditCards);
        mav.addObject("newCreditCard", new CreditCardRequest());
        return mav;
    }
    @GetMapping("/edit")
    public String showEditProfilePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails, Model model) {
        User user = userService.getById(authenticationDetails.getUserId());

        ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .build();

        model.addAttribute("profileUpdateRequest", profileUpdateRequest);
        model.addAttribute("user", user);
        return "profile-edit";
    }

    @PostMapping("/update")
    public String processProfileUpdate(@AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                       @Valid @ModelAttribute ProfileUpdateRequest profileUpdateRequest,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationDetails.getUserId());
            model.addAttribute("user", user);
            return "profile-edit";
        }
        try {
            User updatedUser = userService.updateProfile(authenticationDetails.getUserId(), profileUpdateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/edit";
        }

        return "redirect:/profile";
    }
    @PostMapping("/cards/add")
    public String addCreditCard(@Valid @ModelAttribute ("newCreditCard") CreditCardRequest creditCardRequest,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationDetails.getUserId());
            List<CreditCard> creditCards = creditCardService.getUserCreditCards(user);
            model.addAttribute("creditCards", creditCards);
            model.addAttribute("user", user);
            return "profile";
        }
        try {
            User user = userService.getById(authenticationDetails.getUserId());

            creditCardService.addCreditCard(creditCardRequest,user);
            redirectAttributes.addFlashAttribute("successMessage", "Credit card added successfully");
        } catch (DomainException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage","An error occurred: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
