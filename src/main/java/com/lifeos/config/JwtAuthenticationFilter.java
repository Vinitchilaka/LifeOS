package com.lifeos.config;

import com.lifeos.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // Standard constructor injection
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("\n[FLOW 1] JwtAuthenticationFilter: Intercepted request to URI: " + request.getRequestURI());
        String jwt = parseJwt(request);
        if (jwt == null) {
            System.out.println("[FLOW 1] JwtAuthenticationFilter: No JWT Token found in Authorization header. Proceeding down filter chain.");
        } else {
            System.out.println("[FLOW 1] JwtAuthenticationFilter: JWT Token found. Validating token...");
        }

        try {
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);
                System.out.println("[FLOW 1] JwtAuthenticationFilter: JWT is valid. Username: " + username + ". Loading UserDetails...");
                
                // Note: Loading user details here will also trigger CustomUserDetailsService loadUserByUsername
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set user authentication inside Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[FLOW 1] JwtAuthenticationFilter: SecurityContext successfully populated for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Cannot set user authentication: {}", e);
        }

        // Continue the filter chain execution
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix to extract token
        }

        return null;
    }
}
