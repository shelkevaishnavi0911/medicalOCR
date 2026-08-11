package com.medicalReportOcr.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class MedicalObservation {
	
	 private final String testName;

	    private final String rawValue;

	    private final Double value;

	    private final String unit;

	    private final Double referenceLow;

	    private final Double referenceHigh;

	    private final String interpretation;

	    private final boolean needsReview;

	    private final String reviewReason;

}
