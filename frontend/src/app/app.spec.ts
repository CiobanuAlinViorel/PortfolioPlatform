import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('renders the public shell with skip link, header and footer', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.match(() => true).forEach((req) => req.flush({ profile: { id: 1, firstName: 'Ada', lastName: 'Lovelace' }, contact: { email: 'a@b.c' } }));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('a.skip-link')?.getAttribute('href')).toBe('#main-content');
    expect(el.querySelector('main#main-content')).toBeTruthy();
    expect(el.querySelector('app-header header')).toBeTruthy();
    expect(el.querySelector('app-footer footer #contact')).toBeTruthy();
    expect(el.querySelector('app-header')?.textContent).toContain('Ada Lovelace');
  });

  it('renders the admin shell without the public header and footer', () => {
    const fixture = TestBed.createComponent(App);
    vi.spyOn(fixture.componentInstance, 'isAdminRoute').mockReturnValue(true);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-sidebar')).toBeTruthy();
    expect(el.querySelector('app-header')).toBeNull();
    expect(el.querySelector('app-footer')).toBeNull();
  });
});
