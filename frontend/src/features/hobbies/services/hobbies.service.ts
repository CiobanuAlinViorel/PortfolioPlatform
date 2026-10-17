import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { RequestCache } from "../../../shared/utils/cache";
import { HobbyDetail, HobbyListItem } from "../../admin/features/hobbies/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/hobbies`;

@Injectable({ providedIn: "root" })
export class HobbiesPublicService {
    private readonly http = inject(HttpClient);
    private readonly cache = new RequestCache();

    list(): Observable<HobbyListItem[]> {
        return this.cache.get(API_URL, () => this.http.get<HobbyListItem[]>(API_URL));
    }

    get(id: number): Observable<HobbyDetail> {
        return this.cache.get(`${API_URL}/${id}`, () => this.http.get<HobbyDetail>(`${API_URL}/${id}`));
    }
}
