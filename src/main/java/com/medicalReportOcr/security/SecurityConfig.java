package com.medicalReportOcr.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
	
	


	    @Bean
	    public SecurityFilterChain securityFilterChain(
	            HttpSecurity http) throws Exception {

	        http
	            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	            .csrf(csrf -> csrf.disable())
	            .sessionManagement(session ->
	                session.sessionCreationPolicy(
	                    SessionCreationPolicy.STATELESS
	                )
	            )
	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers(HttpMethod.OPTIONS, "/**")
	                .permitAll()

	                .requestMatchers(
	                    "/extract",
	                    "/actuator/health",
	                    "/swagger-ui/**",
	                    "/v3/api-docs/**"
	                )
	                .permitAll()

	                .anyRequest()
	                .authenticated()
	            );

	        return http.build();
	    }

	    @Bean
	    public CorsConfigurationSource corsConfigurationSource() {

	        CorsConfiguration configuration =
	                new CorsConfiguration();

	        configuration.setAllowedOrigins(List.of(
	                "http://127.0.0.1:5500",
	                "http://localhost:5500"
	        ));

	        configuration.setAllowedMethods(List.of(
	                "GET",
	                "POST",
	                "PUT",
	                "DELETE",
	                "OPTIONS"
	        ));

	        configuration.setAllowedHeaders(List.of(
	                "Authorization",
	                "Content-Type",
	                "Accept"
	        ));

	        configuration.setAllowCredentials(true);

	        UrlBasedCorsConfigurationSource source =
	                new UrlBasedCorsConfigurationSource();

	        source.registerCorsConfiguration(
	                "/**",
	                configuration
	        );

	        return source;
	    }
	}
	
	
	

