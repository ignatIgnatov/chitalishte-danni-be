package bg.chitalishte.service;

import bg.chitalishte.entity.Municipality;
import bg.chitalishte.repository.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for updating demographic-based metrics in municipality_metrics
 * after demographic aggregates have been recalculated
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MunicipalityDemographicMetricsService {

    private final JdbcTemplate jdbcTemplate;
    private final MunicipalityRepository municipalityRepository;

    /**
     * Update demographic-based metrics in municipality_metrics
     * This MUST be called AFTER demographic aggregates are recalculated!
     *
     * Updates:
     * - chitalishta_per_1k_children_under_15
     * - chitalishta_per_1k_elderly
     */
    @Transactional
    public void updateDemographicMetrics() {
        log.info("Starting update of demographic-based metrics in municipality_metrics");

        String sql = """
            UPDATE municipality_metrics mm
            SET
                chitalishta_per_1k_children_under_15 = CASE
                    WHEN m.population_under_15_aggregate > 0
                    THEN ROUND((mm.total_chitalishta::NUMERIC / m.population_under_15_aggregate) * 1000, 1)
                    ELSE NULL
                END,
                chitalishta_per_1k_elderly = CASE
                    WHEN m.population_over_65_aggregate > 0
                    THEN ROUND((mm.total_chitalishta::NUMERIC / m.population_over_65_aggregate) * 1000, 1)
                    ELSE NULL
                END
            FROM municipalities m
            WHERE mm.municipality_id = m.id
            """;

        int updatedRows = jdbcTemplate.update(sql);

        log.info("✅ Updated demographic metrics for {} municipality_metrics rows", updatedRows);
    }

    /**
     * Verify demographic metrics calculations
     * Logs any discrepancies found
     */
    @Transactional(readOnly = true)
    public void verifyDemographicMetrics() {
        log.info("Verifying demographic metrics in municipality_metrics");

        String sql = """
            SELECT 
                m.municipality_code,
                m.municipality,
                mm.total_chitalishta,
                m.population_under_15_aggregate,
                m.population_over_65_aggregate,
                mm.chitalishta_per_1k_children_under_15 as actual_per_1k_children,
                CASE
                    WHEN m.population_under_15_aggregate > 0
                    THEN ROUND((mm.total_chitalishta::NUMERIC / m.population_under_15_aggregate) * 1000, 1)
                    ELSE NULL
                END as expected_per_1k_children,
                mm.chitalishta_per_1k_elderly as actual_per_1k_elderly,
                CASE
                    WHEN m.population_over_65_aggregate > 0
                    THEN ROUND((mm.total_chitalishta::NUMERIC / m.population_over_65_aggregate) * 1000, 1)
                    ELSE NULL
                END as expected_per_1k_elderly
            FROM municipality_metrics mm
            JOIN municipalities m ON mm.municipality_id = m.id
            """;

        jdbcTemplate.query(sql, (rs) -> {
            String municipalityCode = rs.getString("municipality_code");
            Double actualChildren = rs.getDouble("actual_per_1k_children");
            if (rs.wasNull()) actualChildren = null;
            Double expectedChildren = rs.getDouble("expected_per_1k_children");
            if (rs.wasNull()) expectedChildren = null;

            Double actualElderly = rs.getDouble("actual_per_1k_elderly");
            if (rs.wasNull()) actualElderly = null;
            Double expectedElderly = rs.getDouble("expected_per_1k_elderly");
            if (rs.wasNull()) expectedElderly = null;

            boolean mismatch = false;

            if (!areEqual(actualChildren, expectedChildren)) {
                log.warn("Mismatch in chitalishta_per_1k_children_under_15 for {}: actual={}, expected={}",
                        municipalityCode, actualChildren, expectedChildren);
                mismatch = true;
            }

            if (!areEqual(actualElderly, expectedElderly)) {
                log.warn("Mismatch in chitalishta_per_1k_elderly for {}: actual={}, expected={}",
                        municipalityCode, actualElderly, expectedElderly);
                mismatch = true;
            }
        });

        log.info("Demographic metrics verification completed");
    }

    private boolean areEqual(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 0.01; // tolerance for rounding
    }
}