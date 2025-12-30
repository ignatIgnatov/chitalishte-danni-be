package bg.chitalishte.repository;

import bg.chitalishte.entity.MunicipalityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityMetricsRepository extends JpaRepository<MunicipalityMetrics, UUID> {

    Optional<MunicipalityMetrics> findByMunicipalityId(UUID municipalityId);

    Optional<MunicipalityMetrics> findByMunicipalityMunicipalityCode(String municipalityCode);
}