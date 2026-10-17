import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { ProfileSummary } from "../../features/admin/features/general/types";
import { HomeService } from "../../features/home/services/home.service";
import { environment } from "../../environments/environment";

const API_URL = environment.api.baseUrl;

export interface ContactMessageRequest {
    name: string;
    email: string;
    message: string;
}

@Injectable({ providedIn: "root" })
export class ContactService {
    private readonly http = inject(HttpClient);
    private readonly home = inject(HomeService);

    getContactInfo(): Observable<ProfileSummary> {
        return this.home.getProfile();
    }

    sendMessage(request: ContactMessageRequest): Observable<void> {
        return this.http.post<void>(`${API_URL}/contact`, request);
    }
}
