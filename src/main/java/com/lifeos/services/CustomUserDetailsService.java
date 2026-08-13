package com.lifeos.services;

import com.lifeos.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Search by username. If not found, try email (so users can login with either!)
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER") // Default role
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
    }
}
