package com.truestate.retail.services;

import com.truestate.retail.models.SaleRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvFallbackService {

    @Value("${csv.url:}")
    private String csvUrl;

    /**
     * Load all records from CSV, either from the classpath or from the external URL
     * configured via csv.url / CSV_URL. This is used both by the startup data loader
     * and by the fallback search implementation when the database is empty.
     */
    public List<SaleRecord> loadAllRecords() throws Exception {
        return loadAllRecordsFromCsv();
    }

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
        int pageStart = page * size;
        int pageEnd = pageStart + size;
        int matchIndex = 0;
        List<SaleRecord> pageContent = new ArrayList<>(size);

        try (InputStreamReader reader = openCsvReader();
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord csvRecord : csvParser) {
                SaleRecord sale = mapCsvRecord(csvRecord);
                if (!matchesFilters(sale, query, customerRegions, genders, minAge, maxAge,
                        productCategories, tags, paymentMethods, startDate, endDate)) {
                    continue;
                }

                if (matchIndex >= pageStart && matchIndex < pageEnd) {
                    pageContent.add(sale);
                }

                matchIndex++;
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV stream: " + e.getMessage());
            e.printStackTrace();
            return Page.empty();
        } catch (Exception e) {
            System.err.println("Unexpected error during CSV search: " + e.getMessage());
            e.printStackTrace();
            return Page.empty();
        }

        long totalElements = matchIndex;
        // Apply sort within the current page to respect UI expectations
        applyInPageSort(pageContent, sortBy, sortDirection);

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    private InputStreamReader openCsvReader() throws Exception {
        var resource = new ClassPathResource("sales_data.csv");

        if (resource.exists()) {
            System.out.println("Streaming CSV from classpath");
            return new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        }

        if (csvUrl != null && !csvUrl.isBlank()) {
            System.out.println("CSV not in classpath, streaming from URL: " + csvUrl);
            URL url = new URL(csvUrl);
            return new InputStreamReader(new BufferedInputStream(url.openStream()), StandardCharsets.UTF_8);
        }

        throw new IllegalStateException("CSV file not found in classpath and no CSV URL provided");
    }

    private SaleRecord mapCsvRecord(CSVRecord csvRecord) {
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
        return sale;
    }

    private void applyInPageSort(List<SaleRecord> records, String sortBy, String sortDirection) {
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
            records.sort((a, b) -> {
                if (a.getDate() == null || b.getDate() == null) {
                    return 0;
                }
                return ascending ? a.getDate().compareTo(b.getDate()) : b.getDate().compareTo(a.getDate());
            });
        }
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
