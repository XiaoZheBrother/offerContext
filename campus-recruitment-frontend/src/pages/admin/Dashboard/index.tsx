import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, Statistic, Table, Tabs } from 'antd';
import { FileTextOutlined, EyeOutlined, UserOutlined, LinkOutlined } from '@ant-design/icons';
import { getStatistics, getTopCompanies } from '@/services/admin';
import styles from './index.module.css';

const DIMENSIONS = [
  { key: 'click', label: '点击量' },
  { key: 'view', label: '浏览量' },
  { key: 'apply', label: '投递量' },
];

const columns = [
  { title: '排名', key: 'rank', width: 80, render: (_: unknown, __: unknown, index: number) => index + 1 },
  { title: '公司名称', dataIndex: 'companyName', key: 'companyName' },
  { title: '数量', dataIndex: 'clickCount', key: 'clickCount', width: 120 },
];

export default function Dashboard() {
  const [dimension, setDimension] = useState('click');

  const { data: stats } = useQuery({
    queryKey: ['statistics'],
    queryFn: getStatistics,
  });

  const { data: topCompanies } = useQuery({
    queryKey: ['topCompanies', dimension],
    queryFn: () => getTopCompanies(dimension, 10),
  });

  return (
    <div className={styles.dashboard}>
      <div className={styles.pageTitle}>数据概览</div>

      <div className={styles.statCards}>
        <Card className={styles.statCard}>
          <Statistic
            title="在线公告数"
            value={stats?.onlineCount ?? 0}
            prefix={<FileTextOutlined />}
          />
        </Card>
        <Card className={styles.statCard}>
          <Statistic
            title="今日PV"
            value={stats?.todayPv ?? 0}
            prefix={<EyeOutlined />}
          />
        </Card>
        <Card className={styles.statCard}>
          <Statistic
            title="今日UV"
            value={stats?.todayUv ?? 0}
            prefix={<UserOutlined />}
          />
        </Card>
        <Card className={styles.statCard}>
          <Statistic
            title="今日投递跳转"
            value={stats?.todayClickCount ?? 0}
            prefix={<LinkOutlined />}
          />
        </Card>
      </div>

      <div className={styles.topSection}>
        <div className={styles.sectionTitle}>热门企业 TOP 10</div>
        <Tabs
          activeKey={dimension}
          onChange={setDimension}
          items={DIMENSIONS.map((d) => ({
            key: d.key,
            label: d.label,
          }))}
        />
        <Table
          dataSource={topCompanies ?? []}
          columns={columns}
          rowKey="companyName"
          pagination={false}
          size="middle"
          locale={{ emptyText: '暂无数据' }}
        />
      </div>
    </div>
  );
}
