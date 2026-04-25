import { useState } from 'react';
import { Dropdown, Avatar, Space } from 'antd';
import { UserOutlined, HeartOutlined, FileTextOutlined, LogoutOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserAuthStore } from '@/store/userAuthStore';

export default function UserMenu() {
  const { user, clearAuth } = useUserAuthStore();
  const navigate = useNavigate();

  const items = [
    {
      key: 'favorites',
      icon: <HeartOutlined />,
      label: '我的收藏',
      onClick: () => navigate('/favorites'),
    },
    {
      key: 'applications',
      icon: <FileTextOutlined />,
      label: '投递记录',
      onClick: () => navigate('/applications'),
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
      onClick: () => {
        clearAuth();
        navigate('/announcements');
      },
    },
  ];

  return (
    <Dropdown menu={{ items }} placement="bottomRight">
      <Space style={{ cursor: 'pointer' }}>
        <Avatar
          size={28}
          icon={<UserOutlined />}
          src={user?.avatarUrl !== '/default-avatar.png' ? user?.avatarUrl : undefined}
          style={{ backgroundColor: '#1a2744' }}
        />
        <span style={{ color: '#fff', fontSize: 14 }}>{user?.nickname || '用户'}</span>
      </Space>
    </Dropdown>
  );
}
