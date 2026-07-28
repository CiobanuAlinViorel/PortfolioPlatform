import { Component, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import {
    LucideLink,
    LucideMail,
    LucideMapPin,
    LucideMessageCircle,
    LucidePhone,
    LucideSend,
} from "@lucide/angular";
import { ContactInfo } from "../../features/admin/features/general/types";
import { ContactService } from "../services/contact.service";

@Component({
    selector: "app-footer",
    imports: [FormsModule, LucideMail, LucidePhone, LucideMapPin, LucideLink, LucideMessageCircle, LucideSend],
    templateUrl: "../ui/footer.component.html",
})
export class FooterComponent implements OnInit {
    private readonly contactService = inject(ContactService);

    readonly currentYear = new Date().getFullYear();

    private readonly contactSignal = signal<ContactInfo | null>(null);
    contact = this.contactSignal.asReadonly();

    name = "";
    email = "";
    message = "";

    private readonly submittingSignal = signal(false);
    submitting = this.submittingSignal.asReadonly();

    private readonly statusSignal = signal<"idle" | "success" | "error" | "rate-limited">("idle");
    status = this.statusSignal.asReadonly();

    ngOnInit(): void {
        this.contactService.getContactInfo().subscribe({
            next: (summary) => this.contactSignal.set(summary.contact),
        });
    }

    whatsappLink(phone: string): string {
        const digits = phone.replace(/[^\d+]/g, "");
        return `https://wa.me/${digits.replace(/^\+/, "")}`;
    }

    submit(): void {
        if (!this.name.trim() || !this.email.trim() || !this.message.trim() || this.submitting()) {
            return;
        }
        this.submittingSignal.set(true);
        this.statusSignal.set("idle");
        this.contactService
            .sendMessage({ name: this.name.trim(), email: this.email.trim(), message: this.message.trim() })
            .subscribe({
                next: () => {
                    this.submittingSignal.set(false);
                    this.statusSignal.set("success");
                    this.name = "";
                    this.email = "";
                    this.message = "";
                },
                error: (err) => {
                    this.submittingSignal.set(false);
                    this.statusSignal.set(err?.status === 429 ? "rate-limited" : "error");
                },
            });
    }
}
