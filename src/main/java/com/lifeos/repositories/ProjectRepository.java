package com.lifeos.repositories;

import com.lifeos.models.Project;
import com.lifeos.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
    Optional<Project> findByIdAndUser(Long id, User user);
}
