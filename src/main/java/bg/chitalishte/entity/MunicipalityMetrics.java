package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Изчислени метрики за община (без year поле - използват последни налични данни)
 * Calculated metrics for municipality (no year field - uses latest available data)
 *
 * Източници: НСИ 2022, НАП 2023, Субсидии 2025, Регистър на читалищата 2023
 */
@Entity
@Table(name = "municipality_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Връзка към община (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false, unique = true)
    private Municipality municipality;

    // ========== ОСНОВНА ИНФОРМАЦИЯ (6 показателя) ==========

    // Общ брой читалища
    // Изчислено: COUNT(chitalishta) ГДЕ municipality_id = X
    @Column(name = "total_chitalishta")
    private Integer totalChitalishta;

    // Брой селски читалища
    // Изчислено: COUNT(chitalishta) ГДЕ municipality_id = X И villageCity = 'село'
    @Column(name = "village_chitalishta")
    private Integer villageChitalishta;

    // Брой градски читалища
    // Изчислено: COUNT(chitalishta) ГДЕ municipality_id = X И villageCity = 'град'
    @Column(name = "city_chitalishta")
    private Integer cityChitalishta;

    // Размер на държавната субсидия (лв)
    // Формула: FA × 19,555
    // Източници: Субсидии 2025 (колона FA)
    @Column(name = "state_subsidy_amount", precision = 15, scale = 2)
    private BigDecimal stateSubsidyAmount;

    // Държавна субсидия на човек от населението (лв)
    // Формула: (FA × 19,555) / DS
    // Източници: Субсидии 2025 (FA), Преброяване 2021 (DS)
    @Column(name = "state_subsidy_per_capita", precision = 10, scale = 2)
    private BigDecimal stateSubsidyPerCapita;

    // Допълнително отпуснати бройки
    // Източници: Субсидии (колона FB)
    @Column(name = "additional_positions")
    private Integer additionalPositions;

    // ========== ПРИХОДИ И РАЗХОДИ (5 показателя) ==========

    // Процент на приходите от субсидия
    // Формула: (ES / ER) × 100
    // Източници: НСИ 2022 (колони ES, ER)
    @Column(name = "revenue_from_subsidies_percent", precision = 10, scale = 2)
    private BigDecimal revenueFromSubsidiesPercent;

    // Процент на приходите от наеми
    // Формула: (ET / ER) × 100
    // Източници: НСИ 2022 (колони ET, ER)
    @Column(name = "revenue_from_rent_percent", precision = 10, scale = 2)
    private BigDecimal revenueFromRentPercent;

    // Процент на приходите от други източници
    // Формула: ((ER - (ES + ET)) / ER) × 100
    // Източници: НСИ 2022 (колони ER, ES, ET)
    @Column(name = "revenue_from_other_percent", precision = 10, scale = 2)
    private BigDecimal revenueFromOtherPercent;

    // Процент на разходите за заплати и осигуровки
    // Формула: ((EV + EW) / EU) × 100
    // Източници: НСИ 2022 (колони EV, EW, EU)
    @Column(name = "expenses_for_salaries_percent", precision = 10, scale = 2)
    private BigDecimal expensesForSalariesPercent;

    // Процент на другите разходи
    // Формула: ((EU - (EV + EW)) / EU) × 100
    // Източници: НСИ 2022 (колони EU, EV, EW)
    @Column(name = "expenses_other_percent", precision = 10, scale = 2)
    private BigDecimal expensesOtherPercent;

    // ========== ПЕРСОНАЛ (8 показателя) ==========

    // Общ персонал
    // Източници: НСИ 2022 (колона EH)
    @Column(name = "total_staff")
    private Integer totalStaff;

    // Брой уникални лица на трудов договор
    // Източници: НАП 2023 (колона EZ)
    @Column(name = "unique_employment_contracts")
    private Integer uniqueEmploymentContracts;

    // Процент от персонала с висше образование
    // Формула: (EI / EH) × 100
    // Източници: НСИ 2022 (колони EI, EH)
    @Column(name = "staff_higher_education_percent", precision = 10, scale = 2)
    private BigDecimal staffHigherEducationPercent;

    // Процент от персонала със средно образование
    // Формула: (EJ / EH) × 100
    // Източници: НСИ 2022 (колони EJ, EH)
    @Column(name = "staff_secondary_education_percent", precision = 10, scale = 2)
    private BigDecimal staffSecondaryEducationPercent;

    // Брой читалищни секретари
    // Източници: НСИ 2022 (колона EN)
    @Column(name = "secretaries_count")
    private Integer secretariesCount;

    // Процент от секретарите с висше образование
    // Формула: (EO / EN) × 100
    // Източници: НСИ 2022 (колони EO, EN)
    @Column(name = "secretaries_higher_education_percent", precision = 10, scale = 2)
    private BigDecimal secretariesHigherEducationPercent;

    // Среден осигурителен доход
    // Източници: НАП 2023 (колона EY)
    @Column(name = "average_insurance_income", precision = 10, scale = 2)
    private BigDecimal averageInsuranceIncome;

    // Процент читалища без участие в обучения
    // Формула: (COUNT(chitalishta ГДЕ CX = 0) / V) × 100
    // Източници: Регистър на читалищата 2023 (колона CX), V = общ брой читалища
    @Column(name = "chitalishta_no_training_percent", precision = 10, scale = 2)
    private BigDecimal chitalishtaNoTrainingPercent;

    // ========== ПО НАСЕЛЕНИЕ (5 показателя) ==========

    // Читалища на 10,000 жители
    // Формула: (V / DS) × 10,000
    // Източници: V = брой читалища, Преброяване 2021 (DS)
    @Column(name = "chitalishta_per_10k_residents", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer10kResidents;

    // Читалища на 1,000 деца под 15 години
    // Формула: (V / DT) × 1,000
    // Източници: V = брой читалища, Преброяване 2021 (DT)
    @Column(name = "chitalishta_per_1k_children_under_15", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kChildrenUnder15;

    // Читалища на 1,000 ученици
    // Формула: (V / FQ) × 1,000
    // Източници: V = брой читалища, 265 общини (FU)
    @Column(name = "chitalishta_per_1k_students", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kStudents;

    // Читалища на 1,000 деца в детски градини
    // Формула: (V / FU) × 1,000
    // Източници: V = брой читалища, 265 общини (FY)
    @Column(name = "chitalishta_per_1k_kindergarten", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kKindergarten;

    // Читалища на 1,000 жители 65+ години
    // Формула: (V / DV) × 1,000
    // Източници: V = брой читалища, Преброяване 2021 (DV)
    @Column(name = "chitalishta_per_1k_elderly", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kElderly;
}