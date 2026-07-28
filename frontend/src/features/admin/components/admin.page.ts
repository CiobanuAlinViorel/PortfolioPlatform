import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";
import {
    LucideSettings,
    LucideGraduationCap,
    LucideBriefcase,
    LucideFolderKanban,
    LucideSparkles,
    LucideAward,
    LucideHeart,
    LucideHandHeart,
} from "@lucide/angular";

@Component({
    selector: "app-admin-page",
    imports: [
        RouterLink,
        LucideSettings,
        LucideGraduationCap,
        LucideBriefcase,
        LucideFolderKanban,
        LucideSparkles,
        LucideAward,
        LucideHeart,
        LucideHandHeart,
    ],
    templateUrl: "../ui/admin.page.html",
})
export class AdminPageComponent {}
