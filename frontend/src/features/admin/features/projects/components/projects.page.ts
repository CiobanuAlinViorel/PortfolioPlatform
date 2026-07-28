import { isPlatformBrowser } from "@angular/common";
import { ChangeDetectorRef, Component, NgZone, OnDestroy, OnInit, PLATFORM_ID, computed, inject, signal } from "@angular/core";
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import {
    LucideActivity,
    LucideAward,
    LucideCheck,
    LucideCircleAlert,
    LucideExternalLink,
    LucideFilter,
    LucideFolderKanban,
    LucideGitBranch,
    LucideImage,
    LucideListChecks,
    LucideLoaderCircle,
    LucidePencil,
    LucidePlus,
    LucideSearch,
    LucideSparkles,
    LucideStar,
    LucideTag,
    LucideTrash2,
    LucideTriangleAlert,
    LucideUpload,
    LucideVideo,
    LucideX,
} from "@lucide/angular";
import { ProjectsService } from "../services/projects.service";
import {
    ACHIEVEMENT_TYPES,
    AchievementType,
    COMPLEXITY_LEVELS,
    ComplexityLevel,
    DIFFICULTY_LEVELS,
    DifficultyLevel,
    PROFICIENCY_LEVELS,
    PROJECT_STATUSES,
    ProficiencyLevel,
    ProjectAchievement,
    ProjectChallenge,
    ProjectDetail,
    ProjectFeature,
    ProjectListItem,
    ProjectMedia,
    ProjectStatus,
    ProjectWriteRequest,
    RECOGNITION_LEVELS,
    RecognitionLevel,
    SkillInProject,
} from "../types";

type PageMode = "list" | "form";
type FormTab = "basic" | "tags" | "media" | "features" | "challenges" | "skills" | "achievements" | "metrics";

interface MediaSlot {
    id: number | null;
    title: string;
    description: string;
    altText: string;
    sortOrder: number;
    primary: boolean;
    imageUrl: string | null;
    file: File | null;
    previewUrl: string;
    isVideo: boolean;
}

interface FeatureRow {
    id: number | null;
    title: string;
    description: string;
    implementationDate: string | null;
    developmentTimeHours: number | null;
    sortOrder: number;
}

interface ChallengeRow {
    id: number | null;
    title: string;
    description: string;
    solution: string;
    difficulty: DifficultyLevel;
}

interface SkillRow {
    skillName: string;
    proficiency: ProficiencyLevel;
    level: number | null;
    yearsOfExperience: number | null;
    hasCertification: boolean;
    learning: boolean;
    usage: number | null;
    /** true when this row was loaded from an already-linked skill (proficiency/level/etc are read-only display) */
    linked: boolean;
    /** transient UI state: whether the suggestion dropdown is open for this row */
    showSuggestions: boolean;
}

interface AchievementRow {
    id: number | null;
    title: string;
    description: string;
    achievementType: AchievementType;
    achievementDate: string;
    recognitionLevel: RecognitionLevel;
    recognizedBy: string;
    proofUrl: string;
    isFeatured: boolean;
    impactSummary: string;
    demoUrl: string;
    metricBefore: string;
    metricAfter: string;
}

const STATUS_BADGE_CLASSES: Record<ProjectStatus, string> = {
    PLANNING: "bg-slate-500/15 text-slate-700 dark:text-slate-300",
    DEVELOPMENT: "bg-blue-500/15 text-blue-700 dark:text-blue-300",
    TESTING: "bg-amber-500/15 text-amber-700 dark:text-amber-300",
    PRODUCTION: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    MAINTENANCE: "bg-purple-500/15 text-purple-700 dark:text-purple-300",
    ARCHIVED: "bg-gray-500/15 text-gray-600 dark:text-gray-400",
};

