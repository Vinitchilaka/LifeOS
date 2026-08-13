package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.request.LoginRequest;
import com.lifeos.dtos.request.UserRegisterDTO;
import com.lifeos.dtos.response.LoginResponse;
import com.lifeos.dtos.response.UserResponseDTO;
import com.lifeos.exceptions.UserAlreadyExistsException;
import com.lifeos.models.User;
import com.lifeos.models.UserPreferences;
import com.lifeos.repositories.UserRepository;
import com.lifeos.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public CommonResponse registerUser(UserRegisterDTO dto) {
        CommonResponse commonResponse = new CommonResponse();

        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        if (userRepository.existsByUsername(dto.username())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setMobileNo(dto.mobileNo());
        user.setEmail(dto.email());
        user.setUsername(dto.username());
        user.setProvider("LOCAL");
        user.setPassword(passwordEncoder.encode(dto.password()));

        // --- NEW: Instantiate and link default UserPreferences ---
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);          // Link preferences to user
        user.setPreferences(preferences);    // Link user to preferences (cascades save)

        User savedUser = userRepository.save(user);

        commonResponse.setResponseStatus(HttpStatus.OK.value());
        commonResponse.setMessage("User Created Successfully!");
        commonResponse.setStatus("Success");
        return commonResponse;
    }


    public CommonResponse getUsersList() {
        CommonResponse commonResponse = new CommonResponse();
        // Map List<User> to List<UserResponseDTO> to hide passwords
        List<UserResponseDTO> userList = userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getMobileNo(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());
        commonResponse.setResponseStatus(org.springframework.http.HttpStatus.OK.value());
        commonResponse.setMessage("User List Fetched Successfully!");
        commonResponse.setStatus("Success");
        commonResponse.setData(userList);
        return commonResponse;
    }

    public LoginResponse loginUser(LoginRequest dto) {
        // Authenticate the user
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        // Get the canonical username from authentication principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String canonicalUsername = userDetails.getUsername();
        // Generate the token
        String token = jwtUtils.generateToken(canonicalUsername);
        // Return the token (24 hours expiration = 86400000 ms)
        return new LoginResponse(token, "Bearer", 86400000L);
    }

}
