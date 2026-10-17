import { DOCUMENT, isPlatformBrowser } from "@angular/common";
import { Component, ElementRef, HostListener, OnInit, PLATFORM_ID, computed, inject, signal } from "@angular/core";
import { Router, RouterLink, RouterLinkActive } from "@angular/router";
import {
    LucideAward,
    LucideBriefcase,
    LucideChevronDown,
    LucideDownload,
    LucideDynamicIcon,
    LucideFolderKanban,
    LucideGraduationCap,
    LucideHeart,
    LucideHeartHandshake,
    LucideHouse,
    LucideIcon,
    LucideLayers,
    LucideLoaderCircle,
    LucideLogOut,
    LucideMail,
    LucideMenu,
    LucideMoon,
    LucideShieldCheck,
    LucideSun,
    LucideUser,
    LucideX,
} from "@lucide/angular";
import { ThemeService } from "../services/theme.service";
import { AuthService } from "../../features/auth/register/services/auth.service";
import { CvDownloadService } from "../services/cv-download.service";
import { HomeService } from "../../features/home/services/home.service";
import { ButtonComponent } from "./button.component";
import { AvatarComponent } from "./avatar.component";

export interface NavItem {
    label: string;
    path?: string;
    icon: LucideIcon;
    children?: NavItem[];
}

@Component({
    selector: "app-header",
    imports: [
        RouterLink,
        RouterLinkActive,
        LucideSun,
        LucideMoon,
        LucideMenu,
        LucideX,
        LucideDynamicIcon,
        LucideShieldCheck,
        LucideChevronDown,
        LucideDownload,
        LucideLoaderCircle,
        LucideLogOut,
        LucideMail,
        ButtonComponent,
        AvatarComponent,
    ],
    templateUrl: "../ui/header.component.html",
})
export class HeaderComponent implements OnInit {
    readonly nav: NavItem[] = [
        { label: "Home", path: "/", icon: LucideHouse },
        { label: "Projects", path: "/projects", icon: LucideFolderKanban },
        {
            label: "Experience",
            icon: LucideBriefcase,
            children: [
                { label: "Jobs", path: "/experience/jobs", icon: LucideBriefcase },
                { label: "Volunteer", path: "/experience/volunteer", icon: LucideHeartHandshake },
            ],
        },
        { label: "Skills", path: "/skills", icon: LucideLayers },
        {
            label: "About",
            icon: LucideUser,
            children: [
                { label: "Certificates", path: "/certificates", icon: LucideAward },
                { label: "Education", path: "/education", icon: LucideGraduationCap },
                { label: "Hobbies", path: "/hobbies", icon: LucideHeart },
            ],
        },
    ];

    /** Flattened list for the mobile sheet. */
    readonly mobileNav: NavItem[] = this.nav.flatMap((item) => (item.children ? item.children : [item]));

    private readonly themeService = inject(ThemeService);
    private readonly authService = inject(AuthService);
    private readonly elementRef = inject(ElementRef);
    private readonly cvDownloadService = inject(CvDownloadService);
    private readonly homeService = inject(HomeService);
    private readonly router = inject(Router);
    private readonly document = inject(DOCUMENT);
    private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

    readonly brandName = signal("Portfolio");
    readonly brandInitials = computed(() => this.brandName().split(" ").map((p) => p[0]).join("").slice(0, 2).toUpperCase());
    readonly avatarUrl = signal<string | null>(null);

    readonly isDownloadingCv = signal(false);
    readonly isMenuOpen = signal(false);
    readonly openDropdown = signal<string | null>(null);
    readonly isUserMenuOpen = signal(false);
    readonly scrolled = signal(false);

    readonly isDarkMode = computed(() => this.themeService.theme() === "dark");
    readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
    readonly isAdmin = computed(() => this.authService.currentUser()?.role === "ADMIN");

    ngOnInit(): void {
        this.homeService.getProfile().subscribe({
            next: (summary) => {
                const p = summary.profile;
                if (p?.firstName) this.brandName.set(`${p.firstName} ${p.lastName ?? ""}`.trim());
                this.avatarUrl.set(p?.imageLink ?? null);
            },
        });
        if (this.isBrowser) this.scrolled.set(window.scrollY > 8);
    }

    @HostListener("window:scroll")
    onScroll(): void {
        if (this.isBrowser) this.scrolled.set(window.scrollY > 8);
    }

    isChildActive(item: NavItem): boolean {
        return !!item.children?.some((c) => c.path && this.router.isActive(c.path, { paths: "subset", queryParams: "ignored", fragment: "ignored", matrixParams: "ignored" }));
    }

    toggleDropdown(label: string): void {
        this.openDropdown.update((cur) => (cur === label ? null : label));
        this.isUserMenuOpen.set(false);
    }

    closeDropdowns(): void {
        this.openDropdown.set(null);
        this.isUserMenuOpen.set(false);
    }

    toggleUserMenu(): void {
        this.isUserMenuOpen.update((v) => !v);
        this.openDropdown.set(null);
    }

    onMenuKeydown(event: KeyboardEvent, menu: HTMLElement): void {
        const items = Array.from(menu.querySelectorAll<HTMLElement>('[role="menuitem"]'));
        const index = items.indexOf(this.document.activeElement as HTMLElement);
        if (event.key === "ArrowDown") {
            event.preventDefault();
            items[(index + 1) % items.length]?.focus();
        } else if (event.key === "ArrowUp") {
            event.preventDefault();
            items[(index - 1 + items.length) % items.length]?.focus();
        } else if (event.key === "Tab") {
            this.closeDropdowns();
        }
    }

    toggleMenu(): void {
        this.setMenu(!this.isMenuOpen());
    }

    closeMenu(): void {
        this.setMenu(false);
        this.closeDropdowns();
    }

    private setMenu(open: boolean): void {
        this.isMenuOpen.set(open);
        this.document.body?.classList.toggle("scroll-locked", open);
        if (open) {
            setTimeout(() => this.document.getElementById("mobile-nav-close")?.focus(), 50);
        } else {
            this.document.getElementById("mobile-nav-toggle")?.focus();
        }
    }

    @HostListener("document:click", ["$event"])
    onDocumentClick(event: MouseEvent): void {
        if ((this.openDropdown() || this.isUserMenuOpen()) && !this.elementRef.nativeElement.contains(event.target)) {
            this.closeDropdowns();
        }
    }

    @HostListener("document:keydown.escape")
    onEscape(): void {
        if (this.isMenuOpen()) this.closeMenu();
        else this.closeDropdowns();
    }

    logout(): void {
        this.authService.logout().subscribe({ next: () => this.closeMenu() });
    }

    toggleTheme(): void {
        this.themeService.toggleTheme();
    }

    downloadCv(): void {
        if (this.isDownloadingCv()) return;
        this.isDownloadingCv.set(true);
        this.cvDownloadService.generate().subscribe({
            next: () => this.isDownloadingCv.set(false),
            error: () => this.isDownloadingCv.set(false),
        });
    }
}
