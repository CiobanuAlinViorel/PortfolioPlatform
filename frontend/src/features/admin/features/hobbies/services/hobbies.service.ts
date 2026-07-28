import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { HobbyDetail, HobbyListItem, HobbyWriteRequest, SkillOption } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/hobbies`;
const SKILLS_API_URL = `${environment.api.baseUrl}/skills`;

@Injectable({ providedIn: "root" })
export class HobbiesService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    list(): Observable<HobbyListItem[]> {
        return this.http.get<HobbyListItem[]>(API_URL);
    }

    get(id: number): Observable<HobbyDetail> {
        return this.http.get<HobbyDetail>(`${API_URL}/${id}`);
    }

    create(request: HobbyWriteRequest): Observable<HobbyListItem> {
        return this.http.post<HobbyListItem>(API_URL, request, {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    update(id: number, request: HobbyWriteRequest): Observable<HobbyListItem> {
        return this.http.put<HobbyListItem>(`${API_URL}/${id}`, request, {
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

    listSkills(): Observable<SkillOption[]> {
        return this.http.get<SkillOption[]>(SKILLS_API_URL);
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
