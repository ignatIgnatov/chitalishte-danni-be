package bg.chitalishte.service;

import bg.chitalishte.entity.Chitalishte;
import bg.chitalishte.entity.ChitalishteYearData;
import bg.chitalishte.entity.Municipality;
import bg.chitalishte.repository.ChitalishteRepository;
import bg.chitalishte.repository.ChitalishteYearDataRepository;
import bg.chitalishte.repository.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChitalishteImportService {

    private final ChitalishteRepository chitalishteRepository;
    private final MunicipalityRepository municipalityRepository;
    private final ChitalishteYearDataRepository yearDataRepository;
    private final MunicipalityMetricsService metricsService;

    @Transactional
    public Map<String, Integer> importFromExcel(MultipartFile file, boolean clearExisting) throws Exception {
        log.info("🚀 Започване на импорт от Excel файл: {}", file.getOriginalFilename());

        if (clearExisting) {
            log.warn("⚠️ Изтриване на съществуващи данни...");
            yearDataRepository.deleteAll();
            chitalishteRepository.deleteAll();
            municipalityRepository.deleteAll();
            log.info("✅ Старите данни са изтрити");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // skip header
            }

            // Cache структури
            Map<String, Municipality> municipalityCache = new HashMap<>();
            Map<String, Chitalishte> chitalishteCache = new HashMap<>();
            List<ChitalishteYearData> allYearData = new ArrayList<>();

            int newMunicipalities = 0;
            int updatedMunicipalities = 0;
            int newChitalishta = 0;
            int updatedChitalishta = 0;

            // Parse всички редове
            int rowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowCount++;

                if (rowCount % 1000 == 0) {
                    log.info("📊 Обработени {} реда...", rowCount);
                }

                try {
                    // Извличане на ключови полета
                    String municipalityCode = getStringValue(row, 12);
                    String regN = getStringValue(row, 0);
                    Integer year = getIntegerValue(row, 2);

                    if (municipalityCode == null || regN == null || year == null) {
                        log.warn("⚠️ Пропускане на ред {} - липсват задължителни полета", rowCount);
                        continue;
                    }

                    // Вземи/създай/актуализирай община
                    Municipality municipality;
                    if (municipalityCache.containsKey(municipalityCode)) {
                        municipality = municipalityCache.get(municipalityCode);
                    } else {
                        Optional<Municipality> existing = municipalityRepository
                                .findByMunicipalityCode(municipalityCode);

                        if (existing.isPresent()) {
                            municipality = existing.get();
                            updateMunicipalityFromRow(municipality, row);
                            updatedMunicipalities++;
                            log.debug("🔄 Актуализирана община: {}", municipalityCode);
                        } else {
                            municipality = parseMunicipality(row);
                            newMunicipalities++;
                            log.debug("✨ Нова община: {}", municipalityCode);
                        }
                        municipalityCache.put(municipalityCode, municipality);
                    }

                    // Вземи/създай/актуализирай читалище
                    Chitalishte chitalishte;
                    if (chitalishteCache.containsKey(regN)) {
                        chitalishte = chitalishteCache.get(regN);
                    } else {
                        Optional<Chitalishte> existing = chitalishteRepository.findByRegN(regN);

                        if (existing.isPresent()) {
                            chitalishte = existing.get();
                            updateChitalishteFromRow(chitalishte, row, municipality);
                            updatedChitalishta++;
                            log.debug("🔄 Актуализирано читалище: {}", regN);
                        } else {
                            chitalishte = parseChitalishte(row, municipality);
                            newChitalishta++;
                            log.debug("✨ Ново читалище: {}", regN);
                        }
                        chitalishteCache.put(regN, chitalishte);
                    }

                    // Създай/актуализирай годишни данни
                    Optional<ChitalishteYearData> existingYearData =
                            yearDataRepository.findByChitalishteRegNAndYear(regN, year);

                    ChitalishteYearData yearData;
                    if (existingYearData.isPresent()) {
                        yearData = existingYearData.get();
                        updateYearDataFromRow(yearData, row, year);
                        log.debug("🔄 Актуализирани данни за {} - {}", regN, year);
                    } else {
                        yearData = parseYearData(row, chitalishte, year);
                        log.debug("✨ Нови данни за {} - {}", regN, year);
                    }
                    allYearData.add(yearData);

                } catch (Exception e) {
                    log.error("❌ Грешка при обработка на ред {}: {}", rowCount, e.getMessage());
                }
            }

            // Записване в базата
            log.info("💾 Записване на {} общини (нови: {}, актуализирани: {})...",
                    municipalityCache.size(), newMunicipalities, updatedMunicipalities);
            List<Municipality> municipalities = new ArrayList<>(municipalityCache.values());
            municipalityRepository.saveAll(municipalities);

            log.info("💾 Записване на {} читалища (нови: {}, актуализирани: {})...",
                    chitalishteCache.size(), newChitalishta, updatedChitalishta);
            List<Chitalishte> chitalishta = new ArrayList<>(chitalishteCache.values());
            chitalishteRepository.saveAll(chitalishta);

            log.info("💾 Записване на {} годишни записа...", allYearData.size());
            yearDataRepository.saveAll(allYearData);

            // Изчисляване на показатели
            log.info("📊 Изчисляване на показатели за общините...");
            metricsService.calculateAllMetrics();

            log.info("✅ Успешен импорт! Общини: {} ({} нови, {} актуализирани), " +
                            "Читалища: {} ({} нови, {} актуализирани), Годишни данни: {}",
                    municipalities.size(), newMunicipalities, updatedMunicipalities,
                    chitalishta.size(), newChitalishta, updatedChitalishta,
                    allYearData.size());

            Map<String, Integer> result = new HashMap<>();
            result.put("municipalities", municipalities.size());
            result.put("newMunicipalities", newMunicipalities);
            result.put("updatedMunicipalities", updatedMunicipalities);
            result.put("chitalishta", chitalishta.size());
            result.put("newChitalishta", newChitalishta);
            result.put("updatedChitalishta", updatedChitalishta);
            result.put("yearData", allYearData.size());
            return result;

        } catch (Exception e) {
            log.error("❌ Грешка при импорт: {}", e.getMessage(), e);
            throw new Exception("Грешка при импорт на данни: " + e.getMessage(), e);
        }
    }

    /**
     * Актуализира Municipality от Excel ред
     */
    private void updateMunicipalityFromRow(Municipality municipality, Row row) {
        municipality.setMunicipality(getStringValue(row, 4));
        municipality.setMunicipalityNorm(getStringValue(row, 9));
        municipality.setDistrict(getStringValue(row, 3));
        municipality.setDistrictCode(getStringValue(row, 11));
        municipality.setNuts1(getStringValue(row, 14));
        municipality.setNuts2(getStringValue(row, 15));
        municipality.setNuts3(getStringValue(row, 16));
        municipality.setMrrbCategory(getStringValue(row, 18));
        municipality.setSettlementPopulation(getIntegerValue(row, 121));
        municipality.setMunicipalityPopulation(getIntegerValue(row, 122));
        municipality.setTotalPopulation2021(getIntegerValue(row, 122));
        municipality.setPopulationUnder15(getIntegerValue(row, 123));
        municipality.setPopulation1564(getIntegerValue(row, 124));
        municipality.setPopulationOver65(getIntegerValue(row, 125));
        municipality.setHigherEducation(getIntegerValue(row, 126));
        municipality.setSecondaryEducation(getIntegerValue(row, 127));
        municipality.setPrimaryEducation(getIntegerValue(row, 128));
        municipality.setElementaryEducation(getIntegerValue(row, 129));
        municipality.setNoEducation(getIntegerValue(row, 130));
        municipality.setLiterate(getIntegerValue(row, 131));
        municipality.setIlliterate(getIntegerValue(row, 132));
        municipality.setShareBulgarian(getDoubleValue(row, 167));
        municipality.setShareTurkish(getDoubleValue(row, 168));
        municipality.setShareRoma(getDoubleValue(row, 169));
        municipality.setShareOthers(getDoubleValue(row, 170));
        municipality.setUnemploymentRate(getDoubleValue(row, 158));
        municipality.setUnemploymentRate1529(getDoubleValue(row, 159));
        municipality.setGrossWageMonthly(getDoubleValue(row, 160));
        municipality.setGrossValueAddedPerPerson(getDoubleValue(row, 161));
        municipality.setCompaniesNumber(getIntegerValue(row, 162));
        municipality.setCompaniesPerCapita(getDoubleValue(row, 163));
        municipality.setEmploymentRate(getDoubleValue(row, 164));
        municipality.setUrbanPopulationPercent(getDoubleValue(row, 165));
        municipality.setStudentsNumber(getIntegerValue(row, 172));
        municipality.setStudentsPer1000(getDoubleValue(row, 173));
        municipality.setKidsKindergartens(getIntegerValue(row, 176));
        municipality.setHospitals(getIntegerValue(row, 177));
        municipality.setNChitalishaMunip(getIntegerValue(row, 21));
        municipality.setUniquePersonsEmployment(getIntegerValue(row, 155));
        municipality.setMigrationCoefficient(getDoubleValue(row, 171));
        municipality.setTotalRevenueThousands(getBigDecimalValue(row, 147));
        municipality.setRevenueFromSubsidiesThousands(getBigDecimalValue(row, 148));
        municipality.setRevenueFromRentThousands(getBigDecimalValue(row, 149));
        municipality.setTotalExpensesThousands(getBigDecimalValue(row, 150));
        municipality.setExpensesSalariesThousands(getBigDecimalValue(row, 151));
        municipality.setExpensesSocialSecurityThousands(getBigDecimalValue(row, 152));
        municipality.setTotalStaffCount(getIntegerValue(row, 137));
        municipality.setStaffHigherEducationCount(getIntegerValue(row, 138));
        municipality.setStaffSecondaryEducationCount(getIntegerValue(row, 139));
        municipality.setSecretariesCount(getIntegerValue(row, 143));
        municipality.setSecretariesHigherEducationCount(getIntegerValue(row, 144));
        municipality.setAverageInsuranceIncomeTd(getBigDecimalValue(row, 154));
        municipality.setUniqueEmploymentContracts(getIntegerValue(row, 155));
        municipality.setSubsidizedPositions(getIntegerValue(row, 156));
        municipality.setAdditionalPositions(getIntegerValue(row, 157));
    }

    /**
     * Актуализира Chitalishte от Excel ред
     */
    private void updateChitalishteFromRow(Chitalishte chitalishte, Row row, Municipality municipality) {
        chitalishte.setMunicipality(municipality);
        chitalishte.setName(getStringValue(row, 1));
        chitalishte.setTown(getStringValue(row, 5));
        chitalishte.setAddress(getStringValue(row, 6));
        chitalishte.setUic(getStringValue(row, 7));
        chitalishte.setPhone(getStringValue(row, 65));
        chitalishte.setSettlementNorm(getStringValue(row, 8));
        chitalishte.setVillageCity(getStringValue(row, 10));
        chitalishte.setMayoralityCode(getStringValue(row, 13));
        chitalishte.setEkatte(getStringValue(row, 17));
        chitalishte.setIsMunipCenter(getStringValue(row, 20));
        chitalishte.setEmplCategory(getStringValue(row, 22));
        chitalishte.setRegionalList(getStringValue(row, 73));
        chitalishte.setNationalList(getStringValue(row, 74));
    }

    /**
     * Актуализира ChitalishteYearData от Excel ред
     */
    private void updateYearDataFromRow(ChitalishteYearData yearData, Row row, Integer year) {
        yearData.setYear(year);
        yearData.setChairman(getStringValue(row, 64));
        yearData.setSecretary(getStringValue(row, 66));
        yearData.setStatus(getStringValue(row, 67));
        yearData.setTotalMembers(getIntegerValue(row, 68));
        yearData.setSubmittedApplications(getIntegerValue(row, 69));
        yearData.setNewlyAcceptedMembers(getIntegerValue(row, 70));
        yearData.setRejectedApplications(getIntegerValue(row, 71));
        yearData.setLibraryActivity(getStringValue(row, 72));
        yearData.setArtClubs(getIntegerValue(row, 75));
        yearData.setArtClubsText(getStringValue(row, 76));
        yearData.setLanguageSchools(getIntegerValue(row, 77));
        yearData.setLanguageSchoolsText(getStringValue(row, 78));
        yearData.setLocalHistoryClubs(getIntegerValue(row, 79));
        yearData.setLocalHistoryClubsText(getStringValue(row, 80));
        yearData.setMuseumCollections(getIntegerValue(row, 81));
        yearData.setMuseumCollectionsText(getStringValue(row, 82));
        yearData.setFolkloreGroups(getIntegerValue(row, 83));
        yearData.setTheaterGroups(getIntegerValue(row, 84));
        yearData.setDanceGroups(getIntegerValue(row, 85));
        yearData.setClassicalModernGroups(getIntegerValue(row, 86));
        yearData.setVocalGroups(getIntegerValue(row, 87));
        yearData.setOtherClubs(getIntegerValue(row, 88));
        yearData.setEventParticipation(getIntegerValue(row, 89));
        yearData.setProjectsIndependent(getIntegerValue(row, 90));
        yearData.setProjectsCooperation(getIntegerValue(row, 91));
        yearData.setWorkWithDisabilities(getStringValue(row, 92));
        yearData.setOtherActivities(getStringValue(row, 93));
        yearData.setSubsidizedStaff(getBigDecimalValue(row, 94));
        yearData.setTotalStaff(getIntegerValue(row, 96));
        yearData.setSpecialistsHigherEducation(getIntegerValue(row, 97));
        yearData.setSpecializedPositions(getIntegerValue(row, 98));
        yearData.setAdministrativePositions(getIntegerValue(row, 99));
        yearData.setAuxiliaryStaff(getIntegerValue(row, 100));
        yearData.setTrainingParticipation(getIntegerValue(row, 101));
        yearData.setSanctionsImposed(getIntegerValue(row, 102));
        yearData.setLibraryUsers(getIntegerValue(row, 103));
        yearData.setLibraryUsersO(getIntegerValue(row, 104));
        yearData.setLibraryUnits(getIntegerValue(row, 105));
        yearData.setNewlyAcquired(getIntegerValue(row, 106));
        yearData.setNewlyAcquired1(getIntegerValue(row, 107));
        yearData.setBorrowedDocuments(getIntegerValue(row, 108));
        yearData.setHomeVisits(getIntegerValue(row, 109));
        yearData.setReadingRoomVisits(getIntegerValue(row, 110));
        yearData.setInternetAccessEducation(getStringValue(row, 111));
        yearData.setComputerizedWorkplaces(getIntegerValue(row, 112));
        yearData.setComputerizedWorkplaces2(getIntegerValue(row, 113));
        yearData.setProjectParticipationRegional(getIntegerValue(row, 114));
        yearData.setProjectParticipationNational(getIntegerValue(row, 115));
        yearData.setProjectParticipationInternational(getIntegerValue(row, 116));
        yearData.setStaffPositionsTotal(getIntegerValue(row, 117));
        yearData.setStaffPositionsHigherEducation(getIntegerValue(row, 118));
        yearData.setStaffPositionsSecondaryEducation(getIntegerValue(row, 119));
        yearData.setStaffQualificationParticipation(getIntegerValue(row, 120));
        yearData.setBoardMembersTotal(getIntegerValue(row, 133));
        yearData.setBoardMembersHigherEd(getIntegerValue(row, 134));
        yearData.setBoardMembersSecondaryEd(getIntegerValue(row, 135));
        yearData.setBoardMembersPrimaryEd(getIntegerValue(row, 136));
        yearData.setStaffTotal(getIntegerValue(row, 137));
        yearData.setStaffHigherEd(getIntegerValue(row, 138));
        yearData.setStaffSecondaryEd(getIntegerValue(row, 139));
        yearData.setStaffPrimaryEd(getIntegerValue(row, 140));
        yearData.setStaffEmploymentContract(getIntegerValue(row, 141));
        yearData.setStaffCivilContract(getIntegerValue(row, 142));
        yearData.setSecretariesTotal(getIntegerValue(row, 143));
        yearData.setSecretariesHigherEd(getIntegerValue(row, 144));
        yearData.setSecretariesSecondaryEd(getIntegerValue(row, 145));
        yearData.setSecretariesPrimaryEd(getIntegerValue(row, 146));
        yearData.setTotalRevenue(getBigDecimalValue(row, 147));
        yearData.setRevenueSubsidies(getBigDecimalValue(row, 148));
        yearData.setRevenueRent(getBigDecimalValue(row, 149));
        yearData.setTotalExpenses(getBigDecimalValue(row, 150));
        yearData.setExpensesSalaries(getBigDecimalValue(row, 151));
        yearData.setExpensesSocialSecurity(getBigDecimalValue(row, 152));
        yearData.setEmploymentContractsCount(getIntegerValue(row, 153));
        yearData.setAverageInsuranceIncome(getBigDecimalValue(row, 154));
        yearData.setTotalSubsidizedPositions(getIntegerValue(row, 156));
        yearData.setAdditionalPositions(getBigDecimalValue(row, 157));
        // F-формуляри и останалите полета...
        yearData.setF130001TotalExpenditure(getBigDecimalValue(row, 23));
        yearData.setF141001AccProfit(getBigDecimalValue(row, 24));
        yearData.setF144001Pofit(getBigDecimalValue(row, 25));
        yearData.setF150001OperatingIncome(getBigDecimalValue(row, 26));
        yearData.setF180001TotalIncome(getBigDecimalValue(row, 27));
        yearData.setF191001accLoss(getBigDecimalValue(row, 28));
        yearData.setF192001zLoss(getBigDecimalValue(row, 29));
        yearData.setF31000ExtServicesSpending(getBigDecimalValue(row, 30));
        yearData.setF021001NontangibleAssets(getBigDecimalValue(row, 31));
        yearData.setF020001FixedAssets(getBigDecimalValue(row, 32));
        yearData.setF031001MaterialReserves(getBigDecimalValue(row, 33));
        yearData.setF032001Receivables(getBigDecimalValue(row, 34));
        yearData.setF033001Investment(getBigDecimalValue(row, 35));
        yearData.setF034001Bankroll(getBigDecimalValue(row, 36));
        yearData.setF030001CurrentAssets(getBigDecimalValue(row, 37));
        yearData.setF045001TotalAssets(getBigDecimalValue(row, 38));
        yearData.setF050001OwnCapital(getBigDecimalValue(row, 39));
        yearData.setF070001Obligations(getBigDecimalValue(row, 40));
        yearData.setF070011ShorttermObligations(getBigDecimalValue(row, 41));
        yearData.setF070021LongtermObligations(getBigDecimalValue(row, 42));
        yearData.setAverageAnnualStaff(getBigDecimalValue(row, 43));
        yearData.setNetIncome(getBigDecimalValue(row, 44));
        yearData.setRazhodiPersonal(getBigDecimalValue(row, 45));
        yearData.setTradePrice(getBigDecimalValue(row, 46));
        yearData.setIncomeProfit(getBigDecimalValue(row, 47));
        yearData.setEquityProfit(getBigDecimalValue(row, 48));
        yearData.setAssetProfit(getBigDecimalValue(row, 49));
        yearData.setFinancialAutonomy(getBigDecimalValue(row, 50));
        yearData.setFinancialDebt(getBigDecimalValue(row, 51));
        yearData.setShortTermLiquidity(getBigDecimalValue(row, 52));
        yearData.setFastLiquidity(getBigDecimalValue(row, 53));
        yearData.setImmediateLiquidity(getBigDecimalValue(row, 54));
        yearData.setAbsoluteLiquidity(getBigDecimalValue(row, 55));
        yearData.setVremeOborot(getBigDecimalValue(row, 56));
        yearData.setBrOb(getBigDecimalValue(row, 57));
        yearData.setZkma(getStringValue(row, 58));
        yearData.setAktiviPersonal(getBigDecimalValue(row, 59));
        yearData.setZadaljeniaPerс(getBigDecimalValue(row, 60));
        yearData.setPrihodiPers(getBigDecimalValue(row, 61));
        yearData.setPechalbaPerс(getBigDecimalValue(row, 62));
        yearData.setPersonal(getBigDecimalValue(row, 63));
        yearData.setPaymentStandard(getStringValue(row, 19));
        yearData.setMatriculationBel26(getBigDecimalValue(row, 166));
        yearData.setNvoMat(getBigDecimalValue(row, 174));
        yearData.setNvoBel(getBigDecimalValue(row, 175));
        yearData.setPoorHealth(getIntegerValue(row, 178));
    }

    // Оригиналните parse методи остават същите...
    private Municipality parseMunicipality(Row row) {
        return Municipality.builder()
                .municipalityCode(getStringValue(row, 12))
                .municipality(getStringValue(row, 4))
                .municipalityNorm(getStringValue(row, 9))
                .district(getStringValue(row, 3))
                .districtCode(getStringValue(row, 11))
                .nuts1(getStringValue(row, 14))
                .nuts2(getStringValue(row, 15))
                .nuts3(getStringValue(row, 16))
                .mrrbCategory(getStringValue(row, 18))
                .settlementPopulation(getIntegerValue(row, 121))
                .municipalityPopulation(getIntegerValue(row, 122))
                .totalPopulation2021(getIntegerValue(row, 122))
                .populationUnder15(getIntegerValue(row, 123))
                .population1564(getIntegerValue(row, 124))
                .populationOver65(getIntegerValue(row, 125))
                .higherEducation(getIntegerValue(row, 126))
                .secondaryEducation(getIntegerValue(row, 127))
                .primaryEducation(getIntegerValue(row, 128))
                .elementaryEducation(getIntegerValue(row, 129))
                .noEducation(getIntegerValue(row, 130))
                .literate(getIntegerValue(row, 131))
                .illiterate(getIntegerValue(row, 132))
                .shareBulgarian(getDoubleValue(row, 167))
                .shareTurkish(getDoubleValue(row, 168))
                .shareRoma(getDoubleValue(row, 169))
                .shareOthers(getDoubleValue(row, 170))
                .unemploymentRate(getDoubleValue(row, 158))
                .unemploymentRate1529(getDoubleValue(row, 159))
                .grossWageMonthly(getDoubleValue(row, 160))
                .grossValueAddedPerPerson(getDoubleValue(row, 161))
                .companiesNumber(getIntegerValue(row, 162))
                .companiesPerCapita(getDoubleValue(row, 163))
                .employmentRate(getDoubleValue(row, 164))
                .urbanPopulationPercent(getDoubleValue(row, 165))
                .studentsNumber(getIntegerValue(row, 172))
                .studentsPer1000(getDoubleValue(row, 173))
                .kidsKindergartens(getIntegerValue(row, 176))
                .hospitals(getIntegerValue(row, 177))
                .nChitalishaMunip(getIntegerValue(row, 21))
                .uniquePersonsEmployment(getIntegerValue(row, 155))
                .migrationCoefficient(getDoubleValue(row, 171))
                .totalRevenueThousands(getBigDecimalValue(row, 147))
                .revenueFromSubsidiesThousands(getBigDecimalValue(row, 148))
                .revenueFromRentThousands(getBigDecimalValue(row, 149))
                .totalExpensesThousands(getBigDecimalValue(row, 150))
                .expensesSalariesThousands(getBigDecimalValue(row, 151))
                .expensesSocialSecurityThousands(getBigDecimalValue(row, 152))
                .totalStaffCount(getIntegerValue(row, 137))
                .staffHigherEducationCount(getIntegerValue(row, 138))
                .staffSecondaryEducationCount(getIntegerValue(row, 139))
                .secretariesCount(getIntegerValue(row, 143))
                .secretariesHigherEducationCount(getIntegerValue(row, 144))
                .averageInsuranceIncomeTd(getBigDecimalValue(row, 154))
                .uniqueEmploymentContracts(getIntegerValue(row, 155))
                .subsidizedPositions(getIntegerValue(row, 156))
                .additionalPositions(getIntegerValue(row, 157))
                .build();
    }

    private Chitalishte parseChitalishte(Row row, Municipality municipality) {
        return Chitalishte.builder()
                .municipality(municipality)
                .regN(getStringValue(row, 0))
                .name(getStringValue(row, 1))
                .town(getStringValue(row, 5))
                .address(getStringValue(row, 6))
                .uic(getStringValue(row, 7))
                .phone(getStringValue(row, 65))
                .settlementNorm(getStringValue(row, 8))
                .villageCity(getStringValue(row, 10))
                .mayoralityCode(getStringValue(row, 13))
                .ekatte(getStringValue(row, 17))
                .isMunipCenter(getStringValue(row, 20))
                .emplCategory(getStringValue(row, 22))
                .regionalList(getStringValue(row, 73))
                .nationalList(getStringValue(row, 74))
                .build();
    }

    private ChitalishteYearData parseYearData(Row row, Chitalishte chitalishte, Integer year) {
        ChitalishteYearData yearData = new ChitalishteYearData();
        yearData.setChitalishte(chitalishte);
        updateYearDataFromRow(yearData, row, year);
        return yearData;
    }

    // Helper методи остават същите...
    private String getStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        String value;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue().toString();
                } else {
                    value = String.valueOf((long) cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    value = cell.getStringCellValue();
                } catch (Exception e) {
                    value = String.valueOf(cell.getNumericCellValue());
                }
                break;
            default:
                value = null;
        }

        return cleanString(value);
    }

    private String cleanString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Integer getIntegerValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    return Integer.parseInt(strValue);
                case FORMULA:
                    return (int) cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDoubleValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    return Double.parseDouble(strValue);
                case FORMULA:
                    return cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    return new BigDecimal(strValue);
                case FORMULA:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}