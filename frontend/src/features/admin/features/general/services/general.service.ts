import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "../../../../auth/register/services/auth.service";
import { CreateProfileRequest, ProfileInfo, ProfileSummary, UpdateProfileRequest } from "../types";
import { environment } from "../../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/profile`;

@Injectable({ providedIn: "root" })
export class GeneralService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    getSummary(): Observable<ProfileSummary> {
        return this.http.get<ProfileSummary>(`${API_URL}/summary`);
    }

    createProfile(request: CreateProfileRequest, image: File | null): Observable<ProfileInfo> {
        return this.http.post<ProfileInfo>(API_URL, this.buildFormData(request, image), {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    updateProfile(request: UpdateProfileRequest, image: File | null): Observable<ProfileInfo> {
        return this.http.put<ProfileInfo>(API_URL, this.buildFormData(request, image), {
            headers: this.authHeaders(),
            withCredentials: true,
        });
    }

    private authHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }

    private buildFormData(request: CreateProfileRequest | UpdateProfileRequest, image: File | null): FormData {
        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(request)], { type: "application/json" }));
        if (image) {
            formData.append("image", image);
        }
        return formData;
    }
}
