package com.medicalReportOcr.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.springframework.stereotype.Service;

import com.medicalReportOcr.entity.MedicalObservation;

@Service
public class ObservationParser {

	    private static final Pattern TABLE_ROW_PATTERN =
	            Pattern.compile(
	                    "^\\|\\s*(.*?)\\s*\\|\\s*(.*?)\\s*\\|\\s*(.*?)\\s*\\|\\s*(.*?)\\s*\\|\\s*$"
	            );
	    private static final Pattern NUMBER_PATTERN =
	            Pattern.compile(
	                    "[-+]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)"
	            );

	    private static final Pattern RANGE_PATTERN =
	            Pattern.compile(
	                    "([-+]?(?:\\d+(?:\\.\\d+)?|\\.\\d+))"
	                            + "\\s*(?:-|–|—|to)\\s*"
	                            + "([-+]?(?:\\d+(?:\\.\\d+)?|\\.\\d+))",
	                    Pattern.CASE_INSENSITIVE
	            );

	    private static final Pattern MARKDOWN_SEPARATOR_PATTERN =
	            Pattern.compile(
	                    "^\\|?\\s*:?-+:?\\s*"
	                            + "(?:\\|\\s*:?-+:?\\s*)+\\|?$"
	            );


	    public List<MedicalObservation> parse(String markdown) {

	        List<MedicalObservation> observations =
	                new ArrayList<>();

	        if (markdown == null || markdown.isBlank()) {
	            return observations;
	        }

	        String[] lines = markdown.split("\\R");

	        for (String line : lines) {
	            if (line == null || line.isBlank()) {
	                continue;
	            }
	            if (isMarkdownSeparator(line)) {
	                continue;
	            }
	            if (!looksLikeTableRow(line)) {
	                continue;
	            }

	            MedicalObservation observation =
	                    parseTableRow(line);

	            if (observation != null) {
	                observations.add(observation);
	            }
	        }

	        return observations;
	    }
	    private MedicalObservation parseTableRow(String line) {

	        Matcher matcher =
	                TABLE_ROW_PATTERN.matcher(line.trim());

	        if (!matcher.matches()) {
	            return null;
	        }

	        String testName =
	                clean(matcher.group(1));

	        String rawValue =
	                clean(matcher.group(2));

	        String unit =
	                clean(matcher.group(3));

	        String referenceRange =
	                clean(matcher.group(4));
	        if (isHeaderRow(
	                testName,
	                rawValue,
	                unit,
	                referenceRange)) {

	            return null;
	        }

	        if (testName.isBlank()) {
	            return null;
	        }

	        ParsedValue parsedValue =
	                parseNumericValue(rawValue);

	        ParsedRange parsedRange =
	                parseReferenceRange(referenceRange);

	        boolean needsReview =
	                parsedValue.value() == null
	                        || unit.isBlank()
	                        || parsedRange == null;
	        String reviewReason =
	                buildReviewReason(
	                        parsedValue,
	                        unit,
	                        parsedRange
	                );
	        String interpretation =
	                determineInterpretation(
	                        parsedValue.value(),
	                        parsedRange
	                );

	        return MedicalObservation.builder()
	                .testName(testName)
	                .rawValue(rawValue)
	                .value(parsedValue.value())
	                .unit(
	                        unit.isBlank()
	                                ? null
	                                : unit
	                )
	                .referenceLow(
	                        parsedRange == null
	                                ? null
	                                : parsedRange.low()
	                )
	                .referenceHigh(
	                        parsedRange == null
	                                ? null
	                                : parsedRange.high()
	                )
	                .interpretation(interpretation)
	                .needsReview(needsReview)
	                .reviewReason(reviewReason)
	                .build();
	    }

