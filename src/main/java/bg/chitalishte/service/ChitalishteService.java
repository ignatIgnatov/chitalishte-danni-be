package bg.chitalishte.service;

import bg.chitalishte.dto.ChitalishteDTO;
import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.exception.ResourceNotFoundException;
import bg.chitalishte.mapper.ChitalishteMapper;
import bg.chitalishte.repository.ChitalishteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for chitalishte operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChitalishteService {

    private final ChitalishteRepository chitalishteRepository;
    private final ChitalishteMapper chitalishteMapper;

    /**
     * Get all chitalishta with pagination
     */
    public Page<ChitalishteDTO> getAllChitalishta(int page, int size, String sortBy, String direction) {
        log.info("Fetching chitalishta - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        Page<ChitalishteDTO> result = chitalishteRepository.findAll(pageable)
                .map(chitalishteMapper::toDTO);

        log.info("Found {} chitalishta", result.getTotalElements());
        return result;
    }

    /**
     * Get chitalishte by ID
     */
    public ChitalishteDTO getChitalishteById(UUID id) {
        log.info("Fetching chitalishte by id: {}", id);

        Chitalishte chitalishte = chitalishteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Chitalishte not found with id: {}", id);
                    return new ResourceNotFoundException("Chitalishte", id.toString());
                });

        log.info("Chitalishte found: {}", chitalishte.getName());
        return chitalishteMapper.toDTO(chitalishte);
    }

    /**
     * Get chitalishte by registration number
     */
    public ChitalishteDTO getChitalishteByRegN(String regN) {
        log.info("Fetching chitalishte by reg_n: {}", regN);

        Chitalishte chitalishte = chitalishteRepository.findByRegN(regN)
                .orElseThrow(() -> {
                    log.warn("Chitalishte not found with reg_n: {}", regN);
                    return new ResourceNotFoundException("Chitalishte", regN);
                });

        log.info("Chitalishte found: {}", chitalishte.getName());
        return chitalishteMapper.toDTO(chitalishte);
    }

    /**
     * Search chitalishta by name
     */
    public List<ChitalishteDTO> searchChitalishta(String query) {
        log.info("Searching chitalishta with query: {}", query);

        List<ChitalishteDTO> result = chitalishteRepository.searchByName(query).stream()
                .map(chitalishteMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Found {} chitalishta matching query: {}", result.size(), query);
        return result;
    }

    /**
     * Get chitalishta by municipality code
     */
    public List<ChitalishteDTO> getChitalishtaByMunicipality(String municipalityCode) {
        log.info("Fetching chitalishta for municipality: {}", municipalityCode);

        List<ChitalishteDTO> result = chitalishteRepository
                .findByMunicipalityMunicipalityCode(municipalityCode).stream()
                .map(chitalishteMapper::toDTO)
                .collect(Collectors.toList());

        log.info("Found {} chitalishta for municipality: {}", result.size(), municipalityCode);
        return result;
    }

    /**
     * Count chitalishta in municipality
     */
    public Long countChitalishtaInMunicipality(String municipalityCode) {
        log.info("Counting chitalishta in municipality: {}", municipalityCode);

        Long count = chitalishteRepository.countByMunicipalityCode(municipalityCode);

        log.info("Municipality {} has {} chitalishta", municipalityCode, count);
        return count;
    }

    /**
     * Count village chitalishta in municipality
     */
    public Long countVillageChitalishta(String municipalityCode) {
        log.info("Counting village chitalishta in municipality: {}", municipalityCode);

        Long count = chitalishteRepository.countVillageChitalishta(municipalityCode);

        log.info("Municipality {} has {} village chitalishta", municipalityCode, count);
        return count;
    }

    /**
     * Count city chitalishta in municipality
     */
    public Long countCityChitalishta(String municipalityCode) {
        log.info("Counting city chitalishta in municipality: {}", municipalityCode);

        Long count = chitalishteRepository.countCityChitalishta(municipalityCode);

        log.info("Municipality {} has {} city chitalishta", municipalityCode, count);
        return count;
    }
}