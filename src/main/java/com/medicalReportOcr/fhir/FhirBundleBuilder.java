package com.medicalReportOcr.fhir;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import com.medicalReportOcr.entity.MedicalObservation;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FhirBundleBuilder {

	private final FhirObservationMapper observationMapper;

	public Bundle build(List<MedicalObservation> observations) {

		Bundle bundle = new Bundle();

		bundle.setType(Bundle.BundleType.COLLECTION);

		for (MedicalObservation source : observations) {

			Observation observation = observationMapper.map(source);

			bundle.addEntry().setResource(observation);
		}

		return bundle;
	}
}
