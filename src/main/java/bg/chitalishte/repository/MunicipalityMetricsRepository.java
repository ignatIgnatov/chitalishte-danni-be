package bg.chitalishte.repository;

import bg.chitalishte.entity.MunicipalityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityMetricsRepository extends JpaRepository<MunicipalityMetrics, UUID> {

    /**
     * Find metrics by municipality code (nuts4)
     */
    Optional<MunicipalityMetrics> findByMunicipalityMunicipalityCode(String municipalityCode);

    /**
     * Find metrics by municipality ID
     */
    Optional<MunicipalityMetrics> findByMunicipalityId(UUID municipalityId);
}