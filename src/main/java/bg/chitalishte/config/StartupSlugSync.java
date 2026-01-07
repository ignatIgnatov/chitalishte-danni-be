package bg.chitalishte.config;

import bg.chitalishte.service.SlugSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupSlugSync implements ApplicationRunner {

    private final SlugSyncService slugSyncService;

    @Override
    public void run(ApplicationArguments args) {
        slugSyncService.syncSlugs();
    }
}

