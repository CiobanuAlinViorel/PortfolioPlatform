import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { JobExperienceDetail, JobExperienceListItem, JobExperienceWriteRequest, ProjectInJob } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/experience/jobs`;

@Injectable({ providedIn: "root" })
export class JobExperiencesService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<JobExperienceListItem[]> {
        return this.http.get<JobExperienceListItem[]>(API_URL);
    }

    get(id: number): Observable<JobExperienceDetail> {
        return this.http.get<JobExperienceDetail>(`${API_URL}/${id}`);
    }

    create(request: JobExperienceWriteRequest): Observable<JobExperienceDetail> {
        return this.http.post<JobExperienceDetail>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: JobExperienceWriteRequest): Observable<JobExperienceDetail> {
        return this.http.put<JobExperienceDetail>(`${API_URL}/${id}`, request, {
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

    addProject(id: number, projectId: number): Observable<JobExperienceDetail> {
        return this.http.post<JobExperienceDetail>(
            `${API_URL}/${id}/projects/${projectId}`,
            {},
            { headers: this.authHeaders(), withCredentials: true },
        );
    }

    removeProject(id: number, projectId: number): Observable<JobExperienceDetail> {
        return this.http.delete<JobExperienceDetail>(`${API_URL}/${id}/projects/${projectId}`, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    listAvailableProjects(): Observable<ProjectInJob[]> {
        return this.http.get<ProjectInJob[]>(`${API_URL}/available-projects`, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
