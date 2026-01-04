package bg.chitalishte.repository;

import bg.chitalishte.entity.Municipality;
import bg.chitalishte.entity.MunicipalityYearData;
import bg.chitalishte.entity.MunicipalityYearDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MunicipalityYearDataRepository extends JpaRepository<MunicipalityYearData, MunicipalityYearDataId> {

    /**
     * Find all year data for a municipality
     */
    List<MunicipalityYearData> findByMunicipality(Municipality municipality);

    /**
     * Find year data for a municipality by code
     */
    List<MunicipalityYearData> findByMunicipalityCode(String municipalityCode);

    /**
     * Find specific year data for a municipality by code and year
     * This uses the composite key fields directly
     */
    Optional<MunicipalityYearData> findByMunicipalityCodeAndYear(String municipalityCode, Integer year);

    /**
     * Find latest year data for a municipality (ordered by year desc)
     */
    @Query("SELECT myd FROM MunicipalityYearData myd " +
            "WHERE myd.municipalityCode = :municipalityCode " +
            "ORDER BY myd.year DESC")
    List<MunicipalityYearData> findLatestByMunicipalityCode(@Param("municipalityCode") String municipalityCode);

    /**
     * Find year data for specific year across all municipalities
     */
    List<MunicipalityYearData> findByYear(Integer year);
}