import { Card, Tag } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import type { AnnouncementListResponse } from '@/types/announcement';
import { APPLY_STATUS_LABELS, APPLY_STATUS_COLORS, ROUTES } from '@/utils/constants';
import styles from './index.module.css';

interface AnnouncementCardProps {
  data: AnnouncementListResponse;
}

export default function AnnouncementCard({ data }: AnnouncementCardProps) {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(ROUTES.ANNOUNCEMENTS + '/' + data.announcementId);
  };

  return (
    <Card hoverable className={styles.card} onClick={handleClick}>
      <div className={styles.companyName}>{data.companyName}</div>
      <div className={styles.announcementName}>{data.name}</div>
      <div className={styles.tags}>
        {data.classTypeNames.map((name) => (
          <Tag key={name} color="blue">{name}</Tag>
        ))}
        {data.campusTypeNames.map((name) => (
          <Tag key={name} color="green">{name}</Tag>
        ))}
      </div>
      <div className={styles.cities}>
        <EnvironmentOutlined style={{ marginRight: 4 }} />
        {data.cityNames.join(' / ')}
      </div>
      <div className={styles.footer}>
        <span className={styles.date}>
          截止: {dayjs(data.expiredAt).format('YYYY-MM-DD')}
        </span>
        <Tag color={APPLY_STATUS_COLORS[data.applyStatus] || 'default'}>
          {APPLY_STATUS_LABELS[data.applyStatus] || data.applyStatus}
        </Tag>
      </div>
    </Card>
  );
}
