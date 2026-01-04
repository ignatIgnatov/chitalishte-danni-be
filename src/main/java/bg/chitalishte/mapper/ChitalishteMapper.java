package bg.chitalishte.mapper;

import bg.chitalishte.dto.ChitalishteDTO;
import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.entity.ChitalishteYearData;
import org.springframework.stereotype.Component;

/**
 * Mapper for Chitalishte entity to ChitalishteDTO
 * Maps data from new entity structure to existing DTO (no FE changes needed)
 */
@Component
public class ChitalishteMapper {

    public ChitalishteDTO toDTO(Chitalishte entity) {
        if (entity == null) {
            return null;
        }

        ChitalishteYearData latestData = entity.getLatestYearData();

        return ChitalishteDTO.builder()
                .id(entity.getId())
                .regN(entity.getRegN())
                .name(entity.getName())
                .town(entity.getTown())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .settlementNorm(entity.getSettlementNorm())
                .villageCity(entity.getVillageCity())
                .uic(entity.getUic())
                .status(latestData != null ? latestData.getStatus() : null)
                .latestYear(latestData != null ? latestData.getYear() : null)
                .build();
    }
}