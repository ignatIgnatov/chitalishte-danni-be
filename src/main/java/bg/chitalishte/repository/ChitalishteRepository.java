package bg.chitalishte.repository;

import bg.chitalishte.entity.Chitalishte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChitalishteRepository extends JpaRepository<Chitalishte, UUID> {

    /**
     * Find chitalishte by registration number (reg_n)
     */
    Optional<Chitalishte> findByRegN(String regN);

    /**
     * Check if chitalishte exists by registration number
     */
    boolean existsByRegN(String regN);

    /**
     * Find all chitalishta in a municipality by municipality code
     */
    @Query("SELECT c FROM Chitalishte c " +
            "WHERE c.municipality.municipalityCode = :municipalityCode " +
            "ORDER BY c.name")
    List<Chitalishte> findByMunicipalityMunicipalityCode(@Param("municipalityCode") String municipalityCode);

    /**
     * Count chitalishta in a municipality
     */
    @Query("SELECT COUNT(c) FROM Chitalishte c " +
            "WHERE c.municipality.municipalityCode = :municipalityCode")
    Long countByMunicipalityCode(@Param("municipalityCode") String municipalityCode);

    /**
     * Count village chitalishta in a municipality
     */
    @Query("SELECT COUNT(c) FROM Chitalishte c " +
            "WHERE c.municipality.municipalityCode = :municipalityCode " +
            "AND UPPER(c.villageCity) = 'СЕЛО'")
    Long countVillageChitalishta(@Param("municipalityCode") String municipalityCode);

    /**
     * Count city chitalishta in a municipality
     */
    @Query("SELECT COUNT(c) FROM Chitalishte c " +
            "WHERE c.municipality.municipalityCode = :municipalityCode " +
            "AND UPPER(c.villageCity) = 'ГРАД'")
    Long countCityChitalishta(@Param("municipalityCode") String municipalityCode);

    /**
     * Search chitalishta by name (case-insensitive, partial match)
     */
    @Query("SELECT c FROM Chitalishte c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.town) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Chitalishte> searchByName(@Param("query") String query);
}