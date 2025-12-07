package com.truestate.retail.controllers;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.models.SaleRecordRepository;
import com.truestate.retail.services.CsvFallbackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataLoaderController {

    private final SaleRecordRepository repository;
    private final CsvFallbackService csvFallbackService;

    public DataLoaderController(SaleRecordRepository repository, CsvFallbackService csvFallbackService) {
        this.repository = repository;
        this.csvFallbackService = csvFallbackService;
    }

    @GetMapping("/load-data")
    public Map<String, Object> loadData() {
        Map<String, Object> response = new HashMap<>();

        try {
            long existingCount = repository.count();
            response.put("existingRecords", existingCount);

            if (existingCount > 0) {
                response.put("message", "Database already has " + existingCount + " records");
                response.put("alreadyLoaded", true);
                return response;
            }

            // Use the shared CSV loader (classpath or CSV_URL)
            List<SaleRecord> records = csvFallbackService.loadAllRecords();
            if (records.isEmpty()) {
                response.put("success", false);
                response.put("error", "CSV file not found or empty. Check CSV_URL or classpath CSV.");
                return response;
            }

            repository.saveAll(records);

            response.put("success", true);
            response.put("recordsLoaded", records.size());
            response.put("message", "Successfully loaded " + records.size() + " records from CSV");

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("success", false);
        }

        return response;
    }
}
