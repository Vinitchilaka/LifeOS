package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.GoalRequest;
import com.lifeos.dtos.response.GoalResponse;
import com.lifeos.exceptions.ResourceNotFoundException;
import com.lifeos.models.Goal;
import com.lifeos.models.GoalStatus;
import com.lifeos.models.TaskStatus;
import com.lifeos.models.User;
import com.lifeos.repositories.GoalRepository;
import com.lifeos.repositories.TaskRepository;
import com.lifeos.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository, TaskRepository taskRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @CacheEvict(value = "goals", key = "#username")
    @Transactional
    public CommonResponse createGoal(String username, GoalRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        GoalStatus status;
        try {
            status = GoalStatus.valueOf(dto.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + dto.status());
        }

        Goal goal = new Goal();
        goal.setTitle(dto.title());
        goal.setDescription(dto.description());
        goal.setTargetDate(dto.targetDate());
        goal.setStatus(status);
        goal.setUser(user);
        goal.setProgressPercentage(0.0);

        Goal savedGoal = goalRepository.save(goal);

        GoalResponse responseDTO = mapToGoalResponse(savedGoal);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.CREATED.value());
        response.setMessage("Goal Created Successfully!");
        response.setStatus("Success");
        response.setData(responseDTO);
        return response;
    }

    @Cacheable(value = "goals", key = "#username")
    @Transactional(readOnly = true)
    public CommonResponse getUserGoals(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<GoalResponse> goalList = goalRepository.findByUser(user).stream()
                .map(this::mapToGoalResponse)
                .collect(Collectors.toList());

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Goals fetched successfully!");
        response.setStatus("Success");
        response.setData(goalList);
        return response;
    }

    @CacheEvict(value = "goals", key = "#goal.user.username")
    @Transactional
    public void recalculateGoalProgress(Goal goal) {
        if (goal == null) return;
        long totalTasks = taskRepository.countByGoal(goal);
        if (totalTasks == 0) {
            goal.setProgressPercentage(0.0);
        } else {
            long completedTasks = taskRepository.countByGoalAndStatus(goal, TaskStatus.COMPLETED);
            double percentage = ((double) completedTasks / totalTasks) * 100.0;
            goal.setProgressPercentage(Math.round(percentage * 100.0) / 100.0); // round to 2 decimals
        }
        goalRepository.save(goal);
    }

    private GoalResponse mapToGoalResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetDate(),
                goal.getProgressPercentage(),
                goal.getStatus().name(),
                goal.getCreatedAt()
        );
    }
}