@Component({
    selector: "app-admin-projects",
    imports: [
        ReactiveFormsModule,
        FormsModule,
        LucideActivity,
        LucideAward,
        LucideCheck,
        LucideCircleAlert,
        LucideExternalLink,
        LucideFilter,
        LucideFolderKanban,
        LucideGitBranch,
        LucideImage,
        LucideListChecks,
        LucideLoaderCircle,
        LucidePencil,
        LucidePlus,
        LucideSearch,
        LucideSparkles,
        LucideStar,
        LucideTag,
        LucideTrash2,
        LucideTriangleAlert,
        LucideUpload,
        LucideVideo,
        LucideX,
    ],
    templateUrl: "../ui/projects.page.html",
    styles: [":host { display: contents; }"],
})
export class ProjectsPage implements OnInit, OnDestroy {
    private readonly fb = inject(FormBuilder);
    private readonly projectsService = inject(ProjectsService);
    private readonly ngZone = inject(NgZone);
    private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
    private readonly cdr = inject(ChangeDetectorRef);

    private readonly boundDragOver = (e: Event) => this.onMediaDragOver(e as DragEvent);
    private readonly boundDragLeave = (e: Event) => this.onMediaDragLeave(e as DragEvent);
    private readonly boundDrop = (e: Event) => this.onMediaDrop(e as DragEvent);
    private readonly boundPaste = (e: Event) => this.onMediaPaste(e as ClipboardEvent);

    readonly statuses = PROJECT_STATUSES;
    readonly complexityLevels = COMPLEXITY_LEVELS;
    readonly difficultyLevels = DIFFICULTY_LEVELS;
    readonly proficiencyLevels = PROFICIENCY_LEVELS;
    readonly achievementTypes = ACHIEVEMENT_TYPES;
    readonly recognitionLevels = RECOGNITION_LEVELS;

    readonly mode = signal<PageMode>("list");
    readonly activeTab = signal<FormTab>("basic");
    readonly projects = signal<ProjectListItem[]>([]);
    readonly categoryNames = signal<string[]>([]);
    readonly skillNames = signal<string[]>([]);
    readonly editingId = signal<number | null>(null);

    readonly loadingList = signal(false);
    readonly loadingForm = signal(false);
    readonly saving = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly deleteConfirmId = signal<number | null>(null);
    readonly deletingId = signal<number | null>(null);
    readonly isDraggingMedia = signal(false);

    readonly searchQuery = signal("");
    readonly showFilterModal = signal(false);

    /** Applied filters (what's actually narrowing the list). */
    readonly filterCategories = signal<string[]>([]);
    readonly filterSkills = signal<string[]>([]);
    readonly filterYears = signal<number[]>([]);
    readonly filterStatuses = signal<ProjectStatus[]>([]);

    /** Draft copies edited inside the filter modal; only committed to the signals above on "Apply". */
    draftCategories: string[] = [];
    draftSkills: string[] = [];
    draftYears: number[] = [];
    draftStatuses: ProjectStatus[] = [];

    readonly activeFilterCount = computed(
        () => this.filterCategories().length + this.filterSkills().length + this.filterYears().length + this.filterStatuses().length,
    );

    readonly availableYears = computed(() =>
        Array.from(new Set(this.projects().map((p) => p.year).filter((y): y is number => y != null))).sort((a, b) => b - a),
    );

    readonly filteredProjects = computed(() => {
        const query = this.searchQuery().trim().toLowerCase();
        const categories = this.filterCategories();
        const skills = this.filterSkills();
        const years = this.filterYears();
        const statuses = this.filterStatuses();

        return this.projects().filter((p) => {
            if (query && !p.title.toLowerCase().includes(query) && !(p.description ?? "").toLowerCase().includes(query)) return false;
            if (categories.length > 0 && !(p.categoryName && categories.includes(p.categoryName))) return false;
            if (skills.length > 0 && !skills.some((s) => (p.skills ?? []).includes(s))) return false;
            if (years.length > 0 && !(p.year != null && years.includes(p.year))) return false;
            if (statuses.length > 0 && !statuses.includes(p.status)) return false;
            return true;
        });
    });

    tags: string[] = [];
    newTag = "";

    media: MediaSlot[] = [];
    features: FeatureRow[] = [];
    challenges: ChallengeRow[] = [];
    skills: SkillRow[] = [];
    achievements: AchievementRow[] = [];

