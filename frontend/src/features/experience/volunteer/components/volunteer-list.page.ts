import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { LucideArrowRight, LucideFolderKanban, LucideHeartHandshake, LucideMapPin } from "@lucide/angular";
import { VolunteerExperienceListItem } from "../../../admin/features/volunteer-experiences/types";
import { VolunteerExperiencesPublicService } from "../services/volunteer-experiences.service";
import { PageHeaderComponent } from "../../../../shared/components/page-header.component";
import { TimelineComponent, TimelineItemComponent } from "../../../../shared/components/timeline.component";
import { SkeletonComponent } from "../../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../../shared/components/empty-state.component";
import { SearchInputComponent } from "../../../../shared/components/search-input.component";
import { FilterBarComponent, FilterGroup, FilterSelection } from "../../../../shared/components/filter-bar.component";
import { BadgeComponent } from "../../../../shared/components/badge.component";
import { RevealDirective } from "../../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../../shared/pipes/enum-label.pipe";
import { duration } from "../../../../shared/utils/duration";
import { statusVariant } from "../../../../shared/utils/status-variant";
import { countBy, syncQueryParams } from "../../../../shared/utils/query-state";

@Component({
    selector: "app-volunteer-list",
    imports: [
        RouterLink,
        DatePipe,
        LucideArrowRight,
        LucideFolderKanban,
        LucideHeartHandshake,
        LucideMapPin,
        PageHeaderComponent,
        TimelineComponent,
        TimelineItemComponent,
        SkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        SearchInputComponent,
        FilterBarComponent,
        BadgeComponent,
        RevealDirective,
        EnumLabelPipe,
    ],
    templateUrl: "../ui/volunteer-list.page.html",
})
export class VolunteerListPage implements OnInit {
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);
    private readonly enumLabel = new EnumLabelPipe();

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly items = signal<VolunteerExperienceListItem[]>([]);

    readonly searchTerm = signal("");
    readonly filters = signal<FilterSelection>({});
    readonly emptyIcon = LucideHeartHandshake;
    readonly statusVariant = statusVariant;

    readonly showSearch = computed(() => this.items().length >= 6);

    readonly filterGroups = computed((): FilterGroup[] => [
        { id: "type", label: "Type", options: countBy(this.items(), (i) => i.type).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
        { id: "status", label: "Status", options: countBy(this.items(), (i) => i.status).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
    ]);

    readonly visibleItems = computed(() => {
        const f = this.filters();
        const term = this.searchTerm().trim().toLowerCase();
        return this.items()
            .filter((item) => {
                if (f["type"] && item.type !== f["type"]) return false;
                if (f["status"] && item.status !== f["status"]) return false;
                if (term && ![item.organization, item.role, item.location ?? ""].join(" ").toLowerCase().includes(term)) return false;
                return true;
            })
            .sort((a, b) => Number(a.status !== "ONGOING") - Number(b.status !== "ONGOING") || b.startDate.localeCompare(a.startDate));
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || Object.keys(this.filters()).length > 0);

    constructor() {
        syncQueryParams({ q: this.searchTerm }, this.filters, ["type", "status"]);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.volunteerService.list().subscribe({
            next: (items) => { this.items.set(items); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    durationOf(item: VolunteerExperienceListItem): string {
        return duration(item.startDate, item.endDate);
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.filters.set({});
    }
}
