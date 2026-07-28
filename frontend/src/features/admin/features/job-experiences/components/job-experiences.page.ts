import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideBriefcase,
    LucideCheck,
    LucideCircleAlert,
    LucideExternalLink,
    LucideFolderKanban,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSparkles,
    LucideTrash2,
    LucideX,
} from "@lucide/angular";
import { Observable, forkJoin, of, switchMap } from "rxjs";
import { JobExperiencesService } from "../services/job-experiences.service";
import { JobExperienceDetail, JobExperienceListItem, JobExperienceWriteRequest, ProjectInJob } from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "projects";

@Component({
    selector: "app-admin-job-experiences",
    imports: [
        ReactiveFormsModule,
        LucideBriefcase,
        LucideCheck,
        LucideCircleAlert,
        LucideExternalLink,
        LucideFolderKanban,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSparkles,
        LucideTrash2,
        LucideX,
    ],
    templateUrl: "../ui/job-experiences.page.html",
    styles: [":host { display: contents; }"],
})
export class JobExperiencesPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly jobExperiencesService = inject(JobExperiencesService);

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly jobExperiences = signal<JobExperienceListItem[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);

    readonly availableProjects = signal<ProjectInJob[]>([]);
    readonly loadingAvailableProjects = signal(false);

    /** All project link/unlink changes are buffered here and only sent to the backend on Save. */
    linkedProjects: ProjectInJob[] = [];
    private originalProjectIds = new Set<number>();

    readonly form = this.fb.group({
        companyName: this.fb.control("", [Validators.required]),
        role: this.fb.control("", [Validators.required]),
        startDate: this.fb.control("", [Validators.required]),
        endDate: this.fb.control(""),
    });

    ngOnInit(): void {
        this.loadList();
    }

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({ companyName: "", role: "", startDate: "", endDate: "" });
        this.linkedProjects = [];
        this.originalProjectIds = new Set();
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadAvailableProjects();
    }

    startEdit(item: JobExperienceListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.jobExperiencesService.get(item.id).subscribe({
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

    /** Projects already linked here, or unavailable for linking elsewhere, are excluded from the picker. */
    selectableProjects(): ProjectInJob[] {
        const linkedIds = new Set(this.linkedProjects.map((p) => p.id));
        return this.availableProjects().filter((p) => !linkedIds.has(p.id));
    }

    /** Selecting a project in the picker links it locally only — nothing is sent to the backend until Save. */
    onProjectPicked(event: Event): void {
        const select = event.target as HTMLSelectElement;
        const projectId = select.value ? Number(select.value) : null;
        select.value = "";
        if (projectId == null) return;

        const project = this.availableProjects().find((p) => p.id === projectId);
        if (project) this.linkedProjects = [...this.linkedProjects, project];
    }

    /** Unlinking is also local-only until Save; the project is put back in the picker in case it was one of the originally-linked ones. */
    removeProjectLink(projectId: number): void {
        const removed = this.linkedProjects.find((p) => p.id === projectId);
        this.linkedProjects = this.linkedProjects.filter((p) => p.id !== projectId);
        if (removed && !this.availableProjects().some((p) => p.id === projectId)) {
            this.availableProjects.update((list) => [...list, removed]);
        }
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

        const editingId = this.editingId();

        const request: JobExperienceWriteRequest = {
            companyName: raw.companyName!,
            role: raw.role!,
            startDate: raw.startDate!,
            endDate: raw.endDate || null,
            // On create there are no existing links yet, so the full selection can be sent as new links.
            // On edit, project links are diffed and synced separately via syncProjectLinks (see below),
            // since the backend can't accept a resend of already-linked projects without their link id,
            // which the detail response doesn't expose.
            projects: editingId == null ? this.linkedProjects.map((p) => ({ projectId: p.id })) : undefined,
        };

        const save$ =
            editingId == null
                ? this.jobExperiencesService.create(request)
                : this.jobExperiencesService.update(editingId, request).pipe(switchMap(() => this.syncProjectLinks(editingId)));

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

    deleteJobExperience(id: number): void {
        this.deletingId.set(id);
        this.jobExperiencesService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.jobExperiences.update((list) => list.filter((j) => j.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.jobExperiencesService.list().subscribe({
            next: (jobExperiences) => {
                this.jobExperiences.set(jobExperiences);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    /** Diffs the buffered linkedProjects against the snapshot taken when the form was opened, and fires the add/remove calls needed to match it. */
    private syncProjectLinks(jobId: number): Observable<unknown> {
        const currentIds = new Set(this.linkedProjects.map((p) => p.id));
        const toAdd = this.linkedProjects.filter((p) => !this.originalProjectIds.has(p.id)).map((p) => p.id);
        const toRemove = [...this.originalProjectIds].filter((id) => !currentIds.has(id));

        const calls = [
            ...toAdd.map((id) => this.jobExperiencesService.addProject(jobId, id)),
            ...toRemove.map((id) => this.jobExperiencesService.removeProject(jobId, id)),
        ];

        return calls.length > 0 ? forkJoin(calls) : of(null);
    }

    private loadAvailableProjects(): void {
        this.loadingAvailableProjects.set(true);
        this.jobExperiencesService.listAvailableProjects().subscribe({
            next: (projects) => {
                this.availableProjects.set(projects);
                this.loadingAvailableProjects.set(false);
            },
            error: () => {
                this.loadingAvailableProjects.set(false);
            },
        });
    }

    private populateForm(detail: JobExperienceDetail): void {
        this.form.reset({
            companyName: detail.companyName,
            role: detail.role,
            startDate: detail.startDate,
            endDate: detail.endDate ?? "",
        });
        this.linkedProjects = [...detail.projects];
        this.originalProjectIds = new Set(detail.projects.map((p) => p.id));
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
