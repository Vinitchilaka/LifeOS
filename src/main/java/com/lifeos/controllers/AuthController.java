package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.LoginRequest;
import com.lifeos.dtos.request.UserRegisterDTO;
import com.lifeos.dtos.request.TokenRefreshRequest;
import com.lifeos.dtos.request.OAuth2Request;
import com.lifeos.dtos.response.LoginResponse;
import com.lifeos.dtos.response.TokenRefreshResponse;
import com.lifeos.dtos.response.UserResponseDTO;
import com.lifeos.services.UserService;
import com.lifeos.services.GoogleOAuth2Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final GoogleOAuth2Service googleOAuth2Service;

    // Constructor injection
    public AuthController(UserService userService, GoogleOAuth2Service googleOAuth2Service) {
        this.userService = userService;
        this.googleOAuth2Service = googleOAuth2Service;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        CommonResponse response = userService.registerUser(registerDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("[FLOW 2] AuthController: Entering loginUser endpoint with username: " + loginRequest.username());
        LoginResponse response = userService.loginUser(loginRequest);
        System.out.println("[FLOW 8] AuthController: Exiting loginUser. Returning token in ResponseEntity.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshAccessToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenRefreshResponse response = userService.refreshAccessToken(refreshRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/oauth2/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody OAuth2Request request) {
        LoginResponse response = googleOAuth2Service.loginWithGoogle(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
