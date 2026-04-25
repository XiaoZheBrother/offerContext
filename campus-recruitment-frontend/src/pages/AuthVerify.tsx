import { useEffect, useRef, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Spin, Result, Button } from 'antd';
import { verifyMagicLink } from '@/services/auth';
import { useUserAuthStore } from '@/store/userAuthStore';
import { ROUTES } from '@/utils/constants';

export default function AuthVerify() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useUserAuthStore((s) => s.setAuth);
  const processed = useRef(false);
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (processed.current) return;
    processed.current = true;

    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setErrorMsg('缺少验证参数');
      return;
    }

    verifyMagicLink(token)
      .then((res) => {
        setAuth(res.token, res.user);
        setStatus('success');
        setTimeout(() => navigate(ROUTES.ANNOUNCEMENTS, { replace: true }), 1500);
      })
      .catch((err) => {
        setStatus('error');
        setErrorMsg(err?.response?.data?.message || '验证失败，请重新发送登录链接');
      });
  }, []);

  if (status === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spin size="large" tip="正在验证登录..." />
      </div>
    );
  }

  if (status === 'success') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Result status="success" title="登录成功" subTitle="正在跳转..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Result
        status="error"
        title="登录失败"
        subTitle={errorMsg}
        extra={
          <Button type="primary" onClick={() => navigate(ROUTES.ANNOUNCEMENTS)}>
            返回首页
          </Button>
        }
      />
    </div>
  );
}
