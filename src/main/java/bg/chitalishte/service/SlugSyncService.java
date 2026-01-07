package bg.chitalishte.service;

import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.repository.ChitalishteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SlugSyncService {

    private final ChitalishteRepository localRepo;
    private final JdbcTemplate foreignJdbcTemplate;

    public SlugSyncService(ChitalishteRepository localRepo,
                           @Qualifier("foreignJdbcTemplate") JdbcTemplate foreignJdbcTemplate) {
        this.localRepo = localRepo;
        this.foreignJdbcTemplate = foreignJdbcTemplate;
    }

    @Transactional
    public void syncSlugs() {
        log.debug("START TO COPY SLUGS");
        List<Map<String, Object>> foreignRows = foreignJdbcTemplate.queryForList(
                "SELECT registration_number, slug FROM chitalishte"
        );

        Map<String, String> slugMap = foreignRows.stream()
                .collect(Collectors.toMap(
                        r -> r.get("registration_number").toString().replaceAll("\\.0$", ""),
                        r -> r.get("slug").toString()
                ));

        List<Chitalishte> toUpdate = localRepo.findAllBySlugIsNull();
        log.debug("FOUND {} CHITALISHTA WITHOUT SLUG.", toUpdate.size());
        for (Chitalishte c : toUpdate) {
            String reg = c.getRegN().replaceAll("\\.0$", "");
            if (slugMap.containsKey(reg)) {
                    c.setSlug(slugMap.get(reg));
            }
        }

        localRepo.saveAll(toUpdate);
    }
}

