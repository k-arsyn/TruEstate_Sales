# Architecture

## Backend Architecture

The backend follows a layered architecture pattern with clear separation of concerns. Spring Boot handles dependency injection and configuration management. The application uses JPA for database operations with specifications pattern for dynamic query building.

### Technology Stack

- Spring Boot 3.x
- Java 17
- Spring Data JPA
- H2 Database
- Apache Commons CSV
- Maven

### Folder Structure

```
backend/src/main/java/com/truestate/retail/
├── RetailBackendApplication.java
├── config/
│   └── CorsConfig.java
├── controllers/
│   ├── SaleRecordController.java
│   ├── PagedResponse.java
│   └── HealthController.java
├── models/
│   ├── SaleRecord.java
│   └── SaleRecordRepository.java
├── services/
│   ├── SaleRecordService.java
│   ├── SaleRecordSpecification.java
│   └── CsvFallbackService.java
└── utils/
    └── CsvDataLoader.java
```

### Module Responsibilities

**Controllers**: Handle HTTP requests and responses. SaleRecordController exposes GET endpoint for sales data retrieval. PagedResponse provides consistent API response structure. HealthController offers service health monitoring.

**Services**: Contain business logic and orchestration. SaleRecordService routes requests to appropriate data source (database or CSV). SaleRecordSpecification builds dynamic queries using JPA Criteria API. CsvFallbackService handles CSV streaming when database is empty.

**Models**: Define data structure and persistence. SaleRecord entity maps to database table with all transaction fields. SaleRecordRepository extends JpaRepository and JpaSpecificationExecutor for query operations.

**Utils**: Provide helper functions. CsvDataLoader parses CSV records and converts them to entity objects.

**Config**: Manage application configuration. CorsConfig allows cross-origin requests from frontend.

### Data Flow

1. HTTP request arrives at controller with query parameters
2. Controller extracts search, filter, sort, and pagination parameters
3. Service layer checks database for existing records
4. If database has data, JPA specification builds dynamic query with predicates
5. If database is empty, CSV fallback service streams and filters data
6. Results wrapped in PagedResponse with metadata
7. Controller returns JSON response to client

## Frontend Architecture

The frontend uses component-based architecture with React. State management handled through React hooks. API communication abstracted in service layer. Styling implemented with modular CSS files.

### Technology Stack

- React 18
- Vite
- Axios
- JavaScript (JSX)

### Folder Structure

```
frontend/src/
├── components/
│   └── SalesPage.jsx
├── services/
│   └── salesApi.js
├── styles/
│   ├── global.css
│   └── sales-page.css
├── hooks/
├── utils/
├── App.jsx
└── main.jsx
```

### Module Responsibilities

**Components**: Contain UI logic and rendering. SalesPage manages all sales-related functionality including search, filters, sorting, pagination, and data display.

**Services**: Handle external communication. salesApi constructs API requests and processes responses.

**Styles**: Define visual appearance. Global styles apply application-wide. Component styles scope to specific features.

**Hooks**: Custom React hooks for reusable logic. Currently includes debounce implementation within components.

**Utils**: Utility functions for common operations.

### Data Flow

1. User interaction triggers state update in component
2. Debounce mechanism delays API call to reduce requests
3. Service layer constructs query parameters from state
4. Axios sends GET request to backend
5. Response normalized and stored in component state
6. React re-renders affected components with new data
7. Summary cards, table, and pagination update accordingly

## Search Implementation

Backend performs case-insensitive search using SQL LIKE operator on customer name and phone number fields. Query lowercased before matching. Frontend debounces input to prevent excessive API calls. Search combines with active filters using AND logic.

## Filter Implementation

Backend uses JPA Specifications to build predicates dynamically. Multi-select filters (region, gender, category, payment method) converted to IN clauses. Range filters (age, date) use comparison operators. Tag filter performs partial string matching. All predicates combined into single query.

Frontend maintains filter state separately from search. Dropdown filters use checkboxes for multiple selections. Age range implemented with dual-handle slider snapping to predefined groups. Date picker allows single date selection converted to start and end dates. Changes debounced to reduce backend load.

## Sorting Implementation

Backend receives sort field and direction parameters. Creates Sort object passed to repository query. Sorting applied at database level before pagination. Supports date, quantity, and customer name fields.

Frontend provides dropdown with predefined sort options. Selection splits into field and direction sent to backend. Sort state independent of filters and search.

## Pagination Implementation

Backend uses Spring Data Pageable for page-based retrieval. Calculates offset and limit for database query. Returns page content with metadata (total pages, total elements, current page, page size).

Frontend displays sliding window of page numbers. Previous and Next buttons for navigation. Page clicks update state triggering new API request. Pagination resets when search or filters change but not when sort changes.

## API Specification

### GET /api/sales

Query parameters:
- q: search query
- customerRegion, gender, productCategory, paymentMethod: array values
- minAge, maxAge: integer range
- startDate, endDate: date range (YYYY-MM-DD)
- tag: array of tags
- sortBy: field name (date, quantity, customerName)
- direction: asc or desc
- page: zero-indexed page number
- size: records per page

Response format:
```
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalPages": 50,
  "totalElements": 500
}
```

## Performance Considerations

Backend uses database-level filtering and pagination to minimize data transfer. JPA specifications avoid N+1 query problems. CSV fallback streams data instead of loading entire file into memory.

Frontend debounces user input to reduce API calls. Local state for slider provides smooth UX without backend requests during drag. Summary calculations memoized to prevent unnecessary recalculations.

## Known Limitations and Future Improvements

### Backend Optimizations Not Implemented

**Database Indexing**: Currently relies on H2 default indexes. Production deployment requires composite indexes on frequently queried columns (customerName, phoneNumber, date, customerRegion) to improve search and filter performance.

**Caching Layer**: No caching mechanism implemented. Adding Redis or Caffeine cache for frequently accessed queries would reduce database load. Cache invalidation strategy needed for data updates.


### Features Planned But Not Implemented

**Export Functionality**: No CSV or Excel export despite backend having data source. Requires streaming large datasets to avoid memory issues.

**Advanced Search**: Limited to exact substring matching. Fuzzy search, wildcard operators, and field-specific search would improve usability.

**Filter Presets**: Users cannot save frequently used filter combinations. Preset system requires backend storage of user preferences.

**Real-time Updates**: Data static after page load. WebSocket implementation would enable live updates when backend data changes.

### Testing Gaps

**Unit Tests**: No test coverage for backend services and specifications. JUnit tests needed for business logic validation.

**Integration Tests**: API endpoints lack integration tests. Spring Boot Test framework should verify request/response contracts.

**Frontend Testing**: No component tests or end-to-end tests. Jest and React Testing Library needed for component behavior validation.

**Performance Testing**: Load testing not performed. JMeter or Gatling tests required to validate system behavior under concurrent user load.

### Security Concerns

**Input Validation**: Limited validation on query parameters. Malicious input could cause unexpected behavior. Comprehensive validation and sanitization required.

**Rate Limiting**: No request throttling implemented. API vulnerable to abuse without rate limiting per IP or user.

**Authentication**: Endpoints completely open. Production requires authentication mechanism (JWT, OAuth2) and authorization checks.
