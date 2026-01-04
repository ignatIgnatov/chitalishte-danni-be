package bg.chitalishte.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key for MunicipalityYearData
 * Uses municipality_code (String) instead of UUID for better performance and clarity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityYearDataId implements Serializable {
    private String municipalityCode;  // Changed from UUID to String
    private Integer year;
}