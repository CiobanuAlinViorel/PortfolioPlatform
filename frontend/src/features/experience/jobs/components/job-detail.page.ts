import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { LucideArrowLeft, LucideArrowRight, LucideBriefcase, LucideCalendar, LucideFolderKanban } from "@lucide/angular";
import { JobExperienceDetail, JobExperienceListItem } from "../../../admin/features/job-experiences/types";
import { JobExperiencesPublicService } from "../services/job-experiences.service";
import { SeoService } from "../../../../shared/services/seo.service";
import { BreadcrumbComponent } from "../../../../shared/components/breadcrumb.component";
import { BadgeComponent } from "../../../../shared/components/badge.component";
import { ButtonComponent } from "../../../../shared/components/button.component";
import { SkeletonComponent } from "../../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../../shared/components/empty-state.component";
import { ProjectCardComponent } from "../../../../shared/components/project-card.component";
import { RevealDirective } from "../../../../shared/directives/reveal.directive";
import { duration } from "../../../../shared/utils/duration";

@Component({
    selector: "app-job-detail",
    imports: [
        DatePipe,
        RouterLink,
        LucideArrowLeft,
        LucideArrowRight,
        LucideBriefcase,
        LucideCalendar,
        LucideFolderKanban,
        BreadcrumbComponent,
        BadgeComponent,
        ButtonComponent,
        SkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        ProjectCardComponent,
        RevealDirective,
    ],
    templateUrl: "../ui/job-detail.page.html",
})
export class JobDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly jobsService = inject(JobExperiencesPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly notFound = signal(false);
    readonly job = signal<JobExperienceDetail | null>(null);
    readonly allJobs = signal<JobExperienceListItem[]>([]);
    readonly emptyIcon = LucideFolderKanban;

    readonly durationLabel = computed(() => {
        const j = this.job();
        return j ? duration(j.startDate, j.endDate) : "";
    });

    readonly breadcrumb = computed(() => [
        { label: "Home", link: "/" },
        { label: "Work experience", link: "/experience/jobs" },
        { label: this.job()?.companyName ?? "…" },
    ]);

    readonly neighbours = computed(() => {
        const current = this.job();
        const list = [...this.allJobs()].sort((a, b) => Number(!!a.endDate) - Number(!!b.endDate) || b.startDate.localeCompare(a.startDate));
        const index = current ? list.findIndex((j) => j.id === current.id) : -1;
        return { prev: index > 0 ? list[index - 1] : null, next: index >= 0 && index < list.length - 1 ? list[index + 1] : null };
    });

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
        if (Number.isNaN(id)) {
            this.router.navigate(["/404"], { skipLocationChange: true });
            return;
        }
        this.load(id);
        this.jobsService.list().subscribe({ next: (list) => this.allJobs.set(list) });
    }

    load(id: number = Number(this.route.snapshot.paramMap.get("id"))): void {
        this.loading.set(true);
        this.notFound.set(false);
        this.jobsService.get(id).subscribe({
            next: (job) => {
                this.job.set(job);
                this.loading.set(false);
                this.seoService.update(
                    {
                        title: `${job.role} at ${job.companyName}`,
                        description: `Details of Alin-Viorel Ciobanu's role as ${job.role} at ${job.companyName}, including responsibilities and projects delivered.`,
                    },
                    this.router.url,
                );
            },
            error: () => { this.notFound.set(true); this.loading.set(false); },
        });
    }
}
