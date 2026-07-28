import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { ProjectCategory, ProjectDetail, ProjectListItem, ProjectWriteRequest, SkillOption } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/projects`;
const SKILLS_API_URL = `${environment.api.baseUrl}/skills`;

@Injectable({ providedIn: "root" })
export class ProjectsService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<ProjectListItem[]> {
        return this.http.get<ProjectListItem[]>(API_URL);
    }

    get(id: number): Observable<ProjectDetail> {
        return this.http.get<ProjectDetail>(`${API_URL}/${id}`);
    }

    create(request: ProjectWriteRequest, files: File[]): Observable<ProjectDetail> {
        return this.http.post<ProjectDetail>(API_URL, this.buildFormData(request, files), {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: ProjectWriteRequest, files: File[]): Observable<ProjectDetail> {
        return this.http.put<ProjectDetail>(`${API_URL}/${id}`, this.buildFormData(request, files), {
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

    listCategories(): Observable<ProjectCategory[]> {
        return this.http.get<ProjectCategory[]>(`${API_URL}/categories`);
    }

    listSkills(): Observable<SkillOption[]> {
        return this.http.get<SkillOption[]>(SKILLS_API_URL);
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }

    private buildFormData(request: ProjectWriteRequest, files: File[]): FormData {
        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(request)], { type: "application/json" }));
        files.forEach((file) => formData.append("files", file));
        return formData;
    }
}
