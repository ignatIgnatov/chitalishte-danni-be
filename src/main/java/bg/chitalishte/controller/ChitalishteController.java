package bg.chitalishte.controller;

import bg.chitalishte.dto.ChitalishteDTO;
import bg.chitalishte.service.ChitalishteService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for chitalishte operations
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/api/chitalishta")
@RequiredArgsConstructor
public class ChitalishteController {

    private final ChitalishteService chitalishteService;

    /**
     * GET /api/chitalishta
     * Get all chitalishta with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ChitalishteDTO>> getAllChitalishta(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("GET /api/chitalishta - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Page<ChitalishteDTO> chitalishta = chitalishteService.getAllChitalishta(
                page, size, sortBy, direction);

        return ResponseEntity.ok(chitalishta);
    }

    /**
     * GET /api/chitalishta/{id}
     * Get chitalishte by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChitalishteDTO> getChitalishteById(@PathVariable UUID id) {
        log.info("GET /api/chitalishta/{}", id);

        ChitalishteDTO chitalishte = chitalishteService.getChitalishteById(id);

        return ResponseEntity.ok(chitalishte);
    }

    /**
     * GET /api/chitalishta/regn/{regN}
     * Get chitalishte by registration number
     */
    @GetMapping("/regn/{regN}")
    public ResponseEntity<ChitalishteDTO> getChitalishteByRegN(@PathVariable String regN) {
        log.info("GET /api/chitalishta/regn/{}", regN);

        ChitalishteDTO chitalishte = chitalishteService.getChitalishteByRegN(regN);

        return ResponseEntity.ok(chitalishte);
    }

    /**
     * GET /api/chitalishta/search
     * Search chitalishta by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<ChitalishteDTO>> searchChitalishta(
            @RequestParam String query) {

        log.info("GET /api/chitalishta/search?query={}", query);

        List<ChitalishteDTO> chitalishta = chitalishteService.searchChitalishta(query);

        return ResponseEntity.ok(chitalishta);
    }

    /**
     * GET /api/chitalishta/municipality/{code}
     * Get chitalishta by municipality code
     */
    @GetMapping("/municipality/{code}")
    public ResponseEntity<List<ChitalishteDTO>> getChitalishtaByMunicipality(
            @PathVariable String code) {

        log.info("GET /api/chitalishta/municipality/{}", code);

        List<ChitalishteDTO> chitalishta = chitalishteService.getChitalishtaByMunicipality(code);

        return ResponseEntity.ok(chitalishta);
    }

    /**
     * GET /api/chitalishta/municipality/{code}/count
     * Count chitalishta in municipality
     */
    @GetMapping("/municipality/{code}/count")
    public ResponseEntity<Long> countChitalishta(@PathVariable String code) {
        log.info("GET /api/chitalishta/municipality/{}/count", code);

        Long count = chitalishteService.countChitalishtaInMunicipality(code);

        return ResponseEntity.ok(count);
    }

    /**
     * GET /api/chitalishta/municipality/{code}/count/village
     * Count village chitalishta in municipality
     */
    @GetMapping("/municipality/{code}/count/village")
    public ResponseEntity<Long> countVillageChitalishta(@PathVariable String code) {
        log.info("GET /api/chitalishta/municipality/{}/count/village", code);

        Long count = chitalishteService.countVillageChitalishta(code);

        return ResponseEntity.ok(count);
    }

    /**
     * GET /api/chitalishta/municipality/{code}/count/city
     * Count city chitalishta in municipality
     */
    @GetMapping("/municipality/{code}/count/city")
    public ResponseEntity<Long> countCityChitalishta(@PathVariable String code) {
        log.info("GET /api/chitalishta/municipality/{}/count/city", code);

        Long count = chitalishteService.countCityChitalishta(code);

        return ResponseEntity.ok(count);
    }
}