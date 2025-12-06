package com.truestate.retail.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long>, JpaSpecificationExecutor<SaleRecord> {
}

