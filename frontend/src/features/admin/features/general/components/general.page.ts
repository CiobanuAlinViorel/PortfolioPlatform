import { NgTemplateOutlet } from "@angular/common";
import { Component, OnDestroy, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideCheck,
    LucideCircleAlert,
    LucideLink,
    LucideLoaderCircle,
    LucideMail,
    LucideMapPin,
    LucidePencil,
    LucidePhone,
    LucideUpload,
    LucideUser,
    LucideX,
} from "@lucide/angular";
import { GeneralService } from "../services/general.service";
import { ContactInfo, CreateProfileRequest, ProfileInfo, UpdateProfileRequest } from "../types";
import { ImageCropperComponent } from "./image-cropper.component";

type PageMode = "loading" | "view" | "edit" | "create";

@Component({
    selector: "app-admin-general",
    imports: [
        ReactiveFormsModule,
        NgTemplateOutlet,
        ImageCropperComponent,
        LucideCheck,
        LucideCircleAlert,
        LucideLink,
        LucideLoaderCircle,
        LucideMail,
        LucideMapPin,
        LucidePencil,
        LucidePhone,
        LucideUpload,
        LucideUser,
        LucideX,
    ],
    templateUrl: "../ui/general.page.html",
})
export class GeneralPage implements OnInit, OnDestroy {
    private readonly fb = inject(FormBuilder);
    private readonly generalService = inject(GeneralService);

    readonly mode = signal<PageMode>("loading");
    readonly profile = signal<ProfileInfo | null>(null);
    readonly contact = signal<ContactInfo | null>(null);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);
    readonly imagePreview = signal<string | null>(null);
    readonly isDraggingOver = signal(false);
    readonly cropperFile = signal<File | null>(null);

    private selectedImage: File | null = null;
    private objectUrl: string | null = null;

    readonly form = this.fb.group({
        firstName: this.fb.control("", [Validators.required]),
        lastName: this.fb.control("", [Validators.required]),
        age: this.fb.control<number | null>(null),
        description: this.fb.control(""),
        email: this.fb.control("", [Validators.required, Validators.email]),
        phone: this.fb.control(""),
        github: this.fb.control(""),
        linkedin: this.fb.control(""),
        city: this.fb.control(""),
        country: this.fb.control(""),
    });

    ngOnInit(): void {
        this.load();
    }

    ngOnDestroy(): void {
        this.releaseObjectUrl();
    }

    startEdit(): void {
        const p = this.profile();
        const c = this.contact();
        this.form.reset({
            firstName: p?.firstName ?? "",
            lastName: p?.lastName ?? "",
            age: p?.age ?? null,
            description: p?.description ?? "",
            email: c?.email ?? "",
            phone: c?.phone ?? "",
            github: c?.github ?? "",
            linkedin: c?.linkedin ?? "",
            city: c?.city ?? "",
            country: c?.country ?? "",
        });
        this.selectedImage = null;
        this.releaseObjectUrl();
        this.imagePreview.set(p?.imageLink ?? null);
        this.errorMessage.set(null);
        this.mode.set("edit");
    }

    cancelEdit(): void {
        this.errorMessage.set(null);
        this.mode.set(this.profile() ? "view" : "create");
    }

    onImageSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (file) {
            this.cropperFile.set(file);
        }
        input.value = "";
    }

    onDragOver(event: DragEvent): void {
        event.preventDefault();
        this.isDraggingOver.set(true);
    }

    onDragLeave(event: DragEvent): void {
        event.preventDefault();
        this.isDraggingOver.set(false);
    }

    onImageDrop(event: DragEvent): void {
        event.preventDefault();
        this.isDraggingOver.set(false);
        const file = event.dataTransfer?.files?.[0];
        if (file && file.type.startsWith("image/")) {
            this.cropperFile.set(file);
        }
    }

    onImageCropped(file: File): void {
        this.cropperFile.set(null);
        this.applyImageFile(file);
    }

    onCropCancelled(): void {
        this.cropperFile.set(null);
    }

    private applyImageFile(file: File): void {
        this.selectedImage = file;
        this.releaseObjectUrl();
        this.objectUrl = URL.createObjectURL(file);
        this.imagePreview.set(this.objectUrl);
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.errorMessage.set(null);
        this.saving.set(true);
        const raw = this.form.getRawValue();

        const contactInfo: ContactInfo = {
            email: raw.email!,
            phone: raw.phone || undefined,
            github: raw.github || undefined,
            linkedin: raw.linkedin || undefined,
            city: raw.city || undefined,
            country: raw.country || undefined,
        };

        const request: CreateProfileRequest | UpdateProfileRequest = {
            firstName: raw.firstName!,
            lastName: raw.lastName!,
            age: raw.age,
            description: raw.description || undefined,
            contactInfo,
        };

        const save$ =
            this.mode() === "create"
                ? this.generalService.createProfile(request as CreateProfileRequest, this.selectedImage)
                : this.generalService.updateProfile(request, this.selectedImage);

        save$.subscribe({
            next: () => {
                this.saving.set(false);
                this.selectedImage = null;
                this.load();
            },
            error: (err) => {
                this.saving.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private load(): void {
        this.mode.set("loading");
        this.generalService.getSummary().subscribe({
            next: (summary) => {
                this.profile.set(summary.profile);
                this.contact.set(summary.contact);
                this.mode.set("view");
            },
            error: (err) => {
                if (err.status === 404) {
                    this.profile.set(null);
                    this.contact.set(null);
                    this.mode.set("create");
                } else {
                    this.errorMessage.set("Failed to load profile information.");
                    this.mode.set("view");
                }
            },
        });
    }

    private releaseObjectUrl(): void {
        if (this.objectUrl) {
            URL.revokeObjectURL(this.objectUrl);
            this.objectUrl = null;
        }
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
