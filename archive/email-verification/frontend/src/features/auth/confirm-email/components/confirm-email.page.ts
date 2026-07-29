import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { AuthService } from "../../register/services/auth.service";

type ConfirmStatus = "ready" | "loading" | "success" | "error";

@Component({
    selector: "confirm-email-page",
    imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
    templateUrl: "../ui/confirm-email.page.html",
})
export class ConfirmEmailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly authService = inject(AuthService);

    private token: string | null = null;

    readonly status = signal<ConfirmStatus>("ready");
    readonly message = signal<string>("");

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get("token");
        if (!this.token) {
            this.status.set("error");
            this.message.set("This confirmation link is missing its token.");
        }
    }

    confirm(): void {
        if (!this.token) return;

        this.status.set("loading");
        this.authService.verifyEmail(this.token).subscribe({
            next: (res) => {
                this.status.set("success");
                this.message.set(res.message);
            },
            error: (err) => {
                this.status.set("error");
                this.message.set(
                    (err as { error?: { message?: string } })?.error?.message ??
                        "This confirmation link is invalid or has expired.",
                );
            },
        });
    }
}
