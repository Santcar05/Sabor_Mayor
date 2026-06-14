import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Avatar: muestra foto si existe, si no las iniciales del nombre. */
@Component({
  selector: 'app-user-avatar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="avatar" [style.--size.px]="size()" [title]="name()">
      @if (imageUrl()) {
        <img [src]="imageUrl()" [alt]="name()" loading="lazy" />
      } @else {
        <span class="initials">{{ initials() }}</span>
      }
    </span>
  `,
  styles: [
    `
      .avatar {
        --size: 40px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: var(--size);
        height: var(--size);
        border-radius: 50%;
        overflow: hidden;
        background: var(--gradient-gold);
        color: var(--tierra-profunda);
        font-family: var(--font-heading);
        font-weight: 700;
        font-size: calc(var(--size) * 0.4);
        flex-shrink: 0;
        box-shadow: inset 0 0 0 1px rgba(20, 12, 4, 0.2);
      }
      img { width: 100%; height: 100%; object-fit: cover; }
    `,
  ],
})
export class UserAvatarComponent {
  name = input('');
  imageUrl = input<string | null>(null);
  size = input(40);

  protected initials = computed(() => {
    const parts = this.name().trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '·';
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  });
}
