package com.truestate.retail.services;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.models.SaleRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SaleRecordService {

    private final SaleRecordRepository repository;
    private final CsvFallbackService csvFallbackService;

    public SaleRecordService(SaleRecordRepository repository, CsvFallbackService csvFallbackService) {
        this.repository = repository;
        this.csvFallbackService = csvFallbackService;
    }

    public Page<SaleRecord> search(
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
        // Check if database has data
        long count = repository.count();

        if (count == 0) {
            System.out.println("Database is empty, using CSV fallback service");
            // Use CSV fallback if database is empty
            return csvFallbackService.searchFromCsv(
                    query, customerRegions, genders, minAge, maxAge,
                    productCategories, tags, paymentMethods, startDate, endDate,
                    sortBy, sortDirection, page, size
            );
        }

        // Use database if it has data
        var criteria = new SaleRecordSpecification.SearchCriteria(
                query,
                customerRegions,
                genders,
                minAge,
                maxAge,
                productCategories,
                tags,
                paymentMethods,
                startDate,
                endDate
        );
        Specification<SaleRecord> spec = SaleRecordSpecification.build(criteria);

        Sort sort = buildSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(spec, pageable);
    }

    private Sort buildSort(String sortBy, String direction) {
        Sort sort;
        if ("quantity".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("quantity");
        } else if ("customerName".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("customerName");
        } else {
            // default: date newest first
            sort = Sort.by("date");
        }
        if ("asc".equalsIgnoreCase(direction)) {
            return sort.ascending();
        }
        // default desc for date (newest first) and quantity
        return sort.descending();
    }
}
