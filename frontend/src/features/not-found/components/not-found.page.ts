import { Component } from '@angular/core';
import { LucideArrowLeft, LucideFolderKanban } from '@lucide/angular';
import { ButtonComponent } from '../../../shared/components/button.component';

@Component({
  selector: 'app-not-found',
  imports: [ButtonComponent, LucideArrowLeft, LucideFolderKanban],
  template: `
    <section class="flex min-h-[calc(100dvh-var(--header-h))] items-center justify-center px-4 py-16">
      <div class="max-w-md text-center">
        <p class="font-brand text-[clamp(5rem,20vw,9rem)] font-semibold leading-none tracking-tighter text-transparent [-webkit-text-stroke:1.5px_var(--color-accent)] motion-safe:animate-fade-up">404</p>
        <h1 class="mt-2 font-brand text-3xl font-semibold text-fg">This page wandered off</h1>
        <p class="mt-3 text-base text-fg-muted">The page you're looking for doesn't exist or has moved. Let's get you back on track.</p>
        <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <app-button variant="primary" link="/">
            <svg lucideArrowLeft class="h-4 w-4"></svg>
            Back to home
          </app-button>
          <app-button variant="secondary" link="/projects">
            <svg lucideFolderKanban class="h-4 w-4"></svg>
            Browse projects
          </app-button>
        </div>
      </div>
    </section>
  `,
})
export class NotFoundPage {}
