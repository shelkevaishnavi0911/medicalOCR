package com.medicalReportOcr.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatalabSubmitResponse {

	private boolean success;

	private String request_id;

	private String request_check_url;

	private String error;

}