    readonly form = this.fb.group({
        title: this.fb.control("", [Validators.required]),
        description: this.fb.control(""),
        longDescription: this.fb.control(""),
        status: this.fb.control<ProjectStatus>("PLANNING", [Validators.required]),
        complexity: this.fb.control<ComplexityLevel>("BEGINNER", [Validators.required]),
        year: this.fb.control<number | null>(null),
        completionDate: this.fb.control(""),
        developmentTime: this.fb.control<number | null>(null),
        sortOrder: this.fb.control<number | null>(null),
        githubUrl: this.fb.control(""),
        demoUrl: this.fb.control(""),
        categoryName: this.fb.control(""),
        metrics: this.fb.group({
            usersCount: this.fb.control<number | null>(null),
            performanceScore: this.fb.control(""),
            codeQualityScore: this.fb.control(""),
            linesOfCode: this.fb.control<number | null>(null),
            commitsCount: this.fb.control<number | null>(null),
            testCoveragePercentage: this.fb.control<number | null>(null),
        }),
    });

    ngOnInit(): void {
        this.loadList();
        this.loadCategories();
        this.loadSkillNames();

        // Registered manually (rather than via @HostListener) so drag/paste reliably reach us
        // regardless of zone.js coalescing on document-level clipboard/drag events.
        // Guarded to the browser only: `document` isn't available while this route is server-rendered.
        if (!this.isBrowser) return;
        this.ngZone.runOutsideAngular(() => {
            document.addEventListener("dragenter", this.boundDragOver);
            document.addEventListener("dragover", this.boundDragOver);
            document.addEventListener("dragleave", this.boundDragLeave);
            document.addEventListener("drop", this.boundDrop);
            document.addEventListener("paste", this.boundPaste);
        });
    }

    ngOnDestroy(): void {
        this.media.forEach((m) => {
            if (m.file) URL.revokeObjectURL(m.previewUrl);
        });

        if (!this.isBrowser) return;
        document.removeEventListener("dragenter", this.boundDragOver);
        document.removeEventListener("dragover", this.boundDragOver);
        document.removeEventListener("dragleave", this.boundDragLeave);
        document.removeEventListener("drop", this.boundDrop);
        document.removeEventListener("paste", this.boundPaste);
    }

    statusBadgeClass(status: ProjectStatus): string {
        return STATUS_BADGE_CLASSES[status];
    }

    openFilterModal(): void {
        this.draftCategories = [...this.filterCategories()];
        this.draftSkills = [...this.filterSkills()];
        this.draftYears = [...this.filterYears()];
        this.draftStatuses = [...this.filterStatuses()];
        this.showFilterModal.set(true);
        this.cdr.detectChanges();
    }

    closeFilterModal(): void {
        this.showFilterModal.set(false);
        this.cdr.detectChanges();
    }

    applyFilters(): void {
        this.filterCategories.set([...this.draftCategories]);
        this.filterSkills.set([...this.draftSkills]);
        this.filterYears.set([...this.draftYears]);
        this.filterStatuses.set([...this.draftStatuses]);
        this.showFilterModal.set(false);
        this.cdr.detectChanges();
    }

    clearDraftFilters(): void {
        this.draftCategories = [];
        this.draftSkills = [];
        this.draftYears = [];
        this.draftStatuses = [];
        this.cdr.detectChanges();
    }

    toggleDraftCategory(name: string): void {
        this.draftCategories = this.toggleInList(this.draftCategories, name);
        this.cdr.detectChanges();
    }

    toggleDraftSkill(name: string): void {
        this.draftSkills = this.toggleInList(this.draftSkills, name);
        this.cdr.detectChanges();
    }

    toggleDraftYear(year: number): void {
        this.draftYears = this.toggleInList(this.draftYears, year);
        this.cdr.detectChanges();
    }

    toggleDraftStatus(status: ProjectStatus): void {
        this.draftStatuses = this.toggleInList(this.draftStatuses, status);
        this.cdr.detectChanges();
    }

    private toggleInList<T>(list: T[], value: T): T[] {
        return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
    }

    startCreate(): void {
        this.editingId.set(null);
        this.errorMessage.set(null);
        this.form.reset({
            title: "",
            description: "",
            longDescription: "",
            status: "PLANNING",
            complexity: "BEGINNER",
            year: null,
            completionDate: "",
            developmentTime: null,
            sortOrder: null,
            githubUrl: "",
            demoUrl: "",
            categoryName: "",
            metrics: {
                usersCount: null,
                performanceScore: "",
                codeQualityScore: "",
                linesOfCode: null,
                commitsCount: null,
                testCoveragePercentage: null,
            },
        });
        this.tags = [];
        this.newTag = "";
        this.media = [];
        this.features = [];
        this.challenges = [];
        this.skills = [];
        this.achievements = [];
        this.activeTab.set("basic");
        this.mode.set("form");
    }

