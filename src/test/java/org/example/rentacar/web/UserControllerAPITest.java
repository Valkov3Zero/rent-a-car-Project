package org.example.rentacar.web;


import org.example.rentacar.creditCard.model.CreditCard;
import org.example.rentacar.creditCard.service.CreditCardService;
import org.example.rentacar.email.client.dto.NotificationDate;
import org.example.rentacar.email.service.NotificationService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.CreditCardRequest;
import org.example.rentacar.web.dto.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerAPITest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CreditCardService creditCardService;

    @MockitoBean
    private NotificationService notificationService;

    private UUID userId;
    private User testUser;
    private CreditCard testCreditCard;
    private UUID cardId;
    private AuthenticationDetails authDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();

        testCreditCard = CreditCard.builder()
                .id(cardId)
                .cardNumber("1234567890123456")
                .cardholderName("John Doe")
                .expirationDate("12/25")
                .amount(BigDecimal.valueOf(100))
                .user(testUser)
                .build();

        authDetails = new AuthenticationDetails(
                userId,
                "testuser",
                "password",
                UserRole.CUSTOMER,
                true
        );
    }

    @Test
    void getProfilePage_ShouldReturnProfilePage() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(creditCardService.getUserCreditCards(testUser)).thenReturn(Arrays.asList(testCreditCard));
        when(notificationService.getNotificationDate(userId)).thenReturn(NotificationDate.builder()
                        .createdOn(LocalDateTime.now())
                        .build());

        mockMvc.perform(get("/profile")
                .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("creditCards"))
                .andExpect(model().attributeExists("newCreditCard"))
                .andExpect(model().attributeExists("date"));

        verify(userService).getById(userId);
        verify(creditCardService).getUserCreditCards(testUser);
        verify(notificationService).getNotificationDate(userId);

    }

    @Test
    void getEditProfilePage_ShouldReturnEditFormWithUserData() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);

        mockMvc.perform(get("/profile/edit")
                        .with(user(authDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("profileUpdateRequest"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getById(userId);
    }

    @Test
    void updateProfile_WithValidData_ShouldRedirectToProfile() throws Exception {

        ProfileUpdateRequest updateRequest = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@example.com")
                .phoneNumber("9876543210")
                .build();

        when(userService.updateProfile(eq(userId), any(ProfileUpdateRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/profile/update")
                        .with(user(authDetails))
                        .with(csrf())
                        .param("firstName", updateRequest.getFirstName())
                        .param("lastName", updateRequest.getLastName())
                        .param("email", updateRequest.getEmail())
                        .param("phoneNumber", updateRequest.getPhoneNumber()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    void updateProfile_WithError_ShouldRedirectToEditPage() throws Exception {

        ProfileUpdateRequest updateRequest = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@example.com")
                .phoneNumber("9876543210")
                .build();

        when(userService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenThrow(new DomainException("Email already exists"));

        mockMvc.perform(post("/profile/update")
                        .with(user(authDetails))
                        .with(csrf())
                        .param("firstName", updateRequest.getFirstName())
                        .param("lastName", updateRequest.getLastName())
                        .param("email", updateRequest.getEmail())
                        .param("phoneNumber", updateRequest.getPhoneNumber()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/edit"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    void addFunds_ShouldAddFundsAndRedirect() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        doNothing().when(creditCardService).addFunds(eq(cardId), any(BigDecimal.class), eq(testUser));

        mockMvc.perform(post("/profile/cards/{cardId}/add-funds", cardId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).getById(userId);
        verify(creditCardService).addFunds(eq(cardId), any(BigDecimal.class), eq(testUser));
    }


    @Test
    void deleteCreditCard_ShouldDeleteAndRedirect() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        doNothing().when(creditCardService).deleteCreditCard(cardId, testUser);

        mockMvc.perform(post("/profile/cards/{cardId}/delete", cardId)
                        .with(user(authDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).getById(userId);
        verify(creditCardService).deleteCreditCard(cardId, testUser);
    }
    @Test
    void addCreditCard_WithValidData_ShouldRedirectToProfile() throws Exception {

        CreditCardRequest cardRequest = new CreditCardRequest();
        cardRequest.setCardNumber("9876543210987654");
        cardRequest.setCardholderName("John Doe");
        cardRequest.setExpirationDate("12/25");

        when(userService.getById(userId)).thenReturn(testUser);
        doNothing().when(creditCardService).addCreditCard(any(CreditCardRequest.class), eq(testUser));

        mockMvc.perform(post("/profile/cards/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .param("cardNumber", cardRequest.getCardNumber())
                        .param("cardholderName", cardRequest.getCardholderName())
                        .param("expirationDate", cardRequest.getExpirationDate()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).getById(userId);
        verify(creditCardService).addCreditCard(any(CreditCardRequest.class), eq(testUser));
    }
    @Test
    void addCreditCard_WithInvalidData_ShouldReturnProfilePage() throws Exception {

        when(userService.getById(userId)).thenReturn(testUser);
        when(creditCardService.getUserCreditCards(testUser)).thenReturn(Arrays.asList(testCreditCard));
        when(notificationService.getNotificationDate(userId)).thenReturn(NotificationDate.builder()
                .createdOn(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/profile/cards/add")
                        .with(user(authDetails))
                        .with(csrf())
                        .param("cardNumber", "") // Empty card number - invalid
                        .param("cardholderName", "John Doe")
                        .param("expirationDate", "12/25"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("creditCards"))
                .andExpect(model().attributeExists("newCreditCard"));

        verify(userService).getById(userId);
        verify(creditCardService).getUserCreditCards(testUser);
        verify(creditCardService, never()).addCreditCard(any(CreditCardRequest.class), any(User.class));
    }
}
