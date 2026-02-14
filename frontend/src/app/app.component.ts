import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, User, Session, RestaurantChoice } from './api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  // Available users from backend
  users: User[] = [];
  
  // Current session
  currentSession: Session | null = null;
  
  // Form fields
  selectedUser: string = '';
  restaurantName: string = '';
  submitterName: string = '';
  sessionIdInput: string = '';
  
  // UI state
  error: string = '';
  success: string = '';
  loading: boolean = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadUsers();
  }

  // Load all available users
  loadUsers() {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        if (users.length > 0) {
          this.selectedUser = users[0].username;
        }
      },
      error: (err) => this.handleError('Failed to load users', err)
    });
  }

  // Create a new session
  createSession() {
    if (!this.selectedUser) {
      this.error = 'Please select a user';
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.apiService.createSession(this.selectedUser).subscribe({
      next: (session) => {
        this.currentSession = session;
        this.success = `Session created! ID: ${session.id}`;
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Failed to create session', err);
        this.loading = false;
      }
    });
  }

  // Join existing session
  joinSession() {
    if (!this.sessionIdInput) {
      this.error = 'Please enter a session ID';
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.apiService.getSession(this.sessionIdInput).subscribe({
      next: (session) => {
        this.currentSession = session;
        this.success = 'Joined session successfully!';
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Failed to join session', err);
        this.loading = false;
      }
    });
  }

  // Submit a restaurant
  submitRestaurant() {
    if (!this.currentSession) {
      this.error = 'No active session';
      return;
    }

    if (!this.restaurantName.trim()) {
      this.error = 'Please enter a restaurant name';
      return;
    }

    if (!this.submitterName.trim()) {
      this.error = 'Please enter your name';
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.apiService.submitRestaurant(
      this.currentSession.id,
      this.restaurantName,
      this.submitterName
    ).subscribe({
      next: () => {
        this.success = `Restaurant "${this.restaurantName}" submitted!`;
        this.restaurantName = '';
        this.refreshSession();
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Failed to submit restaurant', err);
        this.loading = false;
      }
    });
  }

  // Pick random restaurant
  pickRandom() {
    if (!this.currentSession) {
      this.error = 'No active session';
      return;
    }

    if (!this.submitterName.trim()) {
      this.error = 'Please enter your name';
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.apiService.pickRandom(this.currentSession.id, this.submitterName).subscribe({
      next: (result) => {
        this.success = `Winner: ${result.chosenRestaurant}!`;
        this.refreshSession();
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Failed to pick restaurant', err);
        this.loading = false;
      }
    });
  }

  // Refresh current session
  refreshSession() {
    if (!this.currentSession) return;

    this.apiService.getSession(this.currentSession.id).subscribe({
      next: (session) => {
        this.currentSession = session;
      },
      error: (err) => this.handleError('Failed to refresh session', err)
    });
  }

  // Leave current session
  leaveSession() {
    this.currentSession = null;
    this.restaurantName = '';
    this.submitterName = '';
    this.sessionIdInput = '';
    this.clearMessages();
  }

  // Helper methods
  private clearMessages() {
    this.error = '';
    this.success = '';
  }

  private handleError(message: string, err: any) {
    console.error(message, err);
    if (err.error && err.error.message) {
      this.error = err.error.message;
    } else {
      this.error = message;
    }
  }

  get isSessionClosed(): boolean {
    return this.currentSession?.status === 'CLOSED';
  }

  get canSubmit(): boolean {
    return !this.isSessionClosed && !!this.currentSession;
  }
}
