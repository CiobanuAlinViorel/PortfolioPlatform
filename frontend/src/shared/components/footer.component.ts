import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { RouterLink } from "@angular/router";
import {
    LucideArrowUp,
    LucideCheck,
    LucideMail,
    LucideMapPin,
    LucideMessageCircle,
    LucidePhone,
    LucideSend,
} from "@lucide/angular";
import { ContactInfo, ProfileInfo } from "../../features/admin/features/general/types";
import { ContactService } from "../services/contact.service";
import { ToastService } from "../services/toast.service";
import { ButtonComponent } from "./button.component";
import { BrandIconComponent } from "./brand-icon.component";

@Component({
    selector: "app-footer",
    imports: [
        ReactiveFormsModule,
        RouterLink,
        LucideArrowUp,
        LucideCheck,
                LucideMail,
        LucideMapPin,
        LucideMessageCircle,
        LucidePhone,
        LucideSend,
        ButtonComponent,
        BrandIconComponent,
    ],
    templateUrl: "../ui/footer.component.html",
})
export class FooterComponent implements OnInit {
    private readonly contactService = inject(ContactService);
    private readonly toast = inject(ToastService);
    private readonly fb = inject(FormBuilder);

    readonly currentYear = new Date().getFullYear();

    readonly contact = signal<ContactInfo | null>(null);
    readonly profile = signal<ProfileInfo | null>(null);

    readonly form = this.fb.nonNullable.group({
        name: ["", [Validators.required, Validators.minLength(2)]],
        email: ["", [Validators.required, Validators.email]],
        message: ["", [Validators.required, Validators.minLength(10)]],
    });

    readonly submitting = signal(false);
    readonly status = signal<"idle" | "success" | "error" | "rate-limited">("idle");

    readonly exploreLinks = [
        { label: "Projects", path: "/projects" },
        { label: "Skills", path: "/skills" },
        { label: "Work experience", path: "/experience/jobs" },
        { label: "Volunteering", path: "/experience/volunteer" },
        { label: "Certificates", path: "/certificates" },
        { label: "Education", path: "/education" },
        { label: "Hobbies", path: "/hobbies" },
    ];

    ngOnInit(): void {
        this.contactService.getContactInfo().subscribe({
            next: (summary) => {
                this.contact.set(summary.contact);
                this.profile.set(summary.profile);
            },
        });
    }

    whatsappLink(phone: string): string {
        const digits = phone.replace(/[^\d+]/g, "");
        return `https://wa.me/${digits.replace(/^\+/, "")}`;
    }

    fullName(): string {
        const p = this.profile();
        return p ? `${p.firstName} ${p.lastName}`.trim() : "Alin-Viorel Ciobanu";
    }

    invalid(control: "name" | "email" | "message"): boolean {
        const c = this.form.controls[control];
        return c.invalid && (c.touched || c.dirty);
    }

    submit(): void {
        if (this.submitting()) return;
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.submitting.set(true);
        this.status.set("idle");
        const { name, email, message } = this.form.getRawValue();
        this.contactService.sendMessage({ name: name.trim(), email: email.trim(), message: message.trim() }).subscribe({
            next: () => {
                this.submitting.set(false);
                this.status.set("success");
                this.form.reset();
                this.toast.show("Message sent — thank you!", "success");
            },
            error: (err) => {
                this.submitting.set(false);
                this.status.set(err?.status === 429 ? "rate-limited" : "error");
            },
        });
    }

    sendAnother(): void {
        this.status.set("idle");
    }

    scrollToTop(): void {
        window.scrollTo({ top: 0, behavior: "smooth" });
    }
}
