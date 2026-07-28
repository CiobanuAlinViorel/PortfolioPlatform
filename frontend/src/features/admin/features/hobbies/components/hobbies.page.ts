import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideCheck,
    LucideCircleAlert,
    LucideFilter,
    LucideHeart,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSearch,
    LucideSparkles,
    LucideTrash2,
    LucideX,
} from "@lucide/angular";
import { HobbiesService } from "../services/hobbies.service";
import {
    ACTIVITY_LEVELS,
    ActivityLevel,
    COMPLEXITY_LEVELS,
    ComplexityLevel,
    HOBBY_CATEGORIES,
    HobbyCategory,
    HobbyDetail,
    HobbyListItem,
    HobbyWriteRequest,
    IMPACT_LEVELS,
    ImpactLevel,
    SkillOption,
} from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "skills";

interface HobbySkillRow {
    id: number | null;
    skillId: number | null;
    usagePercentage: number | null;
}

const CATEGORY_BADGE_CLASSES: Record<HobbyCategory, string> = {
    LEARNING: "bg-blue-500/15 text-blue-700 dark:text-blue-300",
    SPORTS: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    CREATIVE: "bg-purple-500/15 text-purple-700 dark:text-purple-300",
    SOCIAL: "bg-pink-500/15 text-pink-700 dark:text-pink-300",
    TECHNOLOGY: "bg-cyan-500/15 text-cyan-700 dark:text-cyan-300",
    MUSIC: "bg-rose-500/15 text-rose-700 dark:text-rose-300",
    TRAVEL: "bg-amber-500/15 text-amber-700 dark:text-amber-300",
    COOKING: "bg-orange-500/15 text-orange-700 dark:text-orange-300",
    GARDENING: "bg-lime-500/15 text-lime-700 dark:text-lime-300",
    READING: "bg-indigo-500/15 text-indigo-700 dark:text-indigo-300",
    GAMING: "bg-fuchsia-500/15 text-fuchsia-700 dark:text-fuchsia-300",
};

