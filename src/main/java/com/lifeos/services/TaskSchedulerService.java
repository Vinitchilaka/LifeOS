package com.lifeos.services;

import com.lifeos.models.Task;
import com.lifeos.models.TaskPriority;
import com.lifeos.models.TaskStatus;
import com.lifeos.repositories.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public TaskSchedulerService(TaskRepository taskRepository, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void escalateOverdueTasks() {
        System.out.println("[CRON SCHEDULER START] Checking for overdue tasks at: " + LocalDateTime.now());

        List<Task> overdueTasks = taskRepository.findByStatusNotAndDueDateBefore(
                TaskStatus.COMPLETED,
                LocalDateTime.now()
        );

        if (overdueTasks.isEmpty()) {
            System.out.println("[CRON SCHEDULER END] No overdue tasks found.");
            return;
        }

        System.out.println("[CRON SCHEDULER] Found " + overdueTasks.size() + " overdue tasks. Escalating priority to HIGH...");

        for (Task task : overdueTasks) {
            if (task.getPriority() != TaskPriority.HIGH) {
                task.setPriority(TaskPriority.HIGH);
                taskRepository.save(task);
                System.out.println("[ESCALATION] Escalated Task ID: " + task.getId() + " - Title: " + task.getTitle());
            }
        }

        // Group overdue tasks by User and send notification digest
        java.util.Map<com.lifeos.models.User, List<Task>> tasksByUser = overdueTasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(Task::getUser));

        tasksByUser.forEach((user, tasks) -> {
            List<String> taskTitles = tasks.stream()
                    .map(Task::getTitle)
                    .collect(java.util.stream.Collectors.toList());
            emailService.sendOverdueSummaryEmail(user.getEmail(), user.getFirstName(), taskTitles);
        });

        System.out.println("[CRON SCHEDULER END] Overdue tasks processing completed.");
    }
}
