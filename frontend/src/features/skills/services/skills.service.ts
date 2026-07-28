import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { SkillCategory, SkillDetail, SkillListItem } from "../../admin/features/skills/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/skills`;

@Injectable({ providedIn: "root" })
export class SkillsPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<SkillListItem[]> {
        return this.http.get<SkillListItem[]>(API_URL);
    }

    get(id: number): Observable<SkillDetail> {
        return this.http.get<SkillDetail>(`${API_URL}/${id}`);
    }

    listCategories(): Observable<SkillCategory[]> {
        return this.http.get<SkillCategory[]>(`${API_URL}/categories`);
    }
}
