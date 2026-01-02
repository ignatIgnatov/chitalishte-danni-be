package bg.chitalishte.service;

import bg.chitalishte.dto.MunicipalityMetricsDTO;
import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.entity.ChitalishteYearData;
import bg.chitalishte.entity.Municipality;
import bg.chitalishte.entity.MunicipalityMetrics;
import bg.chitalishte.mapper.MunicipalityMetricsMapper;
import bg.chitalishte.repository.ChitalishteRepository;
import bg.chitalishte.repository.MunicipalityMetricsRepository;
import bg.chitalishte.repository.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MunicipalityMetricsService {

    private final MunicipalityRepository municipalityRepository;
    private final ChitalishteRepository chitalishteRepository;
    private final MunicipalityMetricsRepository metricsRepository;
    private final MunicipalityMetricsMapper municipalityMetricsMapper;

    private static final BigDecimal SUBSIDY_PER_POSITION = new BigDecimal("19555");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    @Transactional
    public void calculateAllMetrics() {
        log.info("🚀 Започване на изчисляване на показатели...");

        List<Municipality> municipalities = municipalityRepository.findAll();
        int successCount = 0;
        int updateCount = 0;
        int insertCount = 0;

        for (Municipality municipality : municipalities) {
            try {
                // Проверка дали вече съществува запис
                Optional<MunicipalityMetrics> existingMetrics =
                        metricsRepository.findByMunicipalityId(municipality.getId());

                MunicipalityMetrics metrics;

                if (existingMetrics.isPresent()) {
                    // UPDATE съществуващ запис
                    metrics = existingMetrics.get();
                    updateMetrics(metrics, municipality);
                    updateCount++;
                    log.debug("🔄 Актуализирани показатели за {}", municipality.getMunicipality());
                } else {
                    // INSERT нов запис
                    metrics = calculateMetricsForMunicipality(municipality);
                    insertCount++;
                    log.debug("✨ Създадени показатели за {}", municipality.getMunicipality());
                }

                metricsRepository.save(metrics);
                successCount++;

            } catch (Exception e) {
                log.error("❌ Грешка при изчисляване за {}: {}",
                        municipality.getMunicipality(), e.getMessage());
            }
        }

        log.info("✅ Изчислени показатели за {} общини (нови: {}, актуализирани: {})",
                successCount, insertCount, updateCount);
    }

    @Transactional(readOnly = true)
    public Optional<MunicipalityMetricsDTO> getMetrics(String municipalityCode) {
        log.info("Fetching metrics for municipality: {}", municipalityCode);

        return metricsRepository.findByMunicipalityMunicipalityCode(municipalityCode)
                .map(municipalityMetricsMapper::toDTO);
    }

    /**
     * Актуализира съществуващ MunicipalityMetrics запис
     */
    private void updateMetrics(MunicipalityMetrics metrics, Municipality municipality) {
        List<Chitalishte> chitalishta = chitalishteRepository
                .findByMunicipalityId(municipality.getId());

        // === ОСНОВНА ИНФОРМАЦИЯ ===
        metrics.setTotalChitalishta(chitalishta.size());

        long villageCount = chitalishta.stream()
                .filter(c -> "село".equalsIgnoreCase(c.getVillageCity()))
                .count();
        long cityCount = chitalishta.stream()
                .filter(c -> "град".equalsIgnoreCase(c.getVillageCity()))
                .count();

        metrics.setVillageChitalishta((int) villageCount);
        metrics.setCityChitalishta((int) cityCount);

        // Останалите изчисления са същите като в calculateMetricsForMunicipality
        calculateSubsidies(metrics, municipality);
        calculateRevenueAndExpenses(metrics, municipality);
        calculateStaff(metrics, municipality);
        calculateTraining(metrics, chitalishta);
        calculatePopulationMetrics(metrics, municipality, chitalishta);
    }

    /**
     * Създава нов MunicipalityMetrics запис
     */
    private MunicipalityMetrics calculateMetricsForMunicipality(Municipality municipality) {
        List<Chitalishte> chitalishta = chitalishteRepository
                .findByMunicipalityId(municipality.getId());

        MunicipalityMetrics metrics = MunicipalityMetrics.builder()
                .municipality(municipality)
                .build();

        // === ОСНОВНА ИНФОРМАЦИЯ ===
        metrics.setTotalChitalishta(chitalishta.size());

        long villageCount = chitalishta.stream()
                .filter(c -> "село".equalsIgnoreCase(c.getVillageCity()))
                .count();
        long cityCount = chitalishta.stream()
                .filter(c -> "град".equalsIgnoreCase(c.getVillageCity()))
                .count();

        metrics.setVillageChitalishta((int) villageCount);
        metrics.setCityChitalishta((int) cityCount);

        calculateSubsidies(metrics, municipality);
        calculateRevenueAndExpenses(metrics, municipality);
        calculateStaff(metrics, municipality);
        calculateTraining(metrics, chitalishta);
        calculatePopulationMetrics(metrics, municipality, chitalishta);

        return metrics;
    }

    private void calculateSubsidies(MunicipalityMetrics metrics, Municipality municipality) {
        // Държавна субсидия (FA × 19,555)
        if (municipality.getSubsidizedPositions() != null) {
            BigDecimal subsidy = BigDecimal.valueOf(municipality.getSubsidizedPositions())
                    .multiply(SUBSIDY_PER_POSITION);
            metrics.setStateSubsidyAmount(subsidy);

            // Държавна субсидия на човек
            if (municipality.getMunicipalityPopulation() != null &&
                    municipality.getMunicipalityPopulation() > 0) {
                BigDecimal perCapita = subsidy
                        .divide(BigDecimal.valueOf(municipality.getMunicipalityPopulation()),
                                2, RoundingMode.HALF_UP);
                metrics.setStateSubsidyPerCapita(perCapita);
            }
        }

        if (municipality.getAdditionalPositions() != null) {
            metrics.setAdditionalPositions(BigDecimal.valueOf(municipality.getAdditionalPositions()));
        }
    }

    private void calculateRevenueAndExpenses(MunicipalityMetrics metrics, Municipality municipality) {
        BigDecimal totalRevenue = municipality.getTotalRevenueThousands();
        BigDecimal totalExpenses = municipality.getTotalExpensesThousands();

        if (totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            // % приходи от субсидии
            if (municipality.getRevenueFromSubsidiesThousands() != null) {
                BigDecimal percent = municipality.getRevenueFromSubsidiesThousands()
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setRevenueFromSubsidiesPercent(percent);
            }

            // % приходи от наеми
            if (municipality.getRevenueFromRentThousands() != null) {
                BigDecimal percent = municipality.getRevenueFromRentThousands()
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setRevenueFromRentPercent(percent);
            }

            // % приходи от други
            if (municipality.getRevenueFromSubsidiesThousands() != null &&
                    municipality.getRevenueFromRentThousands() != null) {
                BigDecimal subsidiesAndRent = municipality.getRevenueFromSubsidiesThousands()
                        .add(municipality.getRevenueFromRentThousands());
                BigDecimal otherRevenue = totalRevenue.subtract(subsidiesAndRent);
                BigDecimal percent = otherRevenue
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setRevenueFromOtherPercent(percent);
            }
        }

        if (totalExpenses != null && totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            // % разходи за заплати
            if (municipality.getExpensesSalariesThousands() != null &&
                    municipality.getExpensesSocialSecurityThousands() != null) {
                BigDecimal salariesAndSocial = municipality.getExpensesSalariesThousands()
                        .add(municipality.getExpensesSocialSecurityThousands());
                BigDecimal percent = salariesAndSocial
                        .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setExpensesForSalariesPercent(percent);

                // % други разходи
                BigDecimal otherExpenses = totalExpenses.subtract(salariesAndSocial);
                BigDecimal percentOther = otherExpenses
                        .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setExpensesOtherPercent(percentOther);
            }
        }
    }

    private void calculateStaff(MunicipalityMetrics metrics, Municipality municipality) {
        metrics.setTotalStaff(municipality.getTotalStaffCount());
        metrics.setUniqueEmploymentContracts(municipality.getUniqueEmploymentContracts());

        if (municipality.getTotalStaffCount() != null && municipality.getTotalStaffCount() > 0) {
            // % с висше
            if (municipality.getStaffHigherEducationCount() != null) {
                BigDecimal percent = BigDecimal.valueOf(municipality.getStaffHigherEducationCount())
                        .divide(BigDecimal.valueOf(municipality.getTotalStaffCount()), 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setStaffHigherEducationPercent(percent);
            }

            // % със средно
            if (municipality.getStaffSecondaryEducationCount() != null) {
                BigDecimal percent = BigDecimal.valueOf(municipality.getStaffSecondaryEducationCount())
                        .divide(BigDecimal.valueOf(municipality.getTotalStaffCount()), 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setStaffSecondaryEducationPercent(percent);
            }
        }

        metrics.setSecretariesCount(municipality.getSecretariesCount());

        if (municipality.getSecretariesCount() != null && municipality.getSecretariesCount() > 0) {
            if (municipality.getSecretariesHigherEducationCount() != null) {
                BigDecimal percent = BigDecimal.valueOf(municipality.getSecretariesHigherEducationCount())
                        .divide(BigDecimal.valueOf(municipality.getSecretariesCount()), 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED)
                        .setScale(2, RoundingMode.HALF_UP);
                metrics.setSecretariesHigherEducationPercent(percent);
            }
        }

        metrics.setAverageInsuranceIncome(municipality.getAverageInsuranceIncomeTd());
    }

    private void calculateTraining(MunicipalityMetrics metrics, List<Chitalishte> chitalishta) {
        // % читалища без обучение (CX=0 / V × 100)
        if (chitalishta.size() > 0) {
            long noTrainingCount = chitalishta.stream()
                    .filter(c -> {
                        ChitalishteYearData latestData = c.getLatestYearData();
                        if (latestData == null) return false;
                        Integer training = latestData.getTrainingParticipation();
                        return training == null || training == 0;
                    })
                    .count();

            BigDecimal percent = BigDecimal.valueOf(noTrainingCount)
                    .divide(BigDecimal.valueOf(chitalishta.size()), 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
            metrics.setChitalishtaNoTrainingPercent(percent);
        }
    }

    private void calculatePopulationMetrics(MunicipalityMetrics metrics, Municipality municipality,
                                            List<Chitalishte> chitalishta) {
        // Читалища на 10,000 жители
        if (municipality.getMunicipalityPopulation() != null &&
                municipality.getMunicipalityPopulation() > 0) {
            BigDecimal chitalishtaPer10k = BigDecimal.valueOf(chitalishta.size())
                    .multiply(TEN_THOUSAND)
                    .divide(BigDecimal.valueOf(municipality.getMunicipalityPopulation()),
                            1, RoundingMode.HALF_UP);
            metrics.setChitalishtaPer10kResidents(chitalishtaPer10k);
        }

        // Читалища на 1,000 деца под 15
        if (municipality.getPopulationUnder15() != null &&
                municipality.getPopulationUnder15() > 0) {
            BigDecimal chitalishtaPer1kChildren = BigDecimal.valueOf(chitalishta.size())
                    .multiply(THOUSAND)
                    .divide(BigDecimal.valueOf(municipality.getPopulationUnder15()),
                            1, RoundingMode.HALF_UP);
            metrics.setChitalishtaPer1kChildrenUnder15(chitalishtaPer1kChildren);
        }

        // Читалища на 1,000 ученици
        if (municipality.getStudentsNumber() != null &&
                municipality.getStudentsNumber() > 0) {
            BigDecimal chitalishtaPer1kStudents = BigDecimal.valueOf(chitalishta.size())
                    .multiply(THOUSAND)
                    .divide(BigDecimal.valueOf(municipality.getStudentsNumber()),
                            1, RoundingMode.HALF_UP);
            metrics.setChitalishtaPer1kStudents(chitalishtaPer1kStudents);
        }

        // Читалища на 1,000 деца в детски градини
        if (municipality.getKidsKindergartens() != null &&
                municipality.getKidsKindergartens() > 0) {
            BigDecimal chitalishtaPer1kKindergarten = BigDecimal.valueOf(chitalishta.size())
                    .multiply(THOUSAND)
                    .divide(BigDecimal.valueOf(municipality.getKidsKindergartens()),
                            1, RoundingMode.HALF_UP);
            metrics.setChitalishtaPer1kKindergarten(chitalishtaPer1kKindergarten);
        }

        // Читалища на 1,000 жители 65+
        if (municipality.getPopulationOver65() != null &&
                municipality.getPopulationOver65() > 0) {
            BigDecimal chitalishtaPer1kElderly = BigDecimal.valueOf(chitalishta.size())
                    .multiply(THOUSAND)
                    .divide(BigDecimal.valueOf(municipality.getPopulationOver65()),
                            1, RoundingMode.HALF_UP);
            metrics.setChitalishtaPer1kElderly(chitalishtaPer1kElderly);
        }
    }
}