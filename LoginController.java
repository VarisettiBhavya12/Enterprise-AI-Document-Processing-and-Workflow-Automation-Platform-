package com.bhavya.documentworkflowplatform.controller;

import com.bhavya.documentworkflowplatform.dto.LoginRequest;
import com.bhavya.documentworkflowplatform.entity.User;
import com.bhavya.documentworkflowplatform.service.JwtService;
import com.bhavya.documentworkflowplatform.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginController(UserService userService,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userService.getUserByEmail(request.getEmail());

        if (user == null) {
            return "User not found";
        }

        if (passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            String token = jwtService.generateToken(user.getEmail());

            return "Login successful. JWT Token: " + token;
        }

        return "Invalid password";
    }
}