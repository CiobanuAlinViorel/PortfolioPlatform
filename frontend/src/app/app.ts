import { Component, DestroyRef, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { HeaderComponent } from '../shared/components/header.component';
import { FooterComponent } from '../shared/components/footer.component';
import { ThemeService } from '../shared/services/theme.service';
import { SeoData, SeoService } from '../shared/services/seo.service';
import { SidebarComponent } from '../features/admin/components/sidebar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, FooterComponent, SidebarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('Portfolio');

  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly seoService = inject(SeoService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.themeService.setTheme();
    console.log('Initial theme set to', this.themeService.getTheme());

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

  ngOnDestroy(): void {
    this.themeService.toggleTheme();
  }

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }
}
