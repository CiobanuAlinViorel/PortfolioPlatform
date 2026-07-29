import { Component, ElementRef, ViewChild, AfterViewInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatIconModule } from "@angular/material/icon";
import { AuthService } from "../services/auth.service";
import { GoogleIdentityService } from "../services/google-identity.service";

function passwordsMatchValidator(): ValidatorFn {
    return (control): ValidationErrors | null => {
        const password = control.get("password")?.value;
        const confirmPassword = control.get("confirmPassword")?.value;
        return password && confirmPassword && password !== confirmPassword
            ? { passwordsMismatch: true }
            : null;
    };
}

@Component({
    selector: "register-page",
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MatIconModule,
    ],
    templateUrl: "../ui/register.page.html",
})
export class RegisterPage implements AfterViewInit {
    @ViewChild("googleButton") googleButtonRef?: ElementRef<HTMLDivElement>;

    private readonly fb = inject(FormBuilder);
    private readonly authService = inject(AuthService);
    private readonly googleIdentityService = inject(GoogleIdentityService);
    private readonly router = inject(Router);

    readonly isGoogleConfigured = this.googleIdentityService.isConfigured;

    readonly form = this.fb.group(
        {
            email: this.fb.control("", [Validators.required, Validators.email]),
            password: this.fb.control("", [Validators.required, Validators.minLength(6)]),
            confirmPassword: this.fb.control("", [Validators.required]),
        },
        { validators: passwordsMatchValidator() },
    );

    readonly submitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly hidePassword = signal(true);
    readonly hideConfirmPassword = signal(true);

    togglePasswordVisibility(): void {
        this.hidePassword.update((hidden) => !hidden);
    }

    toggleConfirmPasswordVisibility(): void {
        this.hideConfirmPassword.update((hidden) => !hidden);
    }

    ngAfterViewInit(): void {
        if (this.googleButtonRef) {
            this.googleIdentityService.renderButton(this.googleButtonRef.nativeElement, (idToken) =>
                this.handleGoogleCredential(idToken),
            );
        }
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.errorMessage.set(null);
        this.submitting.set(true);
        const { email, password } = this.form.getRawValue();

        this.authService.register({ email: email!, password: password! }).subscribe({
            next: () => {
                this.submitting.set(false);
                this.router.navigate(["/login"]);
            },
            error: (err) => {
                this.submitting.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private handleGoogleCredential(idToken: string): void {
        this.errorMessage.set(null);
        this.submitting.set(true);

        this.authService.googleAuth({ idToken }).subscribe({
            next: () => {
                this.submitting.set(false);
                this.router.navigate(["/"]);
            },
            error: (err) => {
                this.submitting.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private extractErrorMessage(err: unknown): string {
        const body = (err as { error?: { message?: string } })?.error;
        if (body?.message) return body.message;
        if (body && typeof body === "object") {
            const firstFieldError = Object.values(body)[0];
            if (typeof firstFieldError === "string") return firstFieldError;
        }
        return "Something went wrong. Please try again.";
    }
}
