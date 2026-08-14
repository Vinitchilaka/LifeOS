package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.UpdateUserPreferenceRequest;
import com.lifeos.dtos.request.UpdateUserProfileRequest;
import com.lifeos.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users") // Automatically secured (requires valid JWT token!)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getUsersList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        CommonResponse response = userService.getUsersList(page, size, sortBy, sortDir, search);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<CommonResponse> getUserByID(@PathVariable Long id) {
        CommonResponse response = userService.getUserByID(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 1. GET /api/v1/users/me -> Fetch profile
    @GetMapping("/me")
    public ResponseEntity<CommonResponse> getCurrentUserProfile(Principal principal) {
        CommonResponse response = userService.getCurrentUserProfile(principal.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 2. PUT /api/v1/users/me -> Update profile
    @PutMapping("/me")
    public ResponseEntity<CommonResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Principal principal) {
        CommonResponse response = userService.updateCurrentUserProfile(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 3. GET /api/v1/users/me/preferences -> Fetch preferences
    @GetMapping("/me/preferences")
    public ResponseEntity<CommonResponse> getCurrentUserPreferences(Principal principal) {
        CommonResponse response = userService.getCurrentUserPreferences(principal.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 4. PUT /api/v1/users/me/preferences -> Update preferences
    @PutMapping("/me/preferences")
    public ResponseEntity<CommonResponse> updateCurrentUserPreferences(
            @Valid @RequestBody UpdateUserPreferenceRequest request,
            Principal principal) {
        CommonResponse response = userService.updateCurrentUserPreferences(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
