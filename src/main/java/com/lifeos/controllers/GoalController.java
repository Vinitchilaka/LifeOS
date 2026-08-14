package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.GoalRequest;
import com.lifeos.services.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse> createGoal(@Valid @RequestBody GoalRequest request, Principal principal) {
        CommonResponse response = goalService.createGoal(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getUserGoals(Principal principal) {
        CommonResponse response = goalService.getUserGoals(principal.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
