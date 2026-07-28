import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideBookOpen,
    LucideCalendar,
    LucideCheck,
    LucideCircleAlert,
    LucideFilter,
    LucideGraduationCap,
    LucideLink,
    LucideLoaderCircle,
    LucideMapPin,
    LucidePencil,
    LucidePlus,
    LucideSearch,
    LucideTrash2,
    LucideTrophy,
    LucideX,
} from "@lucide/angular";
import { EducationService } from "../services/education.service";
import {
    ACHIEVEMENT_TYPES,
    AchievementType,
    Course,
    EDUCATION_LEVELS,
    EDUCATION_STATUSES,
    EducationDetail,
    EducationLevel,
    EducationListItem,
    EducationStatus,
    EducationWriteRequest,
    ProjectOption,
    RECOGNITION_LEVELS,
    RecognitionLevel,
} from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "courses" | "achievements";

interface CourseProjectRow {
    id: number | null;
    projectId: number | null;
    grade: string;
    contributionPercentage: number | null;
}

interface CourseRow {
    id: number | null;
    title: string;
    description: string;
    grade: string;
    credits: number | null;
    semester: string;
    year: number | null;
    relevant: boolean;
    projects: CourseProjectRow[];
}

interface AchievementRow {
    id: number | null;
    achievementTitle: string;
    achievementDescription: string;
    achievementType: AchievementType;
    achievementDate: string;
    recognitionLevel: RecognitionLevel;
    recognizedBy: string;
    proofUrl: string;
    isFeatured: boolean;
    sortOrder: number | null;
    courseId: number | null;
    topic: string;
    grade: number | null;
    maxGrade: number | null;
    credits: number | null;
    teacherOrSupervisor: string;
    institutionName: string;
    semester: number | null;
    academicYear: number | null;
}

const LEVEL_BADGE_CLASSES: Record<EducationLevel, string> = {
    HIGH_SCHOOL: "bg-slate-500/15 text-slate-700 dark:text-slate-300",
    ASSOCIATE: "bg-blue-500/15 text-blue-700 dark:text-blue-300",
    BACHELOR: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    MASTER: "bg-purple-500/15 text-purple-700 dark:text-purple-300",
    PHD: "bg-rose-500/15 text-rose-700 dark:text-rose-300",
    CERTIFICATE: "bg-amber-500/15 text-amber-700 dark:text-amber-300",
    BOOTCAMP: "bg-cyan-500/15 text-cyan-700 dark:text-cyan-300",
};

const STATUS_BADGE_CLASSES: Record<EducationStatus, string> = {
    COMPLETED: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    ONGOING: "bg-blue-500/15 text-blue-700 dark:text-blue-300",
    DROPPED: "bg-red-500/15 text-red-700 dark:text-red-300",
};

