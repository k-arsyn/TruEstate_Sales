import React, { useEffect, useState, useMemo } from 'react';
import { fetchSales } from '../services/salesApi.js';
import '../styles/sales-page.css';

const PAGE_SIZE = 10;

export function SalesPage() {
  const [query, setQuery] = useState('');
  const [filters, setFilters] = useState({
    customerRegion: [],
    gender: [],
    minAge: '',
    maxAge: '',
    productCategory: [],
    tags: [],
    paymentMethod: [],
    startDate: '',
    endDate: ''
  });
  const [sortBy, setSortBy] = useState('date');
  const [direction, setDirection] = useState('desc');
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [openDropdown, setOpenDropdown] = useState(null);

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
  }, [query, filters, sortBy, direction, page]);

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
    try {
      const response = await fetchSales({
        q: query,
        ...filters,
        sortBy,
        direction,
        page,
        size: PAGE_SIZE
      });
      setData(response);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  }

  function handleSearchChange(e) {
    setPage(0);
    setQuery(e.target.value);
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

  return (
    <div className="sales-page">
      <header className="sales-header">
        <h1>Sales Management System</h1>
        <input
          type="text"
          placeholder="Name, Phone no."
          value={query}
          onChange={handleSearchChange}
          className="search-input"
        />
      </header>

      <section className="filters-row">
        <button
          type="button"
          className="refresh-button"
          title="Reset filters"
          onClick={() => {
            setQuery('');
            setFilters({
              customerRegion: [],
              gender: [],
              minAge: '',
              maxAge: '',
              productCategory: [],
              tags: [],
              paymentMethod: [],
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
          <div className="filter-label">Customer Region</div>
          <div className="filter-dropdown">
            <button
              type="button"
              className="dropdown-toggle"
              onClick={() => toggleDropdown('customerRegion')}
            >
              {filters.customerRegion.length > 0
                ? filters.customerRegion.join(', ')
                : 'Select Region'}
              <span className="dropdown-arrow">▼</span>
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
          <div className="filter-label">Gender</div>
          <div className="filter-dropdown">
            <button
              type="button"
              className="dropdown-toggle"
              onClick={() => toggleDropdown('gender')}
            >
              {filters.gender.length > 0 ? filters.gender.join(', ') : 'Select Gender'}
              <span className="dropdown-arrow">▼</span>
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
          <div className="filter-label">Age Range</div>
          <input
            type="text"
            placeholder="min-max"
            value={filters.minAge && filters.maxAge ? `${filters.minAge}-${filters.maxAge}` : ''}
            onChange={e => {
              const value = e.target.value.replace(/\s/g, '');
              const [min, max] = value.split('-');
              setPage(0);
              setFilters(prev => ({
                ...prev,
                minAge: min || '',
                maxAge: max || ''
              }));
            }}
          />
        </div>

        {/* Product Category Dropdown */}
        <div className="filter-group">
          <div className="filter-label">Product Category</div>
          <div className="filter-dropdown">
            <button
              type="button"
              className="dropdown-toggle"
              onClick={() => toggleDropdown('productCategory')}
            >
              {filters.productCategory.length > 0
                ? filters.productCategory.join(', ')
                : 'Select Category'}
              <span className="dropdown-arrow">▼</span>
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
          <div className="filter-label">Tags</div>
          <input
            type="text"
            placeholder="comma separated"
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
          <div className="filter-label">Payment Method</div>
          <div className="filter-dropdown">
            <button
              type="button"
              className="dropdown-toggle"
              onClick={() => toggleDropdown('paymentMethod')}
            >
              {filters.paymentMethod.length > 0
                ? filters.paymentMethod.join(', ')
                : 'Select Payment'}
              <span className="dropdown-arrow">▼</span>
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

        {/* Date Range */}
        <div className="filter-group">
          <div className="filter-label">Date Range</div>
          <div className="date-range">
            <input
              type="date"
              value={filters.startDate}
              onChange={e => handleDateChange('startDate', e.target.value)}
            />
            <input
              type="date"
              value={filters.endDate}
              onChange={e => handleDateChange('endDate', e.target.value)}
            />
          </div>
        </div>

        {/* Sort by */}
        <div className="filter-group">
          <div className="filter-label">Sort by</div>
          <select
            value={sortBy + ':' + direction}
            onChange={e => {
              const [field, dir] = e.target.value.split(':');
              setSortBy(field);
              setDirection(dir);
            }}
          >
            <option value="date:desc">Date (Newest)</option>
            <option value="quantity:desc">Quantity</option>
            <option value="customerName:asc">Customer Name (A-Z)</option>
          </select>
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
                <th>Customer Region</th>
                <th>Product Category</th>
                <th>Quantity</th>
                <th>Price / Unit</th>
                <th>Final Amount</th>
                <th>Payment Method</th>
                <th>Order Status</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(row => (
                <tr key={row.id}>
                  <td>{row.transactionId}</td>
                  <td>{row.date}</td>
                  <td>{row.customerId}</td>
                  <td>{row.customerName}</td>
                  <td>{row.phoneNumber}</td>
                  <td>{row.gender}</td>
                  <td>{row.age}</td>
                  <td>{row.customerRegion}</td>
                  <td>{row.productCategory}</td>
                  <td>{row.quantity}</td>
                  <td>{row.pricePerUnit}</td>
                  <td>{row.finalAmount}</td>
                  <td>{row.paymentMethod}</td>
                  <td>{row.orderStatus}</td>
                </tr>
              ))}
            </tbody>
          </table>
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
