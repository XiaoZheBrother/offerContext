import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Table, Button, Input, Select, Switch, Popconfirm, message, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getAdminAnnouncements, deleteAnnouncement, updateAnnouncementStatus } from '@/services/admin';
import { getFilterOptions } from '@/services/announcement';
import { APPLY_STATUS_LABELS, APPLY_STATUS_COLORS } from '@/utils/constants';
import type { AnnouncementListResponse } from '@/types/announcement';
import AnnouncementForm from './AnnouncementForm';
import styles from './index.module.css';

export default function AnnouncementManage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<string | undefined>(undefined);
  const [formOpen, setFormOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['adminAnnouncements', page, keyword, status],
    queryFn: () => getAdminAnnouncements({ page, size: 20, keyword: keyword || undefined, status }),
  });

  const { data: filterOptions } = useQuery({
    queryKey: ['filterOptions'],
    queryFn: getFilterOptions,
    staleTime: 5 * 60 * 1000,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAnnouncement,
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['adminAnnouncements'] });
      queryClient.invalidateQueries({ queryKey: ['announcements'] });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, onlineStatus }: { id: number; onlineStatus: number }) =>
      updateAnnouncementStatus(id, onlineStatus),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminAnnouncements'] });
      queryClient.invalidateQueries({ queryKey: ['announcements'] });
    },
  });

  const handleEdit = (record: AnnouncementListResponse) => {
    setEditId(record.announcementId);
    setFormOpen(true);
  };

  const handleCreate = () => {
    setEditId(null);
    setFormOpen(true);
  };

  const columns = [
    { title: '公司名称', dataIndex: 'companyName', key: 'companyName', width: 160, ellipsis: true },
    { title: '公告名称', dataIndex: 'name', key: 'name', width: 200, ellipsis: true },
    {
      title: '届次',
      key: 'classTypeNames',
      width: 100,
      render: (_: unknown, record: AnnouncementListResponse) =>
        record.classTypeNames.join('、'),
    },
    {
      title: '批次',
      key: 'campusTypeNames',
      width: 100,
      render: (_: unknown, record: AnnouncementListResponse) =>
        record.campusTypeNames.join('、'),
    },
    {
      title: '截止日期',
      dataIndex: 'expiredAt',
      key: 'expiredAt',
      width: 110,
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD') : '-',
    },
    {
      title: '状态',
      key: 'applyStatus',
      width: 90,
      render: (_: unknown, record: AnnouncementListResponse) => (
        <span style={{ color: APPLY_STATUS_COLORS[record.applyStatus] || '#999' }}>
          {APPLY_STATUS_LABELS[record.applyStatus] || record.applyStatus}
        </span>
      ),
    },
    {
      title: '上线',
      key: 'onlineStatus',
      width: 70,
      render: (_: unknown, record: AnnouncementListResponse) => (
        <Switch
          size="small"
          checked={record.onlineStatus === 1}
          onChange={(checked) => {
            statusMutation.mutate({
              id: record.announcementId,
              onlineStatus: checked ? 1 : 0,
            });
          }}
        />
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 140,
      render: (_: unknown, record: AnnouncementListResponse) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除该公告吗？"
            onConfirm={() => deleteMutation.mutate(record.announcementId)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.managePage}>
      <div className={styles.pageTitle}>公告管理</div>

      <div className={styles.toolbar}>
        <div className={styles.searchArea}>
          <Input.Search
            placeholder="搜索公司名称"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onSearch={(val) => { setKeyword(val); setPage(1); }}
            allowClear
            style={{ width: 240 }}
          />
          <Select
            placeholder="状态筛选"
            value={status}
            onChange={(val) => { setStatus(val); setPage(1); }}
            allowClear
            style={{ width: 120 }}
            options={[
              { value: 'online', label: '上线' },
              { value: 'offline', label: '下线' },
            ]}
          />
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增公告
        </Button>
      </div>

      <Table
        dataSource={data?.list ?? []}
        columns={columns}
        rowKey="announcementId"
        loading={isLoading}
        pagination={{
          current: page,
          total: data?.total ?? 0,
          pageSize: 20,
          onChange: setPage,
          showTotal: (total) => `共 ${total} 条`,
        }}
        scroll={{ x: 1000 }}
      />

      <AnnouncementForm
        open={formOpen}
        editId={editId}
        filterOptions={filterOptions}
        onClose={() => { setFormOpen(false); setEditId(null); }}
        onSuccess={() => {
          setFormOpen(false);
          setEditId(null);
          queryClient.invalidateQueries({ queryKey: ['adminAnnouncements'] });
          queryClient.invalidateQueries({ queryKey: ['announcements'] });
        }}
      />
    </div>
  );
}
