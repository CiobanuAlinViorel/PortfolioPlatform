import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, map } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { EducationDetail, EducationListItem, EducationWriteRequest, ProjectOption } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/education`;
const PROJECTS_API_URL = `${environment.api.baseUrl}/projects`;

@Injectable({ providedIn: "root" })
export class EducationService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<EducationListItem[]> {
        return this.http.get<EducationListItem[]>(API_URL);
    }

    get(id: number): Observable<EducationDetail> {
        return this.http.get<EducationDetail>(`${API_URL}/${id}`);
    }

    create(request: EducationWriteRequest): Observable<EducationListItem> {
        return this.http.post<EducationListItem>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: EducationWriteRequest): Observable<EducationListItem> {
        return this.http.put<EducationListItem>(`${API_URL}/${id}`, request, {
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

    listProjects(): Observable<ProjectOption[]> {
        return this.http
            .get<{ id: number; title: string }[]>(PROJECTS_API_URL)
            .pipe(map((projects) => projects.map((p) => ({ id: p.id, title: p.title }))));
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
