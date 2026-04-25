import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Breadcrumb, Tag, Button, Modal, Skeleton, Empty, message } from 'antd';
import { CopyOutlined, LinkOutlined, HeartFilled, HeartOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { getAnnouncementDetail } from '@/services/announcement';
import { recordClick, recordPageView } from '@/services/tracking';
import { addFavorite, removeFavorite } from '@/services/favorites';
import { useUserAuthStore } from '@/store/userAuthStore';
import LoginModal from '@/components/LoginModal';
import RecordApplicationModal from '@/components/RecordApplicationModal';
import { APPLICATION_STATUS_LABELS } from '@/types/user';
import { APPLY_STATUS_LABELS, APPLY_STATUS_COLORS, CLICK_TYPE, ROUTES } from '@/utils/constants';
import styles from './index.module.css';

export default function AnnouncementDetail() {
  const { id } = useParams();
  const [emailModalOpen, setEmailModalOpen] = useState(false);
  const [loginOpen, setLoginOpen] = useState(false);
  const [recordModalOpen, setRecordModalOpen] = useState(false);
  const [isFavorited, setIsFavorited] = useState(false);
  const [favLoading, setFavLoading] = useState(false);
  const { user } = useUserAuthStore();
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['announcementDetail', id],
    queryFn: () => getAnnouncementDetail(Number(id!)),
    enabled: !!id,
  });

  useEffect(() => {
    if (data) {
      setIsFavorited(data.isFavorited || false);
    }
  }, [data]);

  useEffect(() => {
    if (id) {
      recordPageView(window.location.href, 'detail', document.referrer);
    }
  }, [id]);

  const handleFavorite = async () => {
    if (!user) {
      setLoginOpen(true);
      return;
    }
    if (!data) return;
    setFavLoading(true);
    try {
      if (isFavorited) {
        await removeFavorite(data.announcementId);
        setIsFavorited(false);
      } else {
        await addFavorite(data.announcementId);
        setIsFavorited(true);
      }
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    } finally {
      setFavLoading(false);
    }
  };

  const handleApply = () => {
    if (!data?.fromUrl) return;

    if (data.fromUrl.includes('@')) {
      setEmailModalOpen(true);
      recordClick(data.announcementId, CLICK_TYPE.EMAIL);
    } else {
      // 弹出投递记录选择
      setRecordModalOpen(true);
      recordClick(data.announcementId, CLICK_TYPE.LINK);
    }
  };

  const handleCopyEmail = () => {
    if (data?.fromUrl) {
      navigator.clipboard.writeText(data.fromUrl).then(() => {
        message.success('邮箱地址已复制');
        setEmailModalOpen(false);
      });
    }
  };

  if (isLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.skeleton}>
          <Skeleton active paragraph={{ rows: 8 }} />
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className={styles.page}>
        <div className={styles.errorState}>
          <Empty description="该公告不存在或已下线" />
        </div>
      </div>
    );
  }

  const isExpired = data.applyStatus === 'expired';
  const isNotStarted = data.applyStatus === 'not_started';

  const infoItems = [
    { label: '发布日期', value: data.publishedAt ? dayjs(data.publishedAt).format('YYYY-MM-DD') : '-' },
    { label: '网申开始', value: data.publishedAt ? dayjs(data.publishedAt).format('YYYY-MM-DD') : '-' },
    { label: '网申截止', value: data.expiredAt ? dayjs(data.expiredAt).format('YYYY-MM-DD') : '招完即止' },
    { label: '投递状态', value: APPLY_STATUS_LABELS[data.applyStatus] || '-' },
    data.salary ? { label: '薪资', value: data.salary } : null,
    data.classTime ? { label: '批次时间', value: data.classTime } : null,
    data.writtenTest !== null ? { label: '是否需要笔试', value: data.writtenTest ? '是' : '否' } : null,
    data.acceptWorkExperience !== null ? { label: '接受有工作经验', value: data.acceptWorkExperience ? '是' : '否' } : null,
  ].filter(Boolean) as { label: string; value: string }[];

  const applyButtonText = data.isApplied
    ? `已投递${data.applicationStatus && data.applicationStatus !== 'APPLIED' ? ` - ${APPLICATION_STATUS_LABELS[data.applicationStatus as keyof typeof APPLICATION_STATUS_LABELS] || data.applicationStatus}` : ''} - 前往投递`
    : '前往投递';

  return (
    <div className={styles.page}>
      <Breadcrumb
        className={styles.breadcrumb}
        items={[
          { title: <Link to={ROUTES.ANNOUNCEMENTS}>首页</Link> },
          { title: data.name },
        ]}
      />

      <div className={styles.content}>
        <div className={styles.header}>
          <div className={styles.companyName}>{data.companyName}</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span className={styles.announcementName}>{data.name}</span>
            <Tag
              className={styles.statusTag}
              color={APPLY_STATUS_COLORS[data.applyStatus] || 'default'}
            >
              {APPLY_STATUS_LABELS[data.applyStatus] || data.applyStatus}
            </Tag>
            <Button
              type="text"
              size="small"
              icon={isFavorited ? <HeartFilled style={{ color: '#e11d48' }} /> : <HeartOutlined />}
              onClick={handleFavorite}
              loading={favLoading}
              style={{ marginLeft: 4 }}
            />
          </div>
        </div>

        {isNotStarted && (
          <div className={styles.notStartedBanner}>
            网申未开始，请关注开始日期
          </div>
        )}

        <div className={styles.infoGrid}>
          {infoItems.map((item) => (
            <div key={item.label} className={styles.infoItem}>
              <span className={styles.infoLabel}>{item.label}</span>
              <span className={styles.infoValue}>{item.value}</span>
            </div>
          ))}
        </div>

        <div className={styles.tagsSection}>
          {data.classTypeNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>毕业年份</div>
              <div className={styles.tagList}>
                {data.classTypeNames.map((n) => <Tag key={n} color="blue">{n}</Tag>)}
              </div>
            </div>
          )}
          {data.campusTypeNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>招聘批次</div>
              <div className={styles.tagList}>
                {data.campusTypeNames.map((n) => <Tag key={n} color="green">{n}</Tag>)}
              </div>
            </div>
          )}
          {data.cityNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>工作城市</div>
              <div className={styles.tagList}>
                {data.cityNames.map((n) => <Tag key={n}>{n}</Tag>)}
              </div>
            </div>
          )}
          {data.degreeNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>学历要求</div>
              <div className={styles.tagList}>
                {data.degreeNames.map((n) => <Tag key={n}>{n}</Tag>)}
              </div>
            </div>
          )}
          {data.industryTypeNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>行业类型</div>
              <div className={styles.tagList}>
                {data.industryTypeNames.map((n) => <Tag key={n}>{n}</Tag>)}
              </div>
            </div>
          )}
          {data.jobCategoryNames.length > 0 && (
            <div className={styles.tagGroup}>
              <div className={styles.tagLabel}>岗位方向</div>
              <div className={styles.tagList}>
                {data.jobCategoryNames.map((n) => <Tag key={n}>{n}</Tag>)}
              </div>
            </div>
          )}
        </div>

        {data.detail && (
          <div className={styles.detailSection}>
            <div className={styles.sectionTitle}>招聘公告</div>
            <div
              className="detail-content"
              dangerouslySetInnerHTML={{ __html: data.detail }}
            />
          </div>
        )}

        {(data.companyDescriptions.length > 0 || data.companyIndustryNames.length > 0 || data.companyWelfare) && (
          <div className={styles.detailSection}>
            <div className={styles.sectionTitle}>公司信息</div>
            {data.companyDescriptions.length > 0 && (
              <p style={{ marginBottom: 8 }}>{data.companyDescriptions.join('；')}</p>
            )}
            {data.companyIndustryNames.length > 0 && (
              <div className={styles.tagGroup}>
                <div className={styles.tagLabel}>所属行业</div>
                <div className={styles.tagList}>
                  {data.companyIndustryNames.map((n) => <Tag key={n}>{n}</Tag>)}
                </div>
              </div>
            )}
            {data.companyWelfare && (
              <div style={{ marginTop: 8 }}>
                <span className={styles.infoLabel}>福利：</span>
                {data.companyWelfare}
              </div>
            )}
          </div>
        )}

        <div className={styles.actions}>
          <Button
            type="primary"
            size="large"
            className={styles.applyButton}
            onClick={handleApply}
            disabled={isExpired || !data.fromUrl}
          >
            {isExpired ? '该校招已截止' : applyButtonText}
          </Button>
          {data.link && (
            <Button
              size="large"
              icon={<LinkOutlined />}
              className={styles.linkButton}
              onClick={() => window.open(data.link, '_blank', 'noopener,noreferrer')}
            >
              查看官方公告
            </Button>
          )}
        </div>
      </div>

      {/* 邮箱投递弹窗 */}
      <Modal
        title="邮箱投递"
        open={emailModalOpen}
        onCancel={() => setEmailModalOpen(false)}
        footer={null}
      >
        <div className={styles.emailModalContent}>
          <p className={styles.emailHint}>该企业通过邮箱接收简历，请复制邮箱地址后发送简历</p>
          <div className={styles.emailAddress}>{data.fromUrl}</div>
          <Button type="primary" icon={<CopyOutlined />} onClick={handleCopyEmail}>
            复制邮箱
          </Button>
        </div>
      </Modal>

      {/* 登录弹窗 */}
      <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} onSuccess={() => queryClient.invalidateQueries({ queryKey: ['announcementDetail', id] })} />

      {/* 投递记录弹窗 */}
      {data.fromUrl && !data.fromUrl.includes('@') && (
        <RecordApplicationModal
          open={recordModalOpen}
          onClose={() => setRecordModalOpen(false)}
          announcementId={data.announcementId}
          announcementName={data.name}
          applyLink={data.fromUrl}
          isApplied={data.isApplied || false}
          applicationStatus={data.applicationStatus}
          onSuccess={() => queryClient.invalidateQueries({ queryKey: ['announcementDetail', id] })}
        />
      )}
    </div>
  );
}
