import { Component, OnInit, OnDestroy } from '@angular/core';
import { MagneticHoverDirective } from '../../directives/magnetic-hover.directive';
import { ParallaxService } from '../../services/parallax.service';

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [MagneticHoverDirective],
  templateUrl: './hero-section.html',
  styleUrl: './hero-section.premium.scss',
})
export class HeroSectionComponent implements OnInit, OnDestroy {
  glowState = 0;
  buttonHoverState: 'idle' | 'hover' | 'active' = 'idle';

  constructor(private parallaxService: ParallaxService) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {}

  onButtonHover(): void {
    this.buttonHoverState = 'hover';
    this.glowState++;
  }

  onButtonLeave(): void {
    this.buttonHoverState = 'idle';
  }

  onButtonClick(): void {
    this.buttonHoverState = 'active';
    setTimeout(() => (this.buttonHoverState = 'idle'), 300);
  }
}
