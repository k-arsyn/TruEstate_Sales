import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/sales';

export async function fetchSales(params) {
  const queryParams = new URLSearchParams();

  if (params.q) queryParams.set('q', params.q);

  ['customerRegion', 'gender', 'productCategory', 'tags', 'paymentMethod'].forEach(key => {
    const value = params[key];
    if (Array.isArray(value)) {
      value.forEach(v => queryParams.append(key === 'tags' ? 'tag' : key, v));
    }
  });

  // Handle date filter - use single date as both startDate and endDate
  const startDate = params.startDate || params.date;
  const endDate = params.endDate || params.date;

  if (params.minAge !== undefined && params.minAge !== null) queryParams.set('minAge', params.minAge);
  if (params.maxAge !== undefined && params.maxAge !== null) queryParams.set('maxAge', params.maxAge);
  if (startDate) queryParams.set('startDate', startDate);
  if (endDate) queryParams.set('endDate', endDate);

  queryParams.set('sortBy', params.sortBy || 'date');
  queryParams.set('direction', params.direction || 'desc');
  queryParams.set('page', params.page ?? 0);
  queryParams.set('size', params.size ?? 10);

  const url = `${API_BASE}?${queryParams.toString()}`;
  const response = await axios.get(url, { timeout: 10000 });
  return response.data;
}