@Component({
    selector: "app-admin-hobbies",
    imports: [
        ReactiveFormsModule,
        FormsModule,
        LucideCheck,
        LucideCircleAlert,
        LucideFilter,
        LucideHeart,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSearch,
        LucideSparkles,
        LucideTrash2,
        LucideX,
    ],
    templateUrl: "../ui/hobbies.page.html",
    styles: [":host { display: contents; }"],
})
export class HobbiesPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly hobbiesService = inject(HobbiesService);

    readonly hobbyCategories = HOBBY_CATEGORIES;
    readonly activityLevels = ACTIVITY_LEVELS;
    readonly complexityLevels = COMPLEXITY_LEVELS;
    readonly impactLevels = IMPACT_LEVELS;

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly hobbies = signal<HobbyListItem[]>([]);
    readonly skillOptions = signal<SkillOption[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);

    readonly searchQuery = signal("");
    readonly showFilterModal = signal(false);

    readonly filterCategories = signal<HobbyCategory[]>([]);
    readonly filterActivityLevels = signal<ActivityLevel[]>([]);

    draftCategories: HobbyCategory[] = [];
    draftActivityLevels: ActivityLevel[] = [];

    readonly activeFilterCount = computed(() => this.filterCategories().length + this.filterActivityLevels().length);

    readonly filteredHobbies = computed(() => {
        const query = this.searchQuery().trim().toLowerCase();
        const categories = this.filterCategories();
        const activityLevels = this.filterActivityLevels();

        return this.hobbies().filter((h) => {
            if (
                query &&
                !h.name.toLowerCase().includes(query) &&
                !(h.description ?? "").toLowerCase().includes(query) &&
                !(h.favoriteAspect ?? "").toLowerCase().includes(query)
            )
                return false;
            if (categories.length > 0 && !categories.includes(h.category)) return false;
            if (activityLevels.length > 0 && !(h.activityLevel && activityLevels.includes(h.activityLevel))) return false;
            return true;
        });
    });

    readonly form = this.fb.group({
        name: this.fb.control("", [Validators.required]),
        category: this.fb.control<HobbyCategory>("LEARNING", [Validators.required]),
        activityLevel: this.fb.control<ActivityLevel | "">(""),
        complexityLevel: this.fb.control<ComplexityLevel | "">(""),
        impactOnWork: this.fb.control<ImpactLevel | "">(""),
        yearsActive: this.fb.control<number | null>(null),
        whyInterested: this.fb.control(""),
        favoriteAspect: this.fb.control(""),
        description: this.fb.control(""),
    });

    skills: HobbySkillRow[] = [];

    categoryBadgeClass(category: HobbyCategory): string {
        return CATEGORY_BADGE_CLASSES[category];
    }

    skillName(skillId: number | null): string {
        if (skillId == null) return "";
        return this.skillOptions().find((s) => s.id === skillId)?.name ?? "";
    }

    ngOnInit(): void {
        this.loadList();
        this.loadSkillOptions();
    }

    // ── List / search / filters ───────────────────────────────────────────────

    openFilterModal(): void {
        this.draftCategories = [...this.filterCategories()];
        this.draftActivityLevels = [...this.filterActivityLevels()];
        this.showFilterModal.set(true);
    }

    closeFilterModal(): void {
        this.showFilterModal.set(false);
    }

    applyFilters(): void {
        this.filterCategories.set([...this.draftCategories]);
        this.filterActivityLevels.set([...this.draftActivityLevels]);
        this.showFilterModal.set(false);
    }

    clearDraftFilters(): void {
        this.draftCategories = [];
        this.draftActivityLevels = [];
    }

    toggleDraftCategory(category: HobbyCategory): void {
        this.draftCategories = this.toggleInList(this.draftCategories, category);
    }

    toggleDraftActivityLevel(level: ActivityLevel): void {
        this.draftActivityLevels = this.toggleInList(this.draftActivityLevels, level);
    }

    private toggleInList<T>(list: T[], value: T): T[] {
        return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
    }

    // ── Create / edit form ────────────────────────────────────────────────────

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            name: "",
            category: "LEARNING",
            activityLevel: "",
            complexityLevel: "",
            impactOnWork: "",
            yearsActive: null,
            whyInterested: "",
            favoriteAspect: "",
            description: "",
        });
        this.skills = [];
        this.activeTab.set("basic");
        this.mode.set("form");
    }

    startEdit(item: HobbyListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.hobbiesService.get(item.id).subscribe({
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

    tabButtonClass(tab: FormTab): string {
        const base = "flex items-center gap-1.5 whitespace-nowrap border-b-2 px-3 py-2.5 text-sm font-medium transition";
        return this.activeTab() === tab
            ? `${base} border-emerald-900 text-emerald-900 dark:border-beige-light dark:text-beige-light`
            : `${base} border-transparent text-emerald-900/50 hover:text-emerald-900/80 dark:text-beige-light/50 dark:hover:text-beige-light/80`;
    }

    addSkill(): void {
        this.skills.push({ id: null, skillId: null, usagePercentage: 50 });
    }

    removeSkill(index: number): void {
        this.skills.splice(index, 1);
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

        const request: HobbyWriteRequest = {
            name: raw.name!,
            description: raw.description || null,
            category: raw.category!,
            activityLevel: raw.activityLevel || undefined,
            complexityLevel: raw.complexityLevel || undefined,
            impactOnWork: raw.impactOnWork || undefined,
            yearsActive: raw.yearsActive,
            whyInterested: raw.whyInterested || null,
            favoriteAspect: raw.favoriteAspect || null,
            skills: this.skills
                .filter((s) => s.skillId != null)
                .map((s) => ({
                    id: s.id,
                    skillId: s.skillId!,
                    usagePercentage: s.usagePercentage ?? 0,
                })),
        };

        const editingId = this.editingId();
        const save$ = editingId == null ? this.hobbiesService.create(request) : this.hobbiesService.update(editingId, request);

        save$.subscribe({
            next: () => {
                this.saving.set(false);
                this.mode.set("list");
                this.loadList();
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

    deleteHobby(id: number): void {
        this.deletingId.set(id);
        this.hobbiesService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.hobbies.update((list) => list.filter((h) => h.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.hobbiesService.list().subscribe({
            next: (hobbies) => {
                this.hobbies.set(hobbies);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadSkillOptions(): void {
        this.hobbiesService.listSkills().subscribe({
            next: (skills) => this.skillOptions.set(skills),
            error: () => {},
        });
    }

    private populateForm(detail: HobbyDetail): void {
        this.form.reset({
            name: detail.name,
            category: detail.category,
            activityLevel: detail.activityLevel ?? "",
            complexityLevel: detail.complexityLevel ?? "",
            impactOnWork: detail.impactOnWork ?? "",
            yearsActive: detail.yearsActive ?? null,
            whyInterested: detail.whyInterested ?? "",
            favoriteAspect: detail.favoriteAspect ?? "",
            description: detail.description ?? "",
        });

        this.skills = detail.skills.map((s) => ({
            id: null,
            skillId: s.skillId,
            usagePercentage: s.usagePercentage,
        }));
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
