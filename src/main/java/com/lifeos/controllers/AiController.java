package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.services.AiPrioritizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiPrioritizationService aiPrioritizationService;

    public AiController(AiPrioritizationService aiPrioritizationService) {
        this.aiPrioritizationService = aiPrioritizationService;
    }

    @PostMapping("/prioritize")
    public ResponseEntity<CommonResponse> prioritizeTasks(@AuthenticationPrincipal UserDetails userDetails) {
        CommonResponse response = aiPrioritizationService.prioritizeTasks(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
