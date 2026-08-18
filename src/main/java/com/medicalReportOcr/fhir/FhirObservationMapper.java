package com.medicalReportOcr.fhir;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Component;
import com.medicalReportOcr.entity.MedicalObservation;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FhirObservationMapper {

	public Observation map(MedicalObservation source) {

		Observation observation = new Observation();

		observation.setStatus(Observation.ObservationStatus.PRELIMINARY);

		CodeableConcept code = new CodeableConcept();

		code.setText(source.getTestName());

		observation.setCode(code);

		if (source.getValue() != null) {

			Quantity quantity = new Quantity();

			quantity.setValue(source.getValue());

			if (source.getUnit() != null && !source.getUnit().isBlank()) {

				quantity.setUnit(source.getUnit());

				quantity.setSystem("http://unitsofmeasure.org");

				quantity.setCode(source.getUnit());
			}

			observation.setValue(quantity);
		}

		if (source.getReferenceLow() != null || source.getReferenceHigh() != null) {

			Observation.ObservationReferenceRangeComponent range = new Observation.ObservationReferenceRangeComponent();

			if (source.getReferenceLow() != null) {

				Quantity low = new Quantity();

				low.setValue(source.getReferenceLow());

				if (source.getUnit() != null) {

					low.setUnit(source.getUnit());

					low.setSystem("http://unitsofmeasure.org");

					low.setCode(source.getUnit());
				}

				range.setLow(low);
			}

			if (source.getReferenceHigh() != null) {

				Quantity high = new Quantity();

				high.setValue(source.getReferenceHigh());

				if (source.getUnit() != null) {

					high.setUnit(source.getUnit());

					high.setSystem("http://unitsofmeasure.org");

					high.setCode(source.getUnit());
				}

				range.setHigh(high);
			}

			observation.addReferenceRange(range);
		}

		return observation;
	}

}
