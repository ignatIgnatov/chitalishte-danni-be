package bg.chitalishte.service;

import bg.chitalishte.dto.MunicipalityMetricsDTO;
import bg.chitalishte.entity.Municipality;
import bg.chitalishte.entity.MunicipalityMetrics;
import bg.chitalishte.entity.MunicipalityYearData;
import bg.chitalishte.mapper.MunicipalityMetricsMapper;
import bg.chitalishte.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Service for calculating and managing municipality metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MunicipalityMetricsService {

    private final MunicipalityMetricsRepository metricsRepository;
    private final MunicipalityYearDataRepository yearDataRepository;
    private final ChitalishteRepository chitalishteRepository;
    private final ChitalishteYearDataRepository chitalishteYearDataRepository;
    private final MunicipalityRepository municipalityRepository;
    private final MunicipalityMetricsMapper metricsMapper;

    private static final BigDecimal SUBSIDY_PER_POSITION = new BigDecimal("19555");
    private static final int REFERENCE_YEAR_NSI = 2022;
    private static final int REFERENCE_YEAR_NAP = 2023;
    private static final int REFERENCE_YEAR_REGISTRY = 2023;

    /**
     * Calculate and save metrics for a municipality
     */
    @Transactional
    public MunicipalityMetrics calculateAndSaveMetrics(Municipality municipality) {
        log.info("Calculating metrics for municipality: {}", municipality.getMunicipalityCode());

        MunicipalityMetrics metrics = metricsRepository.findByMunicipality(municipality)
                .orElse(MunicipalityMetrics.builder()
                        .municipality(municipality)
                        .build());

        // Get year data for calculations
        MunicipalityYearData nsiData = getYearData(municipality, REFERENCE_YEAR_NSI);
        MunicipalityYearData napData = getYearData(municipality, REFERENCE_YEAR_NAP);
        MunicipalityYearData subsidyData = getLatestYearData(municipality);

        // Calculate basic information
        calculateBasicInfo(metrics, municipality, subsidyData);

        // Calculate revenue and expenses
        calculateRevenueAndExpenses(metrics, nsiData);

        // Calculate personnel metrics
        calculatePersonnelMetrics(metrics, nsiData, napData);

        // Calculate population-based metrics
        calculatePopulationMetrics(metrics, municipality, subsidyData);

        MunicipalityMetrics saved = metricsRepository.save(metrics);
        log.info("Metrics calculated and saved for municipality: {}", municipality.getMunicipalityCode());

        return saved;
    }

    /**
     * Calculate metrics for all municipalities
     */
    @Transactional
    public void calculateAllMetrics() {
        log.info("Starting calculation of metrics for all municipalities");

        List<Municipality> municipalities = municipalityRepository.findAll();
        int successCount = 0;
        int errorCount = 0;

        for (Municipality municipality : municipalities) {
            try {
                calculateAndSaveMetrics(municipality);
                successCount++;

                if (successCount % 50 == 0) {
                    log.info("Calculated metrics for {} municipalities", successCount);
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Error calculating metrics for municipality: {}",
                        municipality.getMunicipalityCode(), e);
            }
        }

        log.info("Metrics calculation completed: success={}, errors={}, total={}",
                successCount, errorCount, municipalities.size());
    }

    /**
     * Get metrics DTO for municipality by code
     */
    @Transactional(readOnly = true)
    public Optional<MunicipalityMetricsDTO> getMetrics(String municipalityCode) {
        log.info("Fetching metrics for municipality: {}", municipalityCode);

        return metricsRepository.findByMunicipalityCode(municipalityCode)
                .map(metricsMapper::toDTO);
    }

    /**
     * Calculate basic information (6 indicators)
     */
    private void calculateBasicInfo(MunicipalityMetrics metrics, Municipality municipality, MunicipalityYearData subsidyData) {
        String municipalityCode = municipality.getMunicipalityCode();

        // Total chitalishta
        metrics.setTotalChitalishta(municipality.getTotalChitalishta());

        // Village chitalishta
        Long village = chitalishteRepository.countVillageChitalishta(municipalityCode);
        metrics.setVillageChitalishta(village.intValue());

        // City chitalishta
        Long city = chitalishteRepository.countCityChitalishta(municipalityCode);
        metrics.setCityChitalishta(city.intValue());

        if (subsidyData != null && subsidyData.getSubsidizedPositions() != null) {
            // State subsidy amount: FA × 19,555
            BigDecimal subsidyAmount = new BigDecimal(subsidyData.getSubsidizedPositions())
                    .multiply(SUBSIDY_PER_POSITION);
            metrics.setStateSubsidyAmount(subsidyAmount);

            // State subsidy per capita: (FA × 19,555) / DS
            if (municipality.getMunicipalityPopulation() != null && municipality.getMunicipalityPopulation() > 0) {
                BigDecimal perCapita = subsidyAmount
                        .divide(new BigDecimal(municipality.getMunicipalityPopulation()), 2, RoundingMode.HALF_UP);
                metrics.setStateSubsidyPerCapita(perCapita);
            }

            // Additional positions
            metrics.setAdditionalPositions(subsidyData.getAdditionalPositions());
        }
    }

    /**
     * Calculate revenue and expenses (5 indicators)
     */
    private void calculateRevenueAndExpenses(MunicipalityMetrics metrics, MunicipalityYearData nsiData) {
        if (nsiData == null) {
            return;
        }

        BigDecimal totalRevenue = nsiData.getTotalRevenueThousands();
        BigDecimal subsidyRevenue = nsiData.getRevenueFromSubsidiesThousands();
        BigDecimal rentRevenue = nsiData.getRevenueFromRentThousands();
        BigDecimal totalExpenses = nsiData.getTotalExpensesThousands();
        BigDecimal salariesExpenses = nsiData.getExpensesSalariesThousands();
        BigDecimal socialSecurityExpenses = nsiData.getExpensesSocialSecurityThousands();

        if (totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            // Revenue from subsidies %: (ES / ER) × 100
            if (subsidyRevenue != null) {
                BigDecimal percent = subsidyRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                metrics.setRevenueFromSubsidiesPercent(percent.setScale(2, RoundingMode.HALF_UP));
            }

            // Revenue from rent %: (ET / ER) × 100
            if (rentRevenue != null) {
                BigDecimal percent = rentRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                metrics.setRevenueFromRentPercent(percent.setScale(2, RoundingMode.HALF_UP));
            }

            // Revenue from other %: ((ER - (ES + ET)) / ER) × 100
            if (subsidyRevenue != null && rentRevenue != null) {
                BigDecimal otherRevenue = totalRevenue.subtract(subsidyRevenue).subtract(rentRevenue);
                BigDecimal percent = otherRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                metrics.setRevenueFromOtherPercent(percent.setScale(2, RoundingMode.HALF_UP));
            }
        }

        if (totalExpenses != null && totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            // Expenses for salaries %: ((EV + EW) / EU) × 100
            if (salariesExpenses != null && socialSecurityExpenses != null) {
                BigDecimal totalSalaries = salariesExpenses.add(socialSecurityExpenses);
                BigDecimal percent = totalSalaries.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                metrics.setExpensesForSalariesPercent(percent.setScale(2, RoundingMode.HALF_UP));
            }

            // Other expenses %: ((EU - (EV + EW)) / EU) × 100
            if (salariesExpenses != null && socialSecurityExpenses != null) {
                BigDecimal totalSalaries = salariesExpenses.add(socialSecurityExpenses);
                BigDecimal otherExpenses = totalExpenses.subtract(totalSalaries);
                BigDecimal percent = otherExpenses.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                metrics.setExpensesOtherPercent(percent.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * Calculate personnel metrics (8 indicators)
     */
    private void calculatePersonnelMetrics(MunicipalityMetrics metrics, MunicipalityYearData nsiData, MunicipalityYearData napData) {
        if (nsiData == null) {
            return;
        }

        // Total staff (NSI 2022)
        metrics.setTotalStaff(nsiData.getTotalStaffCount());

        Integer totalStaff = nsiData.getTotalStaffCount();
        Integer higherEdu = nsiData.getStaffHigherEducationCount();
        Integer secondaryEdu = nsiData.getStaffSecondaryEducationCount();
        Integer secretaries = nsiData.getSecretariesCount();
        Integer secretariesHigherEdu = nsiData.getSecretariesHigherEducationCount();

        // Staff higher education %: (EI / EH) × 100
        if (totalStaff != null && totalStaff > 0 && higherEdu != null) {
            BigDecimal percent = new BigDecimal(higherEdu)
                    .divide(new BigDecimal(totalStaff), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            metrics.setStaffHigherEducationPercent(percent.setScale(2, RoundingMode.HALF_UP));
        }

        // Staff secondary education %: (EJ / EH) × 100
        if (totalStaff != null && totalStaff > 0 && secondaryEdu != null) {
            BigDecimal percent = new BigDecimal(secondaryEdu)
                    .divide(new BigDecimal(totalStaff), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            metrics.setStaffSecondaryEducationPercent(percent.setScale(2, RoundingMode.HALF_UP));
        }

        // Secretaries count
        metrics.setSecretariesCount(secretaries);

        // Secretaries higher education %: (EO / EN) × 100
        if (secretaries != null && secretaries > 0 && secretariesHigherEdu != null) {
            BigDecimal percent = new BigDecimal(secretariesHigherEdu)
                    .divide(new BigDecimal(secretaries), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            metrics.setSecretariesHigherEducationPercent(percent.setScale(2, RoundingMode.HALF_UP));
        }

        // NAP 2023 data
        if (napData != null) {
            metrics.setAverageInsuranceIncome(napData.getAverageInsuranceIncome());
            metrics.setUniqueEmploymentContracts(napData.getUniqueEmploymentContracts());
        }

        // Chitalishta with no training %: (COUNT where CX=0 / V) × 100
        Long noTraining = chitalishteYearDataRepository.countChitalishtaWithNoTraining(
                metrics.getMunicipality().getMunicipalityCode(), REFERENCE_YEAR_REGISTRY);
        Integer totalChitalishta = metrics.getTotalChitalishta();

        if (totalChitalishta != null && totalChitalishta > 0) {
            BigDecimal percent = new BigDecimal(noTraining)
                    .divide(new BigDecimal(totalChitalishta), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            metrics.setChitalishtaNoTrainingPercent(percent.setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * Calculate population-based metrics (5 indicators)
     */
    private void calculatePopulationMetrics(MunicipalityMetrics metrics, Municipality municipality, MunicipalityYearData yearData) {
        Integer totalChitalishta = metrics.getTotalChitalishta();
        Integer population = municipality.getMunicipalityPopulation();

        if (totalChitalishta == null || totalChitalishta == 0) {
            return;
        }

        BigDecimal chitalishtaCount = new BigDecimal(totalChitalishta);

        // Chitalishta per 10k residents: (V / DS) × 10,000
        if (population != null && population > 0) {
            BigDecimal per10k = chitalishtaCount
                    .divide(new BigDecimal(population), 5, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("10000"));
            metrics.setChitalishtaPer10kResidents(per10k.setScale(1, RoundingMode.HALF_UP));
        }

        // Chitalishta per 1k children under 15: (V / DT) × 1,000
        Integer childrenUnder15 = municipality.getPopulationUnder15Aggregate();
        if (childrenUnder15 != null && childrenUnder15 > 0) {
            BigDecimal per1k = chitalishtaCount
                    .divide(new BigDecimal(childrenUnder15), 5, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("1000"));
            metrics.setChitalishtaPer1kChildrenUnder15(per1k.setScale(1, RoundingMode.HALF_UP));
        }

        // Chitalishta per 1k elderly (65+): (V / DV) × 1,000
        Integer elderly = municipality.getPopulationOver65Aggregate();
        if (elderly != null && elderly > 0) {
            BigDecimal per1k = chitalishtaCount
                    .divide(new BigDecimal(elderly), 5, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("1000"));
            metrics.setChitalishtaPer1kElderly(per1k.setScale(1, RoundingMode.HALF_UP));
        }

        if (yearData != null) {
            // Chitalishta per 1k students: (V / FU) × 1,000
            if (yearData.getStudentsNumber() != null && yearData.getStudentsNumber() > 0) {
                BigDecimal per1k = chitalishtaCount
                        .divide(new BigDecimal(yearData.getStudentsNumber()), 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("1000"));
                metrics.setChitalishtaPer1kStudents(per1k.setScale(1, RoundingMode.HALF_UP));
            }

            // Chitalishta per 1k kindergarten: (V / FY) × 1,000
            if (yearData.getKidsKindergartens() != null && yearData.getKidsKindergartens() > 0) {
                BigDecimal per1k = chitalishtaCount
                        .divide(new BigDecimal(yearData.getKidsKindergartens()), 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("1000"));
                metrics.setChitalishtaPer1kKindergarten(per1k.setScale(1, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * Get year data for specific year
     */
    private MunicipalityYearData getYearData(Municipality municipality, int year) {
        return yearDataRepository.findByMunicipalityCodeAndYear(
                municipality.getMunicipalityCode(), year).orElse(null);
    }

    /**
     * Get latest year data
     */
    private MunicipalityYearData getLatestYearData(Municipality municipality) {
        return yearDataRepository.findLatestByMunicipalityCode(
                        municipality.getMunicipalityCode()).stream()
                .findFirst()
                .orElse(null);
    }
}