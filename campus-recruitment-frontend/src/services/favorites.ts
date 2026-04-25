import request from '@/utils/request';
import type { FavoriteItem } from '@/types/favorite';

export async function addFavorite(announcementId: number): Promise<string> {
  return request.post('/favorites', { announcementId });
}

export async function removeFavorite(announcementId: number): Promise<string> {
  return request.delete(`/favorites/${announcementId}`);
}

export async function getFavorites(): Promise<FavoriteItem[]> {
  return request.get('/favorites');
}
