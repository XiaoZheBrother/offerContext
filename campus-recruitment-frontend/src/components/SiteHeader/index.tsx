import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from 'antd';
import { LoginOutlined } from '@ant-design/icons';
import { ROUTES } from '@/utils/constants';
import { useUserAuthStore } from '@/store/userAuthStore';
import UserMenu from '@/components/UserMenu';
import LoginModal from '@/components/LoginModal';
import styles from './index.module.css';

export default function SiteHeader() {
  const { user } = useUserAuthStore();
  const [loginOpen, setLoginOpen] = useState(false);

  return (
    <>
      <header className={styles.header}>
        <div className={styles.inner}>
          <Link to={ROUTES.ANNOUNCEMENTS} className={styles.logo}>
            <div className={styles.logoIcon}>招</div>
            <span className={styles.logoText}>校招信息汇总</span>
            <span className={styles.logoSub}>Campus Recruitment</span>
          </Link>
          <nav className={styles.nav}>
            <Link to={ROUTES.ANNOUNCEMENTS} className={styles.navLink}>
              首页
            </Link>
            <Link to={ROUTES.ADMIN} className={styles.navLink}>
              后台管理
            </Link>
            {user ? (
              <UserMenu />
            ) : (
              <Button
                type="primary"
                icon={<LoginOutlined />}
                onClick={() => setLoginOpen(true)}
                size="small"
                style={{ marginLeft: 8 }}
              >
                登录
              </Button>
            )}
          </nav>
        </div>
      </header>
      <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} />
    </>
  );
}
