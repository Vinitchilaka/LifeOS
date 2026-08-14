package com.lifeos.controllers;

import com.lifeos.services.GoogleOAuth2Service;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "management.health.mail.enabled=false"
})
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleOAuth2Service googleOAuth2Service;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private ChatModel chatModel;

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        String invalidUserJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "mobileNo": "1234567890",
                    "email": "invalid-email-format",
                    "username": "johndoe",
                    "password": "SecurePassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andExpect(status().isBadRequest());
    }
}
