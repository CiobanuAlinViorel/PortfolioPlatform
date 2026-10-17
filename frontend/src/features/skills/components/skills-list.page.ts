import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideAward, LucideChevronDown, LucideGraduationCap, LucideLayers } from "@lucide/angular";
import { ProficiencyLevel, SkillListItem } from "../../admin/features/skills/types";
import { SkillsPublicService } from "../services/skills.service";
import { PageHeaderComponent } from "../../../shared/components/page-header.component";
import { FilterBarComponent, FilterGroup, FilterSelection } from "../../../shared/components/filter-bar.component";
import { SearchInputComponent } from "../../../shared/components/search-input.component";
import { ChipComponent } from "../../../shared/components/chip.component";
import { BadgeComponent } from "../../../shared/components/badge.component";
import { CardSkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../shared/components/empty-state.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../shared/pipes/enum-label.pipe";
import { BadgeVariant, statusVariant } from "../../../shared/utils/status-variant";
import { countBy, syncQueryParams } from "../../../shared/utils/query-state";

const PROFICIENCY_ORDER: Record<ProficiencyLevel, number> = { BEGINNER: 0, INTERMEDIATE: 1, ADVANCED: 2, EXPERT: 3 };

const SORTS = [
    { id: "", label: "Level" },
    { id: "az", label: "A – Z" },
    { id: "years", label: "Experience" },
];

export interface SkillGroup {
    name: string;
    skills: SkillListItem[];
}

@Component({
    selector: "app-skills-list",
    imports: [
        DatePipe,
        LucideAward,
        LucideChevronDown,
        LucideGraduationCap,
        PageHeaderComponent,
        FilterBarComponent,
        SearchInputComponent,
        ChipComponent,
        BadgeComponent,
        CardSkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        RevealDirective,
        EnumLabelPipe,
    ],
    templateUrl: "../ui/skills-list.page.html",
})
export class SkillsListPage implements OnInit {
    private readonly skillsService = inject(SkillsPublicService);
    private readonly enumLabel = new EnumLabelPipe();

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly skills = signal<SkillListItem[]>([]);

    readonly searchTerm = signal("");
    readonly filters = signal<FilterSelection>({});
    readonly sort = signal("");
    readonly learningOnly = signal("");
    readonly sortOptions = SORTS;
    readonly emptyIcon = LucideLayers;
    readonly statusVariant = statusVariant;

    readonly legend: Array<{ level: ProficiencyLevel; variant: BadgeVariant }> = [
        { level: "BEGINNER", variant: "warning" },
        { level: "INTERMEDIATE", variant: "info" },
        { level: "ADVANCED", variant: "info" },
        { level: "EXPERT", variant: "success" },
    ];

    readonly showSearch = computed(() => this.skills().length >= 6);
    readonly hasLearning = computed(() => this.skills().some((s) => s.learning));

    readonly filterGroups = computed((): FilterGroup[] => [
        { id: "category", label: "Category", options: countBy(this.skills(), (s) => s.categoryName).map((o) => ({ ...o, label: o.id })).sort((a, b) => a.label.localeCompare(b.label)) },
        {
            id: "proficiency",
            label: "Proficiency",
            options: countBy(this.skills(), (s) => s.proficiency)
                .sort((a, b) => PROFICIENCY_ORDER[b.id as ProficiencyLevel] - PROFICIENCY_ORDER[a.id as ProficiencyLevel])
                .map((o) => ({ ...o, label: this.enumLabel.transform(o.id) })),
        },
    ]);

    readonly visibleSkills = computed(() => {
        const f = this.filters();
        const term = this.searchTerm().trim().toLowerCase();
        const learningOnly = this.learningOnly() === "1";
        const list = this.skills().filter((s) => {
            if (f["category"] && s.categoryName !== f["category"]) return false;
            if (f["proficiency"] && s.proficiency !== f["proficiency"]) return false;
            if (learningOnly && !s.learning) return false;
            if (term && ![s.name, s.description ?? "", s.categoryName ?? ""].join(" ").toLowerCase().includes(term)) return false;
            return true;
        });
        switch (this.sort()) {
            case "az":
                return list.sort((a, b) => a.name.localeCompare(b.name));
            case "years":
                return list.sort((a, b) => (b.yearsOfExperience ?? 0) - (a.yearsOfExperience ?? 0) || a.name.localeCompare(b.name));
            default:
                return list.sort((a, b) => (b.level ?? -1) - (a.level ?? -1) || PROFICIENCY_ORDER[b.proficiency] - PROFICIENCY_ORDER[a.proficiency] || a.name.localeCompare(b.name));
        }
    });

    readonly groups = computed((): SkillGroup[] => {
        const map = new Map<string, SkillListItem[]>();
        for (const s of this.visibleSkills()) {
            const key = s.categoryName || "Other";
            map.set(key, [...(map.get(key) ?? []), s]);
        }
        return Array.from(map, ([name, skills]) => ({ name, skills })).sort((a, b) => (a.name === "Other" ? 1 : b.name === "Other" ? -1 : a.name.localeCompare(b.name)));
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || Object.keys(this.filters()).length > 0 || !!this.learningOnly());

    constructor() {
        syncQueryParams({ q: this.searchTerm, sort: this.sort, learning: this.learningOnly }, this.filters, ["category", "proficiency"]);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.skillsService.list().subscribe({
            next: (skills) => { this.skills.set(skills); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    readonly collapsed = signal<Record<string, boolean>>({});

    isCollapsed(name: string, index: number): boolean {
        return this.collapsed()[name] ?? index > 0;
    }

    toggleGroup(name: string, index: number): void {
        this.collapsed.update((c) => ({ ...c, [name]: !this.isCollapsed(name, index) }));
    }

    toggleLearning(): void {
        this.learningOnly.set(this.learningOnly() === "1" ? "" : "1");
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.filters.set({});
        this.learningOnly.set("");
    }
}
