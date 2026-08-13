import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/theme/theme.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', icon: 'pi pi-home', route: '/dashboard' },
  { label: 'Jobs', icon: 'pi pi-briefcase', route: '/jobs' },
  { label: 'Reports', icon: 'pi pi-chart-bar', route: '/reports' },
  { label: 'Settings', icon: 'pi pi-cog', route: '/settings' },
];

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, TooltipModule],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.scss',
})
export class AppLayoutComponent {
  readonly navItems = NAV_ITEMS;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    readonly themeService: ThemeService,
  ) {}

  get currentUser() {
    return this.authService.currentUser();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
