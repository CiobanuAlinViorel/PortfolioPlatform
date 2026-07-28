import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideAward,
    LucideBadgeCheck,
    LucideCalendar,
    LucideCheck,
    LucideCircleAlert,
    LucideExternalLink,
    LucideFilter,
    LucideFolders,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSearch,
    LucideTrash2,
    LucideX,
} from "@lucide/angular";
import { CertificatesService } from "../services/certificates.service";
import { CategoryWriteRequest, Certificate, CertificateCategory, CertificateWriteRequest } from "../types";

type PageMode = "list" | "form";

interface CategoryRow {
    id: number | null;
    name: string;
    description: string;
    industry: string;
    sortOrder: number | null;
}

@Component({
    selector: "app-admin-certificates",
    imports: [
        ReactiveFormsModule,
        FormsModule,
        LucideAward,
        LucideBadgeCheck,
        LucideCalendar,
        LucideCheck,
        LucideCircleAlert,
        LucideExternalLink,
        LucideFilter,
        LucideFolders,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSearch,
        LucideTrash2,
        LucideX,
    ],
    templateUrl: "../ui/certificates.page.html",
    styles: [":host { display: contents; }"],
})
export class CertificatesPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly certificatesService = inject(CertificatesService);

    readonly mode = signal<PageMode>("list");
    readonly certificates = signal<Certificate[]>([]);
    readonly categories = signal<CertificateCategory[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);

    readonly searchQuery = signal("");
    readonly showFilterModal = signal(false);

    readonly filterCategories = signal<string[]>([]);
    readonly filterVerifiedOnly = signal(false);
    readonly filterExpiringOnly = signal(false);

    draftCategories: string[] = [];
    draftVerifiedOnly = false;
    draftExpiringOnly = false;

    readonly categoryNames = computed(() => this.categories().map((c) => c.name));

    readonly activeFilterCount = computed(
        () => this.filterCategories().length + (this.filterVerifiedOnly() ? 1 : 0) + (this.filterExpiringOnly() ? 1 : 0),
    );

    readonly filteredCertificates = computed(() => {
        const query = this.searchQuery().trim().toLowerCase();
        const categories = this.filterCategories();
        const verifiedOnly = this.filterVerifiedOnly();
        const expiringOnly = this.filterExpiringOnly();

        return this.certificates().filter((c) => {
            if (
                query &&
                !c.name.toLowerCase().includes(query) &&
                !c.provider.toLowerCase().includes(query) &&
                !(c.description ?? "").toLowerCase().includes(query)
            )
                return false;
            if (categories.length > 0 && !(c.categoryName && categories.includes(c.categoryName))) return false;
            if (verifiedOnly && !c.verified) return false;
            if (expiringOnly && !c.hasExpiry) return false;
            return true;
        });
    });

    readonly form = this.fb.group({
        name: this.fb.control("", [Validators.required]),
        provider: this.fb.control("", [Validators.required]),
        categoryName: this.fb.control(""),
        credentialId: this.fb.control(""),
        certificateUrl: this.fb.control(""),
        issueDate: this.fb.control("", [Validators.required]),
        hasExpiry: this.fb.control(false),
        expiryDate: this.fb.control(""),
        score: this.fb.control(""),
        relevanceScore: this.fb.control<number | null>(50),
        verified: this.fb.control(false),
        description: this.fb.control(""),
    });

    // ── Category management modal ─────────────────────────────────────────────
    readonly showCategoryModal = signal(false);
    readonly categoryRows = signal<CategoryRow[]>([]);
    readonly editingCategoryId = signal<number | null>(null);
    readonly savingCategory = signal(false);
    readonly categoryDeleteConfirmId = signal<number | null>(null);
    readonly deletingCategoryId = signal<number | null>(null);
    readonly categoryError = signal<string | null>(null);

    readonly categoryForm = this.fb.group({
        name: this.fb.control("", [Validators.required]),
        description: this.fb.control(""),
        industry: this.fb.control(""),
        sortOrder: this.fb.control<number | null>(null),
    });

    ngOnInit(): void {
        this.loadList();
        this.loadCategories();
    }

    // ── List / search / filters ───────────────────────────────────────────────

    openFilterModal(): void {
        this.draftCategories = [...this.filterCategories()];
        this.draftVerifiedOnly = this.filterVerifiedOnly();
        this.draftExpiringOnly = this.filterExpiringOnly();
        this.showFilterModal.set(true);
    }

    closeFilterModal(): void {
        this.showFilterModal.set(false);
    }

    applyFilters(): void {
        this.filterCategories.set([...this.draftCategories]);
        this.filterVerifiedOnly.set(this.draftVerifiedOnly);
        this.filterExpiringOnly.set(this.draftExpiringOnly);
        this.showFilterModal.set(false);
    }

    clearDraftFilters(): void {
        this.draftCategories = [];
        this.draftVerifiedOnly = false;
        this.draftExpiringOnly = false;
    }

    toggleDraftCategory(name: string): void {
        this.draftCategories = this.draftCategories.includes(name)
            ? this.draftCategories.filter((v) => v !== name)
            : [...this.draftCategories, name];
    }

    // ── Create / edit form ────────────────────────────────────────────────────

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            name: "",
            provider: "",
            categoryName: "",
            credentialId: "",
            certificateUrl: "",
            issueDate: "",
            hasExpiry: false,
            expiryDate: "",
            score: "",
            relevanceScore: 50,
            verified: false,
            description: "",
        });
        this.mode.set("form");
    }

    startEdit(item: Certificate): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.mode.set("form");
        this.loadingForm.set(true);

        this.certificatesService.get(item.id).subscribe({
            next: (detail) => {
                this.populateForm(detail);
                this.loadingForm.set(false);
            },
            error: (err) => {
                this.loadingForm.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
                this.mode.set("list");
            },
        });
    }

    cancelForm(): void {
        this.mode.set("list");
        this.editingId.set(null);
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.errorMessage.set("Please fix the highlighted fields.");
            return;
        }

        this.errorMessage.set(null);
        this.saving.set(true);
        const raw = this.form.getRawValue();

        const request: CertificateWriteRequest = {
            name: raw.name!,
            provider: raw.provider!,
            categoryName: raw.categoryName || null,
            credentialId: raw.credentialId || null,
            certificateUrl: raw.certificateUrl || null,
            issueDate: raw.issueDate!,
            hasExpiry: raw.hasExpiry ?? false,
            expiryDate: raw.hasExpiry ? raw.expiryDate || null : null,
            score: raw.score || null,
            relevanceScore: raw.relevanceScore,
            description: raw.description || null,
        };

        const editingId = this.editingId();
        const save$ = editingId == null ? this.certificatesService.create(request) : this.certificatesService.update(editingId, request);

        save$.subscribe({
            next: () => {
                this.saving.set(false);
                this.mode.set("list");
                this.loadList();
                this.loadCategories();
            },
            error: (err) => {
                this.saving.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    confirmDelete(id: number): void {
        this.deleteConfirmId.set(id);
    }

    cancelDelete(): void {
        this.deleteConfirmId.set(null);
    }

    deleteCertificate(id: number): void {
        this.deletingId.set(id);
        this.certificatesService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.certificates.update((list) => list.filter((c) => c.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.certificatesService.list().subscribe({
            next: (certificates) => {
                this.certificates.set(certificates);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadCategories(): void {
        this.certificatesService.listCategories().subscribe({
            next: (categories) => this.categories.set(categories),
            error: () => {},
        });
    }

    private populateForm(detail: Certificate): void {
        this.form.reset({
            name: detail.name,
            provider: detail.provider,
            categoryName: detail.categoryName ?? "",
            credentialId: detail.credentialId ?? "",
            certificateUrl: detail.certificateUrl ?? "",
            issueDate: detail.issueDate ?? "",
            hasExpiry: detail.hasExpiry ?? false,
            expiryDate: detail.expiryDate ?? "",
            score: detail.score ?? "",
            relevanceScore: detail.relevanceScore ?? 50,
            verified: detail.verified ?? false,
            description: detail.description ?? "",
        });
    }

    // ── Category management modal ─────────────────────────────────────────────

    openCategoryModal(): void {
        this.categoryError.set(null);
        this.refreshCategoryRows();
        this.startCreateCategory();
        this.showCategoryModal.set(true);
    }

    closeCategoryModal(): void {
        this.showCategoryModal.set(false);
        this.loadCategories();
    }

    private refreshCategoryRows(): void {
        this.categoryRows.set(
            this.categories().map((c) => ({
                id: c.id,
                name: c.name,
                description: c.description ?? "",
                industry: c.industry ?? "",
                sortOrder: c.sortOrder ?? null,
            })),
        );
    }

    startCreateCategory(): void {
        this.editingCategoryId.set(null);
        this.categoryError.set(null);
        this.categoryForm.reset({ name: "", description: "", industry: "", sortOrder: null });
    }

    startEditCategory(row: CategoryRow): void {
        this.editingCategoryId.set(row.id);
        this.categoryError.set(null);
        this.categoryForm.reset({
            name: row.name,
            description: row.description,
            industry: row.industry,
            sortOrder: row.sortOrder,
        });
    }

    submitCategory(): void {
        if (this.categoryForm.invalid) {
            this.categoryForm.markAllAsTouched();
            this.categoryError.set("Please fill in the category name.");
            return;
        }

        this.categoryError.set(null);
        this.savingCategory.set(true);
        const raw = this.categoryForm.getRawValue();

        const request: CategoryWriteRequest = {
            name: raw.name!,
            description: raw.description || null,
            industry: raw.industry || null,
            sortOrder: raw.sortOrder,
        };

        const editingId = this.editingCategoryId();
        const save$ =
            editingId == null ? this.certificatesService.createCategory(request) : this.certificatesService.updateCategory(editingId, request);

        save$.subscribe({
            next: (category) => {
                this.savingCategory.set(false);
                this.categories.update((list) => {
                    const exists = list.some((c) => c.id === category.id);
                    return exists ? list.map((c) => (c.id === category.id ? category : c)) : [...list, category];
                });
                this.refreshCategoryRows();
                this.startCreateCategory();
            },
            error: (err) => {
                this.savingCategory.set(false);
                this.categoryError.set(this.extractErrorMessage(err));
            },
        });
    }

    confirmDeleteCategory(id: number): void {
        this.categoryDeleteConfirmId.set(id);
    }

    cancelDeleteCategory(): void {
        this.categoryDeleteConfirmId.set(null);
    }

    deleteCategory(id: number): void {
        this.deletingCategoryId.set(id);
        this.certificatesService.deleteCategory(id).subscribe({
            next: () => {
                this.deletingCategoryId.set(null);
                this.categoryDeleteConfirmId.set(null);
                this.categories.update((list) => list.filter((c) => c.id !== id));
                this.refreshCategoryRows();
                if (this.editingCategoryId() === id) this.startCreateCategory();
            },
            error: (err) => {
                this.deletingCategoryId.set(null);
                this.categoryError.set(this.extractErrorMessage(err));
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
