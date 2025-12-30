package bg.chitalishte.service;

import bg.chitalishte.dto.*;
import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.mapper.ChitalishteMapper;
import bg.chitalishte.mapper.MunicipalityMapper;
import bg.chitalishte.mapper.MunicipalityMetricsMapper;
import bg.chitalishte.repository.ChitalishteRepository;
import bg.chitalishte.repository.MunicipalityMetricsRepository;
import bg.chitalishte.repository.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MunicipalityService {

    private final MunicipalityRepository municipalityRepository;
    private final ChitalishteRepository chitalishteRepository;
    private final MunicipalityMapper municipalityMapper;
    private final ChitalishteMapper chitalishteMapper;

    public Page<MunicipalityDTO> getAllMunicipalities(int page, int size, String sortBy, String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        return municipalityRepository.findAll(pageable)
                .map(municipalityMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MunicipalityDTO> getMunicipalityByCode(String code) {
        log.info("Fetching municipality by code: {}", code);

        return municipalityRepository.findByMunicipalityCode(code)
                .map(municipalityMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ChitalishteDTO> getChitalishta(String code) {
        log.info("Fetching chitalishta for municipality: {}", code);

        List<Chitalishte> chitalishta = chitalishteRepository.findByMunicipalityMunicipalityCode(code);

        return chitalishta.stream()
                .map(chitalishteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MunicipalityDTO> searchMunicipalities(String query) {
        return municipalityRepository.searchByName(query).stream()
                .map(municipalityMapper::toDTO)
                .collect(Collectors.toList());
    }
}