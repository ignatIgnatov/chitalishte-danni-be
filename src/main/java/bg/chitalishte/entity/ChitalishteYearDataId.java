package bg.chitalishte.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key for ChitalishteYearData
 * Uses reg_n (String) instead of UUID for better performance and clarity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChitalishteYearDataId implements Serializable {
    private String regN;
    private Integer year;
}