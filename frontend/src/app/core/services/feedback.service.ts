import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { FeedbackResponse, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private http = inject(HttpClient);

  submit(travelId: string, rating: number, comment: string): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(`${environment.apiUrl}/feedback`, { travelId, rating, comment });
  }

  getTravelFeedback(travelId: string, page = 0, size = 10): Observable<PageResponse<FeedbackResponse>> {
    return this.http.get<PageResponse<FeedbackResponse>>(
      `${environment.apiUrl}/travels/${travelId}/feedback`,
      { params: new HttpParams().set('page', page).set('size', size) }
    );
  }

  getManagerFeedback(page = 0): Observable<PageResponse<FeedbackResponse>> {
    return this.http.get<PageResponse<FeedbackResponse>>(
      `${environment.apiUrl}/manager/feedback`,
      { params: new HttpParams().set('page', page) }
    );
  }
}
