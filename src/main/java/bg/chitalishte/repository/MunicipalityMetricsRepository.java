package bg.chitalishte.repository;

import bg.chitalishte.entity.Municipality;
import bg.chitalishte.entity.MunicipalityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityMetricsRepository extends JpaRepository<MunicipalityMetrics, UUID> {

    /**
     * Find metrics by municipality
     */
    Optional<MunicipalityMetrics> findByMunicipality(Municipality municipality);

    /**
     * Find metrics by municipality code
     */
    @Query("SELECT mm FROM MunicipalityMetrics mm " +
            "WHERE mm.municipality.municipalityCode = :municipalityCode")
    Optional<MunicipalityMetrics> findByMunicipalityCode(@Param("municipalityCode") String municipalityCode);

    /**
     * Check if metrics exist for municipality code
     */
    @Query("SELECT CASE WHEN COUNT(mm) > 0 THEN true ELSE false END " +
            "FROM MunicipalityMetrics mm " +
            "WHERE mm.municipality.municipalityCode = :municipalityCode")
    boolean existsByMunicipalityCode(@Param("municipalityCode") String municipalityCode);
}