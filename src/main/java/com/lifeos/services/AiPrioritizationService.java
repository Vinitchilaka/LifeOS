package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.response.PrioritizedTasksResponse;
import com.lifeos.exceptions.ResourceNotFoundException;
import com.lifeos.models.Task;
import com.lifeos.models.TaskStatus;
import com.lifeos.models.User;
import com.lifeos.repositories.TaskRepository;
import com.lifeos.repositories.UserRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiPrioritizationService {

    private final ChatModel chatModel;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AiPrioritizationService(ChatModel chatModel, TaskRepository taskRepository, UserRepository userRepository) {
        this.chatModel = chatModel;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public CommonResponse prioritizeTasks(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<Task> pendingTasks = taskRepository.findByUserAndStatusNot(user, TaskStatus.COMPLETED);

        if (pendingTasks.isEmpty()) {
            CommonResponse response = new CommonResponse();
            response.setResponseStatus(HttpStatus.OK.value());
            response.setMessage("No pending tasks to prioritize.");
            response.setStatus("Success");
            response.setData(new PrioritizedTasksResponse(List.of()));
            return response;
        }

        // 1. Build the DTO object converter for structured Output format
        BeanOutputConverter<PrioritizedTasksResponse> outputConverter = 
                new BeanOutputConverter<>(PrioritizedTasksResponse.class);

        // 2. Build the tasks payload description for the prompt
        String tasksJsonPayload = pendingTasks.stream()
                .map(t -> String.format(
                        "{\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"priority\": \"%s\", \"dueDate\": \"%s\", \"estimatedEffort\": %s, \"goal\": %s}",
                        t.getId(),
                        t.getTitle().replace("\"", "\\\""),
                        t.getDescription() == null ? "" : t.getDescription().replace("\"", "\\\""),
                        t.getPriority() == null ? "MEDIUM" : t.getPriority().name(),
                        t.getDueDate() == null ? "None" : t.getDueDate().toString(),
                        t.getEstimatedEffort() == null ? "None" : t.getEstimatedEffort().toString() + " hours",
                        t.getGoal() == null ? "None" : "\"" + t.getGoal().getTitle().replace("\"", "\\\"") + "\""
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        // 3. Assemble the System Message (Role, rules, constraints)
        String systemInstruction = "You are a professional task prioritization assistant. Your goal is to analyze the user's task list and output a smart prioritized schedule using the Eisenhower Matrix rules (prioritizing urgent and important tasks first).\n"
                + "Ensure to factor in due dates (earlier due dates are higher priority), estimated effort, and if the task is linked to a Goal (which makes it more important).\n"
                + "You must respond ONLY with JSON matching the following schema instruction:\n"
                + outputConverter.getFormat();

        SystemMessage systemMessage = new SystemMessage(systemInstruction);

        // 4. Assemble User Message
        String userInstruction = "Here is the list of pending tasks:\n" + tasksJsonPayload + "\n\nPlease prioritize them.";
        UserMessage userMessage = new UserMessage(userInstruction);

        // 5. Send Prompt
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        var chatResponse = chatModel.call(prompt);

        // 6. Convert response to DTO record
        String rawResponse = chatResponse.getResult().getOutput().getContent();
        PrioritizedTasksResponse parsedResponse = outputConverter.convert(rawResponse);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Tasks prioritized successfully by AI!");
        response.setStatus("Success");
        response.setData(parsedResponse);
        return response;
    }
}
