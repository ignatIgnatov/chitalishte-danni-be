package bg.chitalishte.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MunicipalityDetailDTO {
    private String nuts4;
    private String name;
    private String nameBg;
    private String nameEn;
    private String nuts3;
    private String oblast;
    private String ekatte;
    private Integer totalChitalishta;
    private Integer totalStaff;
    private BigDecimal totalSubsidy;
    private List<ChitalishteDTO> chitalishta;
}
