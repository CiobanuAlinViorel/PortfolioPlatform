import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideDynamicIcon, LucideHeart, LucideQuote } from "@lucide/angular";
import { HobbyListItem } from "../../admin/features/hobbies/types";
import { HobbiesPublicService } from "../services/hobbies.service";
import { PageHeaderComponent } from "../../../shared/components/page-header.component";
import { SearchInputComponent } from "../../../shared/components/search-input.component";
import { ChipComponent } from "../../../shared/components/chip.component";
import { BadgeComponent } from "../../../shared/components/badge.component";
import { CardSkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../shared/components/empty-state.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { EnumLabelPipe } from "../../../shared/pipes/enum-label.pipe";
import { BadgeVariant } from "../../../shared/utils/status-variant";
import { countBy, syncQueryParams } from "../../../shared/utils/query-state";
import { hobbyIcon, tileStyle } from "../../../shared/utils/visual";

const ACTIVITY_VARIANT: Record<string, BadgeVariant> = { OCCASIONAL: "neutral", REGULAR: "info", FREQUENT: "success", DAILY: "accent" };

@Component({
    selector: "app-hobbies-list",
    imports: [
        LucideDynamicIcon,
        LucideQuote,
        PageHeaderComponent,
        SearchInputComponent,
        ChipComponent,
        BadgeComponent,
        CardSkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        RevealDirective,
        EnumLabelPipe,
    ],
    templateUrl: "../ui/hobbies-list.page.html",
})
export class HobbiesListPage implements OnInit {
    private readonly hobbiesService = inject(HobbiesPublicService);

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly hobbies = signal<HobbyListItem[]>([]);

    readonly searchTerm = signal("");
    readonly category = signal("");
    readonly emptyIcon = LucideHeart;
    readonly hobbyIcon = hobbyIcon;
    readonly tileStyle = tileStyle;

    readonly showSearch = computed(() => this.hobbies().length >= 6);
    readonly categories = computed(() => countBy(this.hobbies(), (h) => h.category));

    readonly visibleHobbies = computed(() => {
        const category = this.category();
        const term = this.searchTerm().trim().toLowerCase();
        return this.hobbies().filter((h) => {
            if (category && h.category !== category) return false;
            if (term && ![h.name, h.description ?? "", h.favoriteAspect ?? "", h.category].join(" ").toLowerCase().includes(term)) return false;
            return true;
        });
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || !!this.category());

    constructor() {
        syncQueryParams({ q: this.searchTerm, category: this.category });
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.hobbiesService.list().subscribe({
            next: (hobbies) => { this.hobbies.set(hobbies); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    activityVariant(level: string | undefined): BadgeVariant {
        return (level && ACTIVITY_VARIANT[level]) || "neutral";
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.category.set("");
    }
}
