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
public class ChitalishteDTO {
    private UUID id;
    private String regN;
    private String name;
    private String town;
    private String address;
    private String phone;
    private String settlementNorm;
    private String villageCity;
    private String uic;
    private String status;
    private Integer latestYear;
}