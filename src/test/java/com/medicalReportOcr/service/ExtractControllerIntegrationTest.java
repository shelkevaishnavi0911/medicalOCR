package com.medicalReportOcr.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.medicalReportOcr.DTO.DatalabResultResponse;
import com.medicalReportOcr.datalab.DatalabClient;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@AutoConfigureMockMvc
public class ExtractControllerIntegrationTest {
	
	

	    @Autowired
	    private MockMvc mockMvc;

	    @MockitoBean
	    private DatalabClient datalabClient;

	    @Test
	    void shouldExtractMedicalObservations()
	            throws Exception {

	        DatalabResultResponse ocrResponse =
	                new DatalabResultResponse();

	        ocrResponse.setMarkdown("""
	                # CBC Report

	                | Test | Result | Unit | Reference Range |
	                |---|---:|---|---|
	                | Hemoglobin | 11.2 | g/dL | 12.0 - 16.0 |
	                | Glucose | 108 | mg/dL | 70 - 99 |
	                | Creatinine | 0.9 | mg/dL | 0.6 - 1.1 |
	                """);

	       
	        ocrResponse.setStatus("complete");
	        ocrResponse.setSuccess(true);
	        ocrResponse.setOutput_format("markdown");
	        ocrResponse.setPage_count(1);
	        ocrResponse.setParse_quality_score(0.98);
	        ocrResponse.setError(null);
	        when(
	                datalabClient.process(
	                        any(MultipartFile.class)
	                )
	        ).thenReturn(ocrResponse);
	        MockMultipartFile file =
	                new MockMultipartFile(
	                        "file",
	                        "medical-report.pdf",
	                        MediaType.APPLICATION_PDF_VALUE,
	                        "fake-pdf-content".getBytes()
	                );
	        mockMvc.perform(
	                multipart("/extract")
	                        .file(file)
	                        .header(
	                                "Authorization",
	                                "Bearer my-secret-token"
	                        )
	        )
	        .andExpect(
	                status().isOk()
	        )
	        .andExpect(
	                content()
	                        .contentTypeCompatibleWith(
	                                MediaType.APPLICATION_JSON
	                        )
	        )
	        .andExpect(
	                jsonPath("$.resourceType")
	                        .value("Bundle")
	        )

	        .andExpect(
	                jsonPath("$.type")
	                        .value("collection")
	        )
	        .andExpect(
	                jsonPath("$.entry")
	                        .isArray()
	        )
	        .andExpect(
	                jsonPath("$.entry.length()")
	                        .value(3)
	        )
	        .andExpect(
	                jsonPath(
	                        "$.entry[0].resource.resourceType"
	                )
	                .value("Observation")
	        )
	        .andExpect(
	                jsonPath("$.meta.source")
	                        .value("ocr-extraction")
	        )

	        .andExpect(
	                jsonPath("$.meta.needsReview")
	                        .isArray()
	        );
	    }
	}
	
		



