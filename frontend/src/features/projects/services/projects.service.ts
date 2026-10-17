import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { RequestCache } from "../../../shared/utils/cache";
import { ProjectDetail, ProjectListItem } from "../../admin/features/projects/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/projects`;

@Injectable({ providedIn: "root" })
export class ProjectsPublicService {
    private readonly http = inject(HttpClient);
    private readonly cache = new RequestCache();

    list(): Observable<ProjectListItem[]> {
        return this.cache.get(`${API_URL}/public`, () => this.http.get<ProjectListItem[]>(`${API_URL}/public`));
    }

    get(id: number): Observable<ProjectDetail> {
        return this.cache.get(`${API_URL}/${id}`, () => this.http.get<ProjectDetail>(`${API_URL}/${id}`));
    }
}
