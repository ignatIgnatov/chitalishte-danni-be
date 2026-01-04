package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Year-dependent data for municipality (NSI, NAP, subsidies, economic indicators)
 * Composite key: (municipality_code, year)
 */
@Entity
@Table(name = "municipality_year_data")
@IdClass(MunicipalityYearDataId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityYearData {

    // ========== COMPOSITE KEY ==========

    @Id
    @Column(name = "municipality_code", length = 10, nullable = false)
    private String municipalityCode;  // Part of composite key

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;  // Part of composite key

    // ========== FOREIGN KEY RELATIONSHIP ==========

    // Foreign key to municipality (not part of ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    // ========== НСИ ПРОУЧВАНЕ НА ЧИТАЛИЩАТА 2022 - ПЕРСОНАЛ ==========

    // Общ персонал
    // Колона EH: Общо персонал, брой
    @Column(name = "total_staff_count")
    private Integer totalStaffCount;

    // Персонал с висше образование
    // Колона EI: Персонал с висше образование, брой
    @Column(name = "staff_higher_education_count")
    private Integer staffHigherEducationCount;

    // Персонал със средно образование
    // Колона EJ: Персонал със средно образование, брой
    @Column(name = "staff_secondary_education_count")
    private Integer staffSecondaryEducationCount;

    // Брой читалищни секретари
    // Колона EN: Читалищни секретари, брой
    @Column(name = "secretaries_count")
    private Integer secretariesCount;

    // Секретари с висше образование
    // Колона EO: Читалищни секретари с висше образование, брой
    @Column(name = "secretaries_higher_education_count")
    private Integer secretariesHigherEducationCount;

    // ========== НСИ ПРОУЧВАНЕ НА ЧИТАЛИЩАТА 2022 - ФИНАНСИ ==========

    // Общ размер на приходите
    // Колона ER: Общ размер на приходите, хил. лева
    @Column(name = "total_revenue_thousands", precision = 15, scale = 2)
    private BigDecimal totalRevenueThousands;

    // Приходи от субсидия
    // Колона ES: Приходи от субсидия от държавния/общинските бюджети, хил. лева
    @Column(name = "revenue_from_subsidies_thousands", precision = 15, scale = 2)
    private BigDecimal revenueFromSubsidiesThousands;

    // Приходи от наеми
    // Колона ET: Приходи от наеми на движимо и недвижимо имущество, хил. лева
    @Column(name = "revenue_from_rent_thousands", precision = 15, scale = 2)
    private BigDecimal revenueFromRentThousands;

    // Общ размер на разходите
    // Колона EU: Общ размер на разходите, хил. лева
    @Column(name = "total_expenses_thousands", precision = 15, scale = 2)
    private BigDecimal totalExpensesThousands;

    // Разходи за работна заплата
    // Колона EV: Разходи за работна заплата, хил. лева
    @Column(name = "expenses_salaries_thousands", precision = 15, scale = 2)
    private BigDecimal expensesSalariesThousands;

    // Разходи за социални осигуровки
    // Колона EW: Разходи за социални осигуровки и надбавки, хил. лева
    @Column(name = "expenses_social_security_thousands", precision = 15, scale = 2)
    private BigDecimal expensesSocialSecurityThousands;

    // ========== НАП ДАННИ 2023 ==========

    // Среден осигурителен доход
    // Колона EY: Среден осигурителен доход ТД
    @Column(name = "average_insurance_income", precision = 10, scale = 2)
    private BigDecimal averageInsuranceIncome;

    // Уникални лица на трудов договор
    // Колона EZ: Уникални лица ТД
    @Column(name = "unique_employment_contracts")
    private Integer uniqueEmploymentContracts;

    // ========== СУБСИДИИ ОТ МИНИСТЕРСТВО НА ФИНАНСИТЕ ==========

    // Общо субсидирани бройки
    // Колона FA: Общо субсидирани бройки
    @Column(name = "subsidized_positions")
    private Integer subsidizedPositions;

    // Допълнително отпуснати бройки
    // Колона FB: Допълнително отпуснати бройки
    @Column(name = "additional_positions")
    private Integer additionalPositions;

    // ========== ИКОНОМИЧЕСКИ ПОКАЗАТЕЛИ (265 ОБЩИНИ) ==========

    // Безработица (%)
    // Колона FC: unemployment %
    @Column(name = "unemployment_rate")
    private Double unemploymentRate;

    // Безработица младежи 15-29 (%)
    // Колона FD: unemployment 15-29, %
    @Column(name = "unemployment_rate_15_29")
    private Double unemploymentRate1529;

    // Средна брутна заплата (лв/месец)
    // Колона FE: gross wage, lv/month
    @Column(name = "gross_wage_monthly")
    private Double grossWageMonthly;

    // Брутна добавена стойност на човек
    // Колона FF: gross value added lv./person
    @Column(name = "gross_value_added_per_person")
    private Double grossValueAddedPerPerson;

    // Брой фирми
    // Колона FG: companies_number
    @Column(name = "companies_number")
    private Integer companiesNumber;

    // Фирми на човек от населението
    // Колона FH: companies_percapita
    @Column(name = "companies_per_capita")
    private Double companiesPerCapita;

    // Заетост (% от населението 15+)
    // Колона FI: employment, % of population 15+
    @Column(name = "employment_rate")
    private Double employmentRate;

    // Градско население (%)
    // Колона FJ: urban population, %
    @Column(name = "urban_population_percent")
    private Double urbanPopulationPercent;

    // ========== ОБРАЗОВАНИЕ И ИНФРАСТРУКТУРА ==========

    // Брой ученици
    // Колона FU: students_number
    @Column(name = "students_number")
    private Integer studentsNumber;

    // Ученици на 1000 души
    // Колона FV: students_per1000
    @Column(name = "students_per_1000")
    private Double studentsPer1000;

    // Деца в детски градини
    // Колона FY: kids_kindergartens
    @Column(name = "kids_kindergartens")
    private Integer kidsKindergartens;

    // Болници
    // Колона FV: hospitals (само за 2022)
    @Column(name = "hospitals")
    private Integer hospitals;

    // Лошо здраве
    // Колона FW: poor_health (само за 2021)
    @Column(name = "poor_health")
    private Double poorHealth;
}