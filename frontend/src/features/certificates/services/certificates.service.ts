import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { Certificate, CertificateCategory } from "../../admin/features/certificates/types";
import { environment } from "../../../environments/environment";

const API_URL = `${environment.api.baseUrl}/certificates`;

@Injectable({ providedIn: "root" })
export class CertificatesPublicService {
    private readonly http = inject(HttpClient);

    list(): Observable<Certificate[]> {
        return this.http.get<Certificate[]>(API_URL);
    }

    get(id: number): Observable<Certificate> {
        return this.http.get<Certificate>(`${API_URL}/${id}`);
    }

    listCategories(): Observable<CertificateCategory[]> {
        return this.http.get<CertificateCategory[]>(`${API_URL}/categories`);
    }
}
