import { useState } from 'react';
import { Empty, Spin, Tag, Typography } from 'antd';
import { HeartOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { getFavorites } from '@/services/favorites';
import { useUserAuthStore } from '@/store/userAuthStore';
import LoginModal from '@/components/LoginModal';
import type { FavoriteItem } from '@/types/favorite';
import { ROUTES } from '@/utils/constants';
import styles from './Favorites.module.css';

const { Text } = Typography;

export default function Favorites() {
  const { user } = useUserAuthStore();
  const [loginOpen, setLoginOpen] = useState(false);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: favorites = [], isLoading } = useQuery({
    queryKey: ['favorites'],
    queryFn: getFavorites,
    enabled: !!user,
  });

  if (!user) {
    return (
      <div className={styles.emptyPage}>
        <HeartOutlined style={{ fontSize: 48, color: '#d1d5db' }} />
        <Text type="secondary" style={{ fontSize: 16 }}>请先登录查看收藏</Text>
        <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} onSuccess={() => queryClient.invalidateQueries({ queryKey: ['favorites'] })} />
        {!loginOpen && (
          <a onClick={() => setLoginOpen(true)} style={{ color: '#1a2744', fontWeight: 600, cursor: 'pointer' }}>
            点击登录
          </a>
        )}
      </div>
    );
  }

  if (isLoading) {
    return <div className={styles.emptyPage}><Spin size="large" /></div>;
  }

  if (favorites.length === 0) {
    return (
      <div className={styles.emptyPage}>
        <Empty description="暂无收藏，去首页看看吧">
          <a onClick={() => navigate('/announcements')} style={{ color: '#1a2744' }}>返回首页</a>
        </Empty>
      </div>
    );
  }

  const now = dayjs();

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>我的收藏</h1>
      <div className={styles.list}>
        {favorites.map((item: FavoriteItem) => {
          const expiredAt = item.expiredAt ? dayjs(item.expiredAt) : null;
          const hoursUntilDeadline = expiredAt ? expiredAt.diff(now, 'hour') : null;
          const isExpiringSoon = hoursUntilDeadline !== null && hoursUntilDeadline > 0 && hoursUntilDeadline <= 72;
          const isExpired = item.applyStatus === 'expired';

          return (
            <div
              key={item.favoriteId}
              className={`${styles.item} ${isExpired ? styles.itemExpired : ''} ${isExpiringSoon ? styles.itemExpiring : ''}`}
              onClick={() => navigate(`${ROUTES.ANNOUNCEMENTS}/${item.announcementId}`)}
            >
              <div className={styles.itemBody}>
                <div className={styles.itemCompany}>{item.companyName}</div>
                <div className={styles.itemName}>{item.announcementName}</div>
                <div className={styles.itemMeta}>
                  {expiredAt && (
                    <span className={styles.itemDate}>
                      截止: {expiredAt.format('YYYY-MM-DD')}
                    </span>
                  )}
                  {isExpiringSoon && <Tag color="red">即将截止</Tag>}
                  {isExpired && <Tag color="default">已截止</Tag>}
                </div>
              </div>
              <div className={styles.itemTime}>
                收藏于 {dayjs(item.favoritedAt).format('MM-DD')}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
