import { HttpClient } from "@angular/common/http";
import { Injectable, computed, signal } from "@angular/core";
import { Observable, tap } from "rxjs";
import {
    AuthResponse,
    ForgotPasswordRequest,
    GoogleAuthRequest,
    LoginRequest,
    MessageResponse,
    RegisterRequest,
    RegisterResponse,
    ResetPasswordRequest,
} from "../types";
import { environment } from "../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/auth`;

@Injectable({ providedIn: "root" })
export class AuthService {
    private readonly currentUserSignal = signal<AuthResponse | null>(null);
    readonly currentUser = this.currentUserSignal.asReadonly();
    readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

    constructor(private readonly http: HttpClient) {}

    register(req: RegisterRequest): Observable<RegisterResponse> {
        return this.http.post<RegisterResponse>(`${API_URL}/register`, req);
    }

    isAdmin():boolean {
        const user = this.currentUserSignal();
        return !!user && user.role === 'ADMIN';
    }

    login(req: LoginRequest): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${API_URL}/login`, req, { withCredentials: true })
            .pipe(tap((res) => this.currentUserSignal.set(res)));
    }

    logout(): Observable<MessageResponse> {
        return this.http
            .post<MessageResponse>(`${API_URL}/logout`, {}, { withCredentials: true })
            .pipe(tap(() => this.currentUserSignal.set(null)));
    }

    refresh(): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${API_URL}/refresh`, {}, { withCredentials: true })
            .pipe(tap((res) => this.currentUserSignal.set(res)));
    }

    forgotPassword(req: ForgotPasswordRequest): Observable<MessageResponse> {
        return this.http.post<MessageResponse>(`${API_URL}/forgot-password`, req);
    }

    resetPassword(req: ResetPasswordRequest): Observable<MessageResponse> {
        return this.http.post<MessageResponse>(`${API_URL}/reset-password`, req);
    }

    googleAuth(req: GoogleAuthRequest): Observable<AuthResponse> {
        return this.http
            .post<AuthResponse>(`${API_URL}/google`, req, { withCredentials: true })
            .pipe(tap((res) => this.currentUserSignal.set(res)));
    }

    getToken(): string | null {
        return this.currentUserSignal()?.token ?? null;
    }
}
