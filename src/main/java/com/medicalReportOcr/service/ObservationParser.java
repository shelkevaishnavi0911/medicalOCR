package com.medicalReportOcr.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.springframework.stereotype.Service;

import com.medicalReportOcr.entity.MedicalObservation;
import com.medicalReportOcr.entity.ParsedRange;
import com.medicalReportOcr.entity.ParsedValue;
import java.util.Locale;

@Service
public class ObservationParser {

	private static final Pattern TABLE_ROW_PATTERN = Pattern
			.compile("^\\s*\\|\\s*(.*?)\\s*\\|\\s*(.*?)\\s*\\|\\s*(.*?)\\s*\\|\\s*$");

	private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

	private static final Pattern RANGE_PATTERN = Pattern
			.compile("(?i)([-+]?\\d+(?:\\.\\d+)?)\\s*(?:-|–|—|to)\\s*([-+]?\\d+(?:\\.\\d+)?)");

	private static final Pattern UPTO_PATTERN = Pattern
			.compile("(?i)\\b(?:up\\s*to|upto|less\\s*than\\s*or\\s*equal\\s*to)\\s*([-+]?\\d+(?:\\.\\d+)?)");

	private static final Pattern HTML_NUMBER_PATTERN = Pattern
			.compile("(?i)<[^>]*>\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*</[^>]+>");

	public List<MedicalObservation> parse(String markdown) {

		List<MedicalObservation> observations = new ArrayList<>();

		if (markdown == null || markdown.isBlank()) {
			return observations;
		}

		String normalizedMarkdown = normalize(markdown);

		String[] lines = normalizedMarkdown.split("\\R");

		for (String line : lines) {

			if (line == null || line.isBlank()) {
				continue;
			}

			Matcher matcher = TABLE_ROW_PATTERN.matcher(line);

			if (!matcher.matches()) {
				continue;
			}

			String testName = cleanText(matcher.group(1));
			String observedText = cleanText(matcher.group(2));
			String referenceText = cleanText(matcher.group(3));

			if (isNonObservation(testName, observedText, referenceText)) {
				continue;
			}

			ParsedValue parsedValue = parseValue(observedText);

			if (parsedValue == null || parsedValue.getValue() == null) {

				observations.add(MedicalObservation.builder().testName(testName).needsReview(true)
						.reviewReason("Unable to parse observed value").build());

				continue;
			}

			ParsedRange parsedRange = parseReferenceRange(referenceText);

			boolean needsReview = false;
			String reviewReason = null;

			if (parsedRange == null) {

				needsReview = true;
				reviewReason = "Unable to confidently parse biological reference interval";
			}

			String interpretation = null;

			if (parsedRange != null) {

				interpretation = determineInterpretation(parsedValue.getValue(), parsedRange);
			}

			MedicalObservation observation = MedicalObservation.builder().testName(testName)
					.value(parsedValue.getValue()).unit(parsedValue.getUnit())
					.referenceLow(parsedRange != null ? parsedRange.getLow() : null)
					.referenceHigh(parsedRange != null ? parsedRange.getHigh() : null)
					.needsReview(needsReview).reviewReason(reviewReason).build();

			observations.add(observation);
		}

		return observations;
	}

	private String normalize(String text) {

		return text.replace("\r\n", "\n").replace("\r", "\n").replace("&nbsp;", " ").replace("<br>", " ")
				.replace("<br/>", " ").replace("<br />", " ");
	}

	private String cleanText(String text) {

		if (text == null) {
			return "";
		}

		return text.replaceAll("(?i)<br\\s*/?>", " ").replaceAll("<[^>]+>", "").replace("**", "").replace("__", "")
				.replace("\\*", "").trim().replaceAll("\\s+", " ");
	}

	private boolean isNonObservation(String testName, String observedText, String referenceText) {

		if (testName == null || testName.isBlank()) {
			return true;
		}

		String normalized = testName.trim().toLowerCase(Locale.ROOT);

		if (testName.trim().matches("[-_=]{5,}")) {
			return true;
		}

		if (testName.matches("[-: ]+")) {
			return true;
		}

		if (normalized.equals("test description") || normalized.equals("observed")
				|| normalized.equals("observed value") || normalized.equals("biological reference interval")) {

			return true;
		}

		if (normalized.contains("clinical chemistry") || normalized.contains("liver function test")) {

			return true;
		}

		if (observedText == null || observedText.isBlank()) {
			return true;
		}

		return false;
	}

	private ParsedValue parseValue(String observedText) {

		if (observedText == null || observedText.isBlank()) {
			return null;
		}

		String cleaned = cleanText(observedText);

		Matcher htmlMatcher = HTML_NUMBER_PATTERN.matcher(cleaned);

		Double value = null;

		if (htmlMatcher.find()) {

			value = Double.valueOf(htmlMatcher.group(1));

		} else {

			Matcher matcher = NUMBER_PATTERN.matcher(cleaned);

			if (!matcher.find()) {
				return null;
			}

			try {

				value = Double.valueOf(matcher.group());

			} catch (NumberFormatException e) {
				return null;
			}
		}

		String unit = extractUnit(cleaned);

		return new ParsedValue(value, unit);
	}

	private String extractUnit(String text) {

		if (text == null) {
			return null;
		}

		String normalized = text.toLowerCase(Locale.ROOT);

		if (normalized.contains("mg/dl")) {
			return "mg/dL";
		}

		if (normalized.contains("g/dl")) {
			return "g/dL";
		}

		if (normalized.contains("u/lt")) {
			return "U/Lt";
		}

		if (normalized.contains("u/l")) {
			return "U/L";
		}

		return null;
	}

	private ParsedRange parseReferenceRange(String referenceText) {

		if (referenceText == null || referenceText.isBlank()) {
			return null;
		}

		String cleaned = cleanText(referenceText);

		Matcher rangeMatcher = RANGE_PATTERN.matcher(cleaned);

		if (rangeMatcher.find()) {

			try {

				Double low = Double.valueOf(rangeMatcher.group(1));

				Double high = Double.valueOf(rangeMatcher.group(2));

				return new ParsedRange(low, high);

			} catch (NumberFormatException e) {

				return null;
			}
		}

		Matcher uptoMatcher = UPTO_PATTERN.matcher(cleaned);

		if (uptoMatcher.find()) {

			try {

				Double high = Double.valueOf(uptoMatcher.group(1));

				return new ParsedRange(null, high);

			} catch (NumberFormatException e) {

				return null;
			}
		}

		return null;
	}

	private String determineInterpretation(Double value, ParsedRange range) {

		if (value == null || range == null) {
			return null;
		}

		Double low = range.getLow();
		Double high = range.getHigh();

		if (low != null && high != null) {

			if (value >= low && value <= high) {
				return "N";
			}

			if (value < low) {
				return "L";
			}

			if (value > high) {
				return "H";
			}
		}

		if (low == null && high != null) {

			if (value <= high) {
				return "N";
			}

			return "H";
		}

		if (low != null && high == null) {

			if (value >= low) {
				return "N";
			}

			return "L";
		}

		return null;
	}

}
