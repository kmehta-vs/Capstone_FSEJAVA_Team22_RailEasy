import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

describe('jwtInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('attaches the Authorization header when a token is present', () => {
    spyOnProperty(authService, 'token', 'get').and.returnValue('jwt-token');

    httpClient.get(`${environment.apiBaseUrl}/trains`).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/trains`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    req.flush([]);
  });

  it('does not attach an Authorization header when there is no token', () => {
    spyOnProperty(authService, 'token', 'get').and.returnValue(null);

    httpClient.get(`${environment.apiBaseUrl}/trains`).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/trains`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });
});
