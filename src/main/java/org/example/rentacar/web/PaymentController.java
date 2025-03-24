package org.example.rentacar.web;

import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.payment.service.PaymentService;
import org.example.rentacar.rental.model.Rental;
import org.example.rentacar.rental.service.RentalService;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final RentalService rentalService;
    private final CreditCardService creditCardService;

    @Autowired
    public PaymentController(PaymentService paymentService, UserService userService, RentalService rentalService, CreditCardService creditCardService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.rentalService = rentalService;
        this.creditCardService = creditCardService;
    }

    @GetMapping("/process/{rentalId}")
    public String showPaymentForm(@PathVariable UUID rentalId,
                                  @AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getById(authenticationDetails.getUserId());
            Rental rental = rentalService.getRentalById(rentalId);
            if(paymentService.getPaymentByRental(rental).isPresent()){
                redirectAttributes.addFlashAttribute("errorMessage","This rental is already paid");
                return "redirect:/rentals";
            }
            List<CreditCard> userCreditCards = creditCardService.getUserCreditCards(user);

            model.addAttribute("creditCards", userCreditCards);
            model.addAttribute("rental", rental);
            model.addAttribute("user", user);

//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.setRentalId(rental.getId());
//            model.addAttribute("paymentRequest", paymentRequest);
            return "payment-form";
        }catch (DomainException e){
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
            return "redirect:/rentals";
        }
    }
}
