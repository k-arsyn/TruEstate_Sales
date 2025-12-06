import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/sales';

export async function fetchSales(params) {
  const queryParams = new URLSearchParams();

  if (params.q) queryParams.set('q', params.q);

  ['customerRegion', 'gender', 'productCategory', 'tags', 'paymentMethod'].forEach(key => {
    const value = params[key];
    if (Array.isArray(value)) {
      value.forEach(v => queryParams.append(key === 'tags' ? 'tag' : key, v));
    }
  });

  if (params.minAge) queryParams.set('minAge', params.minAge);
  if (params.maxAge) queryParams.set('maxAge', params.maxAge);
  if (params.startDate) queryParams.set('startDate', params.startDate);
  if (params.endDate) queryParams.set('endDate', params.endDate);

  queryParams.set('sortBy', params.sortBy || 'date');
  queryParams.set('direction', params.direction || 'desc');
  queryParams.set('page', params.page ?? 0);
  queryParams.set('size', params.size ?? 10);

  const response = await axios.get(`${API_BASE}?${queryParams.toString()}`);
  return response.data;
}

