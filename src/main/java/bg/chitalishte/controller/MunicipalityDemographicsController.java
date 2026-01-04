package bg.chitalishte.controller;

import bg.chitalishte.service.MunicipalityDemographicAggregateService;
import bg.chitalishte.service.MunicipalityMetricsService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing municipality demographic aggregates and recalculating metrics
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/api/admin/demographics")
@RequiredArgsConstructor
public class MunicipalityDemographicsController {

    private final MunicipalityDemographicAggregateService demographicService;
    private final MunicipalityMetricsService metricsService;

    /**
     * Calculate missing demographic aggregates for all municipalities
     * POST /api/admin/demographics/calculate-aggregates
     */
    @PostMapping("/calculate-aggregates")
    public ResponseEntity<Map<String, String>> calculateAggregates() {
        log.info("Received request to calculate missing demographic aggregates");

        try {
            demographicService.calculateMissingDemographicAggregates();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Demographic aggregates calculated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating demographic aggregates", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Calculate demographic aggregates for a specific municipality
     * POST /api/admin/demographics/calculate-aggregates/{municipalityCode}
     */
    @PostMapping("/calculate-aggregates/{municipalityCode}")
    public ResponseEntity<Map<String, String>> calculateAggregatesForMunicipality(
            @PathVariable String municipalityCode) {
        log.info("Received request to calculate demographic aggregates for: {}", municipalityCode);

        try {
            demographicService.calculateForMunicipality(municipalityCode);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Demographic aggregates calculated for " + municipalityCode);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating demographic aggregates for {}", municipalityCode, e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Verify demographic aggregates match settlements data
     * GET /api/admin/demographics/verify-aggregates
     */
    @GetMapping("/verify-aggregates")
    public ResponseEntity<Map<String, String>> verifyAggregates() {
        log.info("Received request to verify demographic aggregates");

        try {
            demographicService.verifyDemographicAggregates();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Demographic aggregates verified (check logs for details)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying demographic aggregates", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Recalculate metrics for all municipalities
     * POST /api/admin/demographics/recalculate-metrics
     */
    @PostMapping("/recalculate-metrics")
    public ResponseEntity<Map<String, String>> recalculateMetrics() {
        log.info("Received request to recalculate all municipality metrics");

        try {
            metricsService.calculateAllMetrics();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Metrics recalculated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recalculating metrics", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Full pipeline: calculate aggregates + recalculate metrics
     * POST /api/admin/demographics/full-recalculation
     */
    @PostMapping("/full-recalculation")
    public ResponseEntity<Map<String, String>> fullRecalculation() {
        log.info("Received request for full demographic and metrics recalculation");

        try {
            // Step 1: Calculate missing demographic aggregates
            log.info("Step 1: Calculating demographic aggregates");
            demographicService.calculateMissingDemographicAggregates();

            // Step 2: Recalculate all metrics
            log.info("Step 2: Recalculating metrics");
            metricsService.calculateAllMetrics();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Full recalculation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during full recalculation", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}