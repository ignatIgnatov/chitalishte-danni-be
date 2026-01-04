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
 * Service for calculating missing demographic aggregates in municipalities
 * from their settlements data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MunicipalityDemographicAggregateService {

    private final MunicipalityRepository municipalityRepository;
    private final SettlementRepository settlementRepository;

    /**
     * Calculate and populate missing demographic aggregates for all municipalities
     * by summing data from their settlements
     */
    @Transactional
    public void calculateMissingDemographicAggregates() {
        log.info("Starting calculation of missing demographic aggregates");

        List<Municipality> municipalities = municipalityRepository.findAll();
        int updatedCount = 0;

        for (Municipality municipality : municipalities) {
            boolean updated = false;

            // Check if population_under_15_aggregate is missing
            if (municipality.getPopulationUnder15Aggregate() == null) {
                Integer under15Sum = calculatePopulationUnder15(municipality);
                municipality.setPopulationUnder15Aggregate(under15Sum);
                updated = true;
                log.info("Calculated population_under_15_aggregate for {}: {}",
                        municipality.getMunicipalityCode(), under15Sum);
            }

            // Check if population_over_65_aggregate is missing
            if (municipality.getPopulationOver65Aggregate() == null) {
                Integer over65Sum = calculatePopulationOver65(municipality);
                municipality.setPopulationOver65Aggregate(over65Sum);
                updated = true;
                log.info("Calculated population_over_65_aggregate for {}: {}",
                        municipality.getMunicipalityCode(), over65Sum);
            }

            if (updated) {
                municipalityRepository.save(municipality);
                updatedCount++;
            }
        }

        log.info("Demographic aggregates calculation completed. Updated {} municipalities", updatedCount);
    }

    /**
     * FORCE recalculate ALL demographic aggregates for all municipalities
     * (even if they already have values)
     * Use this after data import to ensure everything is up-to-date
     */
    @Transactional
    public void recalculateAllDemographicAggregates() {
        log.info("Starting FORCED recalculation of ALL demographic aggregates");

        List<Municipality> municipalities = municipalityRepository.findAll();

        for (Municipality municipality : municipalities) {
            Integer under15Sum = calculatePopulationUnder15(municipality);
            Integer over65Sum = calculatePopulationOver65(municipality);

            municipality.setPopulationUnder15Aggregate(under15Sum);
            municipality.setPopulationOver65Aggregate(over65Sum);

            municipalityRepository.save(municipality);

            log.debug("Recalculated demographics for {}: under15={}, over65={}",
                    municipality.getMunicipalityCode(), under15Sum, over65Sum);
        }

        log.info("Demographic aggregates FORCED recalculation completed for {} municipalities",
                municipalities.size());
    }

    /**
     * Calculate demographic aggregates for a specific municipality
     */
    @Transactional
    public void calculateForMunicipality(String municipalityCode) {
        log.info("Calculating demographic aggregates for municipality: {}", municipalityCode);

        Municipality municipality = municipalityRepository.findByMunicipalityCode(municipalityCode)
                .orElseThrow(() -> new RuntimeException("Municipality not found: " + municipalityCode));

        Integer under15Sum = calculatePopulationUnder15(municipality);
        Integer over65Sum = calculatePopulationOver65(municipality);

        municipality.setPopulationUnder15Aggregate(under15Sum);
        municipality.setPopulationOver65Aggregate(over65Sum);

        municipalityRepository.save(municipality);

        log.info("Demographic aggregates calculated for {}: under15={}, over65={}",
                municipalityCode, under15Sum, over65Sum);
    }

    /**
     * Calculate total population under 15 for a municipality from its settlements
     */
    private Integer calculatePopulationUnder15(Municipality municipality) {
        Integer sum = settlementRepository.sumPopulationUnder15ByMunicipalityCode(
                municipality.getMunicipalityCode()
        );
        return sum != null ? sum : 0;
    }

    /**
     * Calculate total population over 65 for a municipality from its settlements
     */
    private Integer calculatePopulationOver65(Municipality municipality) {
        return settlementRepository.sumPopulationOver65ByMunicipalityCode(
                municipality.getMunicipalityCode()
        );
    }

    /**
     * Verify demographic aggregates match settlements data
     */
    @Transactional(readOnly = true)
    public void verifyDemographicAggregates() {
        log.info("Verifying demographic aggregates");

        List<Municipality> municipalities = municipalityRepository.findAll();
        int mismatchCount = 0;

        for (Municipality municipality : municipalities) {
            Integer expectedUnder15 = calculatePopulationUnder15(municipality);
            Integer expectedOver65 = calculatePopulationOver65(municipality);

            Integer actualUnder15 = municipality.getPopulationUnder15Aggregate();
            Integer actualOver65 = municipality.getPopulationOver65Aggregate();

            boolean mismatch = false;

            if (actualUnder15 == null || !actualUnder15.equals(expectedUnder15)) {
                log.warn("Mismatch in population_under_15 for {}: expected={}, actual={}",
                        municipality.getMunicipalityCode(), expectedUnder15, actualUnder15);
                mismatch = true;
            }

            if (actualOver65 == null || !actualOver65.equals(expectedOver65)) {
                log.warn("Mismatch in population_over_65 for {}: expected={}, actual={}",
                        municipality.getMunicipalityCode(), expectedOver65, actualOver65);
                mismatch = true;
            }

            if (mismatch) {
                mismatchCount++;
            }
        }

        if (mismatchCount == 0) {
            log.info("All demographic aggregates verified successfully");
        } else {
            log.warn("Found {} municipalities with demographic aggregate mismatches", mismatchCount);
        }
    }
}