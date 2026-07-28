import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
    LucideArrowLeft,
    LucideCalendar,
    LucideCode,
    LucideExternalLink,
    LucideLayers,
    LucideTag,
} from "@lucide/angular";
import { ProjectDetail, ProjectMedia } from "../../admin/features/projects/types";
import { ProjectsPublicService } from "../services/projects.service";
import { SeoService } from "../../../shared/services/seo.service";

export type ProjectTabId = "overview" | "features" | "challenges" | "tech" | "metrics" | "achievements" | "tags";

interface ProjectTab {
    id: ProjectTabId;
    label: string;
}

@Component({
    selector: "app-project-detail",
    imports: [RouterLink, LucideArrowLeft, LucideCalendar, LucideCode, LucideExternalLink, LucideLayers, LucideTag],
    templateUrl: "../ui/project-detail.page.html",
})
export class ProjectDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly projectsService = inject(ProjectsPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly project = signal<ProjectDetail | null>(null);
    readonly selectedImageIndex = signal(0);
    readonly activeTab = signal<ProjectTabId>("overview");

    readonly orderedMedia = computed<ProjectMedia[]>(() => {
        const media = this.project()?.media ?? [];
        return [...media].sort((a, b) => (a.primary === b.primary ? 0 : a.primary ? -1 : 1));
    });

    readonly mainImage = computed<ProjectMedia | null>(() => {
        const media = this.orderedMedia();
        return media[this.selectedImageIndex()] ?? media[0] ?? null;
    });

    readonly tabs = computed<ProjectTab[]>(() => {
        const p = this.project();
        if (!p) return [];
        const tabs: ProjectTab[] = [];
        if (p.description || p.longDescription) tabs.push({ id: "overview", label: "Overview" });
        if (p.features.length > 0) tabs.push({ id: "features", label: "Features" });
        if (p.challenges.length > 0) tabs.push({ id: "challenges", label: "Challenges" });
        if (p.skills.length > 0) tabs.push({ id: "tech", label: "Tech Stack" });
        if (p.metrics) tabs.push({ id: "metrics", label: "Metrics" });
        if (p.achievements.length > 0) tabs.push({ id: "achievements", label: "Achievements" });
        if (p.tags.length > 0) tabs.push({ id: "tags", label: "Tags" });
        return tabs;
    });

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
        this.projectsService.get(id).subscribe({
            next: (project) => {
                this.project.set(project);
                const firstTab = this.tabs()[0];
                if (firstTab) this.activeTab.set(firstTab.id);
                this.loading.set(false);

                const primaryImage = this.orderedMedia()[0]?.imageUrl ?? undefined;
                this.seoService.update(
                    {
                        title: project.title,
                        description: this.seoService.truncate(project.description ?? project.longDescription ?? `${project.title} — a software project by Alin-Viorel Ciobanu.`),
                        image: primaryImage,
                    },
                    this.router.url,
                );
            },
            error: () => {
                this.errorMessage.set("This project could not be found.");
                this.loading.set(false);
            },
        });
    }

    selectImage(index: number): void {
        this.selectedImageIndex.set(index);
    }

    selectTab(tab: ProjectTabId): void {
        this.activeTab.set(tab);
    }
}
