import { useState } from 'react';
import { Modal, Button, Typography, Space } from 'antd';
import { FileTextOutlined, LinkOutlined } from '@ant-design/icons';
import { createApplication } from '@/services/applications';

const { Text } = Typography;

interface RecordApplicationModalProps {
  open: boolean;
  onClose: () => void;
  announcementId: number;
  announcementName: string;
  applyLink: string;
  isApplied: boolean;
  applicationStatus?: string | null;
  onSuccess?: () => void;
}

export default function RecordApplicationModal({
  open,
  onClose,
  announcementId,
  announcementName,
  applyLink,
  isApplied,
  applicationStatus,
  onSuccess,
}: RecordApplicationModalProps) {
  const [loading, setLoading] = useState(false);

  const handleRecordAndGo = async () => {
    if (!isApplied) {
      setLoading(true);
      try {
        await createApplication({ announcementId });
        onSuccess?.();
      } finally {
        setLoading(false);
      }
    }
    window.open(applyLink, '_blank');
    onClose();
  };

  const handleGoOnly = () => {
    window.open(applyLink, '_blank');
    onClose();
  };

  return (
    <Modal
      title="前往投递"
      open={open}
      onCancel={onClose}
      footer={null}
      width={420}
      destroyOnClose
    >
      <Space direction="vertical" style={{ width: '100%' }} size="middle">
        <Text>是否记录本次投递到「投递记录」？</Text>

        {isApplied && (
          <Text type="secondary">
            已有投递记录
            {applicationStatus && `，当前状态：${applicationStatus}`}
          </Text>
        )}

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', marginTop: 8 }}>
          <Button onClick={handleGoOnly} icon={<LinkOutlined />}>
            仅跳转
          </Button>
          <Button
            type="primary"
            onClick={handleRecordAndGo}
            loading={loading}
            icon={<FileTextOutlined />}
          >
            {isApplied ? '前往投递' : '记录并跳转'}
          </Button>
        </div>
      </Space>
    </Modal>
  );
}
