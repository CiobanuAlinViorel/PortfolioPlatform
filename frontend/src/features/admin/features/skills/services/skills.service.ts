import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { SkillCategory, SkillDetail, SkillListItem, SkillWriteRequest } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/skills`;

@Injectable({ providedIn: "root" })
export class SkillsService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<SkillListItem[]> {
        return this.http.get<SkillListItem[]>(API_URL);
    }

    get(id: number): Observable<SkillDetail> {
        return this.http.get<SkillDetail>(`${API_URL}/${id}`);
    }

    create(request: SkillWriteRequest): Observable<SkillDetail> {
        return this.http.post<SkillDetail>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: SkillWriteRequest): Observable<SkillDetail> {
        return this.http.put<SkillDetail>(`${API_URL}/${id}`, request, {
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

    listCategories(): Observable<SkillCategory[]> {
        return this.http.get<SkillCategory[]>(`${API_URL}/categories`);
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
