package bg.chitalishte.mapper;

import bg.chitalishte.dto.MunicipalityDTO;
import bg.chitalishte.entity.Municipality;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityMapper {

    public MunicipalityDTO toDTO(Municipality entity) {
        if (entity == null) return null;

        return MunicipalityDTO.builder()
                .id(entity.getId())
                .municipalityCode(entity.getMunicipalityCode())
                .municipality(entity.getMunicipality())
                .municipalityNorm(entity.getMunicipalityNorm())
                .district(entity.getDistrict())
                .districtCode(entity.getDistrictCode())
                .nuts1(entity.getNuts1())
                .nuts2(entity.getNuts2())
                .nuts3(entity.getNuts3())
                .mrrbCategory(entity.getMrrbCategory())
                .municipalityPopulation(entity.getMunicipalityPopulation())
                .totalChitalishta(entity.getChitalishta() != null ? entity.getChitalishta().size() : 0)
                .build();
    }
}