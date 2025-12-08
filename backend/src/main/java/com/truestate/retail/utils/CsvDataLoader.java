package com.truestate.retail.utils;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.models.SaleRecordRepository;
import com.truestate.retail.services.CsvFallbackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CsvDataLoader implements CommandLineRunner {

    private final SaleRecordRepository repository;
    private final CsvFallbackService csvFallbackService;

    @Value("${csv.startup.load.enabled:false}")
    private boolean startupLoadEnabled;

    public CsvDataLoader(SaleRecordRepository repository, CsvFallbackService csvFallbackService) {
        this.repository = repository;
        this.csvFallbackService = csvFallbackService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== CSV Data Loader Starting ===");

        if (!startupLoadEnabled) {
            System.out.println("CSV startup load is disabled (csv.startup.load.enabled=false). Skipping DB pre-load.");
            return;
        }

        long count = repository.count();
        System.out.println("Current record count in database: " + count);

        if (count > 0) {
            System.out.println("Database already has data, skipping CSV load");
            return;
        }

        // Use CsvFallbackService to load all records from either classpath or CSV_URL
        try {
            List<SaleRecord> records = csvFallbackService.loadAllRecords();
            if (records.isEmpty()) {
                System.err.println("No records loaded from CSV. Check CSV_URL or classpath CSV.");
                return;
            }

            repository.saveAll(records);
            System.out.println("Successfully loaded " + records.size() + " records into the database from CSV");
        } catch (Exception e) {
            System.err.println("Error loading CSV data at startup: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== CSV Data Loader Finished ===");
    }
}
