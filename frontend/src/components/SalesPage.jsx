import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { fetchSales } from '../services/salesApi.js';
import '../styles/sales-page.css';

const PAGE_SIZE = 10;

// Debounce hook
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

export function SalesPage() {
  const [query, setQuery] = useState('');
  const [filters, setFilters] = useState({
    customerRegion: [],
    gender: [],
    minAge: 18,
    maxAge: 100,
    productCategory: [],
    tags: [],
    paymentMethod: [],
    date: '',
    startDate: '',
    endDate: ''
  });

  // Separate state for slider dragging (local only, not triggering API)
  const [localAgeRange, setLocalAgeRange] = useState({ minAge: 18, maxAge: 100 });

  const [sortBy, setSortBy] = useState('date');
  const [direction, setDirection] = useState('desc');
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [openDropdown, setOpenDropdown] = useState(null);
  const [copiedPhone, setCopiedPhone] = useState(null);

  // Debounce query and filters
  const debouncedQuery = useDebounce(query, 500);
  const debouncedFilters = useDebounce(filters, 300);

  // derived totals for current page
  const { totalUnits, totalAmount, totalDiscount } = useMemo(() => {
    const units = data.content.reduce((sum, row) => sum + (row.quantity || 0), 0);
    const amount = data.content.reduce((sum, row) => sum + (row.totalAmount || 0), 0);
    const discount = data.content.reduce(
      (sum, row) => sum + ((row.totalAmount || 0) - (row.finalAmount || 0)),
      0
    );
    return { totalUnits: units, totalAmount: amount, totalDiscount: discount };
  }, [data]);

  useEffect(() => {
    loadData();
  }, [debouncedQuery, debouncedFilters, sortBy, direction, page]);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (!event.target.closest('.filter-dropdown')) {
        setOpenDropdown(null);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');
    const params = {
      q: debouncedQuery,
      ...debouncedFilters,
      sortBy,
      direction,
      page,
      size: PAGE_SIZE
    };
    console.debug('fetchSales params ->', params);
    try {
      const response = await fetchSales(params);
      console.debug('fetchSales response ->', response);
      // normalize response in case backend returns Page or PagedResponse
      if (response && response.content !== undefined) {
        setData(response);
      } else if (response && response.page !== undefined && response.content !== undefined) {
        setData(response);
      } else if (Array.isArray(response)) {
        setData({ content: response, totalPages: 1, totalElements: response.length });
      } else {
        // unknown shape
        setData({ content: [], totalPages: 0, totalElements: 0 });
      }
    } catch (err) {
      console.error('Failed to load data', err);
      const msg = err?.response?.data || err?.message || 'Failed to load data';
      setError(String(msg));
    } finally {
      setLoading(false);
    }
  }

  function handleSearchChange(e) {
    setPage(0);
    setQuery(e.target.value);
  }

  function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(() => {
        setCopiedPhone(text);
        setTimeout(() => setCopiedPhone(null), 2000);
      }).catch(err => {
        console.error('Failed to copy:', err);
      });
    } else {
      // Fallback for older browsers
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        setCopiedPhone(text);
        setTimeout(() => setCopiedPhone(null), 2000);
      } catch (err) {
        console.error('Failed to copy:', err);
      }
      document.body.removeChild(textarea);
    }
  }

  function handleMultiSelectChange(name, values) {
    setPage(0);
    setFilters(prev => ({ ...prev, [name]: values }));
  }

  function handleDateChange(name, value) {
    setPage(0);
    setFilters(prev => ({ ...prev, [name]: value }));
  }

  function nextPage() {
    if (page + 1 < data.totalPages) {
      setPage(p => p + 1);
    }
  }

  function prevPage() {
    if (page > 0) {
      setPage(p => p - 1);
    }
  }

  function toggleDropdown(name) {
    setOpenDropdown(openDropdown === name ? null : name);
  }

  function handleCheckboxChange(filterName, value) {
    const current = filters[filterName] || [];
    const newValues = current.includes(value)
      ? current.filter(v => v !== value)
      : [...current, value];
    handleMultiSelectChange(filterName, newValues);
  }

  function handleAgeChange(type, value) {
    setPage(0);
    const numValue = parseInt(value) || (type === 'min' ? 18 : 100);

    if (type === 'min') {
      setFilters(prev => ({
        ...prev,
        minAge: Math.min(numValue, prev.maxAge)
      }));
    } else {
      setFilters(prev => ({
        ...prev,
        maxAge: Math.max(numValue, prev.minAge)
      }));
    }
  }

  function snapToAgeGroup(value, type) {
    const ageGroups = [18, 24, 25, 34, 35, 44, 45, 54, 55, 64, 65, 100];

    if (type === 'min') {
      // Find closest lower snap point
      for (let i = ageGroups.length - 1; i >= 0; i--) {
        if (value >= ageGroups[i]) return ageGroups[i];
      }
      return 18;
    } else {
      // Find closest upper snap point
      for (let i = 0; i < ageGroups.length; i++) {
        if (value <= ageGroups[i]) return ageGroups[i];
      }
      return 100;
    }
  }

  return (
    <div className="sales-page">
      <header className="sales-header">
        <h1>Sales Management System</h1>
        <div className="search-container">
          <svg className="search-icon" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 12C9.76142 12 12 9.76142 12 7C12 4.23858 9.76142 2 7 2C4.23858 2 2 4.23858 2 7C2 9.76142 4.23858 12 7 12Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M10.5 10.5L14 14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          <input
            type="text"
            placeholder="Name, Phone no."
            value={query}
            onChange={handleSearchChange}
            className="search-input"
          />
        </div>
      </header>

      <section className="filters-row">
        <div className="filters-left">
          <button
            type="button"
            className="refresh-button"
            title="Reset filters"
            onClick={() => {
              setQuery('');
              setFilters({
                customerRegion: [],
                gender: [],
                minAge: 18,
                maxAge: 100,
                productCategory: [],
                tags: [],
                paymentMethod: [],
                date: '',
                startDate: '',
                endDate: ''
              });
              setPage(0);
            }}
          >
            ⟳
          </button>

          {/* Customer Region Dropdown */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('customerRegion')}
              >
                {filters.customerRegion.length > 0
                  ? filters.customerRegion.join(', ')
                  : 'Customer Region'}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'customerRegion' && (
                <div className="dropdown-menu">
                  {['North', 'South', 'East', 'West', 'Central'].map(option => (
                    <label key={option} className="dropdown-option">
                      <input
                        type="checkbox"
                        checked={filters.customerRegion.includes(option)}
                        onChange={() => handleCheckboxChange('customerRegion', option)}
                      />
                      {option}
                    </label>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Gender Dropdown */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('gender')}
              >
                {filters.gender.length > 0 ? filters.gender.join(', ') : 'Gender'}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'gender' && (
                <div className="dropdown-menu">
                  {['Male', 'Female', 'Other'].map(option => (
                    <label key={option} className="dropdown-option">
                      <input
                        type="checkbox"
                        checked={filters.gender.includes(option)}
                        onChange={() => handleCheckboxChange('gender', option)}
                      />
                      {option}
                    </label>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Age Range */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('ageRange')}
              >
                {`Age Range: ${filters.minAge}-${filters.maxAge}`}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'ageRange' && (
                <div className="dropdown-menu age-range-dropdown">
                  <div className="age-range-container">
                    <div className="age-inputs">
                      <div className="age-input-group">
                        <label>Min</label>
                        <input
                          type="number"
                          min="18"
                          max="100"
                          value={filters.minAge}
                          onChange={(e) => handleAgeChange('min', e.target.value)}
                          onBlur={(e) => {
                            const snapped = snapToAgeGroup(parseInt(e.target.value) || 18, 'min');
                            handleAgeChange('min', snapped);
                          }}
                        />
                      </div>
                      <div className="age-input-group">
                        <label>Max</label>
                        <input
                          type="number"
                          min="18"
                          max="100"
                          value={filters.maxAge}
                          onChange={(e) => handleAgeChange('max', e.target.value)}
                          onBlur={(e) => {
                            const snapped = snapToAgeGroup(parseInt(e.target.value) || 100, 'max');
                            handleAgeChange('max', snapped);
                          }}
                        />
                      </div>
                    </div>
                    <div className="age-slider-container">
                      <div className="age-slider-track">
                        <div
                          className="age-slider-range"
                          style={{
                            left: `${((localAgeRange.minAge - 18) / 82) * 100}%`,
                            width: `${((localAgeRange.maxAge - localAgeRange.minAge) / 82) * 100}%`
                          }}
                        ></div>
                        <input
                          type="range"
                          min="18"
                          max="100"
                          value={localAgeRange.minAge}
                          onChange={(e) => {
                            const value = parseInt(e.target.value);
                            setLocalAgeRange(prev => ({
                              ...prev,
                              minAge: Math.min(value, prev.maxAge)
                            }));
                          }}
                          onMouseUp={(e) => {
                            const value = parseInt(e.target.value);
                            const snapped = snapToAgeGroup(value, 'min');
                            setLocalAgeRange(prev => ({
                              ...prev,
                              minAge: snapped
                            }));
                            handleAgeChange('min', snapped);
                          }}
                          onTouchEnd={(e) => {
                            const value = parseInt(e.target.value);
                            const snapped = snapToAgeGroup(value, 'min');
                            setLocalAgeRange(prev => ({
                              ...prev,
                              minAge: snapped
                            }));
                            handleAgeChange('min', snapped);
                          }}
                          className="age-slider age-slider-min"
                        />
                        <input
                          type="range"
                          min="18"
                          max="100"
                          value={localAgeRange.maxAge}
                          onChange={(e) => {
                            const value = parseInt(e.target.value);
                            setLocalAgeRange(prev => ({
                              ...prev,
                              maxAge: Math.max(value, prev.minAge)
                            }));
                          }}
                          onMouseUp={(e) => {
                            const value = parseInt(e.target.value);
                            const snapped = snapToAgeGroup(value, 'max');
                            setLocalAgeRange(prev => ({
                              ...prev,
                              maxAge: snapped
                            }));
                            handleAgeChange('max', snapped);
                          }}
                          onTouchEnd={(e) => {
                            const value = parseInt(e.target.value);
                            const snapped = snapToAgeGroup(value, 'max');
                            setLocalAgeRange(prev => ({
                              ...prev,
                              maxAge: snapped
                            }));
                            handleAgeChange('max', snapped);
                          }}
                          className="age-slider age-slider-max"
                        />
                      </div>
                      <div className="age-slider-labels">
                        <span>18</span>
                        <span>100+</span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Product Category Dropdown */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('productCategory')}
              >
                {filters.productCategory.length > 0
                  ? filters.productCategory.join(', ')
                  : 'Product Category'}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'productCategory' && (
                <div className="dropdown-menu">
                  {['Clothing', 'Beauty', 'Electronic', 'Grocery'].map(option => (
                    <label key={option} className="dropdown-option">
                      <input
                        type="checkbox"
                        checked={filters.productCategory.includes(option)}
                        onChange={() => handleCheckboxChange('productCategory', option)}
                      />
                      {option}
                    </label>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Tags */}
          <div className="filter-group">
            <input
              type="text"
              placeholder="Tags"
              value={filters.tags.join(',')}
              onChange={e =>
                handleMultiSelectChange(
                  'tags',
                  e.target.value
                    .split(',')
                    .map(v => v.trim())
                    .filter(Boolean)
                )
              }
            />
          </div>

          {/* Payment Method Dropdown */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('paymentMethod')}
              >
                {filters.paymentMethod.length > 0
                  ? filters.paymentMethod.join(', ')
                  : 'Payment Method'}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'paymentMethod' && (
                <div className="dropdown-menu">
                  {['Cash', 'UPI', 'Debit Card', 'Credit Card'].map(option => (
                    <label key={option} className="dropdown-option">
                      <input
                        type="checkbox"
                        checked={filters.paymentMethod.includes(option)}
                        onChange={() => handleCheckboxChange('paymentMethod', option)}
                      />
                      {option}
                    </label>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Date */}
          <div className="filter-group">
            <div className="filter-dropdown">
              <button
                type="button"
                className="dropdown-toggle"
                onClick={() => toggleDropdown('date')}
              >
                {filters.date || 'Date'}
                <span className="dropdown-arrow">▾</span>
              </button>
              {openDropdown === 'date' && (
                <div className="dropdown-menu date-picker-dropdown">
                  <input
                    type="date"
                    value={filters.date}
                    onChange={e => {
                      handleDateChange('date', e.target.value);
                      setOpenDropdown(null);
                    }}
                  />
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="filters-right">
          {/* Sort by */}
          <div className="filter-group">
            <select
              value={sortBy + ':' + direction}
              onChange={e => {
                const [field, dir] = e.target.value.split(':');
                setSortBy(field);
                setDirection(dir);
              }}
            >
              <option value="date:desc">Sort by: Date (Newest)</option>
              <option value="quantity:desc">Sort by: Quantity</option>
              <option value="customerName:asc">Sort by: Customer Name (A-Z)</option>
            </select>
          </div>
        </div>
      </section>

      <section className="summary-row">
        <div className="summary-card">
          <div className="summary-label">Total units sold</div>
          <div className="summary-value">{totalUnits}</div>
        </div>
        <div className="summary-card">
          <div className="summary-label">Total Amount</div>
          <div className="summary-value">₹{totalAmount.toFixed(2)}</div>
        </div>
        <div className="summary-card">
          <div className="summary-label">Total Discount</div>
          <div className="summary-value">₹{totalDiscount.toFixed(2)}</div>
        </div>
      </section>

      <section className="table-section">
        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}
        {!loading && !error && data.content.length === 0 && (
          <p>No results match the current search and filters.</p>
        )}

        {!loading && !error && data.content.length > 0 && (
          <div className="table-wrapper">
            <table className="sales-table">
              <thead>
                <tr>
                  <th>Transaction ID</th>
                  <th>Date</th>
                  <th>Customer ID</th>
                  <th>Customer Name</th>
                  <th>Phone Number</th>
                  <th>Gender</th>
                  <th>Age</th>
                  <th>Product Category</th>
                  <th>Quantity</th>
                  <th>Final Amount</th>
                  <th>Customer Region</th>
                  <th>Product ID</th>
                  <th>Employee Name</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map(row => (
                  <tr key={row.id}>
                    <td>{row.transactionId}</td>
                    <td>{row.date}</td>
                    <td>{row.customerId}</td>
                    <td>{row.customerName}</td>
                    <td>
                      <div className="phone-number-cell">
                        <span>{row.phoneNumber}</span>
                        <button
                          className={`copy-button ${copiedPhone === row.phoneNumber ? 'copied' : ''}`}
                          onClick={() => copyToClipboard(row.phoneNumber)}
                          title="Copy phone number"
                        >
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                          </svg>
                        </button>
                        {copiedPhone === row.phoneNumber && (
                          <span className="copied-tooltip">Copied!</span>
                        )}
                      </div>
                    </td>
                    <td>{row.gender}</td>
                    <td>{row.age}</td>
                    <td>{row.productCategory}</td>
                    <td>{row.quantity}</td>
                    <td>₹{row.finalAmount?.toFixed(2)}</td>
                    <td>{row.customerRegion}</td>
                    <td>{row.productId}</td>
                    <td>{row.employeeName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <footer className="pagination-footer">
        <button onClick={prevPage} disabled={page === 0}>
          Previous
        </button>
        <div className="page-numbers">
          {(() => {
            const total = data.totalPages || 0;
            if (total === 0) return null;
            const windowSize = 5;
            let start = Math.max(0, page - Math.floor(windowSize / 2));
            let end = start + windowSize - 1;
            if (end >= total - 1) {
              end = total - 1;
              start = Math.max(0, end - windowSize + 1);
            }
            const buttons = [];
            for (let i = start; i <= end; i++) {
              buttons.push(
                <button
                  key={i}
                  className={i === page ? 'page-btn active' : 'page-btn'}
                  onClick={() => setPage(i)}
                >
                  {i + 1}
                </button>
              );
            }
            return buttons;
          })()}
        </div>
        <button onClick={nextPage} disabled={page + 1 >= data.totalPages}>
          Next
        </button>
      </footer>
    </div>
  );
}
