import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';
import SiteHeader from '@/components/SiteHeader';
import AdminLayout from '@/components/AdminLayout';
import AnnouncementList from '@/pages/AnnouncementList';
import AnnouncementDetail from '@/pages/AnnouncementDetail';
import AuthVerify from '@/pages/AuthVerify';
import Favorites from '@/pages/Favorites';
import Applications from '@/pages/Applications';
import Login from '@/pages/admin/Login';
import Dashboard from '@/pages/admin/Dashboard';
import AnnouncementManage from '@/pages/admin/AnnouncementManage';

function RootLayout() {
  return (
    <>
      <SiteHeader />
      <Outlet />
    </>
  );
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
    element: <RootLayout />,
    children: [
      { path: '/', element: <Navigate to="/announcements" replace /> },
      { path: '/announcements', element: <AnnouncementList /> },
      { path: '/announcements/:id', element: <AnnouncementDetail /> },
      { path: '/auth/verify', element: <AuthVerify /> },
      { path: '/favorites', element: <Favorites /> },
      { path: '/applications', element: <Applications /> },
      { path: '/admin/login', element: <Login /> },
      {
        element: <AdminRoute />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              { path: '/admin', element: <Dashboard /> },
              { path: '/admin/announcements', element: <AnnouncementManage /> },
            ],
          },
        ],
      },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
