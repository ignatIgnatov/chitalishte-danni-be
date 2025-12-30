package bg.chitalishte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityMetricsDTO {
    private UUID id;
    private String municipalityName;
    private Integer totalChitalishta;
    private Integer villageChitalishta;
    private Integer cityChitalishta;
    private BigDecimal stateSubsidyAmount;
    private BigDecimal stateSubsidyPerCapita;
    private BigDecimal additionalPositions;
    private BigDecimal revenueFromSubsidiesPercent;
    private BigDecimal revenueFromRentPercent;
    private BigDecimal revenueFromOtherPercent;
    private BigDecimal expensesForSalariesPercent;
    private BigDecimal expensesOtherPercent;
    private Integer totalStaff;
    private BigDecimal staffHigherEducationPercent;
    private BigDecimal staffSecondaryEducationPercent;
    private Integer secretariesCount;
    private BigDecimal secretariesHigherEducationPercent;
    private BigDecimal averageInsuranceIncome;
    private BigDecimal chitalishtaNoTrainingPercent;
    private BigDecimal chitalishtaPer10kResidents;
    private BigDecimal chitalishtaPer1kChildrenUnder15;
    private BigDecimal chitalishtaPer1kStudents;
    private BigDecimal chitalishtaPer1kKindergarten;
    private BigDecimal chitalishtaPer1kElderly;
    private Integer uniqueEmploymentContracts;
}