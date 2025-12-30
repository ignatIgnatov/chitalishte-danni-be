package bg.chitalishte.repository;

import bg.chitalishte.entity.Chitalishte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChitalishteRepository extends JpaRepository<Chitalishte, UUID> {

    Optional<Chitalishte> findByRegN(String regN);

    List<Chitalishte> findByMunicipalityId(UUID municipalityId);

    List<Chitalishte> findByMunicipalityMunicipalityCode(String municipalityCode);
}