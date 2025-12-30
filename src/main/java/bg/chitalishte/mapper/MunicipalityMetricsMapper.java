package bg.chitalishte.mapper;

import bg.chitalishte.dto.MunicipalityMetricsDTO;
import bg.chitalishte.entity.MunicipalityMetrics;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityMetricsMapper {

    public MunicipalityMetricsDTO toDTO(MunicipalityMetrics entity) {
        if (entity == null) return null;

        return MunicipalityMetricsDTO.builder()
                .id(entity.getId())
                .municipalityName(entity.getMunicipality() != null ?
                        entity.getMunicipality().getMunicipality() : null)
                .totalChitalishta(entity.getTotalChitalishta())
                .villageChitalishta(entity.getVillageChitalishta())
                .cityChitalishta(entity.getCityChitalishta())
                .stateSubsidyAmount(entity.getStateSubsidyAmount())
                .stateSubsidyPerCapita(entity.getStateSubsidyPerCapita())
                .additionalPositions(entity.getAdditionalPositions())
                .revenueFromSubsidiesPercent(entity.getRevenueFromSubsidiesPercent())
                .revenueFromRentPercent(entity.getRevenueFromRentPercent())
                .revenueFromOtherPercent(entity.getRevenueFromOtherPercent())
                .expensesForSalariesPercent(entity.getExpensesForSalariesPercent())
                .expensesOtherPercent(entity.getExpensesOtherPercent())
                .totalStaff(entity.getTotalStaff())
                .staffHigherEducationPercent(entity.getStaffHigherEducationPercent())
                .staffSecondaryEducationPercent(entity.getStaffSecondaryEducationPercent())
                .secretariesCount(entity.getSecretariesCount())
                .secretariesHigherEducationPercent(entity.getSecretariesHigherEducationPercent())
                .averageInsuranceIncome(entity.getAverageInsuranceIncome())
                .chitalishtaNoTrainingPercent(entity.getChitalishtaNoTrainingPercent())
                .chitalishtaPer10kResidents(entity.getChitalishtaPer10kResidents())
                .chitalishtaPer1kChildrenUnder15(entity.getChitalishtaPer1kChildrenUnder15())
                .chitalishtaPer1kStudents(entity.getChitalishtaPer1kStudents())
                .chitalishtaPer1kKindergarten(entity.getChitalishtaPer1kKindergarten())
                .chitalishtaPer1kElderly(entity.getChitalishtaPer1kElderly())
                .uniqueEmploymentContracts(entity.getUniqueEmploymentContracts())
                .build();
    }
}