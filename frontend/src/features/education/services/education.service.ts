import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { RequestCache } from "../../../shared/utils/cache";
import { EducationDetail, EducationListItem } from "../../admin/features/education/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/education`;

@Injectable({ providedIn: "root" })
export class EducationPublicService {
    private readonly http = inject(HttpClient);
    private readonly cache = new RequestCache();

    list(): Observable<EducationListItem[]> {
        return this.cache.get(API_URL, () => this.http.get<EducationListItem[]>(API_URL));
    }

    get(id: number): Observable<EducationDetail> {
        return this.cache.get(`${API_URL}/${id}`, () => this.http.get<EducationDetail>(`${API_URL}/${id}`));
    }
}
