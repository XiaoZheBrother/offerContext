import { useState } from 'react';
import { Modal, Input, Button, Typography, Space } from 'antd';
import { MailOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { sendMagicLink, verifyMagicLink } from '@/services/auth';
import { useUserAuthStore } from '@/store/userAuthStore';

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
  const [devToken, setDevToken] = useState('');
  const [verifyToken, setVerifyToken] = useState('');
  const [verifying, setVerifying] = useState(false);
  const setAuth = useUserAuthStore((s) => s.setAuth);

  const handleSend = async () => {
    if (!email.trim()) return;
    setLoading(true);
    try {
      const res = await sendMagicLink({ email: email.trim() });
      setSent(true);
      // 开发模式：显示token
      if (res.token) {
        setDevToken(res.token);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    if (!verifyToken.trim()) return;
    setVerifying(true);
    try {
      const res = await verifyMagicLink(verifyToken.trim());
      setAuth(res.token, res.user);
      onSuccess?.();
      handleClose();
    } finally {
      setVerifying(false);
    }
  };

  const handleClose = () => {
    setEmail('');
    setSent(false);
    setDevToken('');
    setVerifyToken('');
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
              <Text>已发送登录链接到您的邮箱，15分钟内有效</Text>
            </div>
          </div>

          {/* 开发模式：手动输入token验证 */}
          <div style={{ padding: 16, background: '#f6f6f6', borderRadius: 8 }}>
            <Text type="secondary" style={{ fontSize: 12 }}>
              开发模式：复制下方token手动验证
            </Text>
            {devToken && (
              <Input.TextArea
                value={devToken}
                readOnly
                rows={2}
                style={{ marginTop: 8, fontFamily: 'monospace', fontSize: 12 }}
              />
            )}
            <Input
              placeholder="粘贴token进行验证"
              value={verifyToken}
              onChange={(e) => setVerifyToken(e.target.value)}
              style={{ marginTop: 8 }}
            />
            <Button
              type="primary"
              block
              loading={verifying}
              onClick={handleVerify}
              disabled={!verifyToken.trim()}
              style={{ marginTop: 8 }}
            >
              验证并登录
            </Button>
          </div>
        </Space>
      )}
    </Modal>
  );
}
