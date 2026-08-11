package com.medicalReportOcr.fhir;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhn.fhir.context.FhirContext;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;

@Service
public class FhirJsonService {

	 private final FhirContext fhirContext;

	    private final ObjectMapper objectMapper;

	    public FhirJsonService(
	            ObjectMapper objectMapper) {

	        this.fhirContext =
	                FhirContext.forR4();

	        this.objectMapper =
	                objectMapper;
	    }

	    public String encode(
	            Bundle bundle,
	            List<String> needsReview) {

	       
	        String fhirJson =
	                fhirContext
	                        .newJsonParser()
	                        .setPrettyPrint(true)
	                        .encodeResourceToString(bundle);

	        try {

	           
	            JsonNode root =
	                    objectMapper.readTree(
	                            fhirJson
	                    );

	           
	            ObjectNode rootObject =
	                    (ObjectNode) root;

	           
	            ObjectNode meta =
	                    objectMapper.createObjectNode();

	            meta.put(
	                    "source",
	                    "ocr-extraction"
	            );

	            meta.set(
	                    "needsReview",
	                    objectMapper.valueToTree(
	                            needsReview
	                    )
	            );

	        
	            rootObject.set(
	                    "meta",
	                    meta
	            );

	            return objectMapper
	                    .writerWithDefaultPrettyPrinter()
	                    .writeValueAsString(
	                            rootObject
	                    );

	        } catch (Exception exception) {

	            throw new IllegalStateException(
	                    "Failed to serialize FHIR response",
	                    exception
	            );
	        }
	    }
}
