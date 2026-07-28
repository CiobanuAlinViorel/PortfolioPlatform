import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideAward,
    LucideCalendar,
    LucideCheck,
    LucideCircleAlert,
    LucideFilter,
    LucideGraduationCap,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSearch,
    LucideSparkles,
    LucideTag,
    LucideTrash2,
    LucideX,
} from "@lucide/angular";
import { SkillsService } from "../services/skills.service";
import {
    LEARNING_STATUSES,
    LearningProgress,
    LearningStatus,
    PROFICIENCY_LEVELS,
    ProficiencyLevel,
    SkillDetail,
    SkillListItem,
    SkillWriteRequest,
} from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "tags" | "learning";

interface LearningRow {
    id: number | null;
    name: string;
    status: LearningStatus;
    startDate: string;
    completionDate: string;
    progressPercentage: number | null;
    timeSpentHours: number | null;
    estimatedCompletion: string;
    description: string;
}

const PROFICIENCY_BADGE_CLASSES: Record<ProficiencyLevel, string> = {
    BEGINNER: "bg-slate-500/15 text-slate-700 dark:text-slate-300",
    INTERMEDIATE: "bg-blue-500/15 text-blue-700 dark:text-blue-300",
    ADVANCED: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    EXPERT: "bg-purple-500/15 text-purple-700 dark:text-purple-300",
};

