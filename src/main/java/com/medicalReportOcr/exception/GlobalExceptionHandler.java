package com.medicalReportOcr.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GlobalExceptionHandler {

	    @ExceptionHandler(FileValidationException.class)
	    public ResponseEntity<ApiErrorResponse> handleFileValidation(
	            FileValidationException exception,
	            HttpServletRequest request) {

	        return buildResponse(
	                HttpStatus.BAD_REQUEST,
	                exception.getMessage(),
	                request
	        );
	    }

	    @ExceptionHandler(NoObservationsFoundException.class)
	    public ResponseEntity<ApiErrorResponse> handleNoObservations(
	            NoObservationsFoundException exception,
	            HttpServletRequest request) {

	        return buildResponse(
	                HttpStatus.UNPROCESSABLE_ENTITY,
	                exception.getMessage(),
	                request
	        );
	    }

	    @ExceptionHandler(DatalabException.class)
	    public ResponseEntity<ApiErrorResponse> handleDatalabException(
	            DatalabException exception,
	            HttpServletRequest request) {

	        return buildResponse(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                exception.getMessage(),
	                request
	        );
	    }

	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
	            Exception exception,
	            HttpServletRequest request) {
	        return buildResponse(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "An unexpected error occurred",
	                request
	        );
	    }

	    private ResponseEntity<ApiErrorResponse> buildResponse(
	            HttpStatus status,
	            String message,
	            HttpServletRequest request) {

	        ApiErrorResponse response =
	                ApiErrorResponse.builder()
	                        .timestamp(Instant.now())
	                        .status(status.value())
	                        .error(status.getReasonPhrase())
	                        .message(message)
	                        .path(request.getRequestURI())
	                        .build();

	        return ResponseEntity
	                .status(status)
	                .body(response);
	    }
	}

