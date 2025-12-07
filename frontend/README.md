# Sales Management System - Frontend

## Overview

React application providing interface for retail sales data management. Features real-time search, multi-criteria filtering, and interactive data visualization. Built with Vite for fast development and optimized production builds. Communicates with Spring Boot backend via RESTful API.

## Tech Stack

- React 18
- Vite
- Axios

## Search Implementation Summary

Debounced text input with 500ms delay to minimize API calls. Sends query parameter to backend for server-side search across customer name and phone number. Search resets pagination to first page. Updates happen automatically as user types, with visual loading indicator during fetch operations.

## Filter Implementation Summary

Seven filter types implemented: customer region, gender, age range, product category, tags, payment method, and date. Dropdown filters support multi-select with checkboxes. Age range uses dual-handle slider with snap-to-group behavior. All filters debounced at 300ms to reduce backend requests. Filter state maintained separately from search query to allow independent operation.

## Sorting Implementation Summary

Dropdown selector with predefined sort options: date (newest), quantity (descending), and customer name (alphabetical). Sort parameter sent to backend as field and direction combination. Changing sort maintains current filters and search query but does not reset pagination. Default sort is date descending.

## Pagination Implementation Summary

Page-based navigation with sliding window showing five page numbers at a time. Previous and Next buttons disabled at boundaries. Clicking page number triggers API request with page parameter. Pagination state resets to page zero when search or filters change. Total pages and elements displayed from backend response.

## Setup Instructions

### Prerequisites

- Node.js 16 or higher
- npm

### Installation and Development

```bash
cd frontend
npm install
npm run dev
```

Application runs at http://localhost:5173

### Production Build

```bash
npm run build
npm run preview
```

### Configuration

Backend API endpoint configured in `src/services/salesApi.js`
