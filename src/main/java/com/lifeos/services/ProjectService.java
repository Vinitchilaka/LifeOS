package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.ProjectRequest;
import com.lifeos.dtos.response.ProjectResponse;
import com.lifeos.exceptions.ResourceNotFoundException;
import com.lifeos.models.Project;
import com.lifeos.models.User;
import com.lifeos.repositories.ProjectRepository;
import com.lifeos.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "projects", key = "#username")
    @Transactional
    public CommonResponse createProject(String username, ProjectRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setUser(user);

        Project savedProject = projectRepository.save(project);

        ProjectResponse responseDTO = new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getCreatedAt()
        );

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.CREATED.value());
        response.setMessage("Project Created Successfully!");
        response.setStatus("Success");
        response.setData(responseDTO);
        return response;
    }

    @Cacheable(value = "projects", key = "#username")
    @Transactional(readOnly = true)
    public CommonResponse getUserProjects(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<ProjectResponse> projectList = projectRepository.findByUser(user).stream()
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getCreatedAt()
                ))
                .collect(Collectors.toList());

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("Projects fetched successfully!");
        response.setStatus("Success");
        response.setData(projectList);
        return response;
    }
}
