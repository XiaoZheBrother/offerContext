import { useState } from 'react';
import { Tag, message } from 'antd';
import { EnvironmentOutlined, HeartFilled, HeartOutlined, CheckCircleFilled, CheckCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import type { AnnouncementListResponse } from '@/types/announcement';
import { APPLY_STATUS_LABELS, ROUTES } from '@/utils/constants';
import { APPLICATION_STATUS_LABELS } from '@/types/user';
import { useUserAuthStore } from '@/store/userAuthStore';
import { addFavorite, removeFavorite } from '@/services/favorites';
import { toggleApplication } from '@/services/applications';
import styles from './index.module.css';

const MAX_CITIES = 5;

interface AnnouncementCardProps {
  data: AnnouncementListResponse;
  onLoginClick?: () => void;
  onStatusChange?: () => void;
}

export default function AnnouncementCard({ data, onLoginClick, onStatusChange }: AnnouncementCardProps) {
  const navigate = useNavigate();
  const { user } = useUserAuthStore();
  const [isFavorited, setIsFavorited] = useState(data.isFavorited || false);
  const [isApplied, setIsApplied] = useState(data.isApplied || false);
  const [appStatus, setAppStatus] = useState(data.applicationStatus || null);
  const [favLoading, setFavLoading] = useState(false);
  const [appLoading, setAppLoading] = useState(false);

  const handleClick = () => {
    navigate(ROUTES.ANNOUNCEMENTS + '/' + data.announcementId);
  };

  const handleFavorite = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!user) {
      onLoginClick?.();
      return;
    }
    setFavLoading(true);
    try {
      if (isFavorited) {
        await removeFavorite(data.announcementId);
        setIsFavorited(false);
      } else {
        await addFavorite(data.announcementId);
        setIsFavorited(true);
      }
      onStatusChange?.();
    } catch {
      // error handled by request interceptor
    } finally {
      setFavLoading(false);
    }
  };

  const handleToggleApply = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!user) {
      onLoginClick?.();
      return;
    }
    setAppLoading(true);
    try {
      const result = await toggleApplication({ announcementId: data.announcementId });
      if (result === 'applied') {
        setIsApplied(true);
        setAppStatus('APPLIED');
      } else if (result === 'removed') {
        setIsApplied(false);
        setAppStatus(null);
      } else {
        message.info('请在投递记录页面管理');
      }
      onStatusChange?.();
    } catch {
      // error handled by request interceptor
    } finally {
      setAppLoading(false);
    }
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
        <div className={styles.footerRight}>
          <span
            className={`${styles.actionIcon} ${isFavorited ? styles.favorited : ''}`}
            onClick={handleFavorite}
            style={{ opacity: favLoading ? 0.5 : 1 }}
            title={isFavorited ? '取消收藏' : '收藏'}
          >
            {isFavorited ? <HeartFilled /> : <HeartOutlined />}
          </span>
          <span
            className={`${styles.actionIcon} ${isApplied ? styles.applied : ''}`}
            onClick={handleToggleApply}
            style={{ opacity: appLoading ? 0.5 : 1 }}
            title={isApplied ? '重置为未投递' : '标记为已投递'}
          >
            {isApplied ? <CheckCircleFilled /> : <CheckCircleOutlined />}
            {appStatus && appStatus !== 'APPLIED' && (
              <span className={styles.appStatusLabel}>
                {APPLICATION_STATUS_LABELS[appStatus as keyof typeof APPLICATION_STATUS_LABELS] || appStatus}
              </span>
            )}
          </span>
          <span className={`${styles.badge} ${styles[`badge_${data.applyStatus}`] || ''}`}>
            {data.applyStatus === 'ongoing' && <span className={styles.pulse} />}
            {APPLY_STATUS_LABELS[data.applyStatus] || data.applyStatus}
          </span>
        </div>
      </div>
    </div>
  );
}
