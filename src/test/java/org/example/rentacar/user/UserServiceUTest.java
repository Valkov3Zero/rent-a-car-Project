package org.example.rentacar.user;

import org.example.rentacar.email.service.NotificationService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.repository.UserRepository;
import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.ProfileUpdateRequest;
import org.example.rentacar.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private User existingUser;
    private UUID userId;
    private ProfileUpdateRequest profileUpdateRequest;

    @BeforeEach
    void setUp(){

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setPassword("testpassword");
        validRegisterRequest.setEmail("testemail@test.com");
        validRegisterRequest.setFirstName("testfirstname");
        validRegisterRequest.setLastName("testlastname");
        validRegisterRequest.setPhoneNumber("1234567890");
        userId = UUID.randomUUID();

        existingUser = User.builder()
                .id(userId)
                .username("existinguser")
                .password("encodedPassword")
                .email("existing@email.com")
                .firstName("Existing")
                .lastName("User")
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();

        profileUpdateRequest = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@email.com")
                .phoneNumber("0236547891")
                .build();
    }


@Test
void registerWhitValidData (){

    when(userRepository.findByUsername(validRegisterRequest.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword");

    User mockSavedUser = User.builder()
            .id(UUID.randomUUID())
            .username(validRegisterRequest.getUsername())
            .email(validRegisterRequest.getEmail())
            .firstName(validRegisterRequest.getFirstName())
            .lastName(validRegisterRequest.getLastName())
            .phone(validRegisterRequest.getPhoneNumber())
            .password("encodedPassword")
            .role(UserRole.CUSTOMER)
            .isActive(true)
            .build();

    when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

    userService.register(validRegisterRequest);

    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userArgumentCaptor.capture());

    User capturedUser = userArgumentCaptor.getValue();

    assertEquals(validRegisterRequest.getUsername(), capturedUser.getUsername());
    assertEquals(validRegisterRequest.getEmail(), capturedUser.getEmail());
    assertEquals(validRegisterRequest.getFirstName(), capturedUser.getFirstName());
    assertEquals(validRegisterRequest.getLastName(), capturedUser.getLastName());
    assertEquals(validRegisterRequest.getPhoneNumber(), capturedUser.getPhone());
    assertEquals("encodedPassword", capturedUser.getPassword());
    assertEquals(UserRole.CUSTOMER, capturedUser.getRole());
    assertTrue(capturedUser.isActive());
}

    @Test
    void registerWithExistingUsername_ShouldThrowException(){
        when(userRepository.findByUsername(validRegisterRequest.getUsername())).thenReturn(Optional.of(existingUser));

        assertThrows(DomainException.class,()-> userService.register(validRegisterRequest));

        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void getById_WithValidId_ShouldReturnUser(){
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        User foundUser = userService.getById(userId);
        assertEquals(existingUser, foundUser);
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException(){
        UUID nonExistingUserId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(Exception.class,()-> userService.getById(nonExistingUserId));

    }

    @Test
    void updateProfile_WithValidData_ShouldUpdateUser(){
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(profileUpdateRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(profileUpdateRequest.getPhoneNumber())).thenReturn(Optional.empty());

        User expectedUpdatedUser = User.builder()
                .id(userId)
                .username(existingUser.getUsername())
                .password(existingUser.getPassword())
                .email(profileUpdateRequest.getEmail())
                .firstName(profileUpdateRequest.getFirstName())
                .lastName(profileUpdateRequest.getLastName())
                .phone(profileUpdateRequest.getPhoneNumber())
                .role(existingUser.getRole())
                .isActive(existingUser.isActive())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        User updated = userService.updateProfile(userId, profileUpdateRequest);

        assertEquals(expectedUpdatedUser.getFirstName(), updated.getFirstName());
        assertEquals(expectedUpdatedUser.getLastName(), updated.getLastName());
        assertEquals(expectedUpdatedUser.getPhone(), updated.getPhone());
        assertEquals(expectedUpdatedUser.getEmail(), updated.getEmail());
        assertEquals(existingUser.getUsername(), updated.getUsername());
        assertEquals(existingUser.getPassword(), updated.getPassword());
        assertEquals(existingUser.getRole(), updated.getRole());
        assertEquals(existingUser.isActive(), updated.isActive());
    }

    @Test
    void updateProfile_WithExistingEmail_ShouldThrowException(){
        User userWithSameEmail = User.builder()
                .id(UUID.randomUUID())
                .email(profileUpdateRequest.getEmail())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(profileUpdateRequest.getEmail())).thenReturn(Optional.of(userWithSameEmail));

        assertThrows(DomainException.class,()-> userService.updateProfile(userId, profileUpdateRequest));
        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void updateProfile_WithExistingPhone_ShouldThrowException() {

        User userWithSamePhone = User.builder()
                .id(UUID.randomUUID())
                .phone(profileUpdateRequest.getPhoneNumber())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(profileUpdateRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(profileUpdateRequest.getPhoneNumber())).thenReturn(Optional.of(userWithSamePhone));

        assertThrows(DomainException.class, () -> userService.updateProfile(userId, profileUpdateRequest));
        verify(userRepository, never()).save(any(User.class));
    }


}
