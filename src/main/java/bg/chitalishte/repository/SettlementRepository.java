package bg.chitalishte.repository;

import bg.chitalishte.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {

    /**
     * Find settlement by EKATTE code
     */
    Optional<Settlement> findByEkatte(String ekatte);

    /**
     * Check if settlement exists by EKATTE code
     */
    boolean existsByEkatte(String ekatte);

    @Query("SELECT COALESCE(SUM(s.populationUnder15), 0) FROM Settlement s WHERE s.municipality.municipalityCode = :code")
    Integer sumPopulationUnder15ByMunicipalityCode(@Param("code") String code);

    @Query("SELECT COALESCE(SUM(s.populationOver65), 0) FROM Settlement s WHERE s.municipality.municipalityCode = :code")
    Integer sumPopulationOver65ByMunicipalityCode(@Param("code") String code);
}