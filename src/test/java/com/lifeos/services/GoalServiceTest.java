package com.lifeos.services;

import com.lifeos.models.Goal;
import com.lifeos.models.TaskStatus;
import com.lifeos.repositories.GoalRepository;
import com.lifeos.repositories.TaskRepository;
import com.lifeos.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GoalService goalService;

    private Goal goal;

    @BeforeEach
    void setUp() {
        goal = new Goal();
        goal.setId(1L);
        goal.setTitle("Test Goal");
        goal.setProgressPercentage(0.0);
    }

    @Test
    void recalculateGoalProgress_NoTasks_SetsProgressToZero() {
        // Arrange
        when(taskRepository.countByGoal(goal)).thenReturn(0L);

        // Act
        goalService.recalculateGoalProgress(goal);

        // Assert
        assertEquals(0.0, goal.getProgressPercentage());
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void recalculateGoalProgress_WithTasks_CalculatesCorrectPercentage() {
        // Arrange
        when(taskRepository.countByGoal(goal)).thenReturn(4L);
        when(taskRepository.countByGoalAndStatus(goal, TaskStatus.COMPLETED)).thenReturn(2L);

        // Act
        goalService.recalculateGoalProgress(goal);

        // Assert
        assertEquals(50.0, goal.getProgressPercentage());
        verify(goalRepository, times(1)).save(goal);
    }
}
