import { Tag } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import type { AnnouncementListResponse } from '@/types/announcement';
import { APPLY_STATUS_LABELS, ROUTES } from '@/utils/constants';
import styles from './index.module.css';

const MAX_CITIES = 5;

interface AnnouncementCardProps {
  data: AnnouncementListResponse;
}

export default function AnnouncementCard({ data }: AnnouncementCardProps) {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(ROUTES.ANNOUNCEMENTS + '/' + data.announcementId);
  };

  const displayCities = data.cityNames.length > MAX_CITIES
    ? [...data.cityNames.slice(0, MAX_CITIES), '等']
    : data.cityNames;

  const statusClass = data.applyStatus === 'ongoing' ? styles.ongoing
    : data.applyStatus === 'expired' ? styles.expired
    : data.applyStatus === 'not_started' ? styles.notStarted
    : '';

  return (
    <div
      className={`${styles.card} ${statusClass}`}
      onClick={handleClick}
    >
      <div className={styles.cardBody}>
        <div className={styles.companyName}>{data.companyName}</div>
        <div className={styles.announcementName}>{data.name}</div>
        <div className={styles.tags}>
          {data.classTypeNames.map((name) => (
            <Tag key={name} className={styles.tagClass}>{name}</Tag>
          ))}
          {data.campusTypeNames.map((name) => (
            <Tag key={name} className={styles.tagCampus}>{name}</Tag>
          ))}
        </div>
        <div className={styles.cities}>
          <EnvironmentOutlined className={styles.locIcon} />
          <span className={styles.cityText}>{displayCities.join(' / ')}</span>
        </div>
      </div>
      <div className={styles.footer}>
        <span className={styles.date}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="14" height="14">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          截止: {dayjs(data.expiredAt).format('YYYY-MM-DD')}
        </span>
        <span className={`${styles.badge} ${styles[`badge_${data.applyStatus}`] || ''}`}>
          {data.applyStatus === 'ongoing' && <span className={styles.pulse} />}
          {APPLY_STATUS_LABELS[data.applyStatus] || data.applyStatus}
        </span>
      </div>
    </div>
  );
}
