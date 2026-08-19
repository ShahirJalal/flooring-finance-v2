import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeToggleButtonComponent } from '../../shared/theme-toggle/theme-toggle-button.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, CardModule, InputTextModule, PasswordModule, MessageModule,
    ThemeToggleButtonComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  /** e.g. "Password changed. Please log in again." after a redirect from Settings. */
  readonly infoMessage = signal<string | null>(null);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const message = this.route.snapshot.queryParamMap.get('message');
    if (message) {
      this.infoMessage.set(message);
    }
  }

  submit(): void {
    if (!this.username || !this.password) {
      this.errorMessage.set('Please enter both username and password.');
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/jobs']);
      },
      error: err => {
        this.loading.set(false);
        // Was previously hardcoded to "Invalid username or password" for
        // every failure, which was actively misleading for anything else -
        // most notably a 429 lockout after repeated failed attempts, which
        // looked identical to a wrong password with no way to tell them apart.
        this.errorMessage.set(err?.error?.message || 'Invalid username or password.');
      },
    });
  }
}
