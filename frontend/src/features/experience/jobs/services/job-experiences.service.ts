import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { JobExperienceDetail, JobExperienceListItem } from "../../../admin/features/job-experiences/types";
import { environment } from "../../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/experience/jobs`;

@Injectable({ providedIn: "root" })
export class JobExperiencesPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<JobExperienceListItem[]> {
        return this.http.get<JobExperienceListItem[]>(API_URL);
    }

    get(id: number): Observable<JobExperienceDetail> {
        return this.http.get<JobExperienceDetail>(`${API_URL}/${id}`);
    }
}
