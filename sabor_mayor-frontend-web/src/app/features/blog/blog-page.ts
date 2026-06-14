import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ContentService } from '../../shared/services/content.service';
import { BlogPost } from '../../shared/models/content.model';
import { LoadingSpinnerComponent } from '../../components/ui/loading-spinner/loading-spinner';

@Component({
  selector: 'app-blog-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, LoadingSpinnerComponent],
  templateUrl: './blog-page.html',
  styleUrl: './blog-page.scss',
})
export class BlogPageComponent {
  private readonly content = inject(ContentService);

  protected readonly postsState = toSignal(
    this.content.getPosts(0, 12).pipe(
      catchError(() => of(null)),
    ),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this.postsState() === undefined);
  protected readonly posts = computed(() =>
    (this.postsState()?.content ?? []).map((p) => ({
      ...p,
      coverImageUrl: p.coverImageUrl ?? p.coverImage ?? '',
    })),
  );
  protected readonly error = computed(() => this.postsState() === null);

  protected trackBySlug(_: number, post: BlogPost): string {
    return post.slug;
  }
}
