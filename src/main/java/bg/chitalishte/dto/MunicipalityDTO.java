package bg.chitalishte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDTO {
    private UUID id;
    private String municipalityCode;
    private String municipality;
    private String municipalityNorm;
    private String district;
    private String districtCode;
    private String nuts1;
    private String nuts2;
    private String nuts3;
    private String mrrbCategory;
    private Integer municipalityPopulation;
    private Integer totalChitalishta;
}