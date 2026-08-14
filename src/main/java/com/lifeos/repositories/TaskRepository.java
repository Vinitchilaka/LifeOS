package com.lifeos.repositories;

import com.lifeos.models.Project;
import com.lifeos.models.Task;
import com.lifeos.models.TaskStatus;
import com.lifeos.models.User;
import com.lifeos.models.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUser(User user, Pageable pageable);
    List<Task> findByProjectAndUser(Project project, User user);
    Page<Task> findByUserAndStatus(User user, TaskStatus status, Pageable pageable);
    long countByGoal(Goal goal);
    long countByGoalAndStatus(Goal goal, TaskStatus status);
    List<Task> findByStatusNotAndDueDateBefore(TaskStatus status, java.time.LocalDateTime dateTime);
    List<Task> findByUserAndStatusNot(User user, TaskStatus status);
}
