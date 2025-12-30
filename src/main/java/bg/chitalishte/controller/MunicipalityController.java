package bg.chitalishte.controller;

import bg.chitalishte.dto.*;
import bg.chitalishte.service.MunicipalityMetricsService;
import bg.chitalishte.service.MunicipalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MunicipalityController {

    private final MunicipalityService municipalityService;
    private final MunicipalityMetricsService municipalityMetricsService;

    /**
     * Get all municipalities (paginated)
     * GET /api/municipalities?page=0&size=20&sort=municipality&dir=asc
     */
    @GetMapping
    public ResponseEntity<Page<MunicipalityDTO>> getAllMunicipalities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "municipality") String sort,
            @RequestParam(defaultValue = "asc") String dir) {
        log.info("Fetching municipalities: page={}, size={}, sort={}, dir={}", page, size, sort, dir);
        return ResponseEntity.ok(municipalityService.getAllMunicipalities(page, size, sort, dir));
    }

    /**
     * Search municipalities by query
     * GET /api/municipalities/search?q=благоевград
     */
    @GetMapping("/search")
    public ResponseEntity<List<MunicipalityDTO>> searchMunicipalities(@RequestParam String q) {
        log.info("Searching municipalities: query={}", q);
        return ResponseEntity.ok(municipalityService.searchMunicipalities(q));
    }

    /**
     * Get municipality by municipality_code (nuts4)
     * GET /api/municipalities/BLG52
     */
    @GetMapping("/{code}")
    public ResponseEntity<MunicipalityDTO> getMunicipalityByCode(@PathVariable String code) {
        log.info("Fetching municipality by code: {}", code);
        return municipalityService.getMunicipalityByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Municipality not found: {}", code);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get chitalishta for municipality
     * GET /api/municipalities/BLG52/chitalishta
     */
    @GetMapping("/{code}/chitalishta")
    public ResponseEntity<List<ChitalishteDTO>> getChitalishta(@PathVariable String code) {
        log.info("Fetching chitalishta for municipality: {}", code);
        List<ChitalishteDTO> chitalishta = municipalityService.getChitalishta(code);
        return ResponseEntity.ok(chitalishta);
    }

    /**
     * Get metrics for municipality
     * GET /api/municipalities/BLG52/metrics
     */
    @GetMapping("/{code}/metrics")
    public ResponseEntity<MunicipalityMetricsDTO> getMetrics(@PathVariable String code) {
        log.info("Fetching metrics for municipality: {}", code);
        return municipalityMetricsService.getMetrics(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Metrics not found for municipality: {}", code);
                    return ResponseEntity.notFound().build();
                });
    }
}