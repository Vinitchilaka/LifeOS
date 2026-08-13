package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users") // Automatically secured (requires valid JWT token!)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getUsersList() {
        CommonResponse response = userService.getUsersList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