    startEdit(item: ProjectListItem): void {
        this.editingId.set(item.id);
        this.errorMessage.set(null);
        this.activeTab.set("basic");
        this.mode.set("form");
        this.loadingForm.set(true);

        this.projectsService.get(item.id).subscribe({
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

    onMediaFilesSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const files = input.files;
        if (!files) return;
        this.addMediaFiles(Array.from(files));
        input.value = "";
    }

    /** Dragging a file in from anywhere in the form jumps to the Media tab so the highlighted dropzone is visible. */
    private onMediaDragOver(event: DragEvent): void {
        if (this.mode() !== "form" || !event.dataTransfer?.types.includes("Files")) return;
        event.preventDefault();
        this.ngZone.run(() => {
            this.isDraggingMedia.set(true);
            this.activeTab.set("media");
            this.cdr.detectChanges();
        });
    }

    private onMediaDragLeave(event: DragEvent): void {
        // only clear the highlight once the drag actually leaves the window, not when it crosses a child element
        if (!event.relatedTarget) {
            this.ngZone.run(() => {
                this.isDraggingMedia.set(false);
                this.cdr.detectChanges();
            });
        }
    }

    private onMediaDrop(event: DragEvent): void {
        if (this.mode() !== "form") return;
        event.preventDefault();
        const files = event.dataTransfer?.files;
        this.ngZone.run(() => {
            this.isDraggingMedia.set(false);
            if (files && files.length > 0) {
                this.activeTab.set("media");
                this.addMediaFiles(Array.from(files).filter((f) => f.type.startsWith("image/") || f.type.startsWith("video/")));
            }
            this.cdr.detectChanges();
        });
    }

    /** Lets users paste a screenshot straight from the clipboard anywhere in the form; jumps to Media to show it landed. */
    private onMediaPaste(event: ClipboardEvent): void {
        if (this.mode() !== "form") return;
        const items = event.clipboardData?.items;
        if (!items) return;

        const files: File[] = [];
        for (const item of Array.from(items)) {
            if (item.kind === "file" && item.type.startsWith("image/")) {
                const file = item.getAsFile();
                if (file) files.push(file);
            }
        }
        if (files.length > 0) {
            event.preventDefault();
            this.ngZone.run(() => {
                this.activeTab.set("media");
                this.addMediaFiles(files);
                this.cdr.detectChanges();
            });
        }
    }

    private addMediaFiles(files: File[]): void {
        files.forEach((file) => {
            this.media.push({
                id: null,
                title: "",
                description: "",
                altText: "",
                sortOrder: this.media.length,
                primary: this.media.length === 0,
                imageUrl: null,
                file,
                previewUrl: URL.createObjectURL(file),
                isVideo: file.type.startsWith("video/"),
            });
        });
    }

    removeMedia(index: number): void {
        const slot = this.media[index];
        if (slot.file) URL.revokeObjectURL(slot.previewUrl);
        const wasPrimary = slot.primary;
        this.media.splice(index, 1);
        if (wasPrimary && this.media.length > 0) {
            this.media[0].primary = true;
        }
    }

    setPrimaryMedia(index: number): void {
        this.media.forEach((m, i) => (m.primary = i === index));
    }

    addFeature(): void {
        this.features.push({
            id: null,
            title: "",
            description: "",
            implementationDate: null,
            developmentTimeHours: null,
            sortOrder: this.features.length,
        });
    }

    removeFeature(index: number): void {
        this.features.splice(index, 1);
    }

    addChallenge(): void {
        this.challenges.push({
            id: null,
            title: "",
            description: "",
            solution: "",
            difficulty: "MEDIUM",
        });
    }

    removeChallenge(index: number): void {
        this.challenges.splice(index, 1);
    }

    addSkill(): void {
        this.skills.push({
            skillName: "",
            proficiency: "BEGINNER",
            level: 1,
            yearsOfExperience: 0,
            hasCertification: false,
            learning: false,
            usage: null,
            linked: false,
            showSuggestions: false,
        });
    }

    removeSkill(index: number): void {
        this.skills.splice(index, 1);
    }

    /** A skill row counts as "linked to an existing skill" once its name matches a known global skill. */
    onSkillNameChange(row: SkillRow): void {
        row.linked = this.skillNames().some((name) => name.toLowerCase() === row.skillName.trim().toLowerCase());
    }

    /** Up to 6 existing skills whose name contains the row's current input, for the suggestion dropdown. */
    filteredSkillSuggestions(row: SkillRow): string[] {
        const query = row.skillName.trim().toLowerCase();
        const names = this.skillNames();
        const matches = query ? names.filter((name) => name.toLowerCase().includes(query)) : names;
        return matches.slice(0, 6);
    }

    selectSkillSuggestion(row: SkillRow, name: string): void {
        row.skillName = name;
        row.linked = true;
        row.showSuggestions = false;
    }

    addAchievement(): void {
        this.achievements.push({
            id: null,
            title: "",
            description: "",
            achievementType: "MILESTONE",
            achievementDate: "",
            recognitionLevel: "LOCAL",
            recognizedBy: "",
            proofUrl: "",
            isFeatured: false,
            impactSummary: "",
            demoUrl: "",
            metricBefore: "",
            metricAfter: "",
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

        const request: ProjectWriteRequest = {
            title: raw.title!,
            description: raw.description || undefined,
            longDescription: raw.longDescription || undefined,
            status: raw.status!,
            complexity: raw.complexity!,
            year: raw.year,
            completionDate: raw.completionDate || null,
            developmentTime: raw.developmentTime,
            sortOrder: raw.sortOrder,
            githubUrl: raw.githubUrl || undefined,
            demoUrl: raw.demoUrl || undefined,
            categoryName: raw.categoryName || null,
            tags: this.tags,
            features: this.features.map((f) => ({
                id: f.id,
                title: f.title,
                description: f.description || undefined,
                implementationDate: f.implementationDate || null,
                developmentTimeHours: f.developmentTimeHours,
                sortOrder: f.sortOrder,
            })),
            challenges: this.challenges.map((c) => ({
                id: c.id,
                title: c.title,
                description: c.description,
                solution: c.solution || undefined,
                difficulty: c.difficulty,
            })),
            media: this.media.map((m) => ({
                id: m.id,
                title: m.title || undefined,
                description: m.description || undefined,
                altText: m.altText || undefined,
                sortOrder: m.sortOrder,
                primary: m.primary,
                imageUrl: m.file ? null : m.imageUrl,
            })),
            metrics: {
                usersCount: raw.metrics?.usersCount,
                performanceScore: raw.metrics?.performanceScore || undefined,
                codeQualityScore: raw.metrics?.codeQualityScore || undefined,
                linesOfCode: raw.metrics?.linesOfCode,
                commitsCount: raw.metrics?.commitsCount,
                testCoveragePercentage: raw.metrics?.testCoveragePercentage,
            },
            skills: this.skills
                .filter((s) => s.skillName.trim())
                .map((s) => ({
                    skillName: s.skillName.trim(),
                    proficiency: s.proficiency,
                    level: s.level,
                    yearsOfExperience: s.yearsOfExperience,
                    hasCertification: s.hasCertification,
                    learning: s.learning,
                    usage: s.usage,
                })),
            achievements: this.achievements.map((a) => ({
                id: a.id,
                title: a.title,
                description: a.description,
                achievementType: a.achievementType,
                achievementDate: a.achievementDate,
                recognitionLevel: a.recognitionLevel,
                recognizedBy: a.recognizedBy || undefined,
                proofUrl: a.proofUrl || undefined,
                isFeatured: a.isFeatured,
                impactSummary: a.impactSummary || undefined,
                demoUrl: a.demoUrl || undefined,
                metricBefore: a.metricBefore || undefined,
                metricAfter: a.metricAfter || undefined,
            })),
        };

        const files = this.media.filter((m) => m.file).map((m) => m.file!);
        const editingId = this.editingId();
        const save$ = editingId == null ? this.projectsService.create(request, files) : this.projectsService.update(editingId, request, files);

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

    deleteProject(id: number): void {
        this.deletingId.set(id);
        this.projectsService.delete(id).subscribe({
            next: () => {
                this.deletingId.set(null);
                this.deleteConfirmId.set(null);
                this.projects.update((list) => list.filter((p) => p.id !== id));
            },
            error: (err) => {
                this.deletingId.set(null);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadList(): void {
        this.loadingList.set(true);
        this.projectsService.list().subscribe({
            next: (projects) => {
                this.projects.set(projects);
                this.loadingList.set(false);
            },
            error: (err) => {
                this.loadingList.set(false);
                this.errorMessage.set(this.extractErrorMessage(err));
            },
        });
    }

    private loadCategories(): void {
        this.projectsService.listCategories().subscribe({
            next: (categories) => this.categoryNames.set(categories.map((c) => c.name)),
            error: () => {},
        });
    }

    private loadSkillNames(): void {
        this.projectsService.listSkills().subscribe({
            next: (skills) => this.skillNames.set(skills.map((s) => s.name)),
            error: () => {},
        });
    }

    private populateForm(detail: ProjectDetail): void {
        this.form.reset({
            title: detail.title,
            description: detail.description ?? "",
            longDescription: detail.longDescription ?? "",
            status: detail.status,
            complexity: detail.complexity,
            year: detail.year ?? null,
            completionDate: detail.completionDate ?? "",
            developmentTime: detail.developmentTime ?? null,
            sortOrder: null,
            githubUrl: detail.githubUrl ?? "",
            demoUrl: detail.demoUrl ?? "",
            categoryName: detail.categoryName ?? "",
            metrics: {
                usersCount: detail.metrics?.usersCount ?? null,
                performanceScore: detail.metrics?.performanceScore ?? "",
                codeQualityScore: detail.metrics?.codeQualityScore ?? "",
                linesOfCode: detail.metrics?.linesOfCode ?? null,
                commitsCount: detail.metrics?.commitsCount ?? null,
                testCoveragePercentage: detail.metrics?.testCoveragePercentage ?? null,
            },
        });

        this.tags = [...detail.tags];

        this.media = detail.media.map((m: ProjectMedia) => ({
            id: m.id ?? null,
            title: m.title ?? "",
            description: m.description ?? "",
            altText: m.altText ?? "",
            sortOrder: m.sortOrder ?? 0,
            primary: m.primary,
            imageUrl: m.imageUrl ?? null,
            file: null,
            previewUrl: m.imageUrl ?? "",
            isVideo: this.isVideoUrl(m.imageUrl ?? ""),
        }));

        this.features = detail.features.map((f: ProjectFeature) => ({
            id: f.id ?? null,
            title: f.title,
            description: f.description ?? "",
            implementationDate: f.implementationDate ?? null,
            developmentTimeHours: f.developmentTimeHours ?? null,
            sortOrder: f.sortOrder ?? 0,
        }));

        this.challenges = detail.challenges.map((c: ProjectChallenge) => ({
            id: c.id ?? null,
            title: c.title,
            description: c.description,
            solution: c.solution ?? "",
            difficulty: c.difficulty,
        }));

        this.skills = detail.skills.map((s: SkillInProject) => ({
            skillName: s.name,
            proficiency: s.proficiency,
            level: null,
            yearsOfExperience: null,
            hasCertification: false,
            learning: false,
            usage: s.usage ?? null,
            linked: true,
            showSuggestions: false,
        }));

        this.achievements = detail.achievements.map((a: ProjectAchievement) => ({
            id: a.id ?? null,
            title: a.title,
            description: a.description,
            achievementType: a.achievementType,
            achievementDate: a.achievementDate,
            recognitionLevel: a.recognitionLevel,
            recognizedBy: a.recognizedBy ?? "",
            proofUrl: a.proofUrl ?? "",
            isFeatured: a.isFeatured ?? false,
            impactSummary: a.impactSummary ?? "",
            demoUrl: a.demoUrl ?? "",
            metricBefore: a.metricBefore ?? "",
            metricAfter: a.metricAfter ?? "",
        }));
    }

    private isVideoUrl(url: string): boolean {
        if (!url) return false;
        if (url.includes("/video/upload/")) return true;
        return /\.(mp4|mov|avi|webm|mkv|ogg)$/i.test(url);
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
