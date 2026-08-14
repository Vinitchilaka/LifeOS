package com.lifeos.services;

import com.lifeos.dtos.request.OAuth2Request;
import com.lifeos.dtos.response.LoginResponse;
import com.lifeos.models.User;
import com.lifeos.models.UserPreferences;
import com.lifeos.repositories.UserRepository;
import com.lifeos.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    @Value("${app.oauth2.google.client-id}")
    private String clientId;

    @Value("${app.oauth2.google.client-secret}")
    private String clientSecret;

    public GoogleOAuth2Service(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.restTemplate = new RestTemplate(); // Directly instantiated
    }

    @Transactional
    public LoginResponse loginWithGoogle(OAuth2Request request) {
        // Step 1: Exchange code for Google Access Token
        GoogleTokenResponse tokenResponse = exchangeCodeForToken(request);
        if (tokenResponse == null || tokenResponse.access_token() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("Failed to exchange authorization code with Google");
        }

        // Step 2: Fetch user profile from Google using the access token
        GoogleUserInfo userInfo = fetchGoogleUserInfo(tokenResponse.access_token());
        if (userInfo == null || userInfo.email() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("Failed to fetch user profile from Google");
        }

        // Step 3: Map user to our local database
        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> registerNewGoogleUser(userInfo));

        // If user existed locally but didn't have Google linked, link it
        if (!"GOOGLE".equals(user.getProvider())) {
            user.setProvider("GOOGLE");
            user.setProviderId(userInfo.sub());
            user.setEmailVerified(userInfo.email_verified());
            userRepository.save(user);
        }

        // Step 4: Generate local JWT and Refresh Token
        String accessToken = jwtUtils.generateToken(user.getUsername());
        String refreshToken = UUID.randomUUID().toString();

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        return new LoginResponse(accessToken, "Bearer", 900000L, refreshToken); // 15 mins Access token
    }

    private GoogleTokenResponse exchangeCodeForToken(OAuth2Request request) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", request.code());
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", request.redirectUri());
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(tokenUrl, httpEntity, GoogleTokenResponse.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private GoogleUserInfo fetchGoogleUserInfo(String googleAccessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, httpEntity, GoogleUserInfo.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private User registerNewGoogleUser(GoogleUserInfo userInfo) {
        User user = new User();
        user.setFirstName(userInfo.given_name());
        user.setLastName(userInfo.family_name());
        user.setEmail(userInfo.email());
        
        // Define default username from email prefix
        String defaultUsername = userInfo.email().split("@")[0];
        if (userRepository.existsByUsername(defaultUsername)) {
            defaultUsername = defaultUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
        }
        user.setUsername(defaultUsername);
        
        user.setProvider("GOOGLE");
        user.setProviderId(userInfo.sub());
        user.setEmailVerified(userInfo.email_verified());
        
        // Generate secure random password
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        // Link default preferences
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        user.setPreferences(preferences);

        return userRepository.save(user);
    }

    // Helper records for mapping JSON responses
    private record GoogleTokenResponse(
            String access_token,
            String expires_in,
            String scope,
            String token_type,
            String id_token
    ) {}

    private record GoogleUserInfo(
            String sub,
            String name,
            String given_name,
            String family_name,
            String picture,
            String email,
            boolean email_verified
    ) {}
}
