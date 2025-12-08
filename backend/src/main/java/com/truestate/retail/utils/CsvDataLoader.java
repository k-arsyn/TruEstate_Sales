package com.truestate.retail.utils;

import com.truestate.retail.models.SaleRecordRepository;
import com.truestate.retail.services.CsvFallbackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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

        // For large datasets on low-memory environments (like Render free tier),
        // we no longer support bulk preloading of the entire CSV into H2.
        // All CSV-backed queries should instead use CsvFallbackService.searchFromCsv(...),
        // which streams the file and keeps memory usage bounded.
        System.out.println("CSV startup load was requested but is not supported in this build. " +
                "Use the streaming CSV fallback instead.");

        System.out.println("=== CSV Data Loader Finished ===");
    }
}
