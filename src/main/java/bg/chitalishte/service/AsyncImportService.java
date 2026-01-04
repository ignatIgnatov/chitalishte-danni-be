package bg.chitalishte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for asynchronous data import operations
 * Handles background import tasks to avoid blocking HTTP requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncImportService {

    private final ChitalishteImportService importService;

    /**
     * Import data from Excel file asynchronously
     *
     * @param file Excel file to import
     * @param clearExisting whether to clear existing data before import (not implemented yet)
     * @return CompletableFuture with import statistics
     */
    @Async
    public CompletableFuture<Map<String, Integer>> importAsync(MultipartFile file, boolean clearExisting) {
        try {
            log.info("Starting asynchronous import from file: {}", file.getOriginalFilename());

            // Copy file temporarily (MultipartFile is not thread-safe)
            File tempFile = File.createTempFile("import-", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            log.info("Temporary file created: {}", tempFile.getAbsolutePath());

            // Create CustomMultipartFile from temporary file
            CustomMultipartFile customFile = new CustomMultipartFile(tempFile, file.getOriginalFilename());

            // Perform import
            Map<String, Integer> result = importService.importFromExcel(customFile.getInputStream());

            // Delete temporary file
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }

            log.info("Asynchronous import completed successfully. Stats: {}", result);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Error during asynchronous import: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}