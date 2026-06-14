import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ContentService } from '../../shared/services/content.service';
import { GalleryImage } from '../../shared/models/content.model';
import { LoadingSpinnerComponent } from '../../components/ui/loading-spinner/loading-spinner';

@Component({
  selector: 'app-galeria-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [LoadingSpinnerComponent],
  templateUrl: './galeria-page.html',
  styleUrl: './galeria-page.scss',
})
export class GaleriaPageComponent {
  private readonly content = inject(ContentService);

  protected readonly galleryState = toSignal(
    this.content.getGallery().pipe(catchError(() => of(null))),
    { initialValue: undefined },
  );

  protected readonly loading = computed(() => this.galleryState() === undefined);
  protected readonly images = computed((): GalleryImage[] =>
    (this.galleryState() ?? []).map((img) => ({
      ...img,
      title: img.title ?? img.caption ?? '',
    })),
  );
  protected readonly error = computed(() => this.galleryState() === null);

  protected readonly lightbox = signal<GalleryImage | null>(null);

  protected open(img: GalleryImage): void { this.lightbox.set(img); }
  protected close(): void { this.lightbox.set(null); }
}
