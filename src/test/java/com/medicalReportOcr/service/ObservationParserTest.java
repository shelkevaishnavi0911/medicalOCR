package com.medicalReportOcr.service;

import org.junit.jupiter.api.Test;

import com.medicalReportOcr.entity.MedicalObservation;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ObservationParserTest {

	private final ObservationParser parser = new ObservationParser();

	@Test
	void shouldParseLowHemoglobin() {

		String markdown = """
				| Test | Result | Unit | Reference Range |
				|------|--------|------|-----------------|
				| Hemoglobin | 11.2 | g/dL | 12.0 - 16.0 |
				""";

		List<MedicalObservation> result = parser.parse(markdown);

		assertEquals(1, result.size());

		MedicalObservation observation = result.get(0);

		assertEquals("Hemoglobin", observation.getTestName());

		assertEquals(11.2, observation.getValue());

		assertEquals("g/dL", observation.getUnit());

		assertEquals(12.0, observation.getReferenceLow());

		assertEquals(16.0, observation.getReferenceHigh());

		assertEquals("L", observation.getInterpretation());

		assertFalse(observation.isNeedsReview());
	}

	@Test
	void shouldMarkMissingUnitForReview() {

		String markdown = """
				| Test | Result | Unit | Reference Range |
				|------|--------|------|-----------------|
				| Hemoglobin | 11.2 | | 12.0 - 16.0 |
				""";

		List<MedicalObservation> result = parser.parse(markdown);

		assertEquals(1, result.size());

		MedicalObservation observation = result.get(0);

		assertTrue(observation.isNeedsReview());

		assertNull(observation.getUnit());
	}
}
