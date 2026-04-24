import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { Pagination, Empty, Skeleton, Card } from 'antd';
import { getAnnouncements, getFilterOptions } from '@/services/announcement';
import { recordPageView } from '@/services/tracking';
import { useFilter } from '@/hooks/useFilter';
import { DEFAULT_PAGE_SIZE } from '@/utils/constants';
import type { AnnouncementListResponse } from '@/types/announcement';
import FilterBar from '@/components/FilterBar';
import AnnouncementCard from '@/components/AnnouncementCard';
import styles from './index.module.css';

export default function AnnouncementList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { filters, setFilters, applyFilters, resetFilters } = useFilter();

  const activePage = Number(searchParams.get('page')) || 1;
  const activeKeyword = searchParams.get('keyword') || undefined;
  const activeClassTypeIds = searchParams.get('classTypeIds')?.split(',').map(Number).filter(Boolean) || undefined;
  const activeCampusTypeIds = searchParams.get('campusTypeIds')?.split(',').map(Number).filter(Boolean) || undefined;
  const activeCityIds = searchParams.get('cityIds')?.split(',').map(Number).filter(Boolean) || undefined;
  const activeApplyStatus = searchParams.get('applyStatus') || undefined;

  const { data: filterOptions } = useQuery({
    queryKey: ['filterOptions'],
    queryFn: getFilterOptions,
    staleTime: 5 * 60 * 1000,
  });

  const { data, isLoading } = useQuery({
    queryKey: ['announcements', activeKeyword, activeClassTypeIds, activeCampusTypeIds, activeCityIds, activeApplyStatus, activePage],
    queryFn: () =>
      getAnnouncements({
        keyword: activeKeyword,
        classTypeIds: activeClassTypeIds,
        campusTypeIds: activeCampusTypeIds,
        cityIds: activeCityIds,
        applyStatus: activeApplyStatus,
        page: activePage,
        pageSize: DEFAULT_PAGE_SIZE,
      }),
    placeholderData: keepPreviousData,
  });

  useEffect(() => {
    recordPageView(window.location.href, 'list');
  }, []);

  const handlePageChange = (page: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(page));
    setSearchParams(params);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className={styles.page}>
      {/* Stats Bar */}
      <div className={styles.statsBar}>
        <div className={styles.statChip}>
          <span className={styles.statNum}>{data?.total ?? '-'}</span> 条在招
          <span className={`${styles.dot} ${styles.dotOngoing}`} />
        </div>
        <div className={styles.statChip}>
          <span className={styles.statNum}>{data?.total ?? '-'}</span> 条已截止
          <span className={`${styles.dot} ${styles.dotExpired}`} />
        </div>
        <div className={styles.statChip}>
          <span className={styles.statNum}>{data?.total ?? '-'}</span> 条即将开始
          <span className={`${styles.dot} ${styles.dotNotStarted}`} />
        </div>
      </div>

      {filterOptions && (
        <FilterBar
          filterOptions={filterOptions}
          filters={filters}
          onFiltersChange={setFilters}
          onApply={applyFilters}
          onReset={resetFilters}
        />
      )}

      {isLoading ? (
        <div className={styles.skeletonGrid}>
          {Array.from({ length: 6 }).map((_, i) => (
            <Card key={i} style={{ height: 260 }}>
              <Skeleton active paragraph={{ rows: 3 }} />
            </Card>
          ))}
        </div>
      ) : !data?.list?.length ? (
        <div className={styles.emptyState}>
          <Empty
            description={
              searchParams.toString()
                ? '没有符合条件的招聘公告，请调整筛选条件'
                : '暂无招聘公告'
            }
          />
        </div>
      ) : (
        <>
          <div className={styles.grid}>
            {data.list.map((item: AnnouncementListResponse) => (
              <AnnouncementCard key={item.announcementId} data={item} />
            ))}
          </div>
          {data.totalPages > 1 && (
            <div className={styles.pagination}>
              <Pagination
                current={data.page}
                total={data.total}
                pageSize={data.pageSize}
                onChange={handlePageChange}
                showSizeChanger={false}
                showTotal={(total) => `共 ${total} 条`}
              />
            </div>
          )}
        </>
      )}
    </div>
  );
}
