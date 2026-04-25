export interface FavoriteItem {
  favoriteId: number;
  announcementId: number;
  companyName: string;
  announcementName: string;
  expiredAt: string | null;
  applyStatus: string;
  favoritedAt: string;
}
