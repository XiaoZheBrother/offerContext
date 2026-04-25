import { useState } from 'react';
import { Modal, Input, Button, Typography, Space } from 'antd';
import { MailOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { sendMagicLink } from '@/services/auth';

const { Text } = Typography;

interface LoginModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function LoginModal({ open, onClose, onSuccess }: LoginModalProps) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSend = async () => {
    if (!email.trim()) return;
    setLoading(true);
    try {
      await sendMagicLink({ email: email.trim() });
      setSent(true);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setEmail('');
    setSent(false);
    setLoading(false);
    onClose();
  };

  return (
    <Modal
      title="登录"
      open={open}
      onCancel={handleClose}
      footer={null}
      width={420}
      destroyOnClose
    >
      {!sent ? (
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Text type="secondary">输入邮箱，我们将发送登录链接到您的邮箱</Text>
          <Input
            prefix={<MailOutlined />}
            placeholder="请输入邮箱"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onPressEnter={handleSend}
            size="large"
            type="email"
          />
          <Button
            type="primary"
            block
            size="large"
            loading={loading}
            onClick={handleSend}
            disabled={!email.trim()}
          >
            发送登录链接
          </Button>
        </Space>
      ) : (
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <div style={{ textAlign: 'center' }}>
            <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a' }} />
            <div style={{ marginTop: 12 }}>
              <Text>已发送登录链接到 <strong>{email}</strong>，15分钟内有效</Text>
            </div>
            <div style={{ marginTop: 8 }}>
              <Text type="secondary" style={{ fontSize: 13 }}>
                请在邮箱中点击登录按钮完成验证
              </Text>
            </div>
          </div>
        </Space>
      )}
    </Modal>
  );
}
