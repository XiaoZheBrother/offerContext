import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';
import { useWindowWidth } from '@/hooks/useWindowWidth';
import { MIN_PC_WIDTH } from '@/utils/constants';
import AnnouncementList from '@/pages/AnnouncementList';

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

function Placeholder({ title }: { title: string }) {
  return (
    <div style={{ padding: '48px', textAlign: 'center' }}>
      <h1>{title}</h1>
      <p style={{ color: '#999', marginTop: '12px' }}>页面开发中...</p>
    </div>
  );
}

const router = createBrowserRouter([
  {
    element: <MobileGuard />,
    children: [
      { path: '/', element: <Navigate to="/announcements" replace /> },
      { path: '/announcements', element: <AnnouncementList /> },
      { path: '/announcements/:id', element: <Placeholder title="公告详情" /> },
      { path: '/admin/login', element: <Placeholder title="管理员登录" /> },
      {
        element: <AdminRoute />,
        children: [
          { path: '/admin', element: <Placeholder title="数据概览" /> },
          { path: '/admin/announcements', element: <Placeholder title="公告管理" /> },
        ],
      },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