@Component({
    selector: "app-admin-education",
    imports: [
        ReactiveFormsModule,
        FormsModule,
        LucideBookOpen,
        LucideCalendar,
        LucideCheck,
        LucideCircleAlert,
        LucideFilter,
        LucideGraduationCap,
        LucideLink,
        LucideLoaderCircle,
        LucideMapPin,
        LucidePencil,
        LucidePlus,
        LucideSearch,
        LucideTrash2,
        LucideTrophy,
        LucideX,
    ],
    templateUrl: "../ui/education.page.html",
    styles: [":host { display: contents; }"],
})
export class EducationPage implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly educationService = inject(EducationService);

    readonly educationLevels = EDUCATION_LEVELS;
    readonly educationStatuses = EDUCATION_STATUSES;
    readonly achievementTypes = ACHIEVEMENT_TYPES;
    readonly recognitionLevels = RECOGNITION_LEVELS;

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly educations = signal<EducationListItem[]>([]);
    readonly projectOptions = signal<ProjectOption[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);

    readonly searchQuery = signal("");
    readonly showFilterModal = signal(false);

    readonly filterLevels = signal<EducationLevel[]>([]);
    readonly filterStatuses = signal<EducationStatus[]>([]);

    draftLevels: EducationLevel[] = [];
    draftStatuses: EducationStatus[] = [];

    readonly activeFilterCount = computed(() => this.filterLevels().length + this.filterStatuses().length);

    readonly filteredEducations = computed(() => {
        const query = this.searchQuery().trim().toLowerCase();
        const levels = this.filterLevels();
        const statuses = this.filterStatuses();

        return this.educations().filter((e) => {
            if (
                query &&
                !e.institution.toLowerCase().includes(query) &&
                !e.fieldOfStudy.toLowerCase().includes(query) &&
                !(e.degree ?? "").toLowerCase().includes(query) &&
                !(e.description ?? "").toLowerCase().includes(query)
            )
                return false;
            if (levels.length > 0 && !levels.includes(e.level)) return false;
            if (statuses.length > 0 && !statuses.includes(e.status)) return false;
            return true;
        });
    });

    readonly form = this.fb.group({
        level: this.fb.control<EducationLevel>("BACHELOR", [Validators.required]),
        institution: this.fb.control("", [Validators.required]),
        degree: this.fb.control(""),
        fieldOfStudy: this.fb.control("", [Validators.required]),
        location: this.fb.control(""),
        startDate: this.fb.control("", [Validators.required]),
        endDate: this.fb.control(""),
        status: this.fb.control<EducationStatus>("ONGOING", [Validators.required]),
        gpa: this.fb.control(""),
        description: this.fb.control(""),
    });

    courses: CourseRow[] = [];
    achievements: AchievementRow[] = [];

    readonly savedCourses = computed(() => this.courses.filter((c) => c.id != null));

    levelBadgeClass(level: EducationLevel): string {
        return LEVEL_BADGE_CLASSES[level];
    }

    statusBadgeClass(status: EducationStatus): string {
        return STATUS_BADGE_CLASSES[status];
    }

    projectTitle(projectId: number | null): string {
        if (projectId == null) return "";
        return this.projectOptions().find((p) => p.id === projectId)?.title ?? "";
    }

    ngOnInit(): void {
        this.loadList();
        this.loadProjectOptions();
    }

    // ── List / search / filters ───────────────────────────────────────────────

    openFilterModal(): void {
        this.draftLevels = [...this.filterLevels()];
        this.draftStatuses = [...this.filterStatuses()];
        this.showFilterModal.set(true);
    }

    closeFilterModal(): void {
        this.showFilterModal.set(false);
    }

    applyFilters(): void {
        this.filterLevels.set([...this.draftLevels]);
        this.filterStatuses.set([...this.draftStatuses]);
        this.showFilterModal.set(false);
    }

    clearDraftFilters(): void {
        this.draftLevels = [];
        this.draftStatuses = [];
    }

    toggleDraftLevel(level: EducationLevel): void {
        this.draftLevels = this.toggleInList(this.draftLevels, level);
    }

    toggleDraftStatus(status: EducationStatus): void {
        this.draftStatuses = this.toggleInList(this.draftStatuses, status);
    }

    private toggleInList<T>(list: T[], value: T): T[] {
        return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
    }

    // ── Create / edit form ────────────────────────────────────────────────────

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            level: "BACHELOR",
            institution: "",
            degree: "",
            fieldOfStudy: "",
            location: "",
            startDate: "",
            endDate: "",
            status: "ONGOING",
            gpa: "",
            description: "",
        });
        this.courses = [];
        this.achievements = [];
        this.activeTab.set("basic");
        this.mode.set("form");
    }

    startEdit(item: EducationListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.educationService.get(item.id).subscribe({
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

    // ── Courses ───────────────────────────────────────────────────────────────

    addCourse(): void {
        this.courses.push({
            id: null,
            title: "",
            description: "",
            grade: "",
            credits: null,
            semester: "",
            year: null,
            relevant: false,
            projects: [],
        });
    }

    removeCourse(index: number): void {
        const removed = this.courses[index];
        this.courses.splice(index, 1);
        if (removed.id != null) {
            this.achievements = this.achievements.map((a) => (a.courseId === removed.id ? { ...a, courseId: null } : a));
        }
    }

    addCourseProject(courseIndex: number): void {
        this.courses[courseIndex].projects.push({ id: null, projectId: null, grade: "", contributionPercentage: 100 });
    }

    removeCourseProject(courseIndex: number, projectIndex: number): void {
        this.courses[courseIndex].projects.splice(projectIndex, 1);
    }

    // ── Achievements ─────────────────────────────────────────────────────────

    addAchievement(): void {
        this.achievements.push({
            id: null,
            achievementTitle: "",
            achievementDescription: "",
            achievementType: "COMPLETION",
            achievementDate: "",
            recognitionLevel: "LOCAL",
            recognizedBy: "",
            proofUrl: "",
            isFeatured: false,
            sortOrder: null,
            courseId: null,
            topic: "",
            grade: null,
            maxGrade: null,
            credits: null,
            teacherOrSupervisor: "",
            institutionName: "",
            semester: null,
            academicYear: null,
        });
    }

    removeAchievement(index: number): void {
        this.achievements.splice(index, 1);
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

        const request: EducationWriteRequest = {
            level: raw.level!,
            institution: raw.institution!,
            degree: raw.degree || null,
            fieldOfStudy: raw.fieldOfStudy!,
            location: raw.location || null,
            startDate: raw.startDate!,
            endDate: raw.endDate || null,
            status: raw.status!,
            gpa: raw.gpa || null,
            description: raw.description || null,
            courses: this.courses
                .filter((c) => c.title.trim())
                .map((c) => ({
                    id: c.id,
                    title: c.title.trim(),
                    description: c.description || null,
                    grade: c.grade || null,
                    credits: c.credits,
                    semester: c.semester || null,
                    year: c.year,
                    relevant: c.relevant,
                    projects: c.projects
                        .filter((p) => p.projectId != null)
                        .map((p) => ({
                            id: p.id,
                            projectId: p.projectId!,
                            grade: p.grade || null,
                            contributionPercentage: p.contributionPercentage,
                        })),
                })),
            achievements: this.achievements
                .filter((a) => a.achievementTitle.trim())
                .map((a) => ({
                    id: a.id,
                    achievementTitle: a.achievementTitle.trim(),
                    achievementDescription: a.achievementDescription,
                    achievementType: a.achievementType,
                    achievementDate: a.achievementDate,
                    recognitionLevel: a.recognitionLevel,
                    recognizedBy: a.recognizedBy || null,
                    proofUrl: a.proofUrl || null,
                    isFeatured: a.isFeatured,
                    sortOrder: a.sortOrder,
                    courseId: a.courseId,
                    topic: a.topic,
                    grade: a.grade ?? undefined,
                    maxGrade: a.maxGrade ?? undefined,
                    credits: a.credits ?? undefined,
                    teacherOrSupervisor: a.teacherOrSupervisor,
                    institutionName: a.institutionName,
                    semester: a.semester ?? undefined,
                    academicYear: a.academicYear ?? undefined,
                })),
        };

        const editingId = this.editingId();
        const save$ = editingId == null ? this.educationService.create(request) : this.educationService.update(editingId, request);

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

    deleteEducation(id: number): void {
        this.deletingId.set(id);
        this.educationService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.educations.update((list) => list.filter((e) => e.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.educationService.list().subscribe({
            next: (educations) => {
                this.educations.set(educations);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadProjectOptions(): void {
        this.educationService.listProjects().subscribe({
            next: (projects) => this.projectOptions.set(projects),
            error: () => {},
        });
    }

    private populateForm(detail: EducationDetail): void {
        this.form.reset({
            level: detail.level,
            institution: detail.institution,
            degree: detail.degree ?? "",
            fieldOfStudy: detail.fieldOfStudy,
            location: detail.location ?? "",
            startDate: detail.startDate,
            endDate: detail.endDate ?? "",
            status: detail.status,
            gpa: detail.gpa ?? "",
            description: detail.description ?? "",
        });

        this.courses = detail.courses.map((c: Course) => ({
            id: c.id,
            title: c.title,
            description: c.description ?? "",
            grade: c.grade ?? "",
            credits: c.credits ?? null,
            semester: c.semester ?? "",
            year: c.year ?? null,
            relevant: c.relevant ?? false,
            projects: c.projects.map((p) => ({
                id: p.id,
                projectId: p.project.id,
                grade: p.grade ?? "",
                contributionPercentage: p.contributionPercentage ?? 100,
            })),
        }));

        this.achievements = detail.achievements.map((a) => ({
            id: a.id,
            achievementTitle: a.achievementTitle,
            achievementDescription: "",
            achievementType: "COMPLETION",
            achievementDate: "",
            recognitionLevel: "LOCAL",
            recognizedBy: "",
            proofUrl: "",
            isFeatured: false,
            sortOrder: null,
            courseId: a.courseId ?? null,
            topic: a.topic,
            grade: a.grade,
            maxGrade: a.maxGrade,
            credits: a.credits,
            teacherOrSupervisor: a.teacherOrSupervisor,
            institutionName: a.institutionName,
            semester: a.semester,
            academicYear: a.academicYear,
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
