package com.truestate.retail.services;

import com.truestate.retail.models.SaleRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvFallbackService {

    public Page<SaleRecord> searchFromCsv(
            String query,
            List<String> customerRegions,
            List<String> genders,
            Integer minAge,
            Integer maxAge,
            List<String> productCategories,
            List<String> tags,
            List<String> paymentMethods,
            LocalDate startDate,
            LocalDate endDate,
            String sortBy,
            String sortDirection,
            int page,
            int size
    ) {
        try {
            List<SaleRecord> allRecords = loadAllRecordsFromCsv();

            // Apply filters
            List<SaleRecord> filtered = allRecords.stream()
                    .filter(record -> matchesFilters(record, query, customerRegions, genders, minAge, maxAge,
                            productCategories, tags, paymentMethods, startDate, endDate))
                    .collect(Collectors.toList());

            // Apply sorting
            filtered = applySort(filtered, sortBy, sortDirection);

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, filtered.size());

            List<SaleRecord> pageContent = (start < filtered.size())
                    ? filtered.subList(start, end)
                    : new ArrayList<>();

            return new PageImpl<>(pageContent, PageRequest.of(page, size), filtered.size());

        } catch (Exception e) {
            System.err.println("Error loading from CSV: " + e.getMessage());
            e.printStackTrace();
            return Page.empty();
        }
    }

    private List<SaleRecord> loadAllRecordsFromCsv() throws Exception {
        List<SaleRecord> records = new ArrayList<>();
        var resource = new ClassPathResource("sales_data.csv");

        if (!resource.exists()) {
            System.err.println("CSV file not found in classpath");
            return records;
        }

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             var csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord csvRecord : csvParser) {
                SaleRecord sale = new SaleRecord();
                sale.setTransactionId(csvRecord.get("Transaction ID"));
                sale.setDate(LocalDate.parse(csvRecord.get("Date")));
                sale.setCustomerId(csvRecord.get("Customer ID"));
                sale.setCustomerName(csvRecord.get("Customer Name"));
                sale.setPhoneNumber(csvRecord.get("Phone Number"));
                sale.setGender(csvRecord.get("Gender"));
                sale.setAge(parseInt(csvRecord.get("Age")));
                sale.setCustomerRegion(csvRecord.get("Customer Region"));
                sale.setCustomerType(csvRecord.get("Customer Type"));
                sale.setProductId(csvRecord.get("Product ID"));
                sale.setProductName(csvRecord.get("Product Name"));
                sale.setBrand(csvRecord.get("Brand"));
                sale.setProductCategory(csvRecord.get("Product Category"));
                sale.setTags(csvRecord.get("Tags"));
                sale.setQuantity(parseInt(csvRecord.get("Quantity")));
                sale.setPricePerUnit(parseDouble(csvRecord.get("Price per Unit")));
                sale.setDiscountPercentage(parseDouble(csvRecord.get("Discount Percentage")));
                sale.setTotalAmount(parseDouble(csvRecord.get("Total Amount")));
                sale.setFinalAmount(parseDouble(csvRecord.get("Final Amount")));
                sale.setPaymentMethod(csvRecord.get("Payment Method"));
                sale.setOrderStatus(csvRecord.get("Order Status"));
                sale.setDeliveryType(csvRecord.get("Delivery Type"));
                sale.setStoreId(csvRecord.get("Store ID"));
                sale.setStoreLocation(csvRecord.get("Store Location"));
                sale.setSalespersonId(csvRecord.get("Salesperson ID"));
                sale.setEmployeeName(csvRecord.get("Employee Name"));
                records.add(sale);
            }
        }

        System.out.println("Loaded " + records.size() + " records from CSV");
        return records;
    }

    private boolean matchesFilters(SaleRecord record, String query, List<String> customerRegions,
                                     List<String> genders, Integer minAge, Integer maxAge,
                                     List<String> productCategories, List<String> tags,
                                     List<String> paymentMethods, LocalDate startDate, LocalDate endDate) {

        // Search query (name or phone)
        if (query != null && !query.isBlank()) {
            String lowerQuery = query.toLowerCase();
            boolean matchesName = record.getCustomerName() != null &&
                    record.getCustomerName().toLowerCase().contains(lowerQuery);
            boolean matchesPhone = record.getPhoneNumber() != null &&
                    record.getPhoneNumber().contains(query);
            if (!matchesName && !matchesPhone) return false;
        }

        // Customer Region
        if (customerRegions != null && !customerRegions.isEmpty()) {
            if (!customerRegions.contains(record.getCustomerRegion())) return false;
        }

        // Gender
        if (genders != null && !genders.isEmpty()) {
            if (!genders.contains(record.getGender())) return false;
        }

        // Age range
        if (minAge != null && record.getAge() != null && record.getAge() < minAge) return false;
        if (maxAge != null && record.getAge() != null && record.getAge() > maxAge) return false;

        // Product Category
        if (productCategories != null && !productCategories.isEmpty()) {
            if (!productCategories.contains(record.getProductCategory())) return false;
        }

        // Tags
        if (tags != null && !tags.isEmpty()) {
            if (record.getTags() == null) return false;
            boolean hasAnyTag = tags.stream().anyMatch(tag -> record.getTags().contains(tag));
            if (!hasAnyTag) return false;
        }

        // Payment Method
        if (paymentMethods != null && !paymentMethods.isEmpty()) {
            if (!paymentMethods.contains(record.getPaymentMethod())) return false;
        }

        // Date range
        if (startDate != null && record.getDate().isBefore(startDate)) return false;
        if (endDate != null && record.getDate().isAfter(endDate)) return false;

        return true;
    }

    private List<SaleRecord> applySort(List<SaleRecord> records, String sortBy, String sortDirection) {
        boolean ascending = "asc".equalsIgnoreCase(sortDirection);

        if ("quantity".equalsIgnoreCase(sortBy)) {
            records.sort((a, b) -> {
                Integer qtyA = a.getQuantity() != null ? a.getQuantity() : 0;
                Integer qtyB = b.getQuantity() != null ? b.getQuantity() : 0;
                return ascending ? qtyA.compareTo(qtyB) : qtyB.compareTo(qtyA);
            });
        } else if ("customerName".equalsIgnoreCase(sortBy)) {
            records.sort((a, b) -> {
                String nameA = a.getCustomerName() != null ? a.getCustomerName() : "";
                String nameB = b.getCustomerName() != null ? b.getCustomerName() : "";
                return ascending ? nameA.compareTo(nameB) : nameB.compareTo(nameA);
            });
        } else {
            // Default: sort by date
            records.sort((a, b) -> ascending ? a.getDate().compareTo(b.getDate()) : b.getDate().compareTo(a.getDate()));
        }

        return records;
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

