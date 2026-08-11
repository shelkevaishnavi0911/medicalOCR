package com.medicalReportOcr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Auth auth = new Auth();
	private Datalab datalab = new Datalab();

	@Getter
	@Setter
	public static class Auth {
		private String token;
	}

	@Getter
	@Setter
	public static class Datalab {

		private String apiKey;
		private String baseUrl;
	}
}
