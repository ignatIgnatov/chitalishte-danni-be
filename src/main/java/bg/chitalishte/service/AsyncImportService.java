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
     */
    @Async
    public CompletableFuture<Map<String, Integer>> importAsync(MultipartFile file, boolean clearExisting) {
        try {
            log.info("Starting asynchronous import from file: {}", file.getOriginalFilename());

            // Директно използвай InputStream без temp file
            Map<String, Integer> result = importService.importFromExcel(file.getInputStream());

            log.info("Asynchronous import completed successfully. Stats: {}", result);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Error during asynchronous import: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}