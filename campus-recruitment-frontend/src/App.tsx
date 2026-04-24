import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';
import { useWindowWidth } from '@/hooks/useWindowWidth';
import { MIN_PC_WIDTH } from '@/utils/constants';
import SiteHeader from '@/components/SiteHeader';
import AnnouncementList from '@/pages/AnnouncementList';
import AnnouncementDetail from '@/pages/AnnouncementDetail';
import Login from '@/pages/admin/Login';
import Dashboard from '@/pages/admin/Dashboard';
import AnnouncementManage from '@/pages/admin/AnnouncementManage';

function MobileGuard() {
  const { isPC } = useWindowWidth();
  if (!isPC) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        padding: '32px',
        textAlign: 'center',
        color: '#666',
      }}>
        <h2 style={{ fontSize: '20px', marginBottom: '12px', color: '#333' }}>
          请使用电脑访问
        </h2>
        <p>为获得最佳体验，请使用屏幕宽度 ≥ {MIN_PC_WIDTH}px 的电脑浏览器访问本站</p>
      </div>
    );
  }
  return <Outlet />;
}

function AdminRoute() {
  const token = useAuthStore((s) => s.token);
  if (!token) {
    return <Navigate to="/admin/login" replace />;
  }
  return <Outlet />;
}

const router = createBrowserRouter([
  {
    element: <MobileGuard />,
    children: [
      { path: '/', element: <Navigate to="/announcements" replace /> },
      { path: '/announcements', element: <AnnouncementList /> },
      { path: '/announcements/:id', element: <AnnouncementDetail /> },
      { path: '/admin/login', element: <Login /> },
      {
        element: <AdminRoute />,
        children: [
          { path: '/admin', element: <Dashboard /> },
          { path: '/admin/announcements', element: <AnnouncementManage /> },
        ],
      },
    ],
  },
]);

function App() {
  return (
    <>
      <SiteHeader />
      <RouterProvider router={router} />
    </>
  );
}

export default App;
