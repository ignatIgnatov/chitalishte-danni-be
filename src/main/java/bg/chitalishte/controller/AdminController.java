package bg.chitalishte.controller;

import bg.chitalishte.service.MunicipalityMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final MunicipalityMetricsService metricsService;

    /**
     * Calculate metrics for all municipalities
     * POST /api/admin/calculate-metrics
     */
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

    /**
     * Health check endpoint
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Chitalishta Admin API");
        return ResponseEntity.ok(response);
    }
}