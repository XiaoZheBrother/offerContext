import request from '@/utils/request';
import type { FavoriteItem } from '@/types/favorite';

export async function addFavorite(announcementId: number): Promise<string> {
  const res = await request.post('/favorites', { announcementId });
  return res.data;
}

export async function removeFavorite(announcementId: number): Promise<string> {
  const res = await request.delete(`/favorites/${announcementId}`);
  return res.data;
}

export async function getFavorites(): Promise<FavoriteItem[]> {
  const res = await request.get('/favorites');
  return res.data as FavoriteItem[];
}
