package com.medicalReportOcr.fhir;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class LoincCodeMapper {

	private static final Map<String, LoincCode> CODES = Map.ofEntries(

			Map.entry("hemoglobin", new LoincCode("718-7", "Hemoglobin")),

			Map.entry("glucose", new LoincCode("2345-7", "Glucose")),

			Map.entry("creatinine", new LoincCode("2160-0", "Creatinine")),

			Map.entry("hematocrit", new LoincCode("4544-3", "Hematocrit")),

			Map.entry("mcv", new LoincCode("787-2", "MCV")),

			Map.entry("mch", new LoincCode("785-6", "MCH")),

			Map.entry("platelet count", new LoincCode("777-3", "Platelet count")),

			Map.entry("white blood cell count", new LoincCode("6690-2", "Leukocytes")),

			Map.entry("alt (sgpt)", new LoincCode("1742-6", "ALT")),

			Map.entry("ast (sgot)", new LoincCode("1920-8", "AST")));

	public LoincCode find(String testName) {

		if (testName == null) {
			return null;
		}

		return CODES.get(testName.trim().toLowerCase());
	}

	public record LoincCode(String code, String display) {
	}
}
