import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { CategoryWriteRequest, Certificate, CertificateCategory, CertificateWriteRequest } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/certificates`;

@Injectable({ providedIn: "root" })
export class CertificatesService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<Certificate[]> {
        return this.http.get<Certificate[]>(API_URL);
    }

    get(id: number): Observable<Certificate> {
        return this.http.get<Certificate>(`${API_URL}/${id}`);
    }

    create(request: CertificateWriteRequest): Observable<Certificate> {
        return this.http.post<Certificate>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: CertificateWriteRequest): Observable<Certificate> {
        return this.http.put<Certificate>(`${API_URL}/${id}`, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${API_URL}/${id}`, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    listCategories(): Observable<CertificateCategory[]> {
        return this.http.get<CertificateCategory[]>(`${API_URL}/categories`);
    }

    createCategory(request: CategoryWriteRequest): Observable<CertificateCategory> {
        return this.http.post<CertificateCategory>(`${API_URL}/categories`, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    updateCategory(id: number, request: CategoryWriteRequest): Observable<CertificateCategory> {
        return this.http.put<CertificateCategory>(`${API_URL}/categories/${id}`, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    deleteCategory(id: number): Observable<void> {
        return this.http.delete<void>(`${API_URL}/categories/${id}`, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
