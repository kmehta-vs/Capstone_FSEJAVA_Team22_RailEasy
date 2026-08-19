import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';

import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthUser } from '../models/api-models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockUser: AuthUser = {
    id: 'u1',
    email: 'rider@raileasy.com',
    name: 'Rider',
    isAdmin: false,
  };

  const mockAuthResponse: AuthResponse = {
    token: 'jwt-token',
    user: mockUser,
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created with no session initially', () => {
    expect(service).toBeTruthy();
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.token).toBeNull();
  });

  it('register() should POST to /auth/register', () => {
    service.register({ email: 'rider@raileasy.com', password: 'Passw0rd', name: 'Rider' }).subscribe((res) => {
      expect(res).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('login() should store the token and user on success', () => {
    service.login({ email: 'rider@raileasy.com', password: 'Passw0rd' }).subscribe((res) => {
      expect(res).toEqual(mockAuthResponse);
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);

    expect(service.token).toBe('jwt-token');
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.user()?.email).toBe('rider@raileasy.com');
    expect(localStorage.getItem('raileasy.token')).toBe('jwt-token');
  });

  it('isAdmin computed should reflect the logged-in user role', () => {
    const adminResponse: AuthResponse = {
      token: 'admin-token',
      user: { ...mockUser, isAdmin: true },
    };

    service.login({ email: 'admin@raileasy.com', password: 'Admin@123' }).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    req.flush(adminResponse);

    expect(service.isAdmin()).toBeTrue();
  });

  it('logout() should clear the session and navigate home', () => {
    service.login({ email: 'rider@raileasy.com', password: 'Passw0rd' }).subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`).flush(mockAuthResponse);

    const navigateSpy = spyOn(router, 'navigate');

    service.logout();
    const logoutReq = httpMock.expectOne(`${environment.apiBaseUrl}/auth/logout`);
    logoutReq.flush({});

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.token).toBeNull();
    expect(localStorage.getItem('raileasy.token')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('logout() should still clear the session even if the backend call errors', () => {
    service.login({ email: 'rider@raileasy.com', password: 'Passw0rd' }).subscribe();
    httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`).flush(mockAuthResponse);

    service.logout();
    const logoutReq = httpMock.expectOne(`${environment.apiBaseUrl}/auth/logout`);
    logoutReq.error(new ProgressEvent('error'));

    expect(service.isLoggedIn()).toBeFalse();
  });
});
