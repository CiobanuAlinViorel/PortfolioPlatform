import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../shared/components/header.component';
import { ThemeService } from '../shared/services/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('Portfolio');

  private readonly themeService = inject(ThemeService);

  ngOnInit(): void {
    this.themeService.setTheme();
    console.log('Initial theme set to', this.themeService.getTheme());
  }

  ngOnDestroy(): void {
    this.themeService.toggleTheme();
  }
}
