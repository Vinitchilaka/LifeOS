package com.lifeos.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences_tbl")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String theme = "DARK"; // Default configuration

    private String language = "en";

    private String timezone = "UTC";

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    // The Owning Side of the One-to-One relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- Standard Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
