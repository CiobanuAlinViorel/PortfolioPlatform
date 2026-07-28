import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import {
    LucideBriefcase, LucideCalendar, LucideExternalLink, LucideListFilter, LucideSearch, LucideX,
} from "@lucide/angular";
import { JobExperienceListItem } from "../../../admin/features/job-experiences/types";
import { JobExperiencesPublicService } from "../services/job-experiences.service";

const ALL = "All";

@Component({
    selector: "app-jobs-list",
    imports: [RouterLink, DatePipe, LucideBriefcase, LucideCalendar, LucideExternalLink, LucideListFilter, LucideSearch, LucideX],
    templateUrl: "../ui/jobs-list.page.html",
})
export class JobsListPage implements OnInit {
    private readonly jobsService = inject(JobExperiencesPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly jobs = signal<JobExperienceListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeStatus = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly statuses = computed(() => [ALL, "Ongoing", "Completed"]);
    readonly activeFilterCount = computed(() => (this.activeStatus() !== ALL ? 1 : 0));

    readonly visibleJobs = computed(() => {
        const status = this.activeStatus();
        const term = this.searchTerm().trim().toLowerCase();
        return this.jobs().filter((job) => {
            const jobStatus = job.endDate ? "Completed" : "Ongoing";
            if (status !== ALL && jobStatus !== status) return false;
            if (term) {
                const haystack = [job.companyName, job.role].join(" ").toLowerCase();
                if (!haystack.includes(term)) return false;
            }
            return true;
        });
    });

    ngOnInit(): void {
        this.jobsService.list().subscribe({
            next: (jobs) => { this.jobs.set(jobs); this.loading.set(false); },
            error: () => { this.errorMessage.set("Failed to load job experience."); this.loading.set(false); },
        });
    }

    statusOf(job: JobExperienceListItem): string {
        return job.endDate ? "Completed" : "Ongoing";
    }

    setStatus(status: string): void { this.activeStatus.set(status); }
    openFilters(): void { this.filtersOpen.set(true); }
    closeFilters(): void { this.filtersOpen.set(false); }
    clearFilters(): void { this.activeStatus.set(ALL); }
    clearSearch(): void { this.searchTerm.set(""); }
    onSearchInput(event: Event): void { this.searchTerm.set((event.target as HTMLInputElement).value); }
}
