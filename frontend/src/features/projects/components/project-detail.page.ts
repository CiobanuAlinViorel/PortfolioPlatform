import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
    LucideArrowLeft,
    LucideArrowRight,
    LucideCalendar,
    LucideChevronLeft,
    LucideChevronRight,
    LucideClock,
    LucideExternalLink,
    LucideImage,
    LucideLayers,
    LucideLightbulb,
    LucideStar,
    LucideTag,
} from "@lucide/angular";
import { ProjectDetail, ProjectListItem, ProjectMedia } from "../../admin/features/projects/types";
import { ProjectsPublicService } from "../services/projects.service";
import { SeoService } from "../../../shared/services/seo.service";
import { BreadcrumbComponent } from "../../../shared/components/breadcrumb.component";
import { BadgeComponent } from "../../../shared/components/badge.component";
import { ButtonComponent } from "../../../shared/components/button.component";
import { StatComponent } from "../../../shared/components/stat.component";
import { SkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { DialogComponent } from "../../../shared/components/dialog.component";
import { BrandIconComponent } from "../../../shared/components/brand-icon.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../shared/pipes/enum-label.pipe";
import { CompactNumberPipe } from "../../../shared/pipes/compact-number.pipe";
import { statusVariant } from "../../../shared/utils/status-variant";
import { syncQueryParams } from "../../../shared/utils/query-state";

export type ProjectTabId = "overview" | "features" | "challenges" | "tech" | "metrics" | "achievements";

interface ProjectTab {
    id: ProjectTabId;
    label: string;
    count?: number;
}

@Component({
    selector: "app-project-detail",
    imports: [
        DatePipe,
        RouterLink,
        LucideArrowLeft,
        LucideArrowRight,
        LucideCalendar,
        LucideChevronLeft,
        LucideChevronRight,
        LucideClock,
        LucideExternalLink,
        LucideImage,
        LucideLayers,
        LucideLightbulb,
        LucideStar,
        LucideTag,
        BreadcrumbComponent,
        BadgeComponent,
        ButtonComponent,
        StatComponent,
        SkeletonComponent,
        ErrorStateComponent,
        DialogComponent,
        BrandIconComponent,
        RevealDirective,
        EnumLabelPipe,
        CompactNumberPipe,
    ],
    templateUrl: "../ui/project-detail.page.html",
})
export class ProjectDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly projectsService = inject(ProjectsPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly notFound = signal(false);
    readonly project = signal<ProjectDetail | null>(null);
    readonly allProjects = signal<ProjectListItem[]>([]);
    readonly selectedImageIndex = signal(0);
    readonly tab = signal<string>("");
    readonly lightboxOpen = signal(false);
    readonly statusVariant = statusVariant;

    readonly orderedMedia = computed<ProjectMedia[]>(() => {
        const media = (this.project()?.media ?? []).filter((m) => !!m.imageUrl);
        return [...media].sort((a, b) => Number(b.primary) - Number(a.primary) || (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
    });

    readonly mainImage = computed<ProjectMedia | null>(() => this.orderedMedia()[this.selectedImageIndex()] ?? this.orderedMedia()[0] ?? null);

    readonly hasMetrics = computed(() => {
        const m = this.project()?.metrics;
        return !!m && Object.values(m).some((v) => v !== null && v !== undefined && v !== "");
    });

    readonly tabs = computed<ProjectTab[]>(() => {
        const p = this.project();
        if (!p) return [];
        const tabs: ProjectTab[] = [];
        if (p.longDescription || p.description) tabs.push({ id: "overview", label: "Overview" });
        if (p.features.length) tabs.push({ id: "features", label: "Features", count: p.features.length });
        if (p.challenges.length) tabs.push({ id: "challenges", label: "Challenges", count: p.challenges.length });
        if (p.skills.length) tabs.push({ id: "tech", label: "Tech stack", count: p.skills.length });
        if (this.hasMetrics()) tabs.push({ id: "metrics", label: "Metrics" });
        if (p.achievements.length) tabs.push({ id: "achievements", label: "Achievements", count: p.achievements.length });
        return tabs;
    });

    readonly activeTab = computed<ProjectTabId | null>(() => {
        const tabs = this.tabs();
        const requested = this.tab() as ProjectTabId;
        return tabs.find((t) => t.id === requested)?.id ?? tabs[0]?.id ?? null;
    });

    readonly sortedFeatures = computed(() => [...(this.project()?.features ?? [])].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)));

    readonly neighbours = computed(() => {
        const current = this.project();
        const list = [...this.allProjects()].sort((a, b) => (a.sortOrder ?? 9999) - (b.sortOrder ?? 9999) || (b.year ?? 0) - (a.year ?? 0) || a.title.localeCompare(b.title));
        const index = current ? list.findIndex((p) => p.id === current.id) : -1;
        return { prev: index > 0 ? list[index - 1] : null, next: index >= 0 && index < list.length - 1 ? list[index + 1] : null };
    });

    readonly breadcrumb = computed(() => [{ label: "Home", link: "/" }, { label: "Projects", link: "/projects" }, { label: this.project()?.title ?? "…" }]);

    constructor() {
        syncQueryParams({ tab: this.tab });
    }

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
        if (Number.isNaN(id)) {
            this.router.navigate(["/404"], { skipLocationChange: true });
            return;
        }
        this.load(id);
        this.projectsService.list().subscribe({ next: (list) => this.allProjects.set(list) });
    }

    load(id: number = Number(this.route.snapshot.paramMap.get("id"))): void {
        this.loading.set(true);
        this.notFound.set(false);
        this.projectsService.get(id).subscribe({
            next: (project) => {
                this.project.set(project);
                this.loading.set(false);
                this.seoService.update(
                    {
                        title: project.title,
                        description: this.seoService.truncate(project.description ?? project.longDescription ?? `${project.title} — a software project by Alin-Viorel Ciobanu.`),
                        image: this.orderedMedia()[0]?.imageUrl ?? undefined,
                    },
                    this.router.url,
                );
            },
            error: () => {
                this.notFound.set(true);
                this.loading.set(false);
            },
        });
    }

    selectTab(id: ProjectTabId): void {
        this.tab.set(id === this.tabs()[0]?.id ? "" : id);
    }

    onTabKeydown(event: KeyboardEvent): void {
        const tabs = this.tabs();
        const index = tabs.findIndex((t) => t.id === this.activeTab());
        if (event.key === "ArrowRight") {
            event.preventDefault();
            this.selectTab(tabs[(index + 1) % tabs.length].id);
            this.focusTab((index + 1) % tabs.length);
        } else if (event.key === "ArrowLeft") {
            event.preventDefault();
            const next = (index - 1 + tabs.length) % tabs.length;
            this.selectTab(tabs[next].id);
            this.focusTab(next);
        }
    }

    private focusTab(index: number): void {
        document.getElementById(`tab-${this.tabs()[index].id}`)?.focus();
    }

    selectImage(index: number): void {
        this.selectedImageIndex.set(index);
    }

    stepImage(delta: number): void {
        const n = this.orderedMedia().length;
        if (!n) return;
        this.selectedImageIndex.set((this.selectedImageIndex() + delta + n) % n);
    }

    onGalleryKeydown(event: KeyboardEvent): void {
        if (event.key === "ArrowRight") { event.preventDefault(); this.stepImage(1); }
        if (event.key === "ArrowLeft") { event.preventDefault(); this.stepImage(-1); }
    }
}
