package com.medicalReportOcr.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatalabResultResponse {

	private String status;

	private boolean success;

	private String output_format;

	private String markdown;

	private String error;

	private Integer page_count;

	private Double parse_quality_score;

}
