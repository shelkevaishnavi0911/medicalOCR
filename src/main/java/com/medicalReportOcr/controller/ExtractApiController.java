package com.medicalReportOcr.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.medicalReportOcr.DTO.DatalabResultResponse;
import com.medicalReportOcr.datalab.DatalabClient;
import com.medicalReportOcr.entity.MedicalObservation;
import com.medicalReportOcr.exception.NoObservationsFoundException;
import com.medicalReportOcr.fhir.FhirBundleBuilder;
import com.medicalReportOcr.fhir.FhirJsonService;
import com.medicalReportOcr.service.FileValidationService;
import com.medicalReportOcr.service.ObservationParser;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExtractApiController {

	private final FileValidationService fileValidationService;

	private final DatalabClient datalabClient;

	private final ObservationParser observationParser;

	private final FhirBundleBuilder fhirBundleBuilder;

	private final FhirJsonService fhirJsonService;

	
	@PostMapping("/token")
	public ResponseEntity<Map<String, String>> token() {

		return ResponseEntity.ok(Map.of("message", "Authentication endpoint is available"));
	}

	@PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> extract(@RequestParam("file") MultipartFile file) {

		fileValidationService.validate(file);

		DatalabResultResponse ocrResult = datalabClient.process(file);

		System.out.println("====================================");
		System.out.println("OCR MARKDOWN:");
		System.out.println(ocrResult.getMarkdown());
		System.out.println("====================================");

		if (ocrResult.getMarkdown() == null || ocrResult.getMarkdown().isBlank()) {

			throw new IllegalStateException("OCR returned no extractable text");
		}

		List<MedicalObservation> observations = observationParser.parse(ocrResult.getMarkdown());

		System.out.println("OBSERVATIONS FOUND: " + observations.size());

		if (observations.isEmpty()) {

			throw new NoObservationsFoundException("OCR completed but no medical observations were found");
		}

		Bundle bundle = fhirBundleBuilder.build(observations);

		List<String> needsReview = observations.stream().filter(MedicalObservation::isNeedsReview)
				.map(MedicalObservation::getTestName).distinct().toList();

		String json = fhirJsonService.encode(bundle, needsReview);

		return ResponseEntity.ok(json);
	}
}
