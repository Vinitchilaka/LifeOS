package com.lifeos.controllers;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.TaskRequest;
import com.lifeos.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse> createTask(@Valid @RequestBody TaskRequest request, Principal principal) {
        CommonResponse response = taskService.createTask(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getUserTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status,
            Principal principal) {
        CommonResponse response = taskService.getUserTasks(principal.getName(), page, size, sortBy, sortDir, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<CommonResponse> getTasksByProject(@PathVariable Long projectId, Principal principal) {
        CommonResponse response = taskService.getTasksByProject(principal.getName(), projectId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<CommonResponse> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequest request, Principal principal) {
        CommonResponse response = taskService.updateTask(principal.getName(), taskId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<CommonResponse> deleteTask(@PathVariable Long taskId, Principal principal) {
        CommonResponse response = taskService.deleteTask(principal.getName(), taskId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
