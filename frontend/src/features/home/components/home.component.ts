import { Component, OnInit, inject, signal } from '@angular/core';
import { LucideLink, LucideMail, LucideMapPin, LucidePhone } from '@lucide/angular';
import { ContactInfo, ProfileInfo } from '../../admin/features/general/types';
import { HomeService } from '../services/home.service';

@Component({
    selector: 'home-page',
    imports: [LucideLink, LucideMail, LucideMapPin, LucidePhone],
    templateUrl: '../ui/home.page.html'
})
export class HomePage implements OnInit {
    private readonly homeService = inject(HomeService);

    readonly loading = signal(true);
    readonly profile = signal<ProfileInfo | null>(null);
    readonly contact = signal<ContactInfo | null>(null);

    ngOnInit(): void {
        this.homeService.getSummary().subscribe({
            next: (summary) => {
                this.profile.set(summary.profile);
                this.contact.set(summary.contact);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            },
        });
    }
}
