package org.example.rentacar.user.service;

import lombok.extern.slf4j.Slf4j;
import org.example.rentacar.email.service.NotificationService;
import org.example.rentacar.exception.DomainException;
import org.example.rentacar.security.AuthenticationDetails;
import org.example.rentacar.user.model.User;
import org.example.rentacar.user.model.UserRole;
import org.example.rentacar.user.repository.UserRepository;
import org.example.rentacar.web.dto.ProfileUpdateRequest;
import org.example.rentacar.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional
    public void register (RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());
        if (userOptional.isPresent()) {
            throw new DomainException("Username already exists");
        }
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phone(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        notificationService.sendWelcomeEmail(savedUser.getId(), savedUser.getEmail());

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("User with this username does not exist"));

        return new AuthenticationDetails(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }


    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(userId.toString()));
    }

    public User updateProfile(UUID userId, ProfileUpdateRequest profileUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.toString()));

        Optional<User> userWithEmail = userRepository.findByEmail(profileUpdateRequest.getEmail());
        if (userWithEmail.isPresent()  && !userWithEmail.get().getId().equals(userId)) {
            throw new DomainException("User with this email already exists");
        }

        Optional<User> userWithPhone = userRepository.findByPhone(profileUpdateRequest.getPhoneNumber());
        if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(userId)) {
            throw new DomainException("User with this phone number already exists");
        }
        user.setFirstName(profileUpdateRequest.getFirstName());
        user.setLastName(profileUpdateRequest.getLastName());
        user.setPhone(profileUpdateRequest.getPhoneNumber());
        user.setEmail(profileUpdateRequest.getEmail());

        return userRepository.save(user);
    }


}
