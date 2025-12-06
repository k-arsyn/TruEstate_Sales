package com.truestate.retail.services;

import com.truestate.retail.models.SaleRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleRecordSpecification {

    public record SearchCriteria(
            String query,
            List<String> customerRegions,
            List<String> genders,
            Integer minAge,
            Integer maxAge,
            List<String> productCategories,
            List<String> tags,
            List<String> paymentMethods,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public static Specification<SaleRecord> build(SearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Full-text search on customer name and phone number (case-insensitive)
            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerName")), pattern),
                        cb.like(cb.lower(root.get("phoneNumber")), pattern)
                ));
            }

            // Multi-select filters
            if (criteria.customerRegions() != null && !criteria.customerRegions().isEmpty()) {
                predicates.add(root.get("customerRegion").in(criteria.customerRegions()));
            }

            if (criteria.genders() != null && !criteria.genders().isEmpty()) {
                predicates.add(root.get("gender").in(criteria.genders()));
            }

            if (criteria.productCategories() != null && !criteria.productCategories().isEmpty()) {
                predicates.add(root.get("productCategory").in(criteria.productCategories()));
            }

            if (criteria.paymentMethods() != null && !criteria.paymentMethods().isEmpty()) {
                predicates.add(root.get("paymentMethod").in(criteria.paymentMethods()));
            }

            // Age range handling (including invalid ranges)
            if (criteria.minAge() != null && criteria.maxAge() != null) {
                if (criteria.minAge() > criteria.maxAge()) {
                    // invalid range: always false predicate => no results
                    predicates.add(cb.equal(cb.literal(1), 0));
                } else {
                    predicates.add(cb.between(root.get("age"), criteria.minAge(), criteria.maxAge()));
                }
            } else if (criteria.minAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), criteria.minAge()));
            } else if (criteria.maxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), criteria.maxAge()));
            }

            // Tags filter: treat tags as comma-separated string, match any tag (case-insensitive)
            if (criteria.tags() != null && !criteria.tags().isEmpty()) {
                List<Predicate> tagPredicates = new ArrayList<>();
                for (String tag : criteria.tags()) {
                    if (tag == null || tag.isBlank()) continue;
                    String tagPattern = "%" + tag.toLowerCase() + "%";
                    tagPredicates.add(cb.like(cb.lower(root.get("tags")), tagPattern));
                }
                if (!tagPredicates.isEmpty()) {
                    predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
                }
            }

            // Date range handling (including invalid ranges)
            if (criteria.startDate() != null && criteria.endDate() != null) {
                if (criteria.startDate().isAfter(criteria.endDate())) {
                    predicates.add(cb.equal(cb.literal(1), 0));
                } else {
                    predicates.add(cb.between(root.get("date"), criteria.startDate(), criteria.endDate()));
                }
            } else if (criteria.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), criteria.startDate()));
            } else if (criteria.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), criteria.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
