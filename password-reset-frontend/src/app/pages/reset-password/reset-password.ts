
import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword {

  message = '';
  errorMessage = '';
  loading = false;

  token = '';
  resetForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8)
      ]],
      confirmPassword: ['', Validators.required]
    });

    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';

    if (!this.token) {
      this.errorMessage = 'Le lien de réinitialisation est invalide.';
    }
  }

  onSubmit(): void {

    this.message = '';
    this.errorMessage = '';

    if (!this.token) {
      this.errorMessage = 'Le lien de réinitialisation est invalide.';
      return;
    }

    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    const newPassword = this.resetForm.get('newPassword')?.value;
    const confirmPassword = this.resetForm.get('confirmPassword')?.value;

    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Les deux mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;

    this.authService.resetPassword(this.token, newPassword).subscribe({

      next: (response: any) => {
        this.message = response.message || 'Votre mot de passe a été réinitialisé.';
        this.loading = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Reset password error:', error);

        this.errorMessage =
          error.error?.message || 'Le lien est invalide ou a expiré.';

        this.loading = false;
        this.cdr.detectChanges();
      }

    });
  }
}
