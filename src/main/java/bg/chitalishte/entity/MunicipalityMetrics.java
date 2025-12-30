package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

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
    
    @Column(name = "total_chitalishta")
    private Integer totalChitalishta;
    
    @Column(name = "village_chitalishta")
    private Integer villageChitalishta;
    
    @Column(name = "city_chitalishta")
    private Integer cityChitalishta;
    
    @Column(name = "state_subsidy_amount", precision = 15, scale = 2)
    private BigDecimal stateSubsidyAmount;  // FA × 19,555
    
    @Column(name = "state_subsidy_per_capita", precision = 10, scale = 2)
    private BigDecimal stateSubsidyPerCapita;  // (FA × 19,555) / DS
    
    @Column(name = "additional_positions")
    private BigDecimal additionalPositions;  // FB
    
    // ========== ПРИХОДИ И РАЗХОДИ (5 показателя) ==========
    
    @Column(name = "revenue_from_subsidies_percent", precision = 5, scale = 2)
    private BigDecimal revenueFromSubsidiesPercent;  // (ES / ER) × 100
    
    @Column(name = "revenue_from_rent_percent", precision = 5, scale = 2)
    private BigDecimal revenueFromRentPercent;  // (ET / ER) × 100
    
    @Column(name = "revenue_from_other_percent", precision = 5, scale = 2)
    private BigDecimal revenueFromOtherPercent;  // ((ER-(ES+ET))/ER)×100
    
    @Column(name = "expenses_for_salaries_percent", precision = 5, scale = 2)
    private BigDecimal expensesForSalariesPercent;  // ((EV+EW)/EU)×100
    
    @Column(name = "expenses_other_percent", precision = 5, scale = 2)
    private BigDecimal expensesOtherPercent;  // ((EU-(EV+EW))/EU)×100
    
    // ========== ПЕРСОНАЛ (8 показателя) ==========
    
    @Column(name = "total_staff")
    private Integer totalStaff;  // EH
    
    @Column(name = "unique_employment_contracts")
    private Integer uniqueEmploymentContracts;  // EZ
    
    @Column(name = "staff_higher_education_percent", precision = 5, scale = 2)
    private BigDecimal staffHigherEducationPercent;  // (EI/EH)×100
    
    @Column(name = "staff_secondary_education_percent", precision = 5, scale = 2)
    private BigDecimal staffSecondaryEducationPercent;  // (EJ/EH)×100
    
    @Column(name = "secretaries_count")
    private Integer secretariesCount;  // EN
    
    @Column(name = "secretaries_higher_education_percent", precision = 5, scale = 2)
    private BigDecimal secretariesHigherEducationPercent;  // (EO/EN)×100
    
    @Column(name = "average_insurance_income", precision = 10, scale = 2)
    private BigDecimal averageInsuranceIncome;  // EY
    
    @Column(name = "chitalishta_no_training_percent", precision = 5, scale = 2)
    private BigDecimal chitalishtaNoTrainingPercent;  // (CX=0/V)×100
    
    // ========== ПО НАСЕЛЕНИЕ (5 показателя) ==========
    
    @Column(name = "chitalishta_per_10k_residents", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer10kResidents;  // (V/DS)×10,000
    
    @Column(name = "chitalishta_per_1k_children_under_15", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kChildrenUnder15;  // (V/DT)×1,000
    
    @Column(name = "chitalishta_per_1k_students", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kStudents;  // (V/FQ)×1,000
    
    @Column(name = "chitalishta_per_1k_kindergarten", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kKindergarten;  // (V/FU)×1,000
    
    @Column(name = "chitalishta_per_1k_elderly", precision = 10, scale = 1)
    private BigDecimal chitalishtaPer1kElderly;  // (V/DV)×1,000
}
