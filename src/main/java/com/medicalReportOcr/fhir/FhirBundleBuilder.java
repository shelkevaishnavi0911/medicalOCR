package com.medicalReportOcr.fhir;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import com.medicalReportOcr.entity.MedicalObservation;

import java.util.List;

@Component
public class FhirBundleBuilder {

	private final FhirObservationMapper mapper;

	public FhirBundleBuilder(FhirObservationMapper mapper) {

		this.mapper = mapper;
	}

	public Bundle build(List<MedicalObservation> observations) {

		Bundle bundle = new Bundle();

		bundle.setType(Bundle.BundleType.COLLECTION);

		if (observations == null || observations.isEmpty()) {

			return bundle;
		}

		for (MedicalObservation source : observations) {

			if (source.getTestName() == null || source.getTestName().isBlank()) {

				continue;
			}

			Observation observation = mapper.map(source);

			bundle.addEntry().setResource(observation);
		}

		return bundle;
	}

}
