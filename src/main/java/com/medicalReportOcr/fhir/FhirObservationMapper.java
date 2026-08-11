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

	private static final String LOINC_SYSTEM = "http://loinc.org";
	private static final String UCUM_SYSTEM = "http://unitsofmeasure.org";
	private static final String INTERPRETATION_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";
	private final LoincCodeMapper loincCodeMapper;

	public Observation map(MedicalObservation source) {

		Observation observation = new Observation();
		observation.setStatus(Observation.ObservationStatus.PRELIMINARY);

		mapCode(observation, source);

		if (source.getValue() != null) {

			Quantity quantity = new Quantity();
			quantity.setValue(source.getValue());
			if (source.getUnit() != null && !source.getUnit().isBlank()) {
				quantity.setUnit(source.getUnit());
				quantity.setSystem(UCUM_SYSTEM);
				quantity.setCode(source.getUnit());
			}
			observation.setValue(quantity);
		}


		mapInterpretation(observation, source.getInterpretation());
		mapReferenceRange(observation, source);
		return observation;
	}

	private void mapCode(Observation observation, MedicalObservation source) {

		String testName = source.getTestName();
		CodeableConcept code = new CodeableConcept();
		code.setText(testName);
		LoincCodeMapper.LoincCode loinc = loincCodeMapper.find(testName);

		if (loinc != null) {
			Coding coding = new Coding();
			coding.setSystem(LOINC_SYSTEM);
			coding.setCode(loinc.code());
			coding.setDisplay(loinc.display());
			code.addCoding(coding);
		}
		observation.setCode(code);
	}

	private void mapInterpretation(Observation observation, String interpretation) {

		if (interpretation == null || interpretation.isBlank()) {
			return;
		}

		Coding coding = new Coding();

		coding.setSystem(INTERPRETATION_SYSTEM);

		switch (interpretation) {

		case "L" -> {
			coding.setCode("L");
			coding.setDisplay("Low");
		}

		case "H" -> {
			coding.setCode("H");
			coding.setDisplay("High");
		}

		case "N" -> {
			coding.setCode("N");
			coding.setDisplay("Normal");
		}

		default -> {
			return;
		}
		}
		CodeableConcept concept = new CodeableConcept();
		concept.addCoding(coding);
		observation.addInterpretation(concept);
	}

	private void mapReferenceRange(
	        Observation observation,
	        MedicalObservation source) {

	    if (source.getReferenceLow() == null &&
	            source.getReferenceHigh() == null) {

	        return;
	    }

	    Observation.ObservationReferenceRangeComponent referenceRange =
	            new Observation.ObservationReferenceRangeComponent();

	    if (source.getReferenceLow() != null) {

	        Quantity low = new Quantity();

	        low.setValue(source.getReferenceLow());

	        if (source.getUnit() != null &&
	                !source.getUnit().isBlank()) {

	            low.setUnit(source.getUnit());
	            low.setSystem(UCUM_SYSTEM);
	            low.setCode(source.getUnit());
	        }

	        referenceRange.setLow(low);
	    }

	    if (source.getReferenceHigh() != null) {

	        Quantity high = new Quantity();

	        high.setValue(source.getReferenceHigh());

	        if (source.getUnit() != null &&
	                !source.getUnit().isBlank()) {

	            high.setUnit(source.getUnit());
	            high.setSystem(UCUM_SYSTEM);
	            high.setCode(source.getUnit());
	        }

	        referenceRange.setHigh(high);
	    }

	    observation.addReferenceRange(referenceRange);
	}
}
