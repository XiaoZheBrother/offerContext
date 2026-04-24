import { Link } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';
import styles from './index.module.css';

export default function SiteHeader() {
  return (
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
        </nav>
      </div>
    </header>
  );
}
