package com.medicalReportOcr.FhirResponseMetadata;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FhirResponseMetadata {
	
	 private final String source;

	    private final List<String> needsReview;

}
