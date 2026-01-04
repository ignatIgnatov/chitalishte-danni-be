package bg.chitalishte.service;

import bg.chitalishte.entity.*;
import bg.chitalishte.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for importing chitalishte data from Excel file
 * Optimized with proper cache handling and no manual flush
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChitalishteImportService {

    private final MunicipalityRepository municipalityRepository;
    private final SettlementRepository settlementRepository;
    private final ChitalishteRepository chitalishteRepository;
    private final MunicipalityYearDataRepository municipalityYearDataRepository;
    private final ChitalishteYearDataRepository chitalishteYearDataRepository;
    private final SettlementAggregationService aggregationService;
    private final MunicipalityMetricsService metricsService;

    // Cache for municipalities and settlements to avoid repeated queries
    private final Map<String, Municipality> municipalityCache = new HashMap<>();
    private final Map<String, Settlement> settlementCache = new HashMap<>();
    // Cache for municipality year data to avoid importing same data multiple times
    private final Map<String, Boolean> municipalityYearDataCache = new HashMap<>();

    /**
     * Import data from Excel file
     * Returns statistics about the import process
     */
    @Transactional
    public Map<String, Integer> importFromExcel(InputStream inputStream) {
        log.info("=== STARTING DATA IMPORT FROM EXCEL ===");

        municipalityCache.clear();
        settlementCache.clear();
        municipalityYearDataCache.clear();

        int totalRows = 0;
        int successfulRows = 0;
        int errorRows = 0;
        int municipalitiesCreated = 0;
        int settlementsCreated = 0;
        int chitalishtaCreated = 0;
        int municipalityYearDataImported = 0;

        Workbook workbook = null;
        try {
            log.info("Creating workbook from input stream...");
            workbook = WorkbookFactory.create(inputStream);
            log.info("Workbook created successfully");

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            log.info("Sheet loaded. Total rows: {}", lastRowNum);

            // Skip header row
            log.info("Starting to process rows...");
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    log.debug("Row {} is null, skipping", i);
                    continue;
                }

                totalRows++;

                try {
                    boolean imported = processRow(row);
                    if (imported) {
                        municipalityYearDataImported++;
                    }
                    successfulRows++;

                    // Log progress every 50 rows (NO FLUSH!)
                    if (successfulRows % 50 == 0) {
                        log.info("Progress: processed {} / {} rows successfully", successfulRows, totalRows);
                    }
                } catch (Exception e) {
                    errorRows++;
                    log.error("Error processing row {}: {}", i, e.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debug("Full error for row {}", i, e);
                    }
                }
            }

            municipalitiesCreated = municipalityCache.size();
            settlementsCreated = settlementCache.size();
            chitalishtaCreated = (int) chitalishteRepository.count();

            log.info("=== IMPORT COMPLETED ===");
            log.info("Total rows: {}, Successful: {}, Errors: {}", totalRows, successfulRows, errorRows);
            log.info("Created/Updated - Municipalities: {}, Settlements: {}, Chitalishta: {}",
                    municipalitiesCreated, settlementsCreated, chitalishtaCreated);
            log.info("Municipality year data imported: {}", municipalityYearDataImported);

            // After import, aggregate settlement data and calculate metrics
            log.info("=== POST-PROCESSING STARTED ===");
            log.info("Aggregating settlement data to municipalities...");
            aggregationService.aggregateSettlementDataToMunicipalities();
            log.info("Settlement aggregation completed");

            log.info("Calculating municipality metrics...");
            calculateAllMetrics();
            log.info("Metrics calculation completed");
            log.info("=== POST-PROCESSING COMPLETED ===");

            // Return statistics
            Map<String, Integer> stats = new HashMap<>();
            stats.put("totalRows", totalRows);
            stats.put("successfulRows", successfulRows);
            stats.put("errorRows", errorRows);
            stats.put("municipalitiesCreated", municipalitiesCreated);
            stats.put("settlementsCreated", settlementsCreated);
            stats.put("chitalishtaCreated", chitalishtaCreated);
            stats.put("municipalityYearDataImported", municipalityYearDataImported);

            log.info("=== IMPORT STATISTICS ===");
            stats.forEach((key, value) -> log.info("{}: {}", key, value));

            return stats;

        } catch (Exception e) {
            log.error("=== FATAL ERROR DURING IMPORT ===", e);
            throw new RuntimeException("Failed to import data from Excel: " + e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.warn("Error closing workbook", e);
                }
            }
        }
    }

    /**
     * Process a single row from Excel
     * Returns true if municipality year data was imported
     */
    private boolean processRow(Row row) {
        // Extract basic identifiers
        String regN = getCellValue(row, 0);  // Column A: reg_n
        String year = getCellValue(row, 2);  // Column C: year

        if (regN == null || regN.trim().isEmpty()) {
            log.warn("Skipping row with empty reg_n");
            return false;
        }

        Integer yearInt = parseInteger(year);

        // Process municipality
        Municipality municipality = processOrGetMunicipality(row);

        // Process settlement
        Settlement settlement = processOrGetSettlement(row, municipality);

        // Process chitalishte (static data)
        Chitalishte chitalishte = processOrGetChitalishte(row, municipality, settlement);

        // Process chitalishte year data (if year is present)
        if (yearInt != null) {
            processChitalishteYearData(row, chitalishte, yearInt);
        }

        // Process municipality year data (if year is present)
        boolean imported = false;
        if (yearInt != null) {
            imported = processMunicipalityYearData(row, municipality, yearInt);
        }

        return imported;
    }

    /**
     * Process or retrieve municipality
     */
    private Municipality processOrGetMunicipality(Row row) {
        String municipalityCode = getCellValue(row, 12);  // Column M: municipality_code

        if (municipalityCode == null || municipalityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Municipality code is required");
        }

        // Check cache first
        if (municipalityCache.containsKey(municipalityCode)) {
            return municipalityCache.get(municipalityCode);
        }

        // Try to find in database
        Municipality municipality = municipalityRepository.findByMunicipalityCode(municipalityCode)
                .orElseGet(() -> {
                    log.info("Creating new municipality: {}", municipalityCode);
                    return Municipality.builder()
                            .municipalityCode(municipalityCode)
                            .build();
                });

        // Update municipality data (static data from Census 2021)
        municipality.setMunicipality(getCellValue(row, 4));        // Column E
        municipality.setMunicipalityNorm(getCellValue(row, 9));    // Column J
        municipality.setDistrict(getCellValue(row, 3));            // Column D
        municipality.setDistrictCode(getCellValue(row, 11));       // Column L
        municipality.setNuts1(getCellValue(row, 14));              // Column O
        municipality.setNuts2(getCellValue(row, 15));              // Column P
        municipality.setNuts3(getCellValue(row, 16));              // Column Q
        municipality.setMrrbCategory(getCellValue(row, 18));       // Column S
        municipality.setTotalChitalishta(parseInteger(getCellValue(row, 21)));  // Column V
        municipality.setMunicipalityPopulation(parseInteger(getCellValue(row, 122)));  // Column DS
        municipality.setShareBulgarian(parseDouble(getCellValue(row, 167)));   // Column FL
        municipality.setShareTurkish(parseDouble(getCellValue(row, 168)));     // Column FM
        municipality.setShareRoma(parseDouble(getCellValue(row, 169)));        // Column FN
        municipality.setShareOthers(parseDouble(getCellValue(row, 170)));      // Column FO
        municipality.setMigrationCoefficient(parseDouble(getCellValue(row, 171)));  // Column FP

        municipality = municipalityRepository.save(municipality);
        municipalityCache.put(municipalityCode, municipality);

        return municipality;
    }

    /**
     * Process or retrieve settlement
     */
    private Settlement processOrGetSettlement(Row row, Municipality municipality) {
        String ekatte = getCellValue(row, 17);  // Column R: ekatte

        if (ekatte == null || ekatte.trim().isEmpty()) {
            log.warn("Settlement EKATTE is empty, skipping settlement creation");
            return null;
        }

        // Check cache first
        if (settlementCache.containsKey(ekatte)) {
            return settlementCache.get(ekatte);
        }

        // Try to find in database
        Settlement settlement = settlementRepository.findByEkatte(ekatte)
                .orElseGet(() -> {
                    log.info("Creating new settlement: {}", ekatte);
                    return Settlement.builder()
                            .ekatte(ekatte)
                            .municipality(municipality)
                            .build();
                });

        // Update settlement data (Census 2021 data at settlement level)
        settlement.setSettlementNorm(getCellValue(row, 8));  // Column I
        settlement.setVillageCity(getCellValue(row, 10));    // Column K
        settlement.setSettlementPopulation(parseInteger(getCellValue(row, 121)));     // Column DR
        settlement.setPopulationUnder15(parseInteger(getCellValue(row, 123)));        // Column DT
        settlement.setPopulation1564(parseInteger(getCellValue(row, 124)));           // Column DU
        settlement.setPopulationOver65(parseInteger(getCellValue(row, 125)));         // Column DV
        settlement.setHigherEducation(parseInteger(getCellValue(row, 126)));          // Column DW
        settlement.setSecondaryEducation(parseInteger(getCellValue(row, 127)));       // Column DX
        settlement.setPrimaryEducation(parseInteger(getCellValue(row, 128)));         // Column DY
        settlement.setElementaryEducation(parseInteger(getCellValue(row, 129)));      // Column DZ
        settlement.setNoEducation(parseInteger(getCellValue(row, 130)));              // Column EA
        settlement.setLiterate(parseInteger(getCellValue(row, 131)));                 // Column EB
        settlement.setIlliterate(parseInteger(getCellValue(row, 132)));               // Column EC

        settlement = settlementRepository.save(settlement);
        settlementCache.put(ekatte, settlement);

        return settlement;
    }

    /**
     * Process or retrieve chitalishte
     */
    private Chitalishte processOrGetChitalishte(Row row, Municipality municipality, Settlement settlement) {
        String regN = getCellValue(row, 0);  // Column A: reg_n

        Chitalishte chitalishte = chitalishteRepository.findByRegN(regN)
                .orElseGet(() -> {
                    log.info("Creating new chitalishte: {}", regN);
                    return Chitalishte.builder()
                            .regN(regN)
                            .municipality(municipality)
                            .settlement(settlement)
                            .build();
                });

        // Update static chitalishte data
        chitalishte.setName(getCellValue(row, 1));                  // Column B
        chitalishte.setTown(getCellValue(row, 5));                  // Column F
        chitalishte.setAddress(getCellValue(row, 6));               // Column G
        chitalishte.setUic(getCellValue(row, 7));                   // Column H
        chitalishte.setSettlementNorm(getCellValue(row, 8));        // Column I
        chitalishte.setVillageCity(getCellValue(row, 10));          // Column K
        chitalishte.setMayoralityCode(getCellValue(row, 13));       // Column N
        chitalishte.setEkatteCode(getCellValue(row, 17));           // Column R
        chitalishte.setIsMunipCenter(getCellValue(row, 20));        // Column U
        chitalishte.setEmplCategory(getCellValue(row, 22));         // Column W
        chitalishte.setPhone(getCellValue(row, 64));                // Column CM
        chitalishte.setRegionalList(getCellValue(row, 72));         // Column DC
        chitalishte.setNationalList(getCellValue(row, 73));         // Column DD

        return chitalishteRepository.save(chitalishte);
    }

    /**
     * Process chitalishte year data
     */
    private void processChitalishteYearData(Row row, Chitalishte chitalishte, Integer year) {
        ChitalishteYearData yearData = chitalishteYearDataRepository
                .findByChitalishteRegNAndYear(chitalishte.getRegN(), year)
                .orElseGet(() -> ChitalishteYearData.builder()
                        .regN(chitalishte.getRegN())
                        .chitalishte(chitalishte)
                        .year(year)
                        .build());

        // Financial data from Commercial Register (columns X-BL)
        yearData.setTotalExpenditure(parseBigDecimal(getCellValue(row, 23)));      // Column X
        yearData.setAccumulatedProfit(parseBigDecimal(getCellValue(row, 24)));     // Column Y
        yearData.setProfit(parseBigDecimal(getCellValue(row, 25)));                // Column Z
        yearData.setOperatingIncome(parseBigDecimal(getCellValue(row, 26)));       // Column AA
        yearData.setTotalIncome(parseBigDecimal(getCellValue(row, 27)));           // Column AB
        yearData.setAccumulatedLoss(parseBigDecimal(getCellValue(row, 28)));       // Column AC
        yearData.setLoss(parseBigDecimal(getCellValue(row, 29)));                  // Column AD
        yearData.setExternalServicesSpending(parseBigDecimal(getCellValue(row, 30)));  // Column AE
        yearData.setIntangibleAssets(parseBigDecimal(getCellValue(row, 31)));      // Column AF
        yearData.setFixedAssets(parseBigDecimal(getCellValue(row, 32)));           // Column AG
        yearData.setMaterialReserves(parseBigDecimal(getCellValue(row, 33)));      // Column AH
        yearData.setReceivables(parseBigDecimal(getCellValue(row, 34)));           // Column AI
        yearData.setInvestment(parseBigDecimal(getCellValue(row, 35)));            // Column AJ
        yearData.setCash(parseBigDecimal(getCellValue(row, 36)));                  // Column AK
        yearData.setCurrentAssets(parseBigDecimal(getCellValue(row, 37)));         // Column AL
        yearData.setTotalAssets(parseBigDecimal(getCellValue(row, 38)));           // Column AM
        yearData.setEquity(parseBigDecimal(getCellValue(row, 39)));                // Column AN
        yearData.setLiabilities(parseBigDecimal(getCellValue(row, 40)));           // Column AO
        yearData.setShortTermLiabilities(parseBigDecimal(getCellValue(row, 41)));  // Column AP
        yearData.setLongTermLiabilities(parseBigDecimal(getCellValue(row, 42)));   // Column AQ

        // Financial ratios (columns AR-BL)
        yearData.setAverageAnnualStaff(parseBigDecimal(getCellValue(row, 43)));    // Column AR
        yearData.setNetIncome(parseBigDecimal(getCellValue(row, 44)));             // Column AS
        yearData.setStaffExpenses(parseBigDecimal(getCellValue(row, 45)));         // Column AT
        yearData.setTradePrice(parseBigDecimal(getCellValue(row, 46)));            // Column AU
        yearData.setIncomeProfitability(parseBigDecimal(getCellValue(row, 47)));   // Column AV
        yearData.setEquityProfitability(parseBigDecimal(getCellValue(row, 48)));   // Column AW
        yearData.setAssetProfitability(parseBigDecimal(getCellValue(row, 49)));    // Column AX
        yearData.setFinancialAutonomy(parseBigDecimal(getCellValue(row, 50)));     // Column AY
        yearData.setFinancialDebt(parseBigDecimal(getCellValue(row, 51)));         // Column AZ
        yearData.setShortTermLiquidity(parseBigDecimal(getCellValue(row, 52)));    // Column BA
        yearData.setFastLiquidity(parseBigDecimal(getCellValue(row, 53)));         // Column BB
        yearData.setImmediateLiquidity(parseBigDecimal(getCellValue(row, 54)));    // Column BC
        yearData.setAbsoluteLiquidity(parseBigDecimal(getCellValue(row, 55)));     // Column BD
        yearData.setTurnoverTime(parseBigDecimal(getCellValue(row, 56)));          // Column BE
        yearData.setTurnoverCount(parseBigDecimal(getCellValue(row, 57)));         // Column BF
        yearData.setDebtToTangibleAssets(parseBigDecimal(getCellValue(row, 58)));  // Column BG
        yearData.setAssetsPerStaff(parseBigDecimal(getCellValue(row, 59)));        // Column BH
        yearData.setLiabilitiesPerStaff(parseBigDecimal(getCellValue(row, 60)));   // Column BI
        yearData.setIncomePerStaff(parseBigDecimal(getCellValue(row, 61)));        // Column BJ
        yearData.setProfitPerStaff(parseBigDecimal(getCellValue(row, 62)));        // Column BK
        yearData.setStaffCount(parseInteger(getCellValue(row, 63)));               // Column BL

        // Registry data (columns BM-ED)
        yearData.setChairman(getCellValue(row, 64));                // Column BM
        yearData.setPhoneRegistry(getCellValue(row, 65));           // Column BN
        yearData.setSecretary(getCellValue(row, 66));               // Column BO
        yearData.setStatus(getCellValue(row, 67));                  // Column BP
        yearData.setTotalMembers(parseInteger(getCellValue(row, 68)));             // Column BQ
        yearData.setMembershipApplications(parseInteger(getCellValue(row, 69)));   // Column BR
        yearData.setNewMembers(parseInteger(getCellValue(row, 70)));               // Column BS
        yearData.setRejectedApplications(parseInteger(getCellValue(row, 71)));     // Column BT
        yearData.setLibraryActivity(getCellValue(row, 72));         // Column BU
        yearData.setArtClubs(parseInteger(getCellValue(row, 73)));                 // Column DE
        yearData.setArtClubsText(getCellValue(row, 74));            // Column DF
        yearData.setLanguageSchools(parseInteger(getCellValue(row, 75)));          // Column DG
        yearData.setLanguageSchoolsText(getCellValue(row, 76));     // Column DH
        yearData.setLocalHistoryClubs(parseInteger(getCellValue(row, 77)));        // Column DI
        yearData.setLocalHistoryClubsText(getCellValue(row, 78));   // Column DJ
        yearData.setMuseumCollections(parseInteger(getCellValue(row, 79)));        // Column DK
        yearData.setMuseumCollectionsText(getCellValue(row, 80));   // Column DL
        yearData.setFolkloreGroups(parseInteger(getCellValue(row, 81)));           // Column DM
        yearData.setTheaterGroups(parseInteger(getCellValue(row, 82)));            // Column DN
        yearData.setDanceGroups(parseInteger(getCellValue(row, 83)));              // Column DO
        yearData.setClassicalDanceGroups(parseInteger(getCellValue(row, 84)));     // Column DP
        yearData.setVocalGroups(parseInteger(getCellValue(row, 85)));              // Column DQ
        yearData.setOtherClubs(parseInteger(getCellValue(row, 86)));               // Column DR
        yearData.setEventParticipations(parseInteger(getCellValue(row, 87)));      // Column DS
        yearData.setIndependentProjects(parseInteger(getCellValue(row, 88)));      // Column DT
        yearData.setCollaborativeProjects(parseInteger(getCellValue(row, 89)));    // Column DU
        yearData.setDisabilityWork(getCellValue(row, 90));          // Column DV
        yearData.setOtherActivities(getCellValue(row, 91));         // Column DW
        yearData.setSubsidizedStaffCount(parseInteger(getCellValue(row, 92)));     // Column DX
        yearData.setTotalStaffRegistry(parseInteger(getCellValue(row, 93)));       // Column DY
        yearData.setStaffHigherEdu(parseInteger(getCellValue(row, 94)));           // Column DZ
        yearData.setSpecializedPositions(parseInteger(getCellValue(row, 95)));     // Column EA
        yearData.setAdministrativePositions(parseInteger(getCellValue(row, 96)));  // Column EB
        yearData.setSupportStaff(parseInteger(getCellValue(row, 97)));             // Column EC
        yearData.setTrainingParticipation(parseInteger(getCellValue(row, 101)));    // Column CX
        yearData.setImposedSanctions(parseInteger(getCellValue(row, 99)));         // Column ED

        // Library data (columns CZ-DQ: 103-120)
        yearData.setLibraryUsers(parseInteger(getCellValue(row, 103)));            // Column CZ
        yearData.setLibraryUsersOnline(parseInteger(getCellValue(row, 104)));      // Column DA
        yearData.setLibraryUnits(parseInteger(getCellValue(row, 105)));            // Column DB
        yearData.setNewlyAcquired(parseInteger(getCellValue(row, 106)));           // Column DC
        yearData.setNewlyAcquiredAlt(parseInteger(getCellValue(row, 107)));        // Column DD
        yearData.setBorrowedDocuments(parseInteger(getCellValue(row, 108)));       // Column DE
        yearData.setHomeVisits(parseInteger(getCellValue(row, 109)));              // Column DF
        yearData.setReadingRoomVisits(parseInteger(getCellValue(row, 110)));       // Column DG
        yearData.setInternetAccess(parseInteger(getCellValue(row, 111)));          // Column DH
        yearData.setComputerizedWorkstations(parseInteger(getCellValue(row, 112)));     // Column DI
        yearData.setComputerizedWorkstationsAlt(parseInteger(getCellValue(row, 113)));  // Column DJ
        yearData.setRegionalProjects(parseInteger(getCellValue(row, 114)));        // Column DK
        yearData.setNationalProjects(parseInteger(getCellValue(row, 115)));        // Column DL
        yearData.setInternationalProjects(parseInteger(getCellValue(row, 116)));   // Column DM
        yearData.setLibraryStaffTotal(parseInteger(getCellValue(row, 117)));       // Column DN
        yearData.setLibraryStaffHigherEdu(parseInteger(getCellValue(row, 118)));   // Column DO
        yearData.setLibraryStaffSecondaryEdu(parseInteger(getCellValue(row, 119)));     // Column DP
        yearData.setLibraryStaffTraining(parseInteger(getCellValue(row, 120)));    // Column DQ

        chitalishteYearDataRepository.save(yearData);
    }

    /**
     * Process municipality year data
     * NOTE: NSI data (columns EH-EW) is per MUNICIPALITY, not per chitalishte!
     * This data is repeated on every row in Excel, so we only import it ONCE per municipality+year combination
     * Returns true if data was imported, false if skipped (already in cache)
     */
    private boolean processMunicipalityYearData(Row row, Municipality municipality, Integer year) {
        // Create cache key: municipality_code + year
        String cacheKey = municipality.getMunicipalityCode() + "-" + year;

        // Check if we already imported this municipality+year combination
        if (municipalityYearDataCache.containsKey(cacheKey)) {
            // Already imported for this municipality and year - skip!
            log.debug("Municipality year data already imported for {} year {}, skipping",
                    municipality.getMunicipalityCode(), year);
            return false;
        }

        log.info("🔵 Importing municipality year data for {} year {}",
                municipality.getMunicipalityCode(), year);

        // Use municipality code directly in the entity (new composite key structure)
        MunicipalityYearData yearData = municipalityYearDataRepository
                .findByMunicipalityCodeAndYear(municipality.getMunicipalityCode(), year)
                .orElseGet(() -> MunicipalityYearData.builder()
                        .municipalityCode(municipality.getMunicipalityCode())  // Part of composite key
                        .municipality(municipality)  // Foreign key reference
                        .year(year)  // Part of composite key
                        .build());

        // NSI 2022 - Personnel data (columns EH-EO: 137-144)
        yearData.setTotalStaffCount(parseInteger(getCellValue(row, 137)));                     // Column EH
        yearData.setStaffHigherEducationCount(parseInteger(getCellValue(row, 138)));           // Column EI
        yearData.setStaffSecondaryEducationCount(parseInteger(getCellValue(row, 139)));        // Column EJ
        yearData.setSecretariesCount(parseInteger(getCellValue(row, 143)));                    // Column EN
        yearData.setSecretariesHigherEducationCount(parseInteger(getCellValue(row, 144)));     // Column EO

        // NSI 2022 - Financial data (columns ER-EW: 147-152)
        yearData.setTotalRevenueThousands(parseBigDecimal(getCellValue(row, 147)));            // Column ER
        yearData.setRevenueFromSubsidiesThousands(parseBigDecimal(getCellValue(row, 148)));    // Column ES
        yearData.setRevenueFromRentThousands(parseBigDecimal(getCellValue(row, 149)));         // Column ET
        yearData.setTotalExpensesThousands(parseBigDecimal(getCellValue(row, 150)));           // Column EU
        yearData.setExpensesSalariesThousands(parseBigDecimal(getCellValue(row, 151)));        // Column EV
        yearData.setExpensesSocialSecurityThousands(parseBigDecimal(getCellValue(row, 152)));  // Column EW

        // NAP 2023 data (columns EY-EZ: 154-155)
        yearData.setAverageInsuranceIncome(parseBigDecimal(getCellValue(row, 154)));           // Column EY
        yearData.setUniqueEmploymentContracts(parseInteger(getCellValue(row, 155)));           // Column EZ

        // Subsidies (columns FA-FB: 156-157)
        yearData.setSubsidizedPositions(parseInteger(getCellValue(row, 156)));                 // Column FA
        yearData.setAdditionalPositions(parseInteger(getCellValue(row, 157)));                 // Column FB

        // Economic indicators (columns FC-FJ: 158-165)
        yearData.setUnemploymentRate(parseDouble(getCellValue(row, 158)));                     // Column FC
        yearData.setUnemploymentRate1529(parseDouble(getCellValue(row, 159)));                 // Column FD
        yearData.setGrossWageMonthly(parseDouble(getCellValue(row, 160)));                     // Column FE
        yearData.setGrossValueAddedPerPerson(parseDouble(getCellValue(row, 161)));             // Column FF
        yearData.setCompaniesNumber(parseInteger(getCellValue(row, 162)));                     // Column FG
        yearData.setCompaniesPerCapita(parseDouble(getCellValue(row, 163)));                   // Column FH
        yearData.setEmploymentRate(parseDouble(getCellValue(row, 164)));                       // Column FI
        yearData.setUrbanPopulationPercent(parseDouble(getCellValue(row, 165)));               // Column FJ

        // Education and infrastructure (columns FQ-FW: 172-178)
        yearData.setStudentsNumber(parseInteger(getCellValue(row, 172)));                      // Column FQ
        yearData.setStudentsPer1000(parseDouble(getCellValue(row, 173)));                      // Column FR
        yearData.setKidsKindergartens(parseInteger(getCellValue(row, 176)));                   // Column FU
        yearData.setHospitals(parseInteger(getCellValue(row, 177)));                           // Column FV (only 2022)
        yearData.setPoorHealth(parseDouble(getCellValue(row, 178)));                           // Column FW (only 2021)

        municipalityYearDataRepository.save(yearData);

        // Add to cache to prevent re-importing the same municipality+year
        municipalityYearDataCache.put(cacheKey, true);
        log.info("✅ Saved and cached municipality year data for {}", cacheKey);

        return true;
    }

    /**
     * Calculate metrics for all municipalities
     */
    private void calculateAllMetrics() {
        List<Municipality> municipalities = municipalityRepository.findAll();
        int count = 0;

        for (Municipality municipality : municipalities) {
            try {
                metricsService.calculateAndSaveMetrics(municipality);
                count++;
            } catch (Exception e) {
                log.error("Error calculating metrics for municipality: {}",
                        municipality.getMunicipalityCode(), e);
            }
        }

        log.info("Calculated metrics for {} municipalities", count);
    }

    /**
     * Helper method to get cell value as string
     */
    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Parse integer from string
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    /**
     * Parse double from string
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse BigDecimal from string
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}