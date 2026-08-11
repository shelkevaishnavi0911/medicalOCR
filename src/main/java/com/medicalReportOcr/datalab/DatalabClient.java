package com.medicalReportOcr.datalab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.medicalReportOcr.DTO.DatalabResultResponse;
import com.medicalReportOcr.DTO.DatalabSubmitResponse;
import com.medicalReportOcr.exception.DatalabException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatalabClient {

	private final WebClient.Builder webClientBuilder;

	@Value("${app.datalab.api-key}")
	private String apiKey;

	@Value("${app.datalab.base-url}")
	private String baseUrl;

	@Value("${app.datalab.poll-interval-ms}")
	private long pollIntervalMs;

	@Value("${app.datalab.max-poll-attempts}")
	private int maxPollAttempts;

	public DatalabResultResponse process(MultipartFile file) {

		DatalabSubmitResponse submitResponse = submit(file);

		if (!submitResponse.isSuccess() || submitResponse.getRequest_check_url() == null) {

			throw new DatalabException("Datalab submission failed");
		}

		return poll(submitResponse.getRequest_check_url());
	}

	private DatalabSubmitResponse submit(MultipartFile file) {

		try {

			byte[] fileBytes = file.getBytes();

			ByteArrayResource resource = new ByteArrayResource(fileBytes) {

				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};

			MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
			formData.add("file", resource);
			formData.add("output_format", "markdown");
			formData.add("mode", "balanced");
			return webClientBuilder.baseUrl(baseUrl).build().post().uri("/api/v1/convert").header("X-API-Key", apiKey)
					.contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData(formData))
					.retrieve().bodyToMono(DatalabSubmitResponse.class).block();

		} catch (Exception exception) {

			throw new DatalabException("Failed to submit document to Datalab", exception);
		}
	}

	private DatalabResultResponse poll(String checkUrl) {

		WebClient client = webClientBuilder.build();
		for (int attempt = 1; attempt <= maxPollAttempts; attempt++) 
		{
			try {
				DatalabResultResponse result = client.get().uri(checkUrl).header("X-API-Key", apiKey).retrieve()
						.bodyToMono(DatalabResultResponse.class).block();
				if (result == null) {
					throw new DatalabException("Empty response from Datalab");
				}
				if ("complete".equalsIgnoreCase(result.getStatus())) {

					return result;
				}

				if ("failed".equalsIgnoreCase(result.getStatus())) {

					throw new DatalabException("Datalab processing failed: " + result.getError());
				}

				Thread.sleep(pollIntervalMs);

			} catch (InterruptedException exception) {

				Thread.currentThread().interrupt();

				throw new DatalabException("Datalab polling interrupted", exception);

			} catch (DatalabException exception) {

				throw exception;

			} catch (Exception exception) {

				throw new DatalabException("Failed while polling Datalab", exception);
			}
		}

		throw new DatalabException("Datalab processing timed out");
	}

	
}
