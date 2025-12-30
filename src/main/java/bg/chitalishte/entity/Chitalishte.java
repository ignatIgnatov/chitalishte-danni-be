package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chitalishta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chitalishte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Връзка с община
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    // Уникален идентификатор (колона 0)
    @Column(name = "reg_n", unique = true, nullable = false, length = 50)
    private String regN;

    // Основна информация (колони 1, 5, 6, 7, 65)
    @Column(name = "name", length = 200)
    private String name;  // 1

    @Column(name = "town", length = 200)
    private String town;  // 5

    @Column(name = "address", length = 300)
    private String address;  // 6

    @Column(name = "uic", length = 50)
    private String uic;  // 7

    @Column(name = "phone", length = 300)
    private String phone;  // 65 - може да съдържа бележки и множество телефони

    // Местоположение (колони 8, 10, 13, 17, 20)
    @Column(name = "settlement_norm", length = 200)
    private String settlementNorm;  // 8

    @Column(name = "village_city", length = 20)
    private String villageCity;  // 10 (използва се за филтриране село/град)

    @Column(name = "mayorality_code", length = 10)
    private String mayoralityCode;  // 13

    @Column(name = "ekatte", length = 10)
    private String ekatte;  // 17

    @Column(name = "is_munip_center", length = 10)
    private String isMunipCenter;  // 20

    // Категории (колони 22, 73, 74)
    @Column(name = "empl_category", length = 50)
    private String emplCategory;  // 22

    @Column(name = "regional_list", length = 500)
    private String regionalList;  // 73

    @Column(name = "national_list", length = 500)
    private String nationalList;  // 74

    // Връзка с годишни данни
    @OneToMany(mappedBy = "chitalishte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChitalishteYearData> yearData = new ArrayList<>();

    // Helper методи
    public void addYearData(ChitalishteYearData data) {
        yearData.add(data);
        data.setChitalishte(this);
    }

    public ChitalishteYearData getDataForYear(Integer year) {
        return yearData.stream()
                .filter(d -> d.getYear().equals(year))
                .findFirst()
                .orElse(null);
    }

    public ChitalishteYearData getLatestYearData() {
        return yearData.stream()
                .max(Comparator.comparing(ChitalishteYearData::getYear))
                .orElse(null);
    }
}