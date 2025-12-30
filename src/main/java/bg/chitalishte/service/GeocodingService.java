package bg.chitalishte.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeocodingService {

    @Value("${geocoding.data.path:geocoding-data}")
    private String dataPath;

    private final ObjectMapper objectMapper;
    private Map<String, String> geoJsonCache = new HashMap<>();
    private List<Map<String, String>> municipalitiesData = new ArrayList<>();

    public GeocodingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing GeocodingService with data path: {}", dataPath);
        loadMunicipalitiesCsv();
    }

    /**
     * Get GeoJSON data by type
     * Cached to improve performance
     */
    @Cacheable(value = "geojson", key = "#type")
    public String getGeoJsonData(String type) {
        try {
            String resourcePath = String.format("%s/%s.geojson", dataPath, type);
            Resource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.error("GeoJSON file not found: {}", resourcePath);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            log.error("Error reading GeoJSON file: {}", type, e);
            return null;
        }
    }

    /**
     * Get municipalities CSV data
     */
    @Cacheable("municipalitiesCsv")
    public String getMunicipalitiesCsv() {
        try {
            String resourcePath = String.format("%s/municipalities.csv", dataPath);
            Resource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.error("Municipalities CSV file not found");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            log.error("Error reading municipalities CSV", e);
            return null;
        }
    }

    /**
     * Load municipalities CSV into memory for searching
     */
    private void loadMunicipalitiesCsv() {
        try {
            String csv = getMunicipalitiesCsv();
            if (csv == null) {
                log.warn("Could not load municipalities CSV");
                return;
            }

            String[] lines = csv.split("\n");
            for (int i = 1; i < lines.length; i++) { // Skip header
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 3) {
                    Map<String, String> municipality = new HashMap<>();
                    municipality.put("code", parts[0].replaceAll("\"", ""));
                    municipality.put("name", parts[1].replaceAll("\"", ""));
                    municipality.put("ekatte", parts[2].replaceAll("\"", ""));
                    municipalitiesData.add(municipality);
                }
            }
            log.info("Loaded {} municipalities from CSV", municipalitiesData.size());
        } catch (Exception e) {
            log.error("Error loading municipalities CSV", e);
        }
    }

    /**
     * Search municipalities by name, code, or ekatte
     */
    public List<Map<String, String>> searchMunicipalities(String name, String code, String ekatte) {
        return municipalitiesData.stream()
                .filter(m -> {
                    if (name != null && !name.isEmpty()) {
                        return m.get("name").toLowerCase().contains(name.toLowerCase());
                    }
                    if (code != null && !code.isEmpty()) {
                        return m.get("code").equalsIgnoreCase(code);
                    }
                    if (ekatte != null && !ekatte.isEmpty()) {
                        return m.get("ekatte").equals(ekatte);
                    }
                    return false;
                })
                .limit(50) // Limit results
                .collect(Collectors.toList());
    }

    /**
     * Get municipality feature by NUTS4 code
     */
    public String getMunicipalityByCode(String nuts4) {
        try {
            String allMunicipalities = getGeoJsonData("municipalities");
            if (allMunicipalities == null) return null;

            JsonNode root = objectMapper.readTree(allMunicipalities);
            JsonNode features = root.get("features");

            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    JsonNode properties = feature.get("properties");
                    if (properties != null && properties.has("nuts4")) {
                        String featureNuts4 = properties.get("nuts4").asText();
                        if (featureNuts4.equalsIgnoreCase(nuts4)) {
                            return objectMapper.writeValueAsString(feature);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting municipality by code: {}", nuts4, e);
        }
        return null;
    }

    /**
     * Get province feature by NUTS3 code
     */
    public String getProvinceByCode(String nuts3) {
        try {
            String allProvinces = getGeoJsonData("provinces");
            if (allProvinces == null) return null;

            JsonNode root = objectMapper.readTree(allProvinces);
            JsonNode features = root.get("features");

            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    JsonNode properties = feature.get("properties");
                    if (properties != null && properties.has("nuts3")) {
                        String featureNuts3 = properties.get("nuts3").asText();
                        if (featureNuts3.equalsIgnoreCase(nuts3)) {
                            return objectMapper.writeValueAsString(feature);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting province by code: {}", nuts3, e);
        }
        return null;
    }

    /**
     * Get available layers information
     */
    public List<Map<String, Object>> getAvailableLayers() {
        List<Map<String, Object>> layers = new ArrayList<>();

        layers.add(createLayerInfo("country", "Country Border", "Bulgaria country boundary",
                "/api/geocoding/country"));
        layers.add(createLayerInfo("provinces", "Provinces", "28 provinces (Области)",
                "/api/geocoding/provinces"));
        layers.add(createLayerInfo("municipalities", "Municipalities", "265 municipalities (Общини)",
                "/api/geocoding/municipalities"));
        layers.add(createLayerInfo("settlements", "Settlements", "Cities, towns and villages",
                "/api/geocoding/settlements"));
        layers.add(createLayerInfo("sofiaTraffic", "Sofia Traffic Zones", "Sofia city traffic subzones",
                "/api/geocoding/sofia-traffic"));
        layers.add(createLayerInfo("rpuSofia", "Sofia Police Stations", "Police station regions",
                "/api/geocoding/rpu-sofia"));

        return layers;
    }

    private Map<String, Object> createLayerInfo(String key, String label, String description, String endpoint) {
        Map<String, Object> layer = new HashMap<>();
        layer.put("key", key);
        layer.put("label", label);
        layer.put("description", description);
        layer.put("endpoint", endpoint);
        layer.put("available", true);
        return layer;
    }

    /**
     * Get statistics about the geocoding data
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Count features in each GeoJSON file
            stats.put("provinces", countFeatures("provinces"));
            stats.put("municipalities", countFeatures("municipalities"));
            stats.put("settlements", countFeatures("settlements"));
            stats.put("sofiaTrafficZones", countFeatures("sofiatraffic_subzones"));
            stats.put("rpuSofiaRegions", countFeatures("rpu_sofia"));
            stats.put("totalMunicipalities", municipalitiesData.size());

            stats.put("dataPath", dataPath);
            stats.put("cacheEnabled", true);

        } catch (Exception e) {
            log.error("Error calculating statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    private int countFeatures(String type) {
        try {
            String geoJson = getGeoJsonData(type);
            if (geoJson == null) return 0;

            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode features = root.get("features");

            return features != null && features.isArray() ? features.size() : 0;
        } catch (Exception e) {
            log.error("Error counting features for type: {}", type, e);
            return 0;
        }
    }
}
