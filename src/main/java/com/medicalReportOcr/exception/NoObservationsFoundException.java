package com.medicalReportOcr.exception;

public class NoObservationsFoundException extends RuntimeException
{

	public NoObservationsFoundException(
            String message) {

        super(message);
    }
}
