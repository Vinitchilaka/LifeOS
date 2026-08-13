package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.LoginRequest;
import com.lifeos.dtos.request.UserRegisterDTO;
import com.lifeos.dtos.response.LoginResponse;
import com.lifeos.dtos.response.UserResponseDTO;
import com.lifeos.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    // Standard constructor injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        CommonResponse response = userService.registerUser(registerDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.loginUser(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
