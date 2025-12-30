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
    public Map<String, Integer> importFromExcel(MultipartFile file) throws Exception {
        log.info("Започване на импорт от Excel файл: {}", file.getOriginalFilename());
        
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
            
            // Parse всички редове
            int rowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowCount++;
                
                if (rowCount % 1000 == 0) {
                    log.info("Обработени {} реда...", rowCount);
                }
                
                try {
                    // Извличане на ключови полета
                    String municipalityCode = getStringValue(row, 12);
                    String regN = getStringValue(row, 0);
                    Integer year = getIntegerValue(row, 2);
                    
                    if (municipalityCode == null || regN == null || year == null) {
                        log.warn("Пропускане на ред {} - липсват задължителни полета", rowCount);
                        continue;
                    }
                    
                    // Вземи/създай община
                    Municipality municipality = municipalityCache.computeIfAbsent(
                        municipalityCode,
                        code -> parseMunicipality(row)
                    );
                    
                    // Вземи/създай читалище
                    Chitalishte chitalishte = chitalishteCache.computeIfAbsent(
                        regN,
                        reg -> parseChitalishte(row, municipality)
                    );
                    
                    // Създай годишни данни
                    ChitalishteYearData yearData = parseYearData(row, chitalishte, year);
                    allYearData.add(yearData);
                    
                } catch (Exception e) {
                    log.error("Грешка при обработка на ред {}: {}", rowCount, e.getMessage());
                }
            }
            
            // Записване в базата
            log.info("Записване на {} общини...", municipalityCache.size());
            List<Municipality> municipalities = new ArrayList<>(municipalityCache.values());
            municipalityRepository.saveAll(municipalities);
            
            log.info("Записване на {} читалища...", chitalishteCache.size());
            List<Chitalishte> chitalishta = new ArrayList<>(chitalishteCache.values());
            chitalishteRepository.saveAll(chitalishta);
            
            log.info("Записване на {} годишни записа...", allYearData.size());
            yearDataRepository.saveAll(allYearData);
            
            // Изчисляване на показатели
            log.info("Изчисляване на показатели за общините...");
            metricsService.calculateAllMetrics();
            
            log.info("✅ Успешен импорт! Общини: {}, Читалища: {}, Годишни данни: {}", 
                    municipalities.size(), chitalishta.size(), allYearData.size());
            
            Map<String, Integer> result = new HashMap<>();
            result.put("municipalities", municipalities.size());
            result.put("chitalishta", chitalishta.size());
            result.put("yearData", allYearData.size());
            return result;
            
        } catch (Exception e) {
            log.error("Грешка при импорт: {}", e.getMessage(), e);
            throw new Exception("Грешка при импорт на данни: " + e.getMessage(), e);
        }
    }
    
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
        return ChitalishteYearData.builder()
                .chitalishte(chitalishte)
                .year(year)
                // Ръководство (64, 66, 67)
                .chairman(getStringValue(row, 64))
                .secretary(getStringValue(row, 66))
                .status(getStringValue(row, 67))
                // Членство (68-71)
                .totalMembers(getIntegerValue(row, 68))
                .submittedApplications(getIntegerValue(row, 69))
                .newlyAcceptedMembers(getIntegerValue(row, 70))
                .rejectedApplications(getIntegerValue(row, 71))
                // Библиотечна дейност (72)
                .libraryActivity(getStringValue(row, 72))
                // Дейности (75-93)
                .artClubs(getIntegerValue(row, 75))
                .artClubsText(getStringValue(row, 76))
                .languageSchools(getIntegerValue(row, 77))
                .languageSchoolsText(getStringValue(row, 78))
                .localHistoryClubs(getIntegerValue(row, 79))
                .localHistoryClubsText(getStringValue(row, 80))
                .museumCollections(getIntegerValue(row, 81))
                .museumCollectionsText(getStringValue(row, 82))
                .folkloreGroups(getIntegerValue(row, 83))
                .theaterGroups(getIntegerValue(row, 84))
                .danceGroups(getIntegerValue(row, 85))
                .classicalModernGroups(getIntegerValue(row, 86))
                .vocalGroups(getIntegerValue(row, 87))
                .otherClubs(getIntegerValue(row, 88))
                .eventParticipation(getIntegerValue(row, 89))
                .projectsIndependent(getIntegerValue(row, 90))
                .projectsCooperation(getIntegerValue(row, 91))
                .workWithDisabilities(getStringValue(row, 92))
                .otherActivities(getStringValue(row, 93))
                // Персонал на читалището (94-102)
                .subsidizedStaff(getBigDecimalValue(row, 94))
                .totalStaff(getIntegerValue(row, 96))
                .specialistsHigherEducation(getIntegerValue(row, 97))
                .specializedPositions(getIntegerValue(row, 98))
                .administrativePositions(getIntegerValue(row, 99))
                .auxiliaryStaff(getIntegerValue(row, 100))
                .trainingParticipation(getIntegerValue(row, 101))
                .sanctionsImposed(getIntegerValue(row, 102))
                // Библиотека (103-113)
                .libraryUsers(getIntegerValue(row, 103))
                .libraryUsersO(getIntegerValue(row, 104))
                .libraryUnits(getIntegerValue(row, 105))
                .newlyAcquired(getIntegerValue(row, 106))
                .newlyAcquired1(getIntegerValue(row, 107))
                .borrowedDocuments(getIntegerValue(row, 108))
                .homeVisits(getIntegerValue(row, 109))
                .readingRoomVisits(getIntegerValue(row, 110))
                .internetAccessEducation(getStringValue(row, 111))
                .computerizedWorkplaces(getIntegerValue(row, 112))
                .computerizedWorkplaces2(getIntegerValue(row, 113))
                // Проекти (114-116)
                .projectParticipationRegional(getIntegerValue(row, 114))
                .projectParticipationNational(getIntegerValue(row, 115))
                .projectParticipationInternational(getIntegerValue(row, 116))
                // Щатни бройки (117-120)
                .staffPositionsTotal(getIntegerValue(row, 117))
                .staffPositionsHigherEducation(getIntegerValue(row, 118))
                .staffPositionsSecondaryEducation(getIntegerValue(row, 119))
                .staffQualificationParticipation(getIntegerValue(row, 120))
                // Настоятелство (133-136)
                .boardMembersTotal(getIntegerValue(row, 133))
                .boardMembersHigherEd(getIntegerValue(row, 134))
                .boardMembersSecondaryEd(getIntegerValue(row, 135))
                .boardMembersPrimaryEd(getIntegerValue(row, 136))
                // Общ персонал (137-142)
                .staffTotal(getIntegerValue(row, 137))
                .staffHigherEd(getIntegerValue(row, 138))
                .staffSecondaryEd(getIntegerValue(row, 139))
                .staffPrimaryEd(getIntegerValue(row, 140))
                .staffEmploymentContract(getIntegerValue(row, 141))
                .staffCivilContract(getIntegerValue(row, 142))
                // Секретари (143-146)
                .secretariesTotal(getIntegerValue(row, 143))
                .secretariesHigherEd(getIntegerValue(row, 144))
                .secretariesSecondaryEd(getIntegerValue(row, 145))
                .secretariesPrimaryEd(getIntegerValue(row, 146))
                // Финанси (147-157)
                .totalRevenue(getBigDecimalValue(row, 147))
                .revenueSubsidies(getBigDecimalValue(row, 148))
                .revenueRent(getBigDecimalValue(row, 149))
                .totalExpenses(getBigDecimalValue(row, 150))
                .expensesSalaries(getBigDecimalValue(row, 151))
                .expensesSocialSecurity(getBigDecimalValue(row, 152))
                .employmentContractsCount(getIntegerValue(row, 153))
                .averageInsuranceIncome(getBigDecimalValue(row, 154))
                .totalSubsidizedPositions(getIntegerValue(row, 156))
                .additionalPositions(getBigDecimalValue(row, 157))
                // F-формуляри (23-63)
                .f130001TotalExpenditure(getBigDecimalValue(row, 23))
                .f141001AccProfit(getBigDecimalValue(row, 24))
                .f144001Pofit(getBigDecimalValue(row, 25))
                .f150001OperatingIncome(getBigDecimalValue(row, 26))
                .f180001TotalIncome(getBigDecimalValue(row, 27))
                .f191001accLoss(getBigDecimalValue(row, 28))
                .f192001zLoss(getBigDecimalValue(row, 29))
                .f31000ExtServicesSpending(getBigDecimalValue(row, 30))
                .f021001NontangibleAssets(getBigDecimalValue(row, 31))
                .f020001FixedAssets(getBigDecimalValue(row, 32))
                .f031001MaterialReserves(getBigDecimalValue(row, 33))
                .f032001Receivables(getBigDecimalValue(row, 34))
                .f033001Investment(getBigDecimalValue(row, 35))
                .f034001Bankroll(getBigDecimalValue(row, 36))
                .f030001CurrentAssets(getBigDecimalValue(row, 37))
                .f045001TotalAssets(getBigDecimalValue(row, 38))
                .f050001OwnCapital(getBigDecimalValue(row, 39))
                .f070001Obligations(getBigDecimalValue(row, 40))
                .f070011ShorttermObligations(getBigDecimalValue(row, 41))
                .f070021LongtermObligations(getBigDecimalValue(row, 42))
                .averageAnnualStaff(getBigDecimalValue(row, 43))
                .netIncome(getBigDecimalValue(row, 44))
                .razhodiPersonal(getBigDecimalValue(row, 45))
                .tradePrice(getBigDecimalValue(row, 46))
                .incomeProfit(getBigDecimalValue(row, 47))
                .equityProfit(getBigDecimalValue(row, 48))
                .assetProfit(getBigDecimalValue(row, 49))
                .financialAutonomy(getBigDecimalValue(row, 50))
                .financialDebt(getBigDecimalValue(row, 51))
                .shortTermLiquidity(getBigDecimalValue(row, 52))
                .fastLiquidity(getBigDecimalValue(row, 53))
                .immediateLiquidity(getBigDecimalValue(row, 54))
                .absoluteLiquidity(getBigDecimalValue(row, 55))
                .vremeOborot(getBigDecimalValue(row, 56))
                .brOb(getBigDecimalValue(row, 57))
                .zkma(getStringValue(row, 58))
                .aktiviPersonal(getBigDecimalValue(row, 59))
                .zadaljeniaPerс(getBigDecimalValue(row, 60))
                .prihodiPers(getBigDecimalValue(row, 61))
                .pechalbaPerс(getBigDecimalValue(row, 62))
                .personal(getBigDecimalValue(row, 63))
                // Други (19, 166, 174, 175, 178)
                .paymentStandard(getStringValue(row, 19))
                .matriculationBel26(getBigDecimalValue(row, 166))
                .nvoMat(getBigDecimalValue(row, 174))
                .nvoBel(getBigDecimalValue(row, 175))
                .poorHealth(getIntegerValue(row, 178))
                .build();
    }
    
    // Helper методи
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
