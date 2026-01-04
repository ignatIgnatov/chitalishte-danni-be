package bg.chitalishte.repository;

import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.entity.ChitalishteYearData;
import bg.chitalishte.entity.ChitalishteYearDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChitalishteYearDataRepository extends JpaRepository<ChitalishteYearData, ChitalishteYearDataId> {

    /**
     * Find all year data for a chitalishte
     */
    List<ChitalishteYearData> findByChitalishte(Chitalishte chitalishte);

    /**
     * Find year data for a chitalishte by reg_n
     */
    @Query("SELECT cyd FROM ChitalishteYearData cyd " +
            "WHERE cyd.chitalishte.regN = :regN")
    List<ChitalishteYearData> findByChitalishteRegN(@Param("regN") String regN);

    /**
     * Find specific year data for a chitalishte by reg_n and year
     */
    @Query("SELECT cyd FROM ChitalishteYearData cyd " +
            "WHERE cyd.chitalishte.regN = :regN AND cyd.year = :year")
    Optional<ChitalishteYearData> findByChitalishteRegNAndYear(
            @Param("regN") String regN,
            @Param("year") Integer year);

    /**
     * Find latest year data for a chitalishte
     */
    @Query("SELECT cyd FROM ChitalishteYearData cyd " +
            "WHERE cyd.chitalishte.regN = :regN " +
            "ORDER BY cyd.year DESC")
    List<ChitalishteYearData> findLatestByChitalishteRegN(@Param("regN") String regN);

    /**
     * Count chitalishta with no training participation (CX = 0 or null) in municipality for specific year
     */
    @Query("SELECT COUNT(cyd) FROM ChitalishteYearData cyd " +
            "WHERE cyd.chitalishte.municipality.municipalityCode = :municipalityCode " +
            "AND cyd.year = :year " +
            "AND (cyd.trainingParticipation = 0 OR cyd.trainingParticipation IS NULL)")
    Long countChitalishtaWithNoTraining(@Param("municipalityCode") String municipalityCode, @Param("year") Integer year);
}