import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { ProjectInVolunteer, VolunteerExperienceDetail, VolunteerExperienceListItem, VolunteerExperienceWriteRequest } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/experience/volunteers`;

@Injectable({ providedIn: "root" })
export class VolunteerExperiencesService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<VolunteerExperienceListItem[]> {
        return this.http.get<VolunteerExperienceListItem[]>(API_URL);
    }

    get(id: number): Observable<VolunteerExperienceDetail> {
        return this.http.get<VolunteerExperienceDetail>(`${API_URL}/${id}`);
    }

    create(request: VolunteerExperienceWriteRequest): Observable<VolunteerExperienceDetail> {
        return this.http.post<VolunteerExperienceDetail>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: VolunteerExperienceWriteRequest): Observable<VolunteerExperienceDetail> {
        return this.http.put<VolunteerExperienceDetail>(`${API_URL}/${id}`, request, {
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

    listAvailableProjects(): Observable<ProjectInVolunteer[]> {
        return this.http.get<ProjectInVolunteer[]>(`${API_URL}/available-projects`, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
