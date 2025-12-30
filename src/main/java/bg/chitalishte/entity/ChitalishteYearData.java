package bg.chitalishte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "chitalishte_year_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chitalishte_id", "year"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChitalishteYearData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Връзка към читалище
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chitalishte_id", nullable = false)
    private Chitalishte chitalishte;

    // Година (колона 2)
    @Column(name = "year", nullable = false)
    private Integer year;

    // === РЪКОВОДСТВО (колони 64, 66, 67) ===
    @Column(name = "chairman", length = 200)
    private String chairman;  // 64

    @Column(name = "secretary", length = 200)
    private String secretary;  // 66

    @Column(name = "status", length = 50)
    private String status;  // 67

    // === ЧЛЕНСТВО (колони 68-71) ===
    @Column(name = "total_members")
    private Integer totalMembers;  // 68

    @Column(name = "submitted_applications")
    private Integer submittedApplications;  // 69

    @Column(name = "newly_accepted_members")
    private Integer newlyAcceptedMembers;  // 70

    @Column(name = "rejected_applications")
    private Integer rejectedApplications;  // 71

    // === БИБЛИОТЕЧНА ДЕЙНОСТ (колона 72) ===
    @Column(name = "library_activity", columnDefinition = "TEXT")
    private String libraryActivity;  // 72

    // === ДЕЙНОСТИ И КЛУБОВЕ (колони 75-93) ===
    @Column(name = "art_clubs")
    private Integer artClubs;  // 75

    @Column(name = "art_clubs_text", columnDefinition = "TEXT")
    private String artClubsText;  // 76

    @Column(name = "language_schools")
    private Integer languageSchools;  // 77

    @Column(name = "language_schools_text", columnDefinition = "TEXT")
    private String languageSchoolsText;  // 78

    @Column(name = "local_history_clubs")
    private Integer localHistoryClubs;  // 79

    @Column(name = "local_history_clubs_text", columnDefinition = "TEXT")
    private String localHistoryClubsText;  // 80

    @Column(name = "museum_collections")
    private Integer museumCollections;  // 81

    @Column(name = "museum_collections_text", columnDefinition = "TEXT")
    private String museumCollectionsText;  // 82

    @Column(name = "folklore_groups")
    private Integer folkloreGroups;  // 83

    @Column(name = "theater_groups")
    private Integer theaterGroups;  // 84

    @Column(name = "dance_groups")
    private Integer danceGroups;  // 85

    @Column(name = "classical_modern_groups")
    private Integer classicalModernGroups;  // 86

    @Column(name = "vocal_groups")
    private Integer vocalGroups;  // 87

    @Column(name = "other_clubs")
    private Integer otherClubs;  // 88

    @Column(name = "event_participation")
    private Integer eventParticipation;  // 89

    @Column(name = "projects_independent")
    private Integer projectsIndependent;  // 90

    @Column(name = "projects_cooperation")
    private Integer projectsCooperation;  // 91

    @Column(name = "work_with_disabilities", columnDefinition = "TEXT")
    private String workWithDisabilities;  // 92

    @Column(name = "other_activities", columnDefinition = "TEXT")
    private String otherActivities;  // 93

    // === ПЕРСОНАЛ НА ЧИТАЛИЩЕТО (колони 94-102) ===
    @Column(name = "subsidized_staff", precision = 10, scale = 2)
    private BigDecimal subsidizedStaff;  // 94
    // колона 95 е празна
    @Column(name = "total_staff")
    private Integer totalStaff;  // 96 - Общ персонал

    @Column(name = "specialists_higher_education")
    private Integer specialistsHigherEducation;  // 97 - Специалисти с висше

    @Column(name = "specialized_positions")
    private Integer specializedPositions;  // 98 - Специализирани длъжности

    @Column(name = "administrative_positions")
    private Integer administrativePositions;  // 99 - Административни длъжности

    @Column(name = "auxiliary_staff")
    private Integer auxiliaryStaff;  // 100 - Помощен персонал

    @Column(name = "training_participation")
    private Integer trainingParticipation;  // 101 - Участие в обучения (CX!)

    @Column(name = "sanctions_imposed")
    private Integer sanctionsImposed;  // 102 - Наложени санкции

    // === БИБЛИОТЕКА (колони 103-113) ===
    @Column(name = "library_users")
    private Integer libraryUsers;  // 103

    @Column(name = "library_users_o")
    private Integer libraryUsersO;  // 104

    @Column(name = "library_units")
    private Integer libraryUnits;  // 105

    @Column(name = "newly_acquired")
    private Integer newlyAcquired;  // 106

    @Column(name = "newly_acquired_1")
    private Integer newlyAcquired1;  // 107

    @Column(name = "borrowed_documents")
    private Integer borrowedDocuments;  // 108

    @Column(name = "home_visits")
    private Integer homeVisits;  // 109

    @Column(name = "reading_room_visits")
    private Integer readingRoomVisits;  // 110

    @Column(name = "internet_access_education", length = 50)
    private String internetAccessEducation;  // 111

    @Column(name = "computerized_workplaces")
    private Integer computerizedWorkplaces;  // 112

    @Column(name = "computerized_workplaces_2")
    private Integer computerizedWorkplaces2;  // 113

    // === ПРОЕКТИ (колони 114-116) ===
    @Column(name = "project_participation_regional")
    private Integer projectParticipationRegional;  // 114

    @Column(name = "project_participation_national")
    private Integer projectParticipationNational;  // 115

    @Column(name = "project_participation_international")
    private Integer projectParticipationInternational;  // 116

    // === ЩАТНИ БРОЙКИ (колони 117-120) ===
    @Column(name = "staff_positions_total")
    private Integer staffPositionsTotal;  // 117

    @Column(name = "staff_positions_higher_education")
    private Integer staffPositionsHigherEducation;  // 118

    @Column(name = "staff_positions_secondary_education")
    private Integer staffPositionsSecondaryEducation;  // 119

    @Column(name = "staff_qualification_participation")
    private Integer staffQualificationParticipation;  // 120

    // === НАСТОЯТЕЛСТВО (колони 133-136) ===
    @Column(name = "board_members_total")
    private Integer boardMembersTotal;  // 133

    @Column(name = "board_members_higher_ed")
    private Integer boardMembersHigherEd;  // 134

    @Column(name = "board_members_secondary_ed")
    private Integer boardMembersSecondaryEd;  // 135

    @Column(name = "board_members_primary_ed")
    private Integer boardMembersPrimaryEd;  // 136

    // === ОБЩ ПЕРСОНАЛ, БРОЙ (колони 137-146) ===
    @Column(name = "staff_total")
    private Integer staffTotal;  // 137 - Общо персонал, брой

    @Column(name = "staff_higher_ed")
    private Integer staffHigherEd;  // 138 - Персонал с висше

    @Column(name = "staff_secondary_ed")
    private Integer staffSecondaryEd;  // 139 - Персонал със средно

    @Column(name = "staff_primary_ed")
    private Integer staffPrimaryEd;  // 140 - Персонал с основно

    @Column(name = "staff_employment_contract")
    private Integer staffEmploymentContract;  // 141 - Персонал с трудов договор

    @Column(name = "staff_civil_contract")
    private Integer staffCivilContract;  // 142 - Персонал с граждански договор

    // === СЕКРЕТАРИ (колони 143-146) ===
    @Column(name = "secretaries_total")
    private Integer secretariesTotal;  // 143

    @Column(name = "secretaries_higher_ed")
    private Integer secretariesHigherEd;  // 144

    @Column(name = "secretaries_secondary_ed")
    private Integer secretariesSecondaryEd;  // 145

    @Column(name = "secretaries_primary_ed")
    private Integer secretariesPrimaryEd;  // 146

    // === ФИНАНСОВИ ДАННИ (колони 147-152) ===
    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;  // 147

    @Column(name = "revenue_subsidies", precision = 15, scale = 2)
    private BigDecimal revenueSubsidies;  // 148

    @Column(name = "revenue_rent", precision = 15, scale = 2)
    private BigDecimal revenueRent;  // 149

    @Column(name = "total_expenses", precision = 15, scale = 2)
    private BigDecimal totalExpenses;  // 150

    @Column(name = "expenses_salaries", precision = 15, scale = 2)
    private BigDecimal expensesSalaries;  // 151

    @Column(name = "expenses_social_security", precision = 15, scale = 2)
    private BigDecimal expensesSocialSecurity;  // 152

    @Column(name = "employment_contracts_count")
    private Integer employmentContractsCount;  // 153

    @Column(name = "average_insurance_income", precision = 10, scale = 2)
    private BigDecimal averageInsuranceIncome;  // 154

    @Column(name = "total_subsidized_positions")
    private Integer totalSubsidizedPositions;  // 156

    @Column(name = "additional_positions", precision = 10, scale = 2)
    private BigDecimal additionalPositions;  // 157

    // === F-ФОРМУЛЯРИ ФИНАНСОВИ ПОКАЗАТЕЛИ (колони 23-63) ===
    @Column(name = "f13000_1_total_expenditure", precision = 15, scale = 2)
    private BigDecimal f130001TotalExpenditure;  // 23

    @Column(name = "f14100_1_acc_profit", precision = 15, scale = 2)
    private BigDecimal f141001AccProfit;  // 24

    @Column(name = "f14400_1_pofit", precision = 15, scale = 2)
    private BigDecimal f144001Pofit;  // 25

    @Column(name = "f15000_1_operating_income", precision = 15, scale = 2)
    private BigDecimal f150001OperatingIncome;  // 26

    @Column(name = "f18000_1_total_income", precision = 15, scale = 2)
    private BigDecimal f180001TotalIncome;  // 27

    @Column(name = "f19100_1acc_loss", precision = 15, scale = 2)
    private BigDecimal f191001accLoss;  // 28

    @Column(name = "f19200_1_loss", precision = 15, scale = 2)
    private BigDecimal f192001zLoss;  // 29

    @Column(name = "f31000_ext_services_spending", precision = 15, scale = 2)
    private BigDecimal f31000ExtServicesSpending;  // 30

    @Column(name = "f02100_1_nontangible_assets", precision = 15, scale = 2)
    private BigDecimal f021001NontangibleAssets;  // 31

    @Column(name = "f02000_1_fixed_assets", precision = 15, scale = 2)
    private BigDecimal f020001FixedAssets;  // 32

    @Column(name = "f03100_1_material_reserves", precision = 15, scale = 2)
    private BigDecimal f031001MaterialReserves;  // 33

    @Column(name = "f03200_1_receivables", precision = 15, scale = 2)
    private BigDecimal f032001Receivables;  // 34

    @Column(name = "f03300_1_investment", precision = 15, scale = 2)
    private BigDecimal f033001Investment;  // 35

    @Column(name = "f03400_1_bankroll", precision = 15, scale = 2)
    private BigDecimal f034001Bankroll;  // 36

    @Column(name = "f03000_1_current_assets", precision = 15, scale = 2)
    private BigDecimal f030001CurrentAssets;  // 37

    @Column(name = "f04500_1_total_assets", precision = 15, scale = 2)
    private BigDecimal f045001TotalAssets;  // 38

    @Column(name = "f05000_1_own_capital", precision = 15, scale = 2)
    private BigDecimal f050001OwnCapital;  // 39

    @Column(name = "f07000_1_obligations", precision = 15, scale = 2)
    private BigDecimal f070001Obligations;  // 40

    @Column(name = "f07001_1_shortterm_obligations", precision = 15, scale = 2)
    private BigDecimal f070011ShorttermObligations;  // 41

    @Column(name = "f07002_1_longterm_obligations", precision = 15, scale = 2)
    private BigDecimal f070021LongtermObligations;  // 42

    @Column(name = "average_annual_staff", precision = 10, scale = 2)
    private BigDecimal averageAnnualStaff;  // 43

    @Column(name = "net_income", precision = 15, scale = 2)
    private BigDecimal netIncome;  // 44

    @Column(name = "razhodi_personal", precision = 15, scale = 2)
    private BigDecimal razhodiPersonal;  // 45

    @Column(name = "trade_price", precision = 15, scale = 2)
    private BigDecimal tradePrice;  // 46

    @Column(name = "income_profit", precision = 15, scale = 2)
    private BigDecimal incomeProfit;  // 47

    @Column(name = "equity_profit", precision = 15, scale = 2)
    private BigDecimal equityProfit;  // 48

    @Column(name = "asset_profit", precision = 15, scale = 2)
    private BigDecimal assetProfit;  // 49

    @Column(name = "financial_autonomy", precision = 15, scale = 2)
    private BigDecimal financialAutonomy;  // 50

    @Column(name = "financial_debt", precision = 15, scale = 2)
    private BigDecimal financialDebt;  // 51

    @Column(name = "short_term_liquidity", precision = 15, scale = 2)
    private BigDecimal shortTermLiquidity;  // 52

    @Column(name = "fast_liquidity", precision = 15, scale = 2)
    private BigDecimal fastLiquidity;  // 53

    @Column(name = "immediate_liquidity", precision = 15, scale = 2)
    private BigDecimal immediateLiquidity;  // 54

    @Column(name = "absolute_liquidity", precision = 15, scale = 2)
    private BigDecimal absoluteLiquidity;  // 55

    @Column(name = "vreme_oborot", precision = 15, scale = 2)
    private BigDecimal vremeOborot;  // 56

    @Column(name = "br_ob", precision = 15, scale = 2)
    private BigDecimal brOb;  // 57

    @Column(name = "zkma", length = 50)
    private String zkma;  // 58

    @Column(name = "aktivi_personal", precision = 15, scale = 2)
    private BigDecimal aktiviPersonal;  // 59

    @Column(name = "zadaljenia_pers", precision = 15, scale = 2)
    private BigDecimal zadaljeniaPerс;  // 60

    @Column(name = "prihodi_pers", precision = 15, scale = 2)
    private BigDecimal prihodiPers;  // 61

    @Column(name = "pechalba_pers", precision = 15, scale = 2)
    private BigDecimal pechalbaPerс;  // 62

    @Column(name = "personal", precision = 15, scale = 2)
    private BigDecimal personal;  // 63

    // === ДРУГИ (колони 19, 166, 174, 175, 178) ===
    @Column(name = "payment_standard", length = 50)
    private String paymentStandard;  // 19

    @Column(name = "matriculation_bel_26", precision = 10, scale = 2)
    private BigDecimal matriculationBel26;  // 166

    @Column(name = "nvo_mat", precision = 10, scale = 2)
    private BigDecimal nvoMat;  // 174

    @Column(name = "nvo_bel", precision = 10, scale = 2)
    private BigDecimal nvoBel;  // 175

    @Column(name = "poor_health")
    private Integer poorHealth;  // 178
}