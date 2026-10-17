import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { HeaderComponent } from '../shared/components/header.component';
import { FooterComponent } from '../shared/components/footer.component';
import { ThemeService } from '../shared/services/theme.service';
import { SeoData, SeoService } from '../shared/services/seo.service';
import { SidebarComponent } from '../features/admin/components/sidebar.component';
import { SlowRequestBannerComponent } from '../shared/components/slow-request-banner.component';
import { ToastHostComponent } from '../shared/components/toast-host.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, FooterComponent, SidebarComponent, SlowRequestBannerComponent, ToastHostComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly seoService = inject(SeoService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.themeService.setTheme();

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.applySeoForCurrentRoute());
  }

  private applySeoForCurrentRoute(): void {
    let route = this.activatedRoute.root;
    let data: Record<string, unknown> = {};
    while (route.firstChild) {
      route = route.firstChild;
      data = { ...data, ...route.snapshot.data };
    }

    const seo = data['seo'] as SeoData | undefined;
    if (seo) {
      this.seoService.update(seo, this.router.url);
    }
  }

  ngOnDestroy(): void {}

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }
}
