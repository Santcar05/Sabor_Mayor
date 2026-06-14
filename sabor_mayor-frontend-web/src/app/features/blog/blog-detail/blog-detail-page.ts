import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
  computed,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { ContentService } from '../../../shared/services/content.service';
import { AuthService } from '../../../shared/services/auth.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { BlogPost, Comment } from '../../../shared/models/content.model';
import { LoadingSpinnerComponent } from '../../../components/ui/loading-spinner/loading-spinner';
import { ButtonComponent } from '../../../components/ui/button/button';
import { UserAvatarComponent } from '../../../components/ui/user-avatar/user-avatar';

interface DetailState {
  post: BlogPost | null;
  comments: Comment[];
  error: boolean;
}

@Component({
  selector: 'app-blog-detail-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, FormsModule, DatePipe, LoadingSpinnerComponent, ButtonComponent, UserAvatarComponent],
  templateUrl: './blog-detail-page.html',
  styleUrl: './blog-detail-page.scss',
})
export class BlogDetailPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly content = inject(ContentService);
  protected readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  protected readonly commentText = signal('');
  protected readonly submitting = signal(false);

  get commentTextModel(): string { return this.commentText(); }
  set commentTextModel(v: string) { this.commentText.set(v); }

  protected shareWhatsApp(post: BlogPost): void {
    const text = encodeURIComponent(`Te comparto este artículo de Sabor Mayor: ${post.title}`);
    window.open(`https://wa.me/?text=${text}`, '_blank');
  }

  protected readonly state = toSignal(
    this.route.paramMap.pipe(
      map((p) => p.get('slug') ?? ''),
      switchMap((slug): Observable<DetailState> =>
        this.content.getPost(slug).pipe(
          map((raw) => ({
            ...raw,
            content: raw.content ?? raw.body ?? '',
            coverImageUrl: raw.coverImageUrl ?? raw.coverImage ?? '',
          })),
          switchMap((post): Observable<DetailState> =>
            this.content.getComments(post.id).pipe(
              map((comments): DetailState => ({ post, comments, error: false })),
              catchError((): Observable<DetailState> => of({ post, comments: [], error: false })),
            ),
          ),
          catchError((): Observable<DetailState> => of({ post: null, comments: [], error: true })),
        ),
      ),
    ),
    { initialValue: { post: null, comments: [], error: false } as DetailState },
  );

  protected readonly post = computed(() => this.state()?.post ?? null);
  protected readonly comments = computed(() => this.state()?.comments ?? []);
  protected readonly loading = computed(() => !this.state()?.post && !this.state()?.error);

  protected async submitComment(): Promise<void> {
    const post = this.post();
    const text = this.commentText().trim();
    if (!post || !text || this.submitting()) return;
    this.submitting.set(true);
    this.content.addComment(post.id, { body: text }).subscribe({
      next: () => {
        this.commentText.set('');
        this.notify.success('Comentario publicado');
        this.submitting.set(false);
      },
      error: () => {
        this.notify.error('No se pudo publicar el comentario');
        this.submitting.set(false);
      },
    });
  }
}
