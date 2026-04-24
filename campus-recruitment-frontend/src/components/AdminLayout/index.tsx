import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { DashboardOutlined, FileTextOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/store/auth';
import { ROUTES } from '@/utils/constants';
import styles from './index.module.css';

const NAV_ITEMS = [
  { path: ROUTES.ADMIN, label: '数据概览', icon: <DashboardOutlined /> },
  { path: ROUTES.ADMIN_ANNOUNCEMENTS, label: '公告管理', icon: <FileTextOutlined /> },
];

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { username, clearAuth } = useAuthStore();

  const handleLogout = () => {
    clearAuth();
    navigate(ROUTES.ADMIN_LOGIN);
  };

  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>管理后台</div>
        <nav className={styles.nav}>
          {NAV_ITEMS.map((item) => (
            <div
              key={item.path}
              className={`${styles.navItem} ${location.pathname === item.path ? styles.navActive : ''}`}
              onClick={() => navigate(item.path)}
            >
              {item.icon}
              <span>{item.label}</span>
            </div>
          ))}
        </nav>
        <div className={styles.sidebarFooter}>
          <span className={styles.username}>{username}</span>
          <button className={styles.logoutBtn} onClick={handleLogout}>
            退出登录
          </button>
        </div>
      </aside>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
