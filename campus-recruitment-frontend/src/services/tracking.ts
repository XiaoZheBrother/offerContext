import request from '@/utils/request';

export function recordClick(announcementId: number, clickType: string) {
  request.post('/click-logs', { announcementId, clickType }).catch(() => {});
}

export function recordPageView(pageUrl: string, pageType: string, referer?: string) {
  request.post('/page-views', { pageUrl, pageType, referer }).catch(() => {});
}
