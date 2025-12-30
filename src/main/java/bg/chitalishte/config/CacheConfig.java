package bg.chitalishte.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager for GeoJSON data
     * Since GeoJSON files don't change frequently, we can cache them aggressively
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "geojson",
                "municipalitiesCsv",
                "searches"  // Added searches to the same cache manager
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)  // Increased to handle all cache types
                .expireAfterWrite(24, TimeUnit.HOURS) // Cache for 24 hours
                .recordStats());

        return cacheManager;
    }
}
