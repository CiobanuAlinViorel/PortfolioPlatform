import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideFolderKanban } from "@lucide/angular";
import { ProjectListItem } from "../../admin/features/projects/types";
import { ProjectsPublicService } from "../services/projects.service";
import { PageHeaderComponent } from "../../../shared/components/page-header.component";
import { ProjectCardComponent } from "../../../shared/components/project-card.component";
import { FilterBarComponent, FilterGroup, FilterSelection } from "../../../shared/components/filter-bar.component";
import { SearchInputComponent } from "../../../shared/components/search-input.component";
import { CardSkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../shared/components/empty-state.component";
import { ButtonComponent } from "../../../shared/components/button.component";
import { ChipComponent } from "../../../shared/components/chip.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../shared/pipes/enum-label.pipe";
import { countBy, syncQueryParams } from "../../../shared/utils/query-state";

const SORTS = [
    { id: "", label: "Featured" },
    { id: "newest", label: "Newest" },
    { id: "oldest", label: "Oldest" },
    { id: "az", label: "A – Z" },
];

@Component({
    selector: "app-projects-list",
    imports: [
        PageHeaderComponent,
        ProjectCardComponent,
        FilterBarComponent,
        SearchInputComponent,
        CardSkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        ButtonComponent,
        ChipComponent,
        RevealDirective,
    ],
    templateUrl: "../ui/projects-list.page.html",
})
export class ProjectsListPage implements OnInit {
    private readonly projectsService = inject(ProjectsPublicService);
    private readonly enumLabel = new EnumLabelPipe();

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly projects = signal<ProjectListItem[]>([]);

    readonly searchTerm = signal("");
    readonly filters = signal<FilterSelection>({});
    readonly sort = signal("");
    readonly sortOptions = SORTS;
    readonly emptyIcon = LucideFolderKanban;

    readonly categories = computed(() => countBy(this.projects(), (p) => p.categoryName));
    readonly showCategoryTabs = computed(() => this.categories().length >= 2 && this.categories().length <= 6);

    readonly filterGroups = computed((): FilterGroup[] => {
        const all = this.projects();
        const groups: FilterGroup[] = [
            { id: "status", label: "Status", options: countBy(all, (p) => p.status).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
            { id: "complexity", label: "Complexity", options: countBy(all, (p) => p.complexity).map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })) },
        ];
        if (!this.showCategoryTabs()) {
            groups.unshift({ id: "category", label: "Category", options: this.categories().map((o) => ({ ...o, label: o.id })) });
        }
        return groups;
    });
    readonly showSearch = computed(() => this.projects().length >= 6);

    readonly visibleProjects = computed(() => {
        const f = this.filters();
        const term = this.searchTerm().trim().toLowerCase();
        const list = this.projects().filter((p) => {
            if (f["category"] && p.categoryName !== f["category"]) return false;
            if (f["status"] && p.status !== f["status"]) return false;
            if (f["complexity"] && p.complexity !== f["complexity"]) return false;
            if (term) {
                const haystack = [p.title, p.description ?? "", ...p.tags, ...p.skills].join(" ").toLowerCase();
                if (!haystack.includes(term)) return false;
            }
            return true;
        });
        return this.sorted(list);
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || Object.keys(this.filters()).length > 0);

    constructor() {
        syncQueryParams({ q: this.searchTerm, sort: this.sort }, this.filters, ["category", "status", "complexity"]);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.projectsService.list().subscribe({
            next: (projects) => {
                this.projects.set(projects);
                this.loading.set(false);
            },
            error: () => {
                this.error.set(true);
                this.loading.set(false);
            },
        });
    }

    selectCategory(category: string | null): void {
        const next = { ...this.filters() };
        if (category) next["category"] = category;
        else delete next["category"];
        this.filters.set(next);
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.filters.set({});
    }

    private sorted(list: ProjectListItem[]): ProjectListItem[] {
        const copy = [...list];
        switch (this.sort()) {
            case "newest":
                return copy.sort((a, b) => (b.year ?? 0) - (a.year ?? 0) || a.title.localeCompare(b.title));
            case "oldest":
                return copy.sort((a, b) => (a.year ?? 9999) - (b.year ?? 9999) || a.title.localeCompare(b.title));
            case "az":
                return copy.sort((a, b) => a.title.localeCompare(b.title));
            default:
                return copy.sort((a, b) => (a.sortOrder ?? 9999) - (b.sortOrder ?? 9999) || (b.year ?? 0) - (a.year ?? 0) || a.title.localeCompare(b.title));
        }
    }
}
