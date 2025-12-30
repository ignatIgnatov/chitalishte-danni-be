package bg.chitalishte.controller;

import bg.chitalishte.service.ChitalishteImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chitalishta")
@RequiredArgsConstructor
@Slf4j
public class ChitalishteController {
    
    private final ChitalishteImportService importService;
    
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Получен файл за импорт: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файлът е празен");
            }
            
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body("Поддържа се само .xlsx формат");
            }
            
            Map<String, Integer> result = importService.importFromExcel(file);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Успешен импорт",
                    "municipalities", result.get("municipalities"),
                    "chitalishta", result.get("chitalishta"),
                    "yearData", result.get("yearData")
            ));
            
        } catch (Exception e) {
            log.error("Грешка при импорт: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Chitalishte Import Service е готов"
        ));
    }
}
