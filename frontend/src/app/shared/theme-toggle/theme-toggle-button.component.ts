import { Component, inject } from '@angular/core';
import { TooltipModule } from 'primeng/tooltip';

import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-theme-toggle-button',
  standalone: true,
  imports: [TooltipModule],
  templateUrl: './theme-toggle-button.component.html',
  styleUrl: './theme-toggle-button.component.scss',
})
export class ThemeToggleButtonComponent {
  readonly themeService = inject(ThemeService);
}
