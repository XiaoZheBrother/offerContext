import { useState } from 'react';
import { Empty, Select, Button, Modal, Input, Spin, Typography, Tag } from 'antd';
import { DeleteOutlined, EditOutlined, FileTextOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { getApplications, updateApplication, deleteApplication } from '@/services/applications';
import { useUserAuthStore } from '@/store/userAuthStore';
import LoginModal from '@/components/LoginModal';
import { APPLICATION_STATUS_LABELS, APPLICATION_STATUS_COLORS } from '@/types/user';
import type { ApplicationRecord, ApplicationUpdateRequest } from '@/types/application';
import type { ApplicationStatusType } from '@/types/user';
import { ROUTES } from '@/utils/constants';
import styles from './Applications.module.css';

const { Text } = Typography;
const { TextArea } = Input;

const STATUS_OPTIONS = Object.entries(APPLICATION_STATUS_LABELS).map(([value, label]) => ({
  value,
  label,
}));

export default function Applications() {
  const { user } = useUserAuthStore();
  const [loginOpen, setLoginOpen] = useState(false);
  const [editModal, setEditModal] = useState<{ record: ApplicationRecord; notes: string; status: string } | null>(null);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: applications = [], isLoading } = useQuery({
    queryKey: ['applications'],
    queryFn: getApplications,
    enabled: !!user,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ApplicationUpdateRequest }) =>
      updateApplication(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      setEditModal(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteApplication,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });

  if (!user) {
    return (
      <div className={styles.emptyPage}>
        <FileTextOutlined style={{ fontSize: 48, color: '#d1d5db' }} />
        <Text type="secondary" style={{ fontSize: 16 }}>请先登录查看投递记录</Text>
        <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} />
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

  if (applications.length === 0) {
    return (
      <div className={styles.emptyPage}>
        <Empty description="暂无投递记录">
          <a onClick={() => navigate('/announcements')} style={{ color: '#1a2744' }}>返回首页</a>
        </Empty>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>投递记录</h1>
      <div className={styles.list}>
        {applications.map((record: ApplicationRecord) => (
          <div
            key={record.id}
            className={styles.item}
            onClick={() => navigate(`${ROUTES.ANNOUNCEMENTS}/${record.announcementId}`)}
          >
            <div className={styles.itemBody}>
              <div className={styles.itemCompany}>{record.companyName}</div>
              <div className={styles.itemName}>{record.announcementName}</div>
              <div className={styles.itemMeta}>
                <Tag color={APPLICATION_STATUS_COLORS[record.status]}>
                  {APPLICATION_STATUS_LABELS[record.status]}
                </Tag>
                <span className={styles.itemDate}>
                  投递于 {dayjs(record.appliedAt).format('YYYY-MM-DD')}
                </span>
                {record.notes && (
                  <span className={styles.itemNotes}>{record.notes}</span>
                )}
              </div>
            </div>
            <div className={styles.itemActions} onClick={(e) => e.stopPropagation()}>
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={() =>
                  setEditModal({
                    record,
                    notes: record.notes || '',
                    status: record.status,
                  })
                }
              />
              <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={() => {
                  Modal.confirm({
                    title: '确认删除',
                    content: `删除「${record.announcementName}」的投递记录？`,
                    onOk: () => deleteMutation.mutate(record.id),
                  });
                }}
              />
            </div>
          </div>
        ))}
      </div>

      {/* 编辑弹窗 */}
      <Modal
        title="编辑投递记录"
        open={!!editModal}
        onCancel={() => setEditModal(null)}
        onOk={() => {
          if (editModal) {
            updateMutation.mutate({
              id: editModal.record.id,
              data: {
                status: editModal.status as ApplicationStatusType,
                notes: editModal.notes,
              },
            });
          }
        }}
        confirmLoading={updateMutation.isPending}
      >
        {editModal && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <Text type="secondary">投递状态</Text>
              <Select
                value={editModal.status}
                onChange={(val) => setEditModal({ ...editModal, status: val })}
                options={STATUS_OPTIONS}
                style={{ width: '100%', marginTop: 4 }}
              />
            </div>
            <div>
              <Text type="secondary">备注</Text>
              <TextArea
                value={editModal.notes}
                onChange={(e) => setEditModal({ ...editModal, notes: e.target.value })}
                rows={3}
                placeholder="添加备注..."
                style={{ marginTop: 4 }}
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
