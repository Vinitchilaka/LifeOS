package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.TaskRequest;
import com.lifeos.dtos.response.PaginatedTaskResponse;
import com.lifeos.dtos.response.TaskResponse;
import com.lifeos.exceptions.ResourceNotFoundException;
import com.lifeos.models.*;
import com.lifeos.repositories.ProjectRepository;
import com.lifeos.repositories.TaskRepository;
import com.lifeos.repositories.UserRepository;
import com.lifeos.repositories.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GoalService goalService;
    private final AsyncLogService asyncLogService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository, GoalRepository goalRepository, 
                       GoalService goalService, AsyncLogService asyncLogService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.goalService = goalService;
        this.asyncLogService = asyncLogService;
    }

    @Transactional
    public CommonResponse createTask(String username, TaskRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Project project = null;
        if (dto.projectId() != null) {
            project = projectRepository.findByIdAndUser(dto.projectId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        }

        Goal goal = null;
        if (dto.goalId() != null) {
            goal = goalRepository.findByIdAndUser(dto.goalId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found or access denied"));
        }

        TaskPriority priority;
        try {
            priority = TaskPriority.valueOf(dto.priority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: " + dto.priority());
        }

        TaskStatus status;
        try {
            status = TaskStatus.valueOf(dto.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + dto.status());
        }

        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDueDate(dto.dueDate());
        task.setPriority(priority);
        task.setStatus(status);
        task.setEstimatedEffort(dto.estimatedEffort());
        task.setUser(user);
        task.setProject(project);
        task.setGoal(goal);

        Task savedTask = taskRepository.save(task);

        if (goal != null) {
            goalService.recalculateGoalProgress(goal);
        }

        TaskResponse responseDTO = mapToTaskResponse(savedTask);

        // Trigger non-blocking async logger
        asyncLogService.logTaskEvent("Task created: " + savedTask.getTitle() + " by user: " + username);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.CREATED.value());
        response.setMessage("Task Created Successfully!");
        response.setStatus("Success");
        response.setData(responseDTO);
        return response;
    }

    @Transactional(readOnly = true)
    public CommonResponse getUserTasks(String username, int page, int size, String sortBy, String sortDir, String statusFilter) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        int pageIndex = page > 0 ? page - 1 : 0;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<Task> taskPage;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            TaskStatus status;
            try {
                status = TaskStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter: " + statusFilter);
            }
            taskPage = taskRepository.findByUserAndStatus(user, status, pageable);
        } else {
            taskPage = taskRepository.findByUser(user, pageable);
        }

        List<TaskResponse> taskList = taskPage.getContent().stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());

        PaginatedTaskResponse paginatedResponse = new PaginatedTaskResponse(
                taskList,
                taskPage.getNumber() + 1,
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Tasks fetched successfully!");
        response.setStatus("Success");
        response.setData(paginatedResponse);
        return response;
    }

    @Transactional(readOnly = true)
    public CommonResponse getTasksByProject(String username, Long projectId) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));

        List<TaskResponse> taskList = taskRepository.findByProjectAndUser(project, user).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Project tasks fetched successfully!");
        response.setStatus("Success");
        response.setData(taskList);
        return response;
    }

    @Transactional
    public CommonResponse updateTask(String username, Long taskId, TaskRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // Security check
        if (!task.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to modify this task");
        }

        Goal oldGoal = task.getGoal();

        Project project = null;
        if (dto.projectId() != null) {
            project = projectRepository.findByIdAndUser(dto.projectId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        }

        Goal newGoal = null;
        if (dto.goalId() != null) {
            newGoal = goalRepository.findByIdAndUser(dto.goalId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found or access denied"));
        }

        TaskPriority priority;
        try {
            priority = TaskPriority.valueOf(dto.priority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: " + dto.priority());
        }

        TaskStatus status;
        try {
            status = TaskStatus.valueOf(dto.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + dto.status());
        }

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDueDate(dto.dueDate());
        task.setPriority(priority);
        task.setStatus(status);
        task.setEstimatedEffort(dto.estimatedEffort());
        task.setProject(project);
        task.setGoal(newGoal);

        Task savedTask = taskRepository.save(task);

        // Recalculate progress for old goal
        if (oldGoal != null) {
            goalService.recalculateGoalProgress(oldGoal);
        }
        // Recalculate progress for new goal
        if (newGoal != null && (oldGoal == null || !newGoal.getId().equals(oldGoal.getId()))) {
            goalService.recalculateGoalProgress(newGoal);
        }

        TaskResponse responseDTO = mapToTaskResponse(savedTask);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Task Updated Successfully!");
        response.setStatus("Success");
        response.setData(responseDTO);
        return response;
    }

    @Transactional
    public CommonResponse deleteTask(String username, Long taskId) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // Security check
        if (!task.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to delete this task");
        }

        Goal goal = task.getGoal();

        taskRepository.delete(task);

        if (goal != null) {
            goalService.recalculateGoalProgress(goal);
        }

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Task Deleted Successfully!");
        response.setStatus("Success");
        return response;
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority().name(),
                task.getStatus().name(),
                task.getEstimatedEffort(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,
                task.getGoal() != null ? task.getGoal().getId() : null,
                task.getGoal() != null ? task.getGoal().getTitle() : null,
                task.getCreatedAt()
        );
    }
}
