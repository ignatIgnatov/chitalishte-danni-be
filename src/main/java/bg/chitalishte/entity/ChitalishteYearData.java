package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Year-dependent data for chitalishte (commercial register, registry, library data)
 * Composite key: (reg_n, year)
 */
@Entity
@Table(name = "chitalishte_year_data")
@IdClass(ChitalishteYearDataId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChitalishteYearData {

    // ========== COMPOSITE KEY ==========

    @Id
    @Column(name = "reg_n", length = 50, nullable = false)
    private String regN;  // Part of composite key

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;  // Part of composite key

    // ========== FOREIGN KEY RELATIONSHIP ==========

    // Foreign key to chitalishte (not part of ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chitalishte_id", nullable = false)
    private Chitalishte chitalishte;

    // ========== COMMERCIAL REGISTER - FINANCIAL DATA (columns X-BL) ==========

    // Total expenditure
    // Column X: total_expenditure
    @Column(name = "total_expenditure", precision = 15, scale = 2)
    private BigDecimal totalExpenditure;

    // Accumulated profit
    // Column Y: accumulated_profit
    @Column(name = "accumulated_profit", precision = 15, scale = 2)
    private BigDecimal accumulatedProfit;

    // Profit
    // Column Z: profit
    @Column(name = "profit", precision = 15, scale = 2)
    private BigDecimal profit;

    // Operating income
    // Column AA: operating_income
    @Column(name = "operating_income", precision = 15, scale = 2)
    private BigDecimal operatingIncome;

    // Total income
    // Column AB: total_income
    @Column(name = "total_income", precision = 15, scale = 2)
    private BigDecimal totalIncome;

    // Accumulated loss
    // Column AC: accumulated_loss
    @Column(name = "accumulated_loss", precision = 15, scale = 2)
    private BigDecimal accumulatedLoss;

    // Loss
    // Column AD: loss
    @Column(name = "loss", precision = 15, scale = 2)
    private BigDecimal loss;

    // External services spending
    // Column AE: external_services_spending
    @Column(name = "external_services_spending", precision = 15, scale = 2)
    private BigDecimal externalServicesSpending;

    // Intangible assets
    // Column AF: intangible_assets
    @Column(name = "intangible_assets", precision = 15, scale = 2)
    private BigDecimal intangibleAssets;

    // Fixed assets
    // Column AG: fixed_assets
    @Column(name = "fixed_assets", precision = 15, scale = 2)
    private BigDecimal fixedAssets;

    // Material reserves
    // Column AH: material_reserves
    @Column(name = "material_reserves", precision = 15, scale = 2)
    private BigDecimal materialReserves;

    // Receivables
    // Column AI: receivables
    @Column(name = "receivables", precision = 15, scale = 2)
    private BigDecimal receivables;

    // Investment
    // Column AJ: investment
    @Column(name = "investment", precision = 15, scale = 2)
    private BigDecimal investment;

    // Cash
    // Column AK: cash
    @Column(name = "cash", precision = 15, scale = 2)
    private BigDecimal cash;

    // Current assets
    // Column AL: current_assets
    @Column(name = "current_assets", precision = 15, scale = 2)
    private BigDecimal currentAssets;

    // Total assets
    // Column AM: total_assets
    @Column(name = "total_assets", precision = 15, scale = 2)
    private BigDecimal totalAssets;

    // Equity
    // Column AN: equity
    @Column(name = "equity", precision = 15, scale = 2)
    private BigDecimal equity;

    // Liabilities
    // Column AO: liabilities
    @Column(name = "liabilities", precision = 15, scale = 2)
    private BigDecimal liabilities;

    // Short term liabilities
    // Column AP: short_term_liabilities
    @Column(name = "short_term_liabilities", precision = 15, scale = 2)
    private BigDecimal shortTermLiabilities;

    // Long term liabilities
    // Column AQ: long_term_liabilities
    @Column(name = "long_term_liabilities", precision = 15, scale = 2)
    private BigDecimal longTermLiabilities;

    // ========== FINANCIAL RATIOS (columns AR-BL) ==========

    // Average annual staff
    // Column AR: average_annual_staff
    @Column(name = "average_annual_staff", precision = 10, scale = 2)
    private BigDecimal averageAnnualStaff;

    // Net income
    // Column AS: net_income
    @Column(name = "net_income", precision = 15, scale = 2)
    private BigDecimal netIncome;

    // Staff expenses
    // Column AT: staff_expenses
    @Column(name = "staff_expenses", precision = 15, scale = 2)
    private BigDecimal staffExpenses;

    // Trade price
    // Column AU: trade_price
    @Column(name = "trade_price", precision = 15, scale = 2)
    private BigDecimal tradePrice;

    // Income profitability
    // Column AV: income_profitability
    @Column(name = "income_profitability", precision = 10, scale = 2)
    private BigDecimal incomeProfitability;

    // Equity profitability
    // Column AW: equity_profitability
    @Column(name = "equity_profitability", precision = 10, scale = 2)
    private BigDecimal equityProfitability;

    // Asset profitability
    // Column AX: asset_profitability
    @Column(name = "asset_profitability", precision = 10, scale = 2)
    private BigDecimal assetProfitability;

    // Financial autonomy
    // Column AY: financial_autonomy
    @Column(name = "financial_autonomy", precision = 10, scale = 2)
    private BigDecimal financialAutonomy;

    // Financial debt
    // Column AZ: financial_debt
    @Column(name = "financial_debt", precision = 10, scale = 2)
    private BigDecimal financialDebt;

    // Short term liquidity
    // Column BA: short_term_liquidity
    @Column(name = "short_term_liquidity", precision = 10, scale = 2)
    private BigDecimal shortTermLiquidity;

    // Fast liquidity
    // Column BB: fast_liquidity
    @Column(name = "fast_liquidity", precision = 10, scale = 2)
    private BigDecimal fastLiquidity;

    // Immediate liquidity
    // Column BC: immediate_liquidity
    @Column(name = "immediate_liquidity", precision = 10, scale = 2)
    private BigDecimal immediateLiquidity;

    // Absolute liquidity
    // Column BD: absolute_liquidity
    @Column(name = "absolute_liquidity", precision = 10, scale = 2)
    private BigDecimal absoluteLiquidity;

    // Turnover time
    // Column BE: turnover_time
    @Column(name = "turnover_time", precision = 10, scale = 2)
    private BigDecimal turnoverTime;

    // Turnover count
    // Column BF: turnover_count
    @Column(name = "turnover_count", precision = 10, scale = 2)
    private BigDecimal turnoverCount;

    // Debt to tangible assets
    // Column BG: debt_to_tangible_assets
    @Column(name = "debt_to_tangible_assets", precision = 10, scale = 2)
    private BigDecimal debtToTangibleAssets;

    // Assets per staff
    // Column BH: assets_per_staff
    @Column(name = "assets_per_staff", precision = 15, scale = 2)
    private BigDecimal assetsPerStaff;

    // Liabilities per staff
    // Column BI: liabilities_per_staff
    @Column(name = "liabilities_per_staff", precision = 15, scale = 2)
    private BigDecimal liabilitiesPerStaff;

    // Income per staff
    // Column BJ: income_per_staff
    @Column(name = "income_per_staff", precision = 15, scale = 2)
    private BigDecimal incomePerStaff;

    // Profit per staff
    // Column BK: profit_per_staff
    @Column(name = "profit_per_staff", precision = 15, scale = 2)
    private BigDecimal profitPerStaff;

    // Staff count
    // Column BL: staff_count
    @Column(name = "staff_count")
    private Integer staffCount;

    // ========== CHITALISHTE REGISTRY DATA (columns BM-ED) ==========

    // Chairman
    // Column BM: chairman
    @Column(name = "chairman", columnDefinition = "TEXT")
    private String chairman;

    // Phone (registry)
    // Column BN: phone_registry
    @Column(name = "phone_registry", columnDefinition = "TEXT")
    private String phoneRegistry;

    // Secretary
    // Column BO: secretary
    @Column(name = "secretary", columnDefinition = "TEXT")
    private String secretary;

    // Status
    // Column BP: status
    @Column(name = "status", length = 100)
    private String status;

    // Total members
    // Column BQ: total_members
    @Column(name = "total_members")
    private Integer totalMembers;

    // Membership applications
    // Column BR: membership_applications
    @Column(name = "membership_applications")
    private Integer membershipApplications;

    // New members
    // Column BS: new_members
    @Column(name = "new_members")
    private Integer newMembers;

    // Rejected applications
    // Column BT: rejected_applications
    @Column(name = "rejected_applications")
    private Integer rejectedApplications;

    // Library activity
    // Column BU: library_activity
    @Column(name = "library_activity", columnDefinition = "TEXT")
    private String libraryActivity;

    // Art clubs count
    // Column DE: art_clubs
    @Column(name = "art_clubs")
    private Integer artClubs;

    // Art clubs description
    // Column DF: art_clubs_text
    @Column(name = "art_clubs_text", columnDefinition = "TEXT")
    private String artClubsText;

    // Language schools count
    // Column DG: language_schools
    @Column(name = "language_schools")
    private Integer languageSchools;

    // Language schools description
    // Column DH: language_schools_text
    @Column(name = "language_schools_text", columnDefinition = "TEXT")
    private String languageSchoolsText;

    // Local history clubs count
    // Column DI: local_history_clubs
    @Column(name = "local_history_clubs")
    private Integer localHistoryClubs;

    // Local history clubs description
    // Column DJ: local_history_clubs_text
    @Column(name = "local_history_clubs_text", columnDefinition = "TEXT")
    private String localHistoryClubsText;

    // Museum collections count
    // Column DK: museum_collections
    @Column(name = "museum_collections")
    private Integer museumCollections;

    // Museum collections description
    // Column DL: museum_collections_text
    @Column(name = "museum_collections_text", columnDefinition = "TEXT")
    private String museumCollectionsText;

    // Folklore groups count
    // Column DM: folklore_groups
    @Column(name = "folklore_groups")
    private Integer folkloreGroups;

    // Theater groups count
    // Column DN: theater_groups
    @Column(name = "theater_groups")
    private Integer theaterGroups;

    // Dance groups count
    // Column DO: dance_groups
    @Column(name = "dance_groups")
    private Integer danceGroups;

    // Classical dance groups count
    // Column DP: classical_dance_groups
    @Column(name = "classical_dance_groups")
    private Integer classicalDanceGroups;

    // Vocal groups count
    // Column DQ: vocal_groups
    @Column(name = "vocal_groups")
    private Integer vocalGroups;

    // Other clubs count
    // Column DR: other_clubs
    @Column(name = "other_clubs")
    private Integer otherClubs;

    // Event participations count
    // Column DS: event_participations
    @Column(name = "event_participations")
    private Integer eventParticipations;

    // Independent projects count
    // Column DT: independent_projects
    @Column(name = "independent_projects")
    private Integer independentProjects;

    // Collaborative projects count
    // Column DU: collaborative_projects
    @Column(name = "collaborative_projects")
    private Integer collaborativeProjects;

    // Disability work description
    // Column DV: disability_work
    @Column(name = "disability_work", columnDefinition = "TEXT")
    private String disabilityWork;

    // Other activities description
    // Column DW: other_activities
    @Column(name = "other_activities", columnDefinition = "TEXT")
    private String otherActivities;

    // Subsidized staff count
    // Column DX: subsidized_staff_count
    @Column(name = "subsidized_staff_count")
    private Integer subsidizedStaffCount;

    // Total staff (registry)
    // Column DY: total_staff_registry
    @Column(name = "total_staff_registry")
    private Integer totalStaffRegistry;

    // Staff with higher education
    // Column DZ: staff_higher_edu
    @Column(name = "staff_higher_edu")
    private Integer staffHigherEdu;

    // Specialized positions count
    // Column EA: specialized_positions
    @Column(name = "specialized_positions")
    private Integer specializedPositions;

    // Administrative positions count
    // Column EB: administrative_positions
    @Column(name = "administrative_positions")
    private Integer administrativePositions;

    // Support staff count
    // Column EC: support_staff
    @Column(name = "support_staff")
    private Integer supportStaff;

    // Training participation count
    // Column CX: training_participation (NOTE: CX not EC!)
    @Column(name = "training_participation")
    private Integer trainingParticipation;

    // Imposed sanctions count
    // Column ED: imposed_sanctions
    @Column(name = "imposed_sanctions")
    private Integer imposedSanctions;

    // ========== LIBRARY DATA (columns EE-EV) ==========

    // Library users count
    // Column EE: library_users
    @Column(name = "library_users")
    private Integer libraryUsers;

    // Library users online count
    // Column EF: library_users_online
    @Column(name = "library_users_online")
    private Integer libraryUsersOnline;

    // Library units count
    // Column EG: library_units
    @Column(name = "library_units")
    private Integer libraryUnits;

    // Newly acquired items count
    // Column EH: newly_acquired
    @Column(name = "newly_acquired")
    private Integer newlyAcquired;

    // Newly acquired items count (alternative)
    // Column EI: newly_acquired_alt
    @Column(name = "newly_acquired_alt")
    private Integer newlyAcquiredAlt;

    // Borrowed documents count
    // Column EJ: borrowed_documents
    @Column(name = "borrowed_documents")
    private Integer borrowedDocuments;

    // Home visits count
    // Column EK: home_visits
    @Column(name = "home_visits")
    private Integer homeVisits;

    // Reading room visits count
    // Column EL: reading_room_visits
    @Column(name = "reading_room_visits")
    private Integer readingRoomVisits;

    // Internet access count
    // Column EM: internet_access
    @Column(name = "internet_access")
    private Integer internetAccess;

    // Computerized workstations count
    // Column EN: computerized_workstations
    @Column(name = "computerized_workstations")
    private Integer computerizedWorkstations;

    // Computerized workstations count (alternative)
    // Column EO: computerized_workstations_alt
    @Column(name = "computerized_workstations_alt")
    private Integer computerizedWorkstationsAlt;

    // Regional projects count
    // Column EP: regional_projects
    @Column(name = "regional_projects")
    private Integer regionalProjects;

    // National projects count
    // Column EQ: national_projects
    @Column(name = "national_projects")
    private Integer nationalProjects;

    // International projects count
    // Column ER: international_projects
    @Column(name = "international_projects")
    private Integer internationalProjects;

    // Library staff total count
    // Column ES: library_staff_total
    @Column(name = "library_staff_total")
    private Integer libraryStaffTotal;

    // Library staff with higher education count
    // Column ET: library_staff_higher_edu
    @Column(name = "library_staff_higher_edu")
    private Integer libraryStaffHigherEdu;

    // Library staff with secondary education count
    // Column EU: library_staff_secondary_edu
    @Column(name = "library_staff_secondary_edu")
    private Integer libraryStaffSecondaryEdu;

    // Library staff training participation count
    // Column EV: library_staff_training
    @Column(name = "library_staff_training")
    private Integer libraryStaffTraining;
}