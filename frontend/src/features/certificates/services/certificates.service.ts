import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { RequestCache } from "../../../shared/utils/cache";
import { Certificate, CertificateCategory } from "../../admin/features/certificates/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/certificates`;

@Injectable({ providedIn: "root" })
export class CertificatesPublicService {
    private readonly http = inject(HttpClient);
    private readonly cache = new RequestCache();

    list(): Observable<Certificate[]> {
        return this.cache.get(API_URL, () => this.http.get<Certificate[]>(API_URL));
    }

    get(id: number): Observable<Certificate> {
        return this.cache.get(`${API_URL}/${id}`, () => this.http.get<Certificate>(`${API_URL}/${id}`));
    }

    listCategories(): Observable<CertificateCategory[]> {
        return this.cache.get(`${API_URL}/categories`, () => this.http.get<CertificateCategory[]>(`${API_URL}/categories`));
    }
}
