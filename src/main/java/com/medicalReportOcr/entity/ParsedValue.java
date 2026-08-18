package com.medicalReportOcr.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParsedValue {
	private Double value;
    private String unit;

}
