import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
    LucideArrowLeft,
    LucideArrowRight,
    LucideCalendar,
    LucideExternalLink,
    LucideFolderKanban,
    LucideGlobe,
    LucideHeartHandshake,
    LucideMapPin,
    LucideSparkles,
} from "@lucide/angular";
import { VolunteerExperienceDetail, VolunteerExperienceListItem } from "../../../admin/features/volunteer-experiences/types";
import { VolunteerExperiencesPublicService } from "../services/volunteer-experiences.service";
import { SeoService } from "../../../../shared/services/seo.service";
import { BreadcrumbComponent } from "../../../../shared/components/breadcrumb.component";
import { BadgeComponent } from "../../../../shared/components/badge.component";
import { ButtonComponent } from "../../../../shared/components/button.component";
import { StatComponent } from "../../../../shared/components/stat.component";
import { SkeletonComponent } from "../../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../../shared/components/error-state.component";
import { ProjectCardComponent } from "../../../../shared/components/project-card.component";
import { RevealDirective } from "../../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../../shared/pipes/enum-label.pipe";
import { duration } from "../../../../shared/utils/duration";
import { statusVariant } from "../../../../shared/utils/status-variant";

@Component({
    selector: "app-volunteer-detail",
    imports: [
        DatePipe,
        RouterLink,
        LucideArrowLeft,
        LucideArrowRight,
        LucideCalendar,
        LucideExternalLink,
        LucideFolderKanban,
        LucideGlobe,
        LucideHeartHandshake,
        LucideMapPin,
        LucideSparkles,
        BreadcrumbComponent,
        BadgeComponent,
        ButtonComponent,
        StatComponent,
        SkeletonComponent,
        ErrorStateComponent,
        ProjectCardComponent,
        RevealDirective,
        EnumLabelPipe,
    ],
    templateUrl: "../ui/volunteer-detail.page.html",
})
export class VolunteerDetailPage implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);
    private readonly seoService = inject(SeoService);

    readonly loading = signal(true);
    readonly notFound = signal(false);
    readonly item = signal<VolunteerExperienceDetail | null>(null);
    readonly allItems = signal<VolunteerExperienceListItem[]>([]);
    readonly statusVariant = statusVariant;

    readonly durationLabel = computed(() => {
        const v = this.item();
        return v ? duration(v.startDate, v.endDate) : "";
    });

    readonly responsibilities = computed(() => [...(this.item()?.responsibilities ?? [])].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)));

    readonly breadcrumb = computed(() => [
        { label: "Home", link: "/" },
        { label: "Volunteer experience", link: "/experience/volunteer" },
        { label: this.item()?.organization ?? "…" },
    ]);

    readonly neighbours = computed(() => {
        const current = this.item();
        const list = [...this.allItems()].sort((a, b) => Number(a.status !== "ONGOING") - Number(b.status !== "ONGOING") || b.startDate.localeCompare(a.startDate));
        const index = current ? list.findIndex((v) => v.id === current.id) : -1;
        return { prev: index > 0 ? list[index - 1] : null, next: index >= 0 && index < list.length - 1 ? list[index + 1] : null };
    });

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get("id"));
        if (Number.isNaN(id)) {
            this.router.navigate(["/404"], { skipLocationChange: true });
            return;
        }
        this.load(id);
        this.volunteerService.list().subscribe({ next: (list) => this.allItems.set(list) });
    }

    load(id: number = Number(this.route.snapshot.paramMap.get("id"))): void {
        this.loading.set(true);
        this.notFound.set(false);
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
            error: () => { this.notFound.set(true); this.loading.set(false); },
        });
    }
}
