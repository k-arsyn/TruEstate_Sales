package com.truestate.retail.controllers;

import com.truestate.retail.models.SaleRecord;
import com.truestate.retail.services.SaleRecordService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin
public class SaleRecordController {

    private final SaleRecordService service;

    public SaleRecordController(SaleRecordService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SaleRecord> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> customerRegion,
            @RequestParam(required = false) List<String> gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) List<String> productCategory,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) List<String> paymentMethod,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDate start = startDate != null && !startDate.isBlank() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null;

        return service.search(
                q,
                customerRegion,
                gender,
                minAge,
                maxAge,
                productCategory,
                tag,
                paymentMethod,
                start,
                end,
                sortBy,
                direction,
                page,
                size
        );
    }
}

