import { Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatIconModule } from "@angular/material/icon";
import { AuthService } from "../../register/services/auth.service";

@Component({
    selector: "forgot-password-page",
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
    templateUrl: "../ui/forgot-password.page.html",
})
export class ForgotPasswordPage {
    private readonly fb = inject(FormBuilder);
    private readonly authService = inject(AuthService);

    readonly form = this.fb.group({
        email: this.fb.control("", [Validators.required, Validators.email]),
    });

    readonly submitting = signal(false);
    readonly submitted = signal(false);

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.submitting.set(true);
        const { email } = this.form.getRawValue();

        this.authService.forgotPassword({ email: email! }).subscribe({
            next: () => {
                this.submitting.set(false);
                this.submitted.set(true);
            },
            error: () => {
                // Backend always returns a generic success message to avoid leaking
                // whether an email is registered, so treat errors the same way.
                this.submitting.set(false);
                this.submitted.set(true);
            },
        });
    }
}
