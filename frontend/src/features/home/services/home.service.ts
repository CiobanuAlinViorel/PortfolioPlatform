import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { ProfileSummary } from "../../admin/features/general/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/profile`;

@Injectable({ providedIn: "root" })
export class HomeService {
    private readonly http = inject(HttpClient);

    getSummary(): Observable<ProfileSummary> {
        return this.http.get<ProfileSummary>(`${API_URL}/summary`);
    }
}
