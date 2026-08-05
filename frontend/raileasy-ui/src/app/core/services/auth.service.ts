import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from '../models/api-models';

const TOKEN_KEY = 'raileasy.token';
const USER_KEY = 'raileasy.user';

/**
 * Holds the authenticated session (JWT + user) using signals, and persists it
 * to localStorage so a page refresh keeps the user logged in.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly _user = signal<AuthUser | null>(this.readUser());
  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  /** Current user (null when logged out). */
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly isAdmin = computed(() => this._user()?.isAdmin === true);

  get token(): string | null {
    return this._token();
  }

  /** POST /api/auth/register — creates a passenger account. */
  register(body: RegisterRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.baseUrl}/register`, body);
  }

  /** POST /api/auth/login — stores the JWT + user on success. */
  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, body)
      .pipe(tap((res) => this.setSession(res)));
  }

  /** Clears the session (also calls the backend for symmetry). */
  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession(),
    });
  }

  private setSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.user));
    this._token.set(res.token);
    this._user.set(res.user);
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._token.set(null);
    this._user.set(null);
  }

  private readUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  }
}
