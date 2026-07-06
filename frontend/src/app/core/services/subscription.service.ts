import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { SubscriptionResponse, PaymentInitResponse, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/subscriptions`;

  subscribe(travelId: string, paymentMethod: string): Observable<PaymentInitResponse> {
    return this.http.post<PaymentInitResponse>(this.base, { travelId, paymentMethod });
  }

  unsubscribe(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getMySubscriptions(page = 0, size = 10): Observable<PageResponse<SubscriptionResponse>> {
    return this.http.get<PageResponse<SubscriptionResponse>>(
      `${this.base}/my`,
      { params: new HttpParams().set('page', page).set('size', size) }
    );
  }

  getTravelSubscribers(travelId: string, page = 0): Observable<PageResponse<SubscriptionResponse>> {
    return this.http.get<PageResponse<SubscriptionResponse>>(
      `${environment.apiUrl}/manager/travels/${travelId}/subscribers`,
      { params: new HttpParams().set('page', page) }
    );
  }

  managerUnsubscribe(subscriptionId: string, reason: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/manager/subscriptions/${subscriptionId}`,
      { body: { reason } }
    );
  }
}
