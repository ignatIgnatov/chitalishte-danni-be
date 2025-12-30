package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    
    // Основна информация (от колони 3, 4, 9, 11, 12)
    @Column(name = "municipality_code", unique = true, nullable = false, length = 10)
    private String municipalityCode;  // колона 12
    
    @Column(name = "municipality", length = 100)
    private String municipality;  // колона 4
    
    @Column(name = "municipality_norm", length = 100)
    private String municipalityNorm;  // колона 9
    
    @Column(name = "district", length = 100)
    private String district;  // колона 3
    
    @Column(name = "district_code", length = 10)
    private String districtCode;  // колона 11
    
    // NUTS класификация (колони 14, 15, 16, 18)
    @Column(name = "nuts1", length = 10)
    private String nuts1;
    
    @Column(name = "nuts2", length = 10)
    private String nuts2;
    
    @Column(name = "nuts3", length = 10)
    private String nuts3;
    
    @Column(name = "mrrb_category", length = 100)
    private String mrrbCategory;
    
    // Население (колони 121-132)
    @Column(name = "settlement_population")
    private Integer settlementPopulation;  // 121
    
    @Column(name = "municipality_population")
    private Integer municipalityPopulation;  // 122 (DS)
    
    @Column(name = "total_population_2021")
    private Integer totalPopulation2021;  // 122 (DS) - same as municipality_population
    
    @Column(name = "population_under_15")
    private Integer populationUnder15;  // 123 (DT)
    
    @Column(name = "population_15_64")
    private Integer population1564;  // 124
    
    @Column(name = "population_over_65")
    private Integer populationOver65;  // 125 (DV)
    
    // Образование демография (колони 126-132)
    @Column(name = "higher_education")
    private Integer higherEducation;  // 126
    
    @Column(name = "secondary_education")
    private Integer secondaryEducation;  // 127
    
    @Column(name = "primary_education")
    private Integer primaryEducation;  // 128
    
    @Column(name = "elementary_education")
    private Integer elementaryEducation;  // 129
    
    @Column(name = "no_education")
    private Integer noEducation;  // 130
    
    @Column(name = "literate")
    private Integer literate;  // 131
    
    @Column(name = "illiterate")
    private Integer illiterate;  // 132
    
    // Етнически състав (колони 167-170)
    @Column(name = "share_bulgarian")
    private Double shareBulgarian;  // 167
    
    @Column(name = "share_turkish")
    private Double shareTurkish;  // 168
    
    @Column(name = "share_roma")
    private Double shareRoma;  // 169
    
    @Column(name = "share_others")
    private Double shareOthers;  // 170
    
    // Икономика (колони 158-165)
    @Column(name = "unemployment_rate")
    private Double unemploymentRate;  // 158
    
    @Column(name = "unemployment_rate_15_29")
    private Double unemploymentRate1529;  // 159
    
    @Column(name = "gross_wage_monthly")
    private Double grossWageMonthly;  // 160
    
    @Column(name = "gross_value_added_per_person")
    private Double grossValueAddedPerPerson;  // 161
    
    @Column(name = "companies_number")
    private Integer companiesNumber;  // 162
    
    @Column(name = "companies_per_capita")
    private Double companiesPerCapita;  // 163
    
    @Column(name = "employment_rate")
    private Double employmentRate;  // 164
    
    @Column(name = "urban_population_percent")
    private Double urbanPopulationPercent;  // 165
    
    // Инфраструктура (колони 172, 173, 176, 177)
    @Column(name = "students_number")
    private Integer studentsNumber;  // 172 (FQ)
    
    @Column(name = "students_per_1000")
    private Double studentsPer1000;  // 173
    
    @Column(name = "kids_kindergartens")
    private Integer kidsKindergartens;  // 176 (FU)
    
    @Column(name = "hospitals")
    private Integer hospitals;  // 177
    
    // Други (колони 21, 155, 171)
    @Column(name = "n_chitalisha_munip")
    private Integer nChitalishaMunip;  // 21 (V)
    
    @Column(name = "unique_persons_employment")
    private Integer uniquePersonsEmployment;  // 155
    
    @Column(name = "migration_coefficient")
    private Double migrationCoefficient;  // 171
    
    // ФИНАНСОВИ ДАННИ НСИ 2022 (колони 147-152)
    @Column(name = "total_revenue_thousands", precision = 15, scale = 2)
    private BigDecimal totalRevenueThousands;  // 147 (ER)
    
    @Column(name = "revenue_from_subsidies_thousands", precision = 15, scale = 2)
    private BigDecimal revenueFromSubsidiesThousands;  // 148 (ES)
    
    @Column(name = "revenue_from_rent_thousands", precision = 15, scale = 2)
    private BigDecimal revenueFromRentThousands;  // 149 (ET)
    
    @Column(name = "total_expenses_thousands", precision = 15, scale = 2)
    private BigDecimal totalExpensesThousands;  // 150 (EU)
    
    @Column(name = "expenses_salaries_thousands", precision = 15, scale = 2)
    private BigDecimal expensesSalariesThousands;  // 151 (EV)
    
    @Column(name = "expenses_social_security_thousands", precision = 15, scale = 2)
    private BigDecimal expensesSocialSecurityThousands;  // 152 (EW)
    
    // ПЕРСОНАЛ ДАННИ НСИ 2022 (колони 137-146)
    @Column(name = "total_staff_count")
    private Integer totalStaffCount;  // 137 (EH)
    
    @Column(name = "staff_higher_education_count")
    private Integer staffHigherEducationCount;  // 138 (EI)
    
    @Column(name = "staff_secondary_education_count")
    private Integer staffSecondaryEducationCount;  // 139 (EJ)
    
    @Column(name = "secretaries_count")
    private Integer secretariesCount;  // 143 (EN)
    
    @Column(name = "secretaries_higher_education_count")
    private Integer secretariesHigherEducationCount;  // 144 (EO)
    
    // НАП ДАННИ 2023 (колони 154, 155)
    @Column(name = "average_insurance_income_td", precision = 10, scale = 2)
    private BigDecimal averageInsuranceIncomeTd;  // 154 (EY)
    
    @Column(name = "unique_employment_contracts")
    private Integer uniqueEmploymentContracts;  // 155 (EZ)
    
    // СУБСИДИИ 2025 (колони 156, 157)
    @Column(name = "subsidized_positions")
    private Integer subsidizedPositions;  // 156 (FA)
    
    @Column(name = "additional_positions")
    private Integer additionalPositions;  // 157 (FB)
    
    // Връзки
    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chitalishte> chitalishta = new ArrayList<>();
    
    @OneToOne(mappedBy = "municipality", cascade = CascadeType.ALL, orphanRemoval = true)
    private MunicipalityMetrics metrics;
    
    public void addChitalishte(Chitalishte chitalishte) {
        chitalishta.add(chitalishte);
        chitalishte.setMunicipality(this);
    }
}
