package it.pagopa.selfcare.onboarding.web.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

class FileValidationUtilsTest {

    private MultipartFile mockFile(String filename, String contentType, boolean empty) {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getContentType()).thenReturn(contentType);
        when(file.isEmpty()).thenReturn(empty);
        return file;
    }

    @Test
    void validateAggregatesFileValidCsv() {
        MultipartFile file = mockFile("test.csv", "text/csv", false);
        assertDoesNotThrow(() -> FileValidationUtils.validateAggregatesFile(file));
    }

    @Test
    void validateAggregatesFileError() {
        MultipartFile file = mockFile("test.txt", "text/plain", false);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> FileValidationUtils.validateAggregatesFile(file));
        assertTrue(ex.getMessage().contains("Formato file non supportato"));
    }

    @Test
    void validatePdfFileSuccess() {
        MultipartFile file = mockFile("doc.pdf", "application/pdf", false);
        assertDoesNotThrow(() -> FileValidationUtils.validatePdfFile(file));
    }

    @Test
    void validatePdfFileError() {
        MultipartFile file = mockFile("doc.csv", "text/csv", false);
        assertThrows(IllegalArgumentException.class, () -> FileValidationUtils.validatePdfFile(file));
    }

    @Test
    void validateP7mFileSuccess() {
        MultipartFile file = mockFile("signed.p7m", "application/pkcs7-mime", false);
        assertDoesNotThrow(() -> FileValidationUtils.validateP7mFile(file));
    }

    @Test
    void validateP7mFileError() {
        MultipartFile file = mockFile("file.pdf", "application/pdf", false);
        assertThrows(IllegalArgumentException.class, () -> FileValidationUtils.validateP7mFile(file));
    }

    @Test
    void validatePdfOrP7mSuccess() {
        MultipartFile file = mockFile("contract.pdf", "application/pdf", false);
        assertDoesNotThrow(() -> FileValidationUtils.validatePdfOrP7m(file));
    }

    @Test
    void validateP7mSuccess() {
        MultipartFile file = mockFile("signed.p7m", "application/x-pkcs7-mime", false);
        assertDoesNotThrow(() -> FileValidationUtils.validatePdfOrP7m(file));
    }

    @Test
    void validatePdfOrP7Error() {
        MultipartFile file = mockFile("file.txt", "text/plain", false);
        assertThrows(IllegalArgumentException.class, () -> FileValidationUtils.validatePdfOrP7m(file));
    }

    @Test
    void validateFileNullFile() {
        assertThrows(IllegalArgumentException.class,
                () -> FileValidationUtils.validateFile(null, List.of(".pdf"), List.of("application/pdf")));
    }

    @Test
    void validateFileEmptyFile() {
        MultipartFile file = mockFile("empty.pdf", "application/pdf", true);
        assertThrows(IllegalArgumentException.class,
                () -> FileValidationUtils.validateFile(file, List.of(".pdf"), List.of("application/pdf")));
    }

    @Test
    void validateFileSuccessExtension() {
        MultipartFile file = mockFile("data.xlsx", "unknown/type", false);
        assertDoesNotThrow(() ->
                FileValidationUtils.validateFile(file, List.of(".xls", ".xlsx"), List.of("text/csv")));
    }

    @Test
    void validateFileSuccessMime() {
        MultipartFile file = mockFile("data.unknown", "text/csv", false);
        assertDoesNotThrow(() ->
                FileValidationUtils.validateFile(file, List.of(".xls"), List.of("text/csv")));
    }

    @Test
    void validateFileErrorMime() {
        MultipartFile file = mockFile("data.txt", "text/plain", false);
        assertThrows(IllegalArgumentException.class, () ->
                FileValidationUtils.validateFile(file, List.of(".pdf"), List.of("application/pdf")));
    }
}
