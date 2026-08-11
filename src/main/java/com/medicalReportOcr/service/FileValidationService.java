package com.medicalReportOcr.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.medicalReportOcr.exception.FileValidationException;

@Service
public class FileValidationService {
	
	private final FileSignatureValidator fileSignatureValidator;

	private static final Map<String, String> ALLOWED_FILES = Map.of(".pdf", "application/pdf", ".jpg", "image/jpeg",
			".jpeg", "image/jpeg", ".png", "image/png", ".webp", "image/webp");

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.copyOf(ALLOWED_FILES.values());

	private final long maxFileSize;

	public FileValidationService(
	        @Value("${spring.servlet.multipart.max-file-size}")
	        String maxFileSize,
	        FileSignatureValidator fileSignatureValidator) {

	    this.maxFileSize = parseFileSize(maxFileSize);
	    this.fileSignatureValidator = fileSignatureValidator;
	}

	public void validate(MultipartFile file) {

		validateFileExists(file);
		validateFileSize(file);
		String originalFilename = file.getOriginalFilename();
		validateFilename(originalFilename);

		String extension = extractExtension(originalFilename);

		String contentType = normalizeContentType(file.getContentType());

		validateExtension(extension);

		validateContentType(contentType);
		validateExtensionAndContentType(extension, contentType);
	}

	private void validateFileExists(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new FileValidationException("File is required and cannot be empty");
		}
	}

	private void validateFileSize(MultipartFile file) {

		if (file.getSize() > maxFileSize) {
			throw new FileValidationException(
					"File size exceeds the maximum allowed size of " + formatFileSize(maxFileSize));
		}
	}

	private void validateFilename(String filename) {

		if (filename == null || filename.isBlank()) {
			throw new FileValidationException("File name is required");
		}

		if (filename.contains("..")) {
			throw new FileValidationException("Invalid file name");
		}
	}

	private String extractExtension(String filename) {

		int lastDot = filename.lastIndexOf('.');

		if (lastDot < 0 || lastDot == filename.length() - 1) {
			throw new FileValidationException("File extension is required");
		}

		return filename.substring(lastDot).toLowerCase();
	}

	private void validateExtension(String extension) {

		if (!ALLOWED_FILES.containsKey(extension)) {
			throw new FileValidationException(
					"Unsupported file extension. " + "Supported formats: PDF, JPEG, PNG, WebP");
		}
	}

	private void validateContentType(String contentType) {

		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {

			throw new FileValidationException("Unsupported file type. " + "Supported formats: PDF, JPEG, PNG, WebP");
		}
	}

	private void validateExtensionAndContentType(String extension, String contentType) {

		String expectedContentType = ALLOWED_FILES.get(extension);

		if (!expectedContentType.equals(contentType)) {

			throw new FileValidationException("File extension does not match content type");
		}
	}

	private String normalizeContentType(String contentType) {

		if (contentType == null) {
			return null;
		}

		return contentType.trim().toLowerCase();
	}

	private long parseFileSize(String size) {

		String value = size.trim().toUpperCase();

		if (value.endsWith("MB")) {

			long mb = Long.parseLong(value.substring(0, value.length() - 2).trim());

			return mb * 1024 * 1024;
		}

		if (value.endsWith("KB")) {
			long kb = Long.parseLong(value.substring(0, value.length() - 2).trim());
			return kb * 1024;
		}

		return Long.parseLong(value);
	}

	private String formatFileSize(long bytes) {

		long megabytes = bytes / (1024 * 1024);
		return megabytes + "MB";
	}

}
