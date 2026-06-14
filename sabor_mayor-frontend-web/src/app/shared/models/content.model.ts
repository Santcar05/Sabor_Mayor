/** content-service: blog, comentarios y galería. */
export interface BlogPost {
  id: string;
  title: string;
  slug: string;
  excerpt: string;
  /** Backend field name — aliased to `content` in templates */
  body?: string;
  /** Preferred alias for body */
  content?: string;
  /** Backend field name — aliased to `coverImageUrl` in templates */
  coverImage?: string;
  /** Preferred alias for coverImage */
  coverImageUrl?: string;
  authorId: string;
  authorName: string;
  category?: string;
  tags?: string[];
  readTimeMinutes?: number;
  metaTitle?: string;
  metaDescription?: string;
  published: boolean;
  publishedAt: string;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  authorName: string;
  body: string;
  approved: boolean;
  createdAt: string;
}

export interface GalleryImage {
  id: string;
  url: string;
  /** Backend uses caption; templates use title */
  caption?: string;
  title?: string;
  description?: string;
  category?: string;
  displayOrder: number;
}