	    private ParsedValue parseNumericValue(
	            String rawValue) {

	        if (rawValue == null
	                || rawValue.isBlank()) {

	            return new ParsedValue(
	                    null,
	                    "Value is missing"
	            );
	        }

	        String normalized =
	                rawValue.trim();
	        if (normalized.startsWith("<")
	                || normalized.startsWith(">")) {

	            return new ParsedValue(
	                    null,
	                    "Non-exact numeric value: "
	                            + rawValue
	            );
	        }

	        Matcher matcher =
	                NUMBER_PATTERN.matcher(normalized);

	        if (!matcher.find()) {

	            return new ParsedValue(
	                    null,
	                    "Value is not numeric: "
	                            + rawValue
	            );
	        }

	        String numericPart =
	                matcher.group();

	        try {

	            Double value =
	                    Double.parseDouble(numericPart);

	            if (!Double.isFinite(value)) {

	                return new ParsedValue(
	                        null,
	                        "Value is not finite"
	                );
	            }

	            return new ParsedValue(
	                    value,
	                    null
	            );

	        } catch (NumberFormatException exception) {

	            return new ParsedValue(
	                    null,
	                    "Invalid numeric value"
	            );
	        }
	    }

	    private ParsedRange parseReferenceRange(
	            String rawRange) {

	        if (rawRange == null
	                || rawRange.isBlank()) {

	            return null;
	        }

	        Matcher matcher =
	                RANGE_PATTERN.matcher(rawRange);

	        if (!matcher.find()) {
	            return null;
	        }

	        try {

	            Double low =
	                    Double.parseDouble(
	                            matcher.group(1)
	                    );

	            Double high =
	                    Double.parseDouble(
	                            matcher.group(2)
	                    );

	            if (!Double.isFinite(low)
	                    || !Double.isFinite(high)) {

	                return null;
	            }

	            if (low > high) {
	                return null;
	            }

	            return new ParsedRange(
	                    low,
	                    high
	            );

	        } catch (NumberFormatException exception) {

	            return null;
	        }
	    }

	    private String determineInterpretation(
	            Double value,
	            ParsedRange range) {

	        if (value == null
	                || range == null) {

	            return null;
	        }

	        if (value < range.low()) {
	            return "L";
	        }

	        if (value > range.high()) {
	            return "H";
	        }

	        return "N";
	    }

	    private String buildReviewReason(
	            ParsedValue value,
	            String unit,
	            ParsedRange range) {

	        List<String> reasons =
	                new ArrayList<>();

	        if (value.value() == null) {

	            if (value.error() != null
	                    && !value.error().isBlank()) {

	                reasons.add(value.error());
	            }
	        }

	        if (unit == null
	                || unit.isBlank()) {

	            reasons.add("Unit is missing");
	        }

	        if (range == null) {

	            reasons.add(
	                    "Reference range is missing or invalid"
	            );
	        }

	        if (reasons.isEmpty()) {
	            return null;
	        }

	        return String.join(
	                "; ",
	                reasons
	        );
	    }

	    private boolean isHeaderRow(
	            String testName,
	            String rawValue,
	            String unit,
	            String referenceRange) {

	        return "Test".equalsIgnoreCase(testName)
	                || (
	                    "Result".equalsIgnoreCase(rawValue)
	                    && "Unit".equalsIgnoreCase(unit)
	                );
	    }

	    private boolean isMarkdownSeparator(
	            String line) {

	        if (line == null
	                || line.isBlank()) {

	            return true;
	        }

	        String value =
	                line.trim();

	        if (MARKDOWN_SEPARATOR_PATTERN
	                .matcher(value)
	                .matches()) {

	            return true;
	        }

	        return value.matches("^-+$");
	    }

	    private boolean looksLikeTableRow(
	            String line) {

	        if (line == null
	                || line.isBlank()) {

	            return false;
	        }

	        String value =
	                line.trim();

	        if (!value.startsWith("|")
	                || !value.endsWith("|")) {

	            return false;
	        }

	        return value.split("\\|", -1).length >= 5;
	    }

	    private String clean(String value) {

	        if (value == null) {
	            return "";
	        }

	        return value
	                .replace("&nbsp;", " ")
	                .replace('\u00A0', ' ')
	                .trim();
	    }

	    private record ParsedValue(
	            Double value,
	            String error) {
	    }


	    private record ParsedRange(
	            Double low,
	            Double high) {
	    
	}
}



