package bg.chitalishte.controller;

import bg.chitalishte.dto.ChitalishteDTO;
import bg.chitalishte.dto.MunicipalityDTO;
import bg.chitalishte.dto.MunicipalityMetricsDTO;
import bg.chitalishte.service.MunicipalityMetricsService;
import bg.chitalishte.service.MunicipalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for municipality operations
 */
@Slf4j
@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
public class MunicipalityController {

    private final MunicipalityService municipalityService;
    private final MunicipalityMetricsService municipalityMetricsService;

    /**
     * GET /api/municipalities
     * Get all municipalities with pagination
     * Example: GET /api/municipalities?page=0&size=20&sort=municipality&dir=asc
     */
    @GetMapping
    public ResponseEntity<Page<MunicipalityDTO>> getAllMunicipalities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "265") int size,
            @RequestParam(defaultValue = "municipality") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        log.info("GET /api/municipalities - page: {}, size: {}, sort: {}, dir: {}",
                page, size, sort, dir);

        Page<MunicipalityDTO> municipalities = municipalityService.getAllMunicipalities(
                page, size, sort, dir);

        return ResponseEntity.ok(municipalities);
    }

    /**
     * GET /api/municipalities/search
     * Search municipalities by query
     * Example: GET /api/municipalities/search?q=благоевград
     */
    @GetMapping("/search")
    public ResponseEntity<List<MunicipalityDTO>> searchMunicipalities(@RequestParam String q) {
        log.info("GET /api/municipalities/search?q={}", q);

        List<MunicipalityDTO> municipalities = municipalityService.searchMunicipalities(q);

        return ResponseEntity.ok(municipalities);
    }

    /**
     * GET /api/municipalities/{code}
     * Get municipality by municipality_code
     * Example: GET /api/municipalities/BLG52
     */
    @GetMapping("/{code}")
    public ResponseEntity<MunicipalityDTO> getMunicipalityByCode(@PathVariable String code) {
        log.info("GET /api/municipalities/{}", code);

        return municipalityService.getMunicipalityByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Municipality not found: {}", code);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * GET /api/municipalities/{code}/chitalishta
     * Get all chitalishta for a municipality
     * Example: GET /api/municipalities/BLG52/chitalishta
     */
    @GetMapping("/{code}/chitalishta")
    public ResponseEntity<List<ChitalishteDTO>> getChitalishta(@PathVariable String code) {
        log.info("GET /api/municipalities/{}/chitalishta", code);

        List<ChitalishteDTO> chitalishta = municipalityService.getChitalishta(code);

        return ResponseEntity.ok(chitalishta);
    }

    /**
     * GET /api/municipalities/{code}/metrics
     * Get calculated metrics for a municipality
     * Example: GET /api/municipalities/BLG52/metrics
     */
    @GetMapping("/{code}/metrics")
    public ResponseEntity<MunicipalityMetricsDTO> getMetrics(@PathVariable String code) {
        log.info("GET /api/municipalities/{}/metrics", code);

        return municipalityMetricsService.getMetrics(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Metrics not found for municipality: {}", code);
                    return ResponseEntity.notFound().build();
                });
    }
}