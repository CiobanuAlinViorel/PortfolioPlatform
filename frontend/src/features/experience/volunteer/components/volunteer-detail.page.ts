import { DatePipe } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
    LucideArrowLeft, LucideCalendar, LucideClock, LucideCode, LucideExternalLink, LucideGlobe,
    LucideHeartHandshake, LucideMapPin,
} from "@lucide/angular";
import { VolunteerExperienceDetail } from "../../../admin/features/volunteer-experiences/types";
import { VolunteerExperiencesPublicService } from "../services/volunteer-experiences.service";
import { SeoService } from "../../../../shared/services/seo.service";

@Component({
    selector: "app-volunteer-detail",
    imports: [
        RouterLink, DatePipe, LucideArrowLeft, LucideCalendar, LucideClock, LucideCode, LucideExternalLink,
        LucideGlobe, LucideHeartHandshake, LucideMapPin,
    ],
    templateUrl: "../ui/volunteer-detail.page.html",
})
export class VolunteerDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly item = signal<VolunteerExperienceDetail | null>(null);

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
        this.volunteerService.get(id).subscribe({
            next: (item) => {
                this.item.set(item);
                this.loading.set(false);

                this.seoService.update(
                    {
                        title: `${item.role} at ${item.organization}`,
                        description: this.seoService.truncate(
                            item.description ?? item.impactDescription ?? `Details of Alin-Viorel Ciobanu's volunteer role as ${item.role} at ${item.organization}.`,
                        ),
                    },
                    this.router.url,
                );
            },
            error: () => { this.errorMessage.set("This volunteer experience could not be found."); this.loading.set(false); },
        });
    }
}
