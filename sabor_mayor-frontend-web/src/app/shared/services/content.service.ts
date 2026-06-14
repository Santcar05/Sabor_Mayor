import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Page } from '../models/common.model';
import { BlogPost, Comment, GalleryImage } from '../models/content.model';

/** content-service: blog, comentarios y galería. */
@Injectable({ providedIn: 'root' })
export class ContentService {
  private readonly api = inject(ApiService);

  getPosts(page = 0, size = 9): Observable<Page<BlogPost>> {
    return this.api.get<Page<BlogPost>>('/api/content/posts', { page, size });
  }

  getPost(slug: string): Observable<BlogPost> {
    return this.api.get<BlogPost>(`/api/content/posts/${slug}`);
  }

  getComments(postId: string): Observable<Comment[]> {
    return this.api.get<Comment[]>(`/api/content/posts/${postId}/comments`);
  }

  addComment(postId: string, body: { body: string }): Observable<Comment> {
    return this.api.post<Comment>(`/api/content/posts/${postId}/comments`, body);
  }

  getGallery(): Observable<GalleryImage[]> {
    return this.api.get<GalleryImage[]>('/api/content/gallery');
  }
}
