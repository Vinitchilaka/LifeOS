package com.lifeos.services;

import com.lifeos.dtos.common.CommonResponse;
import com.lifeos.dtos.event.UserRegisteredEvent;
import com.lifeos.dtos.request.LoginRequest;
import com.lifeos.dtos.request.UserRegisterDTO;
import com.lifeos.dtos.response.LoginResponse;
import com.lifeos.dtos.response.UserResponseDTO;
import com.lifeos.dtos.request.UpdateUserPreferenceRequest;
import com.lifeos.dtos.request.UpdateUserProfileRequest;
import com.lifeos.dtos.request.TokenRefreshRequest;
import com.lifeos.dtos.response.PaginatedUserResponse;
import com.lifeos.dtos.response.TokenRefreshResponse;
import com.lifeos.dtos.response.UserPreferencesResponse;
import com.lifeos.exceptions.ResourceNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userEventProducer = userEventProducer;
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

        // Publish registration event to Kafka
        userEventProducer.sendUserRegisteredEvent(new UserRegisteredEvent(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        ));

        commonResponse.setResponseStatus(HttpStatus.OK.value());
        commonResponse.setMessage("User Created Successfully!");
        commonResponse.setStatus("Success");
        return commonResponse;
    }


    public CommonResponse getUsersList(int page, int size, String sortBy, String sortDir, String search) {
        int pageIndex = page > 0 ? page - 1 : 0;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<User> userPage;
        if (search == null || search.trim().isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        }

        List<UserResponseDTO> userList = userPage.getContent().stream()
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

        PaginatedUserResponse paginatedResponse = new PaginatedUserResponse(
                userList,
                userPage.getNumber() + 1, // client sees 1-based page
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );

        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setResponseStatus(HttpStatus.OK.value());
        commonResponse.setMessage("User List Fetched Successfully!");
        commonResponse.setStatus("Success");
        commonResponse.setData(paginatedResponse);
        return commonResponse;
    }

    @Transactional
    public LoginResponse loginUser(LoginRequest dto) {
        System.out.println("[FLOW 3] UserService: Entering loginUser. Delegating authentication to AuthenticationManager...");
        
        // Authenticate the user
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        System.out.println("[FLOW 5] UserService: Authentication successful! Extracting authenticated principal...");

        // Get the canonical username from authentication principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String canonicalUsername = userDetails.getUsername();
        
        System.out.println("[FLOW 5] UserService: Username is: " + canonicalUsername + ". Generating JWT token...");
        // Generate the token
        String token = jwtUtils.generateToken(canonicalUsername);
        
        System.out.println("[FLOW 7] UserService: Token generated successfully. Generating Refresh Token...");
        
        // Generate and set Refresh Token (UUID with 24 hours expiry)
        String refreshToken = UUID.randomUUID().toString();
        
        // Find the user by username to save the refresh token
        User user = userRepository.findByUsername(canonicalUsername)
                .or(() -> userRepository.findByEmail(canonicalUsername))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + canonicalUsername));
        
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        System.out.println("[FLOW 7] UserService: Refresh Token persisted. Returning LoginResponse...");
        // Return both tokens (expiresInMs for Access Token = 15 minutes = 900000 ms)
        return new LoginResponse(token, "Bearer", 900000L, refreshToken);
    }


    public CommonResponse getUserByID(Long id){
        CommonResponse commonResponse = new CommonResponse();

        User userObj = userRepository.findByIdAndStatus(id, true);
        if(userObj != null){
            UserResponseDTO userList =  new UserResponseDTO(
                    userObj.getId(),
                    userObj.getUsername(),
                    userObj.getEmail(),
                    userObj.getFirstName(),
                    userObj.getLastName(),
                    userObj.getMobileNo(),
                    userObj.getCreatedAt()
            );
            commonResponse.setResponseStatus(org.springframework.http.HttpStatus.OK.value());
            commonResponse.setMessage("User Fetched Successfully!");
            commonResponse.setStatus("Success");
            commonResponse.setData(userList);
        }
        return commonResponse;
    }

    @Transactional(readOnly = true)
    public CommonResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + username));

        UserResponseDTO profileDTO = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMobileNo(),
                user.getCreatedAt()
        );

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("User profile fetched successfully!");
        response.setStatus("Success");
        response.setData(profileDTO);
        return response;
    }

    @Transactional
    public CommonResponse updateCurrentUserProfile(String username, UpdateUserProfileRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + username));

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setMobileNo(dto.mobileNo());

        userRepository.save(user);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("User profile updated successfully!");
        response.setStatus("Success");
        return response;
    }

    @Transactional(readOnly = true)
    public CommonResponse getCurrentUserPreferences(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + username));

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            preferences.setUser(user);
            user.setPreferences(preferences);
        }

        UserPreferencesResponse prefsDTO = new UserPreferencesResponse(
                preferences.getTheme(),
                preferences.getLanguage(),
                preferences.getTimezone(),
                preferences.getEmailNotifications()
        );

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("User preferences fetched successfully!");
        response.setStatus("Success");
        response.setData(prefsDTO);
        return response;
    }

    @Transactional
    public CommonResponse updateCurrentUserPreferences(String username, UpdateUserPreferenceRequest dto) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + username));

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            preferences.setUser(user);
            user.setPreferences(preferences);
        }

        preferences.setTheme(dto.theme());
        preferences.setLanguage(dto.language());
        preferences.setTimezone(dto.timezone());
        preferences.setEmailNotifications(dto.emailNotifications());

        userRepository.save(user);

        CommonResponse response = new CommonResponse();
        response.setResponseStatus(HttpStatus.OK.value());
        response.setMessage("User preferences updated successfully!");
        response.setStatus("Success");
        return response;
    }

    @Transactional
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {
        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token has expired. Please log in again.");
        }

        // Generate new Access Token
        String newAccessToken = jwtUtils.generateToken(user.getUsername());

        // Rotate Refresh Token (Generate new one, invalidate old one)
        String newRefreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        return new TokenRefreshResponse(newAccessToken, "Bearer", 900000L, newRefreshToken);
    }

}
