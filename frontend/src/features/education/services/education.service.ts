import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { EducationDetail, EducationListItem } from "../../admin/features/education/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/education`;

@Injectable({ providedIn: "root" })
export class EducationPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<EducationListItem[]> {
        return this.http.get<EducationListItem[]>(API_URL);
    }

    get(id: number): Observable<EducationDetail> {
        return this.http.get<EducationDetail>(`${API_URL}/${id}`);
    }
}
