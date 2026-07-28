import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { ProjectDetail, ProjectListItem } from "../../admin/features/projects/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/projects`;

@Injectable({ providedIn: "root" })
export class ProjectsPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<ProjectListItem[]> {
        return this.http.get<ProjectListItem[]>(`${API_URL}/public`);
    }

    get(id: number): Observable<ProjectDetail> {
        return this.http.get<ProjectDetail>(`${API_URL}/${id}`);
    }
}
