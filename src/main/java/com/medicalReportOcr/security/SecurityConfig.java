package com.medicalReportOcr.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	

	    private final BearerToken bearerToken;

	    public SecurityConfig(BearerToken bearerToken) {
	        this.bearerToken = bearerToken;
	    }

	    @Bean
	    public SecurityFilterChain securityFilterChain(
	            HttpSecurity http) throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())

	            .sessionManagement(session ->
	                session.sessionCreationPolicy(
	                    SessionCreationPolicy.STATELESS
	                )
	            )

	            .authorizeHttpRequests(auth -> auth

	                .requestMatchers(
	                    "/actuator/health"
	                ).permitAll()

	                .requestMatchers(
	                    "/swagger-ui/**",
	                    "/v3/api-docs/**"
	                ).permitAll()

	                .anyRequest().authenticated()
	            )
	            .addFilterBefore(
	                    bearerToken,
	                    UsernamePasswordAuthenticationFilter.class
	            );

	        return http.build();
	    }
	}
	

