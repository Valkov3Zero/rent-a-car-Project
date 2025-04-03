package org.example.rentacar.user;

import org.example.rentacar.email.service.NotificationService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.repository.UserRepository;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class RegistrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @Transactional
    void register_WithValidData_ShouldCreateNewUser() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhoneNumber("1234567890");

        doNothing().when(notificationService).sendWelcomeEmail(any(UUID.class), anyString());


        userService.register(request);


        Optional<User> savedUser = userRepository.findByUsername("testuser");
        assertTrue(savedUser.isPresent());
        User user = savedUser.get();

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName(), "Last name should match");
        assertEquals("1234567890", user.getPhone());
        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertTrue(user.isActive());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));

        verify(notificationService).sendWelcomeEmail(user.getId(), user.getEmail());
    }
    @Test
    @Transactional
    void register_WithDuplicateUsername_ShouldThrowException() {

        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("duplicateuser");
        request1.setPassword("password123");
        request1.setEmail("test1@example.com");
        request1.setFirstName("Test");
        request1.setLastName("User");
        request1.setPhoneNumber("1234567890");

        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("duplicateuser"); // Same username
        request2.setPassword("password456");
        request2.setEmail("test2@example.com");
        request2.setFirstName("Another");
        request2.setLastName("User");
        request2.setPhoneNumber("0987654321");

        doNothing().when(notificationService).sendWelcomeEmail(any(UUID.class), anyString());


        userService.register(request1);

        DomainException exception = assertThrows(DomainException.class, () -> {
            userService.register(request2);
        });

        assertEquals("Username already exists", exception.getMessage());

        assertEquals(1, userRepository.count());
    }

    @Test
    @Transactional
    void register_ShouldEncodePassword() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("passworduser");
        request.setPassword("password123");
        request.setEmail("password@example.com");
        request.setFirstName("Password");
        request.setLastName("User");
        request.setPhoneNumber("1234567890");

        doNothing().when(notificationService).sendWelcomeEmail(any(UUID.class), anyString());

        userService.register(request);

        User savedUser = userRepository.findByUsername("passworduser").orElseThrow();

        assertNotEquals("password123", savedUser.getPassword());

        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }


}