@Component({
    selector: "app-admin-skills",
    imports: [
        ReactiveFormsModule,
        FormsModule,
        LucideAward,
        LucideCalendar,
        LucideCheck,
        LucideCircleAlert,
        LucideFilter,
        LucideGraduationCap,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSearch,
        LucideSparkles,
        LucideTag,
        LucideTrash2,
        LucideX,
    ],
    templateUrl: "../ui/skills.page.html",
    styles: [":host { display: contents; }"],
})
export class SkillsPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly skillsService = inject(SkillsService);

    readonly proficiencyLevels = PROFICIENCY_LEVELS;
    readonly learningStatuses = LEARNING_STATUSES;

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly skills = signal<SkillListItem[]>([]);
    readonly categoryNames = signal<string[]>([]);
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
    readonly filterProficiencies = signal<ProficiencyLevel[]>([]);
    readonly filterLearningOnly = signal(false);
    readonly filterCertifiedOnly = signal(false);

    draftCategories: string[] = [];
    draftProficiencies: ProficiencyLevel[] = [];
    draftLearningOnly = false;
    draftCertifiedOnly = false;

    readonly activeFilterCount = computed(
        () =>
            this.filterCategories().length +
            this.filterProficiencies().length +
            (this.filterLearningOnly() ? 1 : 0) +
            (this.filterCertifiedOnly() ? 1 : 0),
    );

    readonly filteredSkills = computed(() => {
        const query = this.searchQuery().trim().toLowerCase();
        const categories = this.filterCategories();
        const proficiencies = this.filterProficiencies();
        const learningOnly = this.filterLearningOnly();
        const certifiedOnly = this.filterCertifiedOnly();

        return this.skills().filter((s) => {
            if (query && !s.name.toLowerCase().includes(query) && !(s.description ?? "").toLowerCase().includes(query)) return false;
            if (categories.length > 0 && !(s.categoryName && categories.includes(s.categoryName))) return false;
            if (proficiencies.length > 0 && !proficiencies.includes(s.proficiency)) return false;
            if (learningOnly && !s.learning) return false;
            if (certifiedOnly && !s.hasCertification) return false;
            return true;
        });
    });

    tags: string[] = [];
    newTag = "";

    learningProgresses: LearningRow[] = [];

    readonly form = this.fb.group({
        name: this.fb.control("", [Validators.required]),
        categoryName: this.fb.control(""),
        proficiency: this.fb.control<ProficiencyLevel>("BEGINNER", [Validators.required]),
        level: this.fb.control<number | null>(1),
        yearsOfExperience: this.fb.control<number | null>(0),
        lastUsedDate: this.fb.control(""),
        description: this.fb.control(""),
        hasCertification: this.fb.control(false),
        learning: this.fb.control(false),
        sortOrder: this.fb.control<number | null>(null),
    });

    ngOnInit(): void {
        this.loadList();
        this.loadCategories();
    }

    proficiencyBadgeClass(proficiency: ProficiencyLevel): string {
        return PROFICIENCY_BADGE_CLASSES[proficiency];
    }

    openFilterModal(): void {
        this.draftCategories = [...this.filterCategories()];
        this.draftProficiencies = [...this.filterProficiencies()];
        this.draftLearningOnly = this.filterLearningOnly();
        this.draftCertifiedOnly = this.filterCertifiedOnly();
        this.showFilterModal.set(true);
    }

    closeFilterModal(): void {
        this.showFilterModal.set(false);
    }

    applyFilters(): void {
        this.filterCategories.set([...this.draftCategories]);
        this.filterProficiencies.set([...this.draftProficiencies]);
        this.filterLearningOnly.set(this.draftLearningOnly);
        this.filterCertifiedOnly.set(this.draftCertifiedOnly);
        this.showFilterModal.set(false);
    }

    clearDraftFilters(): void {
        this.draftCategories = [];
        this.draftProficiencies = [];
        this.draftLearningOnly = false;
        this.draftCertifiedOnly = false;
    }

    toggleDraftCategory(name: string): void {
        this.draftCategories = this.toggleInList(this.draftCategories, name);
    }

    toggleDraftProficiency(level: ProficiencyLevel): void {
        this.draftProficiencies = this.toggleInList(this.draftProficiencies, level);
    }

    private toggleInList<T>(list: T[], value: T): T[] {
        return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
    }

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            name: "",
            categoryName: "",
            proficiency: "BEGINNER",
            level: 1,
            yearsOfExperience: 0,
            lastUsedDate: "",
            description: "",
            hasCertification: false,
            learning: false,
            sortOrder: null,
        });
        this.tags = [];
        this.newTag = "";
        this.learningProgresses = [];
        this.activeTab.set("basic");
        this.mode.set("form");
    }

    startEdit(item: SkillListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.skillsService.get(item.id).subscribe({
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

    addTag(): void {
        const value = this.newTag.trim();
        if (value && !this.tags.includes(value)) {
            this.tags = [...this.tags, value];
        }
        this.newTag = "";
    }

    removeTag(tag: string): void {
        this.tags = this.tags.filter((t) => t !== tag);
    }

    onTagKeydown(event: KeyboardEvent): void {
        if (event.key === "Enter" || event.key === ",") {
            event.preventDefault();
            this.addTag();
        }
    }

    addLearningProgress(): void {
        this.learningProgresses.push({
            id: null,
            name: "",
            status: "NOT_STARTED",
            startDate: "",
            completionDate: "",
            progressPercentage: 0,
            timeSpentHours: 0,
            estimatedCompletion: "",
            description: "",
        });
    }

    removeLearningProgress(index: number): void {
        this.learningProgresses.splice(index, 1);
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

        const request: SkillWriteRequest = {
            name: raw.name!,
            categoryName: raw.categoryName || null,
            proficiency: raw.proficiency!,
            level: raw.level,
            yearsOfExperience: raw.yearsOfExperience,
            description: raw.description || undefined,
            lastUsedDate: raw.lastUsedDate || null,
            hasCertification: raw.hasCertification ?? false,
            learning: raw.learning ?? false,
            sortOrder: raw.sortOrder,
            tags: this.tags.map((tagName) => ({ tagName })),
            learningProgresses: this.learningProgresses
                .filter((l) => l.name.trim())
                .map((l) => ({
                    id: l.id,
                    name: l.name.trim(),
                    status: l.status,
                    startDate: l.startDate || null,
                    completionDate: l.completionDate || null,
                    progressPercentage: l.progressPercentage,
                    timeSpentHours: l.timeSpentHours,
                    estimatedCompletion: l.estimatedCompletion || undefined,
                    description: l.description || undefined,
                })),
        };

        const editingId = this.editingId();
        const save$ = editingId == null ? this.skillsService.create(request) : this.skillsService.update(editingId, request);

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

    deleteSkill(id: number): void {
        this.deletingId.set(id);
        this.skillsService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.skills.update((list) => list.filter((s) => s.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.skillsService.list().subscribe({
            next: (skills) => {
                this.skills.set(skills);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadCategories(): void {
        this.skillsService.listCategories().subscribe({
            next: (categories) => this.categoryNames.set(categories.map((c) => c.name)),
            error: () => {},
        });
    }

    private populateForm(detail: SkillDetail): void {
        this.form.reset({
            name: detail.name,
            categoryName: detail.categoryName ?? "",
            proficiency: detail.proficiency,
            level: detail.level ?? null,
            yearsOfExperience: detail.yearsOfExperience ?? null,
            lastUsedDate: detail.lastUsedDate ?? "",
            description: detail.description ?? "",
            hasCertification: detail.hasCertification ?? false,
            learning: detail.learning ?? false,
            sortOrder: null,
        });

        this.tags = [...detail.tags];

        this.learningProgresses = detail.learningProgress.map((l: LearningProgress) => ({
            id: l.id ?? null,
            name: l.name,
            status: l.status,
            startDate: l.startDate ?? "",
            completionDate: l.completionDate ?? "",
            progressPercentage: l.progressPercentage ?? null,
            timeSpentHours: l.timeSpentHours ?? null,
            estimatedCompletion: l.estimatedCompletion ?? "",
            description: l.description ?? "",
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
