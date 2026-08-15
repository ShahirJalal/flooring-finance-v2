import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, CardModule, DialogModule,
    InputTextModule, MessageModule, PasswordModule,
  ],
  templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit {
  // --- Profile ---
  profileForm: { email: string; fullName: string | null } = { email: '', fullName: null };
  readonly profileSubmitting = signal(false);
  readonly profileError = signal<string | null>(null);

  // --- Change password ---
  readonly passwordDialogVisible = signal(false);
  readonly passwordSubmitting = signal(false);
  readonly passwordError = signal<string | null>(null);
  passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };

  constructor(
    public readonly authService: AuthService,
    private readonly messageService: MessageService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.profileForm = { email: user.email, fullName: user.fullName ?? null };
    }
  }

  saveProfile(): void {
    if (!this.profileForm.email?.trim()) {
      this.profileError.set('Email is required.');
      return;
    }
    this.profileSubmitting.set(true);
    this.profileError.set(null);
    this.authService.updateProfile(this.profileForm.email.trim(), this.profileForm.fullName?.trim() || null).subscribe({
      next: () => {
        this.profileSubmitting.set(false);
        this.messageService.add({ severity: 'success', summary: 'Profile updated', life: 2500 });
      },
      error: err => {
        this.profileSubmitting.set(false);
        this.profileError.set(err?.error?.message || 'Could not update profile.');
      },
    });
  }

  openChangePassword(): void {
    this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
    this.passwordError.set(null);
    this.passwordDialogVisible.set(true);
  }

  changePassword(): void {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm;
    if (!currentPassword || !newPassword || !confirmPassword) {
      this.passwordError.set('All fields are required.');
      return;
    }
    if (newPassword.length < 8) {
      this.passwordError.set('New password must be at least 8 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      this.passwordError.set('New password and confirmation do not match.');
      return;
    }
    this.passwordSubmitting.set(true);
    this.passwordError.set(null);
    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.passwordSubmitting.set(false);
        this.passwordDialogVisible.set(false);
        // The backend just invalidated every existing session (this one
        // included), so the cookie we're holding no longer works - clear
        // local state and send him to log in fresh rather than leaving him
        // on a page that will 401 on the next request.
        this.authService.clearSession();
        this.router.navigate(['/login'], { queryParams: { message: 'Password changed. Please log in again.' } });
      },
      error: err => {
        this.passwordSubmitting.set(false);
        this.passwordError.set(err?.error?.message || 'Could not change password.');
      },
    });
  }
}
