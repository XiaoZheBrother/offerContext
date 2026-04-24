import { useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';

export interface FilterFormState {
  keyword: string;
  classTypeIds: number[];
  campusTypeIds: number[];
  cityIds: number[];
  applyStatus: string;
}

const defaultFilters: FilterFormState = {
  keyword: '',
  classTypeIds: [],
  campusTypeIds: [],
  cityIds: [],
  applyStatus: 'all',
};

export function useFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<FilterFormState>({
    keyword: searchParams.get('keyword') || '',
    classTypeIds: searchParams.get('classTypeIds')?.split(',').map(Number).filter(Boolean) || [],
    campusTypeIds: searchParams.get('campusTypeIds')?.split(',').map(Number).filter(Boolean) || [],
    cityIds: searchParams.get('cityIds')?.split(',').map(Number).filter(Boolean) || [],
    applyStatus: searchParams.get('applyStatus') || 'all',
  });

  const applyFilters = useCallback(() => {
    const params: Record<string, string> = {};
    const page = searchParams.get('page');
    if (filters.keyword) params.keyword = filters.keyword;
    if (filters.classTypeIds.length) params.classTypeIds = filters.classTypeIds.join(',');
    if (filters.campusTypeIds.length) params.campusTypeIds = filters.campusTypeIds.join(',');
    if (filters.cityIds.length) params.cityIds = filters.cityIds.join(',');
    if (filters.applyStatus && filters.applyStatus !== 'all') params.applyStatus = filters.applyStatus;
    if (page && page !== '1') params.page = page;
    setSearchParams(params);
  }, [filters, searchParams, setSearchParams]);

  const resetFilters = useCallback(() => {
    setFilters(defaultFilters);
    setSearchParams({});
  }, [setSearchParams]);

  return { filters, setFilters, applyFilters, resetFilters };
}
