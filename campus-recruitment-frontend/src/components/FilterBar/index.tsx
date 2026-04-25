import { Input, Select, Radio } from 'antd';
import type { FilterOptionsResponse } from '@/types/announcement';
import type { FilterFormState } from '@/hooks/useFilter';
import { APPLY_STATUS_LABELS } from '@/utils/constants';
import styles from './index.module.css';

interface FilterBarProps {
  filterOptions: FilterOptionsResponse;
  filters: FilterFormState;
  onFiltersChange: (filters: FilterFormState) => void;
  onApply: () => void;
  onReset: () => void;
}

export default function FilterBar({ filterOptions, filters, onFiltersChange, onApply, onReset }: FilterBarProps) {
  const update = (partial: Partial<FilterFormState>) => {
    onFiltersChange({ ...filters, ...partial });
  };

  const handleSearch = (value: string) => {
    update({ keyword: value });
  };

  return (
    <div className={styles.filterBar}>
      <div className={styles.filterRow}>
        <div className={styles.searchItem}>
          <label>搜索</label>
          <Input.Search
            placeholder="输入公司名称搜索"
            value={filters.keyword}
            onChange={(e) => update({ keyword: e.target.value })}
            onSearch={handleSearch}
            enterButton="搜索"
            allowClear
          />
        </div>
        <div className={styles.filterGroup}>
          <label>毕业年份</label>
          <Select
            mode="multiple"
            placeholder="选择毕业年份"
            value={filters.classTypeIds}
            onChange={(val) => update({ classTypeIds: val })}
            options={filterOptions.classTypes.map((t) => ({ value: t.id, label: t.name }))}
            maxTagCount={2}
            allowClear
          />
        </div>
        <div className={styles.filterGroup}>
          <label>招聘批次</label>
          <Select
            mode="multiple"
            placeholder="选择招聘批次"
            value={filters.campusTypeIds}
            onChange={(val) => update({ campusTypeIds: val })}
            options={filterOptions.campusTypes.map((t) => ({ value: t.id, label: t.name }))}
            maxTagCount={2}
            allowClear
          />
        </div>
        <div className={styles.filterGroup}>
          <label>工作城市</label>
          <Select
            mode="multiple"
            placeholder="选择城市"
            value={filters.cityIds}
            onChange={(val) => update({ cityIds: val })}
            options={[
              ...filterOptions.cities
                .filter((c) => c.isTop)
                .map((c) => ({ value: c.id, label: c.name })),
              { value: -1, label: '---', disabled: true },
              ...filterOptions.cities
                .filter((c) => !c.isTop)
                .map((c) => ({ value: c.id, label: c.name })),
            ].filter((item, idx, arr) =>
              item.label !== '---' || (item.label === '---' && idx > 0 && arr[idx - 1]?.label !== '---')
            )}
            maxTagCount={2}
            allowClear
          />
        </div>
        <div className={styles.filterGroup}>
          <label>投递状态</label>
          <Radio.Group
            value={filters.applyStatus}
            onChange={(e) => update({ applyStatus: e.target.value })}
            optionType="button"
            buttonStyle="solid"
            size="middle"
          >
            {Object.entries(APPLY_STATUS_LABELS).map(([key, label]) => (
              <Radio.Button key={key} value={key}>
                {label}
              </Radio.Button>
            ))}
          </Radio.Group>
        </div>
      </div>
      <div className={styles.actions}>
        <button className={styles.applyBtn} onClick={onApply}>
          应用筛选
        </button>
        <button className={styles.resetBtn} onClick={onReset}>
          重置
        </button>
      </div>
    </div>
  );
}
