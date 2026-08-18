package com.medicalReportOcr.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalObservation {

	    private String testName;

	    private Double value;

	    private String unit;

	    private Double referenceLow;

	    private Double referenceHigh;

	    private boolean needsReview;

	    private String reviewReason;
	}


