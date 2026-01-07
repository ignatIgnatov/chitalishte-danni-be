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

/**
 * Chitalishte (cultural center) with basic static information
 */
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

    // ========== ВРЪЗКИ ==========

    // Връзка с община
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    // Връзка с населено място (за демографски данни)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ekatte", referencedColumnName = "ekatte")
    private Settlement settlement;

    // ========== УНИКАЛЕН ИДЕНТИФИКАТОР ==========

    // Регистрационен номер (уникален идентификатор)
    // Колона A: reg_n
    @Column(name = "reg_n", unique = true, nullable = false, length = 50)
    private String regN;

    @Column
    private String slug;

    // ========== ОСНОВНА ИНФОРМАЦИЯ ==========

    // Име на читалище
    // Колона B: name
    @Column(name = "name", length = 200)
    private String name;

    // Населено място
    // Колона F: town
    @Column(name = "town", length = 200)
    private String town;

    // Адрес
    // Колона G: address
    @Column(name = "address", length = 300)
    private String address;

    // ЕИК (булстат)
    // Колона H: uic
    @Column(name = "uic", length = 50)
    private String uic;

    // Телефон и бележки
    // Колона CM: phone
    @Column(name = "phone", length = 300)
    private String phone;

    // ========== МЕСТОПОЛОЖЕНИЕ ==========

    // Нормализирано име на населеното място
    // Колона I: settlement_norm
    @Column(name = "settlement_norm", length = 200)
    private String settlementNorm;

    // Тип населено място (село/град) - за филтриране
    // Колона K: village_city
    @Column(name = "village_city", length = 20)
    private String villageCity;

    // Код на кметство
    // Колона N: mayorality_code
    @Column(name = "mayorality_code", length = 10)
    private String mayoralityCode;

    // ЕКАТТЕ код
    // Колона R: ekatte
    @Column(name = "ekatte_code", length = 10)
    private String ekatteCode;

    // Дали е общински център
    // Колона U: is_munip_center
    @Column(name = "is_munip_center", length = 10)
    private String isMunipCenter;

    // ========== КАТЕГОРИИ ==========

    // Категория заетост
    // Колона W: empl_category
    @Column(name = "empl_category", length = 50)
    private String emplCategory;

    // Регионална листа
    // Колона DC: регионална листа
    @Column(name = "regional_list", length = 500)
    private String regionalList;

    // Национална листа
    // Колона DD: национална листа
    @Column(name = "national_list", length = 500)
    private String nationalList;

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