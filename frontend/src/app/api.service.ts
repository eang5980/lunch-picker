import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  username: string;
}

export interface RestaurantChoice {
  id: number;
  restaurant: string;
  submittedBy: string;
}

export interface Session {
  id: string;
  createdBy: string;
  status: string;
  chosenRestaurant: string | null;
  createdAt: string;
  restaurants: RestaurantChoice[];
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // Get all users
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  // Create a new session
  createSession(username: string): Observable<Session> {
    return this.http.post<Session>(`${this.baseUrl}/sessions?user=${username}`, {});
  }

  // Get session details
  getSession(sessionId: string): Observable<Session> {
    return this.http.get<Session>(`${this.baseUrl}/sessions/${sessionId}`);
  }

  // Submit a restaurant
  submitRestaurant(sessionId: string, restaurant: string, user: string): Observable<RestaurantChoice> {
    return this.http.post<RestaurantChoice>(
      `${this.baseUrl}/sessions/${sessionId}/restaurants`,
      { restaurant, user }
    );
  }

  // Pick random restaurant
  pickRandom(sessionId: string, user: string): Observable<{ chosenRestaurant: string }> {
    return this.http.post<{ chosenRestaurant: string }>(
      `${this.baseUrl}/sessions/${sessionId}/pick?user=${user}`,
      {}
    );
  }
}
