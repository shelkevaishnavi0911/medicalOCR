package com.medicalReportOcr.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

	    @Bean
	    public SecurityFilterChain securityFilterChain(
	            HttpSecurity http) throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())

	            .cors(cors -> {})

	            .sessionManagement(session ->
	                session.sessionCreationPolicy(
	                    SessionCreationPolicy.STATELESS
	                )
	            )

	            .authorizeHttpRequests(auth -> auth

	                // Allow browser CORS preflight requests
	                .requestMatchers(
	                    HttpMethod.OPTIONS,
	                    "/**"
	                ).permitAll()

	               
	                .requestMatchers(
	                    "/extract",
	                    "/actuator/health",
	                    "/swagger-ui/**",
	                    "/v3/api-docs/**"
	                ).permitAll()

	                .anyRequest()
	                .authenticated()
	            );

	        return http.build();
	    }
	}
	
	
	
	
	

