package bg.chitalishte.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MunicipalityStatisticsDTO {
    private Integer chitalishta;
    private Integer staff;
    private BigDecimal subsidy;
    private Double averageSubsidyPerChitalishte;
}
