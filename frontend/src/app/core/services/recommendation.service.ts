import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { TravelSummary } from '../models';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private http = inject(HttpClient);

  getForMe(): Observable<TravelSummary[]> {
    return this.http.get<TravelSummary[]>(`${environment.apiUrl}/recommendations`);
  }

  getTrending(): Observable<TravelSummary[]> {
    return this.http.get<TravelSummary[]>(`${environment.apiUrl}/recommendations/trending`);
  }
}
