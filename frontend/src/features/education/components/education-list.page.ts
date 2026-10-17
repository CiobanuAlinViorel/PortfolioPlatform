import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideGraduationCap, LucideMapPin } from "@lucide/angular";
import { EducationListItem } from "../../admin/features/education/types";
import { EducationPublicService } from "../services/education.service";
import { PageHeaderComponent } from "../../../shared/components/page-header.component";
import { TimelineComponent, TimelineItemComponent } from "../../../shared/components/timeline.component";
import { SkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../shared/components/empty-state.component";
import { SearchInputComponent } from "../../../shared/components/search-input.component";
import { FilterBarComponent, FilterGroup, FilterSelection } from "../../../shared/components/filter-bar.component";
import { BadgeComponent } from "../../../shared/components/badge.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../shared/pipes/enum-label.pipe";
import { duration } from "../../../shared/utils/duration";
import { statusVariant } from "../../../shared/utils/status-variant";
import { countBy, syncQueryParams } from "../../../shared/utils/query-state";

@Component({
    selector: "app-education-list",
    imports: [
        DatePipe,
        LucideGraduationCap,
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
    templateUrl: "../ui/education-list.page.html",
})
export class EducationListPage implements OnInit {
    private readonly educationService = inject(EducationPublicService);
    private readonly enumLabel = new EnumLabelPipe();

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly items = signal<EducationListItem[]>([]);

    readonly searchTerm = signal("");
    readonly filters = signal<FilterSelection>({});
    readonly emptyIcon = LucideGraduationCap;
    readonly statusVariant = statusVariant;

    readonly showSearch = computed(() => this.items().length >= 6);

    readonly filterGroups = computed((): FilterGroup[] => [
        { id: "level", label: "Level", options: countBy(this.items(), (i) => i.level).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
        { id: "status", label: "Status", options: countBy(this.items(), (i) => i.status).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
    ]);

    readonly visibleItems = computed(() => {
        const f = this.filters();
        const term = this.searchTerm().trim().toLowerCase();
        return this.items()
            .filter((i) => {
                if (f["level"] && i.level !== f["level"]) return false;
                if (f["status"] && i.status !== f["status"]) return false;
                if (term && ![i.institution, i.degree ?? "", i.fieldOfStudy, i.location ?? "", i.description ?? ""].join(" ").toLowerCase().includes(term)) return false;
                return true;
            })
            .sort((a, b) => b.startDate.localeCompare(a.startDate));
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || Object.keys(this.filters()).length > 0);

    constructor() {
        syncQueryParams({ q: this.searchTerm }, this.filters, ["level", "status"]);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.educationService.list().subscribe({
            next: (items) => { this.items.set(items); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    durationOf(item: EducationListItem): string {
        return duration(item.startDate, item.endDate);
    }

    title(item: EducationListItem): string {
        return item.degree || item.fieldOfStudy || this.enumLabel.transform(item.level);
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.filters.set({});
    }
}
