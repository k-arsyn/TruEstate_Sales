package com.truestate.retail.controllers;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.models.SaleRecordRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataLoaderController {

    private final SaleRecordRepository repository;

    public DataLoaderController(SaleRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/load-data")
    public Map<String, Object> loadData() {
        Map<String, Object> response = new HashMap<>();

        try {
            long existingCount = repository.count();
            response.put("existingRecords", existingCount);

            if (existingCount > 0) {
                response.put("message", "Database already has " + existingCount + " records");
                return response;
            }

            var resource = new ClassPathResource("sales_data.csv");

            if (!resource.exists()) {
                response.put("error", "CSV file not found in classpath");
                return response;
            }

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
            }

            response.put("success", true);
            response.put("recordsLoaded", recordCount);
            response.put("message", "Successfully loaded " + recordCount + " records from CSV");

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("success", false);
        }

        return response;
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

