package com.truestate.retail.utils;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.models.SaleRecordRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
public class CsvDataLoader implements CommandLineRunner {

    private final SaleRecordRepository repository;

    public CsvDataLoader(SaleRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== CSV Data Loader Starting ===");

        long count = repository.count();
        System.out.println("Current record count in database: " + count);

        if (count > 0) {
            System.out.println("Database already has data, skipping CSV load");
            return;
        }

        var resource = new ClassPathResource("sales_data.csv");
        System.out.println("Looking for CSV file at: sales_data.csv");

        if (!resource.exists()) {
            System.err.println("WARNING: sales_data.csv not found in classpath!");
            return;
        }

        System.out.println("CSV file found, starting to load data...");
        int recordCount = 0;

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             var csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                SaleRecord sale = new SaleRecord();
                sale.setTransactionId(record.get("Transaction ID"));
                sale.setDate(LocalDate.parse(record.get("Date")));
                sale.setCustomerId(record.get("Customer ID"));
                sale.setCustomerName(record.get("Customer Name"));
                sale.setPhoneNumber(record.get("Phone Number"));
                sale.setGender(record.get("Gender"));
                sale.setAge(parseInt(record.get("Age")));
                sale.setCustomerRegion(record.get("Customer Region"));
                sale.setCustomerType(record.get("Customer Type"));
                sale.setProductId(record.get("Product ID"));
                sale.setProductName(record.get("Product Name"));
                sale.setBrand(record.get("Brand"));
                sale.setProductCategory(record.get("Product Category"));
                sale.setTags(record.get("Tags"));
                sale.setQuantity(parseInt(record.get("Quantity")));
                sale.setPricePerUnit(parseDouble(record.get("Price per Unit")));
                sale.setDiscountPercentage(parseDouble(record.get("Discount Percentage")));
                sale.setTotalAmount(parseDouble(record.get("Total Amount")));
                sale.setFinalAmount(parseDouble(record.get("Final Amount")));
                sale.setPaymentMethod(record.get("Payment Method"));
                sale.setOrderStatus(record.get("Order Status"));
                sale.setDeliveryType(record.get("Delivery Type"));
                sale.setStoreId(record.get("Store ID"));
                sale.setStoreLocation(record.get("Store Location"));
                sale.setSalespersonId(record.get("Salesperson ID"));
                sale.setEmployeeName(record.get("Employee Name"));
                repository.save(sale);
                recordCount++;
            }

            System.out.println("Successfully loaded " + recordCount + " records from CSV");
        } catch (Exception e) {
            System.err.println("Error loading CSV data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== CSV Data Loader Finished ===");
    }

    private Integer parseInt(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
