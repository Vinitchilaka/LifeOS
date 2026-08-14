package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.ProjectRequest;
import com.lifeos.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse> createProject(@Valid @RequestBody ProjectRequest request, Principal principal) {
        CommonResponse response = projectService.createProject(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getUserProjects(Principal principal) {
        CommonResponse response = projectService.getUserProjects(principal.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
