package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.LoginRequest;
import com.uca.parcialfinalncapas.dto.response.LoginResponse;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.utils.JwtUtil;
import com.uca.parcialfinalncapas.utils.enums.Rol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getCorreo(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            String jwt = jwtUtil.generateToken(user.getCorreo(), user.getRol().name()); // Usar getRol().name()
            return ResponseEntity.ok(new LoginResponse(jwt, user.getCorreo(), user.getRol().name()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/register-test-user")
    public ResponseEntity<String> registerTestUser(@RequestBody LoginRequest request, @RequestParam String rol) {
        if (userRepository.findByEmail(request.getCorreo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Correo already exists");
        }
        User newUser = new User();
        newUser.setCorreo(request.getCorreo());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar
        try {
            newUser.setRol(Rol.valueOf(rol.toUpperCase())); // Asigna el rol usando tu enum
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid rol. Must be USER or TECH.");
        }
        newUser.setNombre("Test"); // Valores por defecto
        newUser.setApellido("User"); // Valores por defecto
        userRepository.save(newUser);
        return ResponseEntity.ok("Test user registered successfully: " + request.getCorreo() + " with rol " + rol.toUpperCase());
    }
}