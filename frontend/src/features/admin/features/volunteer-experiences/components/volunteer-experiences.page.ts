import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideCheck,
    LucideCircleAlert,
    LucideExternalLink,
    LucideFolderKanban,
    LucideHandHeart,
    LucideListChecks,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSparkles,
    LucideTrash2,
    LucideX,
} from "@lucide/angular";
import { VolunteerExperiencesService } from "../services/volunteer-experiences.service";
import {
    ImpactLevel,
    ProjectInVolunteer,
    VolunteerExperienceDetail,
    VolunteerExperienceListItem,
    VolunteerExperienceWriteRequest,
    VolunteerProject,
    VolunteerResponsibility,
    VolunteerStatus,
    VolunteerType,
} from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "responsibilities" | "projects";

const VOLUNTEER_TYPES: VolunteerType[] = ["ASSOCIATION", "CLUB", "COMMUNITY", "NGO", "CHARITY"];
const VOLUNTEER_STATUSES: VolunteerStatus[] = ["ONGOING", "COMPLETED"];
const IMPACT_LEVELS: ImpactLevel[] = ["LOW", "MEDIUM", "HIGH"];

@Component({
    selector: "app-admin-volunteer-experiences",
    imports: [
        ReactiveFormsModule,
        LucideCheck,
        LucideCircleAlert,
        LucideExternalLink,
        LucideFolderKanban,
        LucideHandHeart,
        LucideListChecks,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSparkles,
        LucideTrash2,
        LucideX,
    ],
    templateUrl: "../ui/volunteer-experiences.page.html",
    styles: [":host { display: contents; }"],
})
export class VolunteerExperiencesPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly volunteerExperiencesService = inject(VolunteerExperiencesService);

    readonly volunteerTypes = VOLUNTEER_TYPES;
    readonly volunteerStatuses = VOLUNTEER_STATUSES;
    readonly impactLevels = IMPACT_LEVELS;

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly volunteerExperiences = signal<VolunteerExperienceListItem[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);

    readonly availableProjects = signal<ProjectInVolunteer[]>([]);
    readonly loadingAvailableProjects = signal(false);

    /** Responsibilities and linked projects are buffered here and only sent to the backend on Save. */
    responsibilities: VolunteerResponsibility[] = [];
    linkedProjects: VolunteerProject[] = [];

    readonly form = this.fb.group({
        organization: this.fb.control("", [Validators.required]),
        role: this.fb.control("", [Validators.required]),
        type: this.fb.control<VolunteerType>("NGO", [Validators.required]),
        location: this.fb.control(""),
        startDate: this.fb.control("", [Validators.required]),
        endDate: this.fb.control(""),
        status: this.fb.control<VolunteerStatus>("ONGOING", [Validators.required]),
        description: this.fb.control(""),
        impactDescription: this.fb.control(""),
        website: this.fb.control(""),
        hoursPerWeek: this.fb.control<number | null>(null),
        totalHours: this.fb.control<number | null>(null),
    });

    ngOnInit(): void {
        this.loadList();
    }

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            organization: "",
            role: "",
            type: "NGO",
            location: "",
            startDate: "",
            endDate: "",
            status: "ONGOING",
            description: "",
            impactDescription: "",
            website: "",
            hoursPerWeek: null,
            totalHours: null,
        });
        this.responsibilities = [];
        this.linkedProjects = [];
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadAvailableProjects();
    }

    startEdit(item: VolunteerExperienceListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.volunteerExperiencesService.get(item.id).subscribe({
            next: (detail) => {
                this.populateForm(detail);
                this.loadingForm.set(false);
                this.loadAvailableProjects();
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

    addResponsibility(): void {
        this.responsibilities = [
            ...this.responsibilities,
            { description: "", impactLevel: "MEDIUM", sortOrder: this.responsibilities.length },
        ];
    }

    removeResponsibility(index: number): void {
        this.responsibilities = this.responsibilities.filter((_, i) => i !== index);
    }

    updateResponsibilityDescription(index: number, value: string): void {
        this.responsibilities = this.responsibilities.map((r, i) => (i === index ? { ...r, description: value } : r));
    }

    updateResponsibilityImpact(index: number, value: ImpactLevel): void {
        this.responsibilities = this.responsibilities.map((r, i) => (i === index ? { ...r, impactLevel: value } : r));
    }

    /** Projects already linked here are excluded from the picker. */
    selectableProjects(): ProjectInVolunteer[] {
        const linkedIds = new Set(this.linkedProjects.map((p) => p.project.id));
        return this.availableProjects().filter((p) => !linkedIds.has(p.id));
    }

    /** Selecting a project in the picker links it locally only — nothing is sent to the backend until Save. */
    onProjectPicked(event: Event): void {
        const select = event.target as HTMLSelectElement;
        const projectId = select.value ? Number(select.value) : null;
        select.value = "";
        if (projectId == null) return;

        const project = this.availableProjects().find((p) => p.id === projectId);
        if (project) this.linkedProjects = [...this.linkedProjects, { contributionPercentage: 100, project }];
    }

    updateContribution(projectId: number, value: number): void {
        this.linkedProjects = this.linkedProjects.map((p) =>
            p.project.id === projectId ? { ...p, contributionPercentage: value } : p,
        );
    }

    removeProjectLink(projectId: number): void {
        const removed = this.linkedProjects.find((p) => p.project.id === projectId);
        this.linkedProjects = this.linkedProjects.filter((p) => p.project.id !== projectId);
        if (removed && !this.availableProjects().some((p) => p.id === projectId)) {
            this.availableProjects.update((list) => [...list, removed.project]);
        }
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.errorMessage.set("Please fix the highlighted fields.");
            return;
        }

        if (this.responsibilities.some((r) => !r.description.trim())) {
            this.errorMessage.set("Please fill in or remove empty responsibilities.");
            this.activeTab.set("responsibilities");
            return;
        }

        this.errorMessage.set(null);
        this.saving.set(true);
        const raw = this.form.getRawValue();

        const request: VolunteerExperienceWriteRequest = {
            organization: raw.organization!,
            role: raw.role!,
            type: raw.type!,
            location: raw.location || undefined,
            startDate: raw.startDate!,
            endDate: raw.endDate || null,
            status: raw.status!,
            description: raw.description || undefined,
            impactDescription: raw.impactDescription || undefined,
            website: raw.website || undefined,
            hoursPerWeek: raw.hoursPerWeek,
            totalHours: raw.totalHours,
            responsibilities: this.responsibilities.map((r, i) => ({
                id: r.id ?? null,
                description: r.description,
                impactLevel: r.impactLevel,
                sortOrder: i,
            })),
            projects: this.linkedProjects.map((p) => ({
                id: p.id ?? null,
                projectId: p.project.id,
                contributionPercentage: p.contributionPercentage,
            })),
        };

        const editingId = this.editingId();
        const save$ =
            editingId == null
                ? this.volunteerExperiencesService.create(request)
                : this.volunteerExperiencesService.update(editingId, request);

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

    deleteVolunteerExperience(id: number): void {
        this.deletingId.set(id);
        this.volunteerExperiencesService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.volunteerExperiences.update((list) => list.filter((v) => v.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.volunteerExperiencesService.list().subscribe({
            next: (volunteerExperiences) => {
                this.volunteerExperiences.set(volunteerExperiences);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadAvailableProjects(): void {
        this.loadingAvailableProjects.set(true);
        this.volunteerExperiencesService.listAvailableProjects().subscribe({
            next: (projects) => {
                this.availableProjects.set(projects);
                this.loadingAvailableProjects.set(false);
            },
            error: () => {
                this.loadingAvailableProjects.set(false);
            },
        });
    }

    private populateForm(detail: VolunteerExperienceDetail): void {
        this.form.reset({
            organization: detail.organization,
            role: detail.role,
            type: detail.type,
            location: detail.location ?? "",
            startDate: detail.startDate,
            endDate: detail.endDate ?? "",
            status: detail.status,
            description: detail.description ?? "",
            impactDescription: detail.impactDescription ?? "",
            website: detail.website ?? "",
            hoursPerWeek: detail.hoursPerWeek ?? null,
            totalHours: detail.totalHours ?? null,
        });
        this.responsibilities = [...detail.responsibilities];
        this.linkedProjects = [...detail.projects];
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
