package com.medicalReportOcr.security;

import org.springframework.stereotype.Component;

import com.medicalReportOcr.config.AppProperties;

@Component
public class TokenValidato {
	
	    private final AppProperties appProperties;

	    public TokenValidato(AppProperties appProperties) {
	        this.appProperties = appProperties;
	    }

	    public boolean isValid(String token) {

	        if (token == null || token.isBlank()) {
	            return false;
	        }

	        String expectedToken =
	                appProperties.getAuth().getToken();

	        if (expectedToken == null || expectedToken.isBlank()) {
	            return false;
	        }

	        return expectedToken.equals(token);
	    }
	}


