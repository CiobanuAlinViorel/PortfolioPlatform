import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { VolunteerExperienceDetail, VolunteerExperienceListItem } from "../../../admin/features/volunteer-experiences/types";
import { environment } from "../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/experience/volunteers`;

@Injectable({ providedIn: "root" })
export class VolunteerExperiencesPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<VolunteerExperienceListItem[]> {
        return this.http.get<VolunteerExperienceListItem[]>(API_URL);
    }

    get(id: number): Observable<VolunteerExperienceDetail> {
        return this.http.get<VolunteerExperienceDetail>(`${API_URL}/${id}`);
    }
}
