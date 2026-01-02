package bg.chitalishte.repository;

import bg.chitalishte.entity.ChitalishteYearData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChitalishteYearDataRepository extends JpaRepository<ChitalishteYearData, UUID> {
    Optional<ChitalishteYearData> findByChitalishteRegNAndYear(String regN, Integer year);
}
