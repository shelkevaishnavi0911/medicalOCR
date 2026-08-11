package com.medicalReportOcr.exception;

public class DatalabException extends RuntimeException {

	public DatalabException(String message) {
		super(message);
	}

	public DatalabException(String message, Throwable cause) {

		super(message, cause);
	}

}
