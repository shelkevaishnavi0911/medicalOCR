package com.medicalReportOcr.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.medicalReportOcr.config.AppProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BearerToken extends OncePerRequestFilter {


	    private static final String BEARER_PREFIX = "Bearer ";

	    private final AppProperties appProperties;

	    @Override
	    protected void doFilterInternal(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            FilterChain filterChain)
	            throws ServletException, IOException {

	        String authorizationHeader =
	                request.getHeader(HttpHeaders.AUTHORIZATION);

	        if (authorizationHeader == null
	                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

	            sendUnauthorizedResponse(
	                    response,
	                    "Missing or invalid Authorization header"
	            );

	            return;
	        }

	        String token =
	                authorizationHeader
	                        .substring(BEARER_PREFIX.length())
	                        .trim();

	        String expectedToken =
	                appProperties
	                        .getAuth()
	                        .getToken();

	        if (expectedToken == null
	                || expectedToken.isBlank()) {

	            sendUnauthorizedResponse(
	                    response,
	                    "Authentication is not configured"
	            );

	            return;
	        }
	        if (!token.equals(expectedToken)) {

	            sendUnauthorizedResponse(
	                    response,
	                    "Invalid bearer token"
	            );

	            return;
	        }
	        UsernamePasswordAuthenticationToken authentication =
	                new UsernamePasswordAuthenticationToken(
	                        "api-client",
	                        null,
	                        AuthorityUtils.NO_AUTHORITIES
	                );

	        authentication.setDetails(
	                new WebAuthenticationDetailsSource()
	                        .buildDetails(request)
	        );

	        SecurityContextHolder
	                .getContext()
	                .setAuthentication(authentication);

	        filterChain.doFilter(request, response);
	    }

	    private void sendUnauthorizedResponse(
	            HttpServletResponse response,
	            String message)
	            throws IOException {

	        response.setStatus(
	                HttpServletResponse.SC_UNAUTHORIZED
	        );

	        response.setContentType(
	                "application/json"
	        );

	        response.getWriter().write(
	                """
	                {
	                    "status": 401,
	                    "error": "Unauthorized",
	                    "message": "%s"
	                }
	                """.formatted(message)
	        );
	    }
	}


