import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { LucideArrowRight, LucideBriefcase, LucideFolderKanban } from "@lucide/angular";
import { JobExperienceListItem } from "../../../admin/features/job-experiences/types";
import { JobExperiencesPublicService } from "../services/job-experiences.service";
import { PageHeaderComponent } from "../../../../shared/components/page-header.component";
import { TimelineComponent, TimelineItemComponent } from "../../../../shared/components/timeline.component";
import { SkeletonComponent } from "../../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../../shared/components/empty-state.component";
import { SearchInputComponent } from "../../../../shared/components/search-input.component";
import { ChipComponent } from "../../../../shared/components/chip.component";
import { BadgeComponent } from "../../../../shared/components/badge.component";
import { RevealDirective } from "../../../../shared/directives/reveal.directive";
import { duration } from "../../../../shared/utils/duration";
import { syncQueryParams } from "../../../../shared/utils/query-state";

type StatusFilter = "" | "ongoing" | "completed";

@Component({
    selector: "app-jobs-list",
    imports: [
        RouterLink,
        DatePipe,
        LucideArrowRight,
        LucideBriefcase,
        LucideFolderKanban,
        PageHeaderComponent,
        TimelineComponent,
        TimelineItemComponent,
        SkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        SearchInputComponent,
        ChipComponent,
        BadgeComponent,
        RevealDirective,
    ],
    templateUrl: "../ui/jobs-list.page.html",
})
export class JobsListPage implements OnInit {
    private readonly jobsService = inject(JobExperiencesPublicService);

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly jobs = signal<JobExperienceListItem[]>([]);

    readonly searchTerm = signal("");
    readonly status = signal<string>("");
    readonly emptyIcon = LucideBriefcase;

    readonly statusOptions: Array<{ id: StatusFilter; label: string }> = [
        { id: "", label: "All" },
        { id: "ongoing", label: "Ongoing" },
        { id: "completed", label: "Completed" },
    ];

    readonly showSearch = computed(() => this.jobs().length >= 6);
    readonly hasOngoing = computed(() => this.jobs().some((j) => !j.endDate));
    readonly hasCompleted = computed(() => this.jobs().some((j) => !!j.endDate));

    readonly visibleJobs = computed(() => {
        const status = this.status();
        const term = this.searchTerm().trim().toLowerCase();
        return this.jobs()
            .filter((job) => {
                if (status === "ongoing" && job.endDate) return false;
                if (status === "completed" && !job.endDate) return false;
                if (term && ![job.companyName, job.role].join(" ").toLowerCase().includes(term)) return false;
                return true;
            })
            .sort((a, b) => Number(!!a.endDate) - Number(!!b.endDate) || b.startDate.localeCompare(a.startDate));
    });

    readonly totalYears = computed(() => {
        const months = this.jobs().reduce((sum, j) => sum + monthsBetween(j.startDate, j.endDate), 0);
        return Math.round((months / 12) * 10) / 10;
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || !!this.status());

    constructor() {
        syncQueryParams({ q: this.searchTerm, status: this.status });
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.jobsService.list().subscribe({
            next: (jobs) => { this.jobs.set(jobs); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    durationOf(job: JobExperienceListItem): string {
        return duration(job.startDate, job.endDate);
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.status.set("");
    }
}

function monthsBetween(start: string, end?: string | null): number {
    const s = new Date(start);
    const e = end ? new Date(end) : new Date();
    return Math.max(0, (e.getFullYear() - s.getFullYear()) * 12 + (e.getMonth() - s.getMonth()));
}
