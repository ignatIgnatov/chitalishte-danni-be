package bg.chitalishte.repository;

import bg.chitalishte.entity.ChitalishteYearData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChitalishteYearDataRepository extends JpaRepository<ChitalishteYearData, UUID> {
    List<ChitalishteYearData> findByChitalishteId(UUID chitalishteId);
    List<ChitalishteYearData> findByYear(Integer year);
    Optional<ChitalishteYearData> findByChitalishteIdAndYear(UUID chitalishteId, Integer year);
}
