package bg.chitalishte.repository;

import bg.chitalishte.entity.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {

    /**
     * Find municipality by code (business key)
     */
    Optional<Municipality> findByMunicipalityCode(String municipalityCode);

    /**
     * Check if municipality exists by code
     */
    boolean existsByMunicipalityCode(String municipalityCode);

    /**
     * Search municipalities by name (case-insensitive, partial match)
     */
    @Query("SELECT m FROM Municipality m WHERE " +
            "LOWER(m.municipality) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.municipalityNorm) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Municipality> searchByName(@Param("query") String query);
}