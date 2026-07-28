import { DatePipe } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { LucideArrowLeft, LucideBriefcase, LucideCalendar, LucideCode, LucideExternalLink } from "@lucide/angular";
import { JobExperienceDetail } from "../../../admin/features/job-experiences/types";
import { JobExperiencesPublicService } from "../services/job-experiences.service";
import { SeoService } from "../../../../shared/services/seo.service";

@Component({
    selector: "app-job-detail",
    imports: [RouterLink, DatePipe, LucideArrowLeft, LucideBriefcase, LucideCalendar, LucideCode, LucideExternalLink],
    templateUrl: "../ui/job-detail.page.html",
})
export class JobDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly jobsService = inject(JobExperiencesPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly job = signal<JobExperienceDetail | null>(null);

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
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
            error: () => { this.errorMessage.set("This job experience could not be found."); this.loading.set(false); },
        });
    }
}
