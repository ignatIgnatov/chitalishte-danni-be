package bg.chitalishte.controller;

import bg.chitalishte.service.AsyncImportService;
import bg.chitalishte.service.ChitalishteImportService;
import bg.chitalishte.service.MunicipalityMetricsService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MunicipalityMetricsService metricsService;
    private final AsyncImportService asyncImportService;
    private final ChitalishteImportService chitalishteImportService;

    @Hidden
    @PostMapping("/chitalishta/import")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "clearExisting", defaultValue = "false") boolean clearExisting) {

        log.info("📁 Получен файл за импорт: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файлът е празен"));
        }

        try {
            // Стартирай асинхронно
            asyncImportService.importAsync(file, clearExisting);

            // Веднага върни response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "processing");
            response.put("message", "Импортът е стартиран и се изпълнява във фонов режим. Проверете логовете за прогрес.");
            response.put("filename", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Грешка при стартиране на импорт: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Грешка при импорт: " + e.getMessage()));
        }
    }

    @GetMapping("/chitalishta/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Chitalishte Import Service е готов"
        ));
    }

    @Hidden
    @PostMapping("/calculate-metrics")
    public ResponseEntity<Map<String, Object>> calculateMetrics() {
        log.info("🚀 Starting metrics calculation...");

        try {
            long startTime = System.currentTimeMillis();

            metricsService.calculateAllMetrics();

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Всички показатели са изчислени успешно");
            response.put("duration", duration + " секунди");

            log.info("✅ Metrics calculation completed in {} seconds", duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error calculating metrics: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Грешка при изчисляване: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Chitalishta Admin API");
        return ResponseEntity.ok(response);
    }
}