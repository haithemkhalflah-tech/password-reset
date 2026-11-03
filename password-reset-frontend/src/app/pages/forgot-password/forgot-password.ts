
import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {

  message = '';
  errorMessage = '';
  loading = false;

  forgotForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    this.message = '';
    this.errorMessage = '';

    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const email = this.forgotForm.value.email;

    this.authService.forgotPassword(email).subscribe({
      next: (response: any) => {
        this.message = response.message;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Request error:', error);
        this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}