package com.medicalReportOcr.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.medicalReportOcr.exception.FileValidationException;

@Component
public class FileSignatureValidator {

	private static final byte[] PDF_SIGNATURE = { 0x25, 0x50, 0x44, 0x46 };

	private static final byte[] PNG_SIGNATURE = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

	private static final byte[] JPEG_SIGNATURE = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };

	public void validate(MultipartFile file, String extension) {

		try (InputStream inputStream = file.getInputStream()) {

			byte[] header = inputStream.readNBytes(12);

			if (header.length < 4) {
				throw new FileValidationException("Unable to determine file type");
			}

			switch (extension) {

			case ".pdf" -> validateSignature(header, PDF_SIGNATURE, "PDF");

			case ".png" -> validateSignature(header, PNG_SIGNATURE, "PNG");

			case ".jpg", ".jpeg" -> validateSignature(header, JPEG_SIGNATURE, "JPEG");

			case ".webp" -> validateWebp(header);

			default -> throw new FileValidationException("Unsupported file type");
			}

		} catch (IOException exception) {

			throw new FileValidationException("Unable to read uploaded file");
		}
	}

	private void validateSignature(byte[] actual, byte[] expected, String type) {

		if (!startsWith(actual, expected)) {
			throw new FileValidationException("File content does not match " + type + " format");
		}
	}

	private void validateWebp(byte[] header) {

		if (header.length < 12 || !Arrays.equals(Arrays.copyOfRange(header, 0, 4), new byte[] { 'R', 'I', 'F', 'F' })
				|| !Arrays.equals(Arrays.copyOfRange(header, 8, 12), new byte[] { 'W', 'E', 'B', 'P' })) {

			throw new FileValidationException("File content does not match WebP format");
		}
	}

	private boolean startsWith(byte[] actual, byte[] expected) {

		if (actual.length < expected.length) {
			return false;
		}
		return Arrays.equals(Arrays.copyOf(actual, expected.length), expected);
	}
}
