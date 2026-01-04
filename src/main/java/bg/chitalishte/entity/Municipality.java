package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Municipality with static information (no year-dependent data)
 */
@Entity
@Table(name = "municipalities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Municipality {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ========== ОСНОВНА ИНФОРМАЦИЯ ==========

    // Код на община (бизнес ключ)
    // Колона M: municipality_code
    @Column(name = "municipality_code", unique = true, nullable = false, length = 10)
    private String municipalityCode;

    // Име на община
    // Колона E: municipality
    @Column(name = "municipality", length = 100)
    private String municipality;

    // Нормализирано име на община
    // Колона J: municipality_norm
    @Column(name = "municipality_norm", length = 100)
    private String municipalityNorm;

    // Област
    // Колона D: district
    @Column(name = "district", length = 100)
    private String district;

    // Код на област
    // Колона L: district_code
    @Column(name = "district_code", length = 10)
    private String districtCode;

    // ========== NUTS КЛАСИФИКАЦИЯ ==========

    // NUTS1 регион
    // Колона O: NUTS1
    @Column(name = "nuts1", length = 10)
    private String nuts1;

    // NUTS2 регион
    // Колона P: NUTS2
    @Column(name = "nuts2", length = 10)
    private String nuts2;

    // NUTS3 регион
    // Колона Q: NUTS3
    @Column(name = "nuts3", length = 10)
    private String nuts3;

    // Категория по МРРБ
    // Колона S: mrrb_category
    @Column(name = "mrrb_category", length = 100)
    private String mrrbCategory;

    @Column(name = "total_chitalishta")
    private Integer totalChitalishta;  // Column V

    // ========== ПРЕБРОЯВАНЕ 2021 - НАСЕЛЕНИЕ НА НИВО ОБЩИНА ==========

    // Население на община
    // Колона DS: Население на община
    @Column(name = "municipality_population")
    private Integer municipalityPopulation;

    // Население под 15 годишна възраст (агрегирано от всички settlements)
    // Колона DT: Население под 15 годишна възраст преброяване 2021 г. (агрегат)
    @Column(name = "population_under_15_aggregate")
    private Integer populationUnder15Aggregate;

    // Население 65 и повече години (агрегирано от всички settlements)
    // Колона DV: Население 65 и повеч години преброяване 2021 г. (агрегат)
    @Column(name = "population_over_65_aggregate")
    private Integer populationOver65Aggregate;

    // ========== ЕТНИЧЕСКИ СЪСТАВ ==========

    // Дял на българите
    // Колона FP: share_bulgarian
    @Column(name = "share_bulgarian")
    private Double shareBulgarian;

    // Дял на турците
    // Колона FQ: Share_turkish
    @Column(name = "share_turkish")
    private Double shareTurkish;

    // Дял на ромите
    // Колона FR: share_roma
    @Column(name = "share_roma")
    private Double shareRoma;

    // Дял на останалите
    // Колона FS: share_others
    @Column(name = "share_others")
    private Double shareOthers;

    // ========== ДРУГИ ДЕМОГРАФСКИ ПОКАЗАТЕЛИ ==========

    // Коефициент на миграция
    // Колона FT: migration_coefficient
    @Column(name = "migration_coefficient")
    private Double migrationCoefficient;

    // Връзки
    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Settlement> settlements = new ArrayList<>();

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chitalishte> chitalishta = new ArrayList<>();

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MunicipalityYearData> yearData = new ArrayList<>();

    @OneToOne(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    private MunicipalityMetrics metrics;

    public void addSettlement(Settlement settlement) {
        settlements.add(settlement);
        settlement.setMunicipality(this);
    }

    public void addChitalishte(Chitalishte chitalishte) {
        chitalishta.add(chitalishte);
        chitalishte.setMunicipality(this);
    }

    public void addYearData(MunicipalityYearData data) {
        yearData.add(data);
        data.setMunicipality(this);
    }
}