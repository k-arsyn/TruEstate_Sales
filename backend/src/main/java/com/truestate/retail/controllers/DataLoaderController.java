package com.truestate.retail.controllers;

import com.truestate.retail.models.SaleRecordRepository;
import com.truestate.retail.services.CsvFallbackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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

        long existingCount = repository.count();
        response.put("existingRecords", existingCount);


        response.put("success", true);
        response.put("message", "Database-backed preload is disabled. Data is served via streaming CSV fallback when the database is empty.");

        return response;
    }
}
