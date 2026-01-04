package bg.chitalishte.controller;

import bg.chitalishte.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    /**
     * Get country boundary GeoJSON
     */
    @GetMapping(value = "/country", produces = "application/json")
    public ResponseEntity<String> getCountryBoundary() {
        log.info("Fetching country boundary GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("country");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get municipalities GeoJSON
     */
    @GetMapping(value = "/municipalities", produces = "application/json")
    public ResponseEntity<String> getMunicipalities() {
        log.info("Fetching municipalities GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("municipalities");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get municipalities with names GeoJSON
     */
    @GetMapping(value = "/municipalities-names", produces = "application/json")
    public ResponseEntity<String> getMunicipalitiesWithNames() {
        log.info("Fetching municipalities with names GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("municipalities_names");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get provinces GeoJSON
     */
    @GetMapping(value = "/provinces", produces = "application/json")
    public ResponseEntity<String> getProvinces() {
        log.info("Fetching provinces GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("provinces");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get settlements GeoJSON
     */
    @GetMapping(value = "/settlements", produces = "application/json")
    public ResponseEntity<String> getSettlements() {
        log.info("Fetching settlements GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("settlements");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get Sofia traffic subzones GeoJSON
     */
    @GetMapping(value = "/sofia-traffic", produces = "application/json")
    public ResponseEntity<String> getSofiaTrafficZones() {
        log.info("Fetching Sofia traffic zones GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("sofiatraffic_subzones");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get Sofia police stations regions GeoJSON
     */
    @GetMapping(value = "/rpu-sofia", produces = "application/json")
    public ResponseEntity<String> getRpuSofia() {
        log.info("Fetching RPU Sofia GeoJSON");
        String geoJson = geocodingService.getGeoJsonData("rpu_sofia");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    /**
     * Get municipalities CSV data
     */
    @GetMapping(value = "/municipalities-csv", produces = "text/csv")
    public ResponseEntity<String> getMunicipalitiesCsv() {
        log.info("Fetching municipalities CSV");
        String csv = geocodingService.getMunicipalitiesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=municipalities.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    /**
     * Get all available GeoJSON layers info
     */
    @GetMapping("/layers")
    public ResponseEntity<List<Map<String, Object>>> getAvailableLayers() {
        log.info("Fetching available layers info");
        List<Map<String, Object>> layers = geocodingService.getAvailableLayers();
        return ResponseEntity.ok(layers);
    }

    /**
     * Search municipalities by name or code
     */
    @GetMapping("/municipalities/search")
    public ResponseEntity<List<Map<String, String>>> searchMunicipalities(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String ekatte) {
        log.info("Searching municipalities: name={}, code={}, ekatte={}", name, code, ekatte);
        List<Map<String, String>> results = geocodingService.searchMunicipalities(name, code, ekatte);
        return ResponseEntity.ok(results);
    }

    /**
     * Get municipality by NUTS4 code
     */
    @GetMapping("/municipalities/{nuts4}")
    public ResponseEntity<String> getMunicipalityByCode(@PathVariable String nuts4) {
        log.info("Fetching municipality by NUTS4 code: {}", nuts4);
        String geoJson = geocodingService.getMunicipalityByCode(nuts4);
        if (geoJson != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(geoJson);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get province by NUTS3 code
     */
    @GetMapping("/provinces/{nuts3}")
    public ResponseEntity<String> getProvinceByCode(@PathVariable String nuts3) {
        log.info("Fetching province by NUTS3 code: {}", nuts3);
        String geoJson = geocodingService.getProvinceByCode(nuts3);
        if (geoJson != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(geoJson);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get statistics about the geocoding data
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Fetching geocoding statistics");
        Map<String, Object> statistics = geocodingService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Bulgarian Geocoding API",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
