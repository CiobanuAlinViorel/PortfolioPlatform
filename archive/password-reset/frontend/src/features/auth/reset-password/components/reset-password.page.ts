import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from "@angular/forms";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatIconModule } from "@angular/material/icon";
import { AuthService } from "../../register/services/auth.service";

function passwordsMatchValidator(): ValidatorFn {
    return (control): ValidationErrors | null => {
        const newPassword = control.get("newPassword")?.value;
        const confirmPassword = control.get("confirmPassword")?.value;
        return newPassword && confirmPassword && newPassword !== confirmPassword
            ? { passwordsMismatch: true }
            : null;
    };
}

type ResetStatus = "form" | "success" | "error";

@Component({
    selector: "reset-password-page",
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
    templateUrl: "../ui/reset-password.page.html",
})
export class ResetPasswordPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly authService = inject(AuthService);
    private readonly route = inject(ActivatedRoute);

    private token: string | null = null;

    readonly status = signal<ResetStatus>("form");
    readonly errorMessage = signal<string | null>(null);
    readonly submitting = signal(false);

    readonly hideNewPassword = signal(true);
    readonly hideConfirmPassword = signal(true);

    toggleNewPasswordVisibility(): void {
        this.hideNewPassword.update((hidden) => !hidden);
    }

    toggleConfirmPasswordVisibility(): void {
        this.hideConfirmPassword.update((hidden) => !hidden);
    }

    readonly form = this.fb.group(
        {
            newPassword: this.fb.control("", [Validators.required, Validators.minLength(6)]),
            confirmPassword: this.fb.control("", [Validators.required]),
        },
        { validators: passwordsMatchValidator() },
    );

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get("token");
        if (!this.token) {
            this.status.set("error");
            this.errorMessage.set("This reset link is missing its token.");
        }
    }

    submit(): void {
        if (!this.token || this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.submitting.set(true);
        this.errorMessage.set(null);
        const { newPassword } = this.form.getRawValue();

        this.authService.resetPassword({ token: this.token, newPassword: newPassword! }).subscribe({
            next: () => {
                this.submitting.set(false);
                this.status.set("success");
            },
            error: (err) => {
                this.submitting.set(false);
                this.status.set("error");
                this.errorMessage.set(
                    (err as { error?: { message?: string } })?.error?.message ??
                        "This reset link is invalid or has expired.",
                );
            },
        });
    }
}
