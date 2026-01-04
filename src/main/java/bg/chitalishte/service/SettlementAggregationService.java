package bg.chitalishte.service;

import bg.chitalishte.entity.Municipality;
import bg.chitalishte.entity.Settlement;
import bg.chitalishte.repository.MunicipalityRepository;
import bg.chitalishte.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for aggregating settlement-level data to municipality level
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementAggregationService {

    private final SettlementRepository settlementRepository;
    private final MunicipalityRepository municipalityRepository;

    /**
     * Aggregate population data from all settlements to their municipalities
     * This calculates:
     * - populationUnder15Aggregate (sum of all settlements in municipality)
     * - populationOver65Aggregate (sum of all settlements in municipality)
     */
    @Transactional
    public void aggregateSettlementDataToMunicipalities() {
        log.info("Starting aggregation of settlement data to municipalities");

        List<Municipality> municipalities = municipalityRepository.findAll();
        int processedCount = 0;

        for (Municipality municipality : municipalities) {
            try {
                aggregateForMunicipality(municipality);
                processedCount++;
            } catch (Exception e) {
                log.error("Error aggregating data for municipality: {}",
                        municipality.getMunicipalityCode(), e);
            }
        }

        log.info("Completed aggregation for {} municipalities", processedCount);
    }

    /**
     * Aggregate data for a specific municipality
     */
    @Transactional
    public void aggregateForMunicipality(Municipality municipality) {
        log.debug("Aggregating settlement data for municipality: {}",
                municipality.getMunicipalityCode());

        List<Settlement> settlements = municipality.getSettlements();

        if (settlements == null || settlements.isEmpty()) {
            log.warn("No settlements found for municipality: {}",
                    municipality.getMunicipalityCode());
            return;
        }

        // Aggregate population under 15
        Integer totalUnder15 = settlements.stream()
                .map(Settlement::getPopulationUnder15)
                .filter(pop -> pop != null)
                .reduce(0, Integer::sum);

        // Aggregate population over 65
        Integer totalOver65 = settlements.stream()
                .map(Settlement::getPopulationOver65)
                .filter(pop -> pop != null)
                .reduce(0, Integer::sum);

        municipality.setPopulationUnder15Aggregate(totalUnder15);
        municipality.setPopulationOver65Aggregate(totalOver65);

        municipalityRepository.save(municipality);

        log.debug("Aggregated data for municipality {}: under15={}, over65={}",
                municipality.getMunicipalityCode(), totalUnder15, totalOver65);
    }

    /**
     * Aggregate data for a specific municipality by code
     */
    @Transactional
    public void aggregateForMunicipalityCode(String municipalityCode) {
        log.info("Aggregating settlement data for municipality code: {}", municipalityCode);

        Municipality municipality = municipalityRepository.findByMunicipalityCode(municipalityCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Municipality not found with code: " + municipalityCode));

        aggregateForMunicipality(municipality);
    }
}