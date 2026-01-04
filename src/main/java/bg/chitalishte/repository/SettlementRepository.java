package bg.chitalishte.repository;

import bg.chitalishte.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
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
}