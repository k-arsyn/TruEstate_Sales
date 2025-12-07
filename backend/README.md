# Retail Sales Management System - Backend

## Overview

RESTful API built with Spring Boot for managing retail sales data. Supports advanced search, filtering, sorting, and pagination capabilities. Uses H2 in-memory database with JPA specifications for dynamic query building. Includes CSV fallback service for data import.

## Tech Stack

- Spring Boot 3.x
- Java 17
- Spring Data JPA
- Apache Commons CSV
- Maven

## Search Implementation Summary

Full-text search implemented using JPA Criteria API with case-insensitive LIKE queries. Searches across customer name and phone number fields simultaneously. Query pattern uses wildcard matching (%query%) on lowercased text. Works in combination with all active filters without performance degradation.

## Filter Implementation Summary

Multi-value filters for customer region, gender, product category, and payment method use SQL IN clause. Range filters for age (min/max) and date (start/end) use BETWEEN predicates. Tag filter matches comma-separated values with partial string matching. All filters combine using AND logic through JPA Specifications, allowing dynamic query construction based on provided parameters.

## Sorting Implementation Summary

Sorting supports three fields: date, quantity, and customer name. Direction controlled by ascending or descending parameter. Implemented at database level using JPA Sort object passed to repository queries. Default sort is date descending (newest first). Sort applies after filtering but before pagination.

## Pagination Implementation Summary

Page-based pagination using Spring Data Pageable interface. Zero-indexed page numbers with configurable page size (default 10). Returns PagedResponse containing content array, current page, page size, total pages, and total elements. CSV fallback service implements manual pagination by collecting only requested page items during single-pass filtering.

## Setup Instructions

### Prerequisites

- JDK 17 or higher
- Maven 3.6+

### Build and Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Server starts at http://localhost:8080

### API Endpoints

- GET /api/sales - Search and filter sales records
- GET /api/health - Health check endpoint

### Configuration

Edit `src/main/resources/application.properties` to modify:
- Server port
- Database settings
- Logging levels

