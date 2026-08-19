import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { authGuard, adminGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard / adminGuard', () => {
  let authService: AuthService;
  let router: Router;

  const runGuard = (guard: typeof authGuard | typeof adminGuard, url = '/bookings') =>
    TestBed.runInInjectionContext(() => guard({} as any, { url } as any));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  it('authGuard allows navigation when the user is logged in', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);

    expect(runGuard(authGuard)).toBeTrue();
  });

  it('authGuard redirects to /login with returnUrl when logged out', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);

    const result = runGuard(authGuard, '/bookings/mine') as UrlTree;

    expect(result instanceof UrlTree || result === false).toBeTruthy();
    expect(router.serializeUrl(result as UrlTree)).toContain('/login');
    expect(router.serializeUrl(result as UrlTree)).toContain('returnUrl');
  });

  it('adminGuard allows navigation for admins', () => {
    spyOn(authService, 'isAdmin').and.returnValue(true);

    expect(runGuard(adminGuard)).toBeTrue();
  });

  it('adminGuard redirects non-admins to home', () => {
    spyOn(authService, 'isAdmin').and.returnValue(false);

    const result = runGuard(adminGuard) as UrlTree;

    expect(router.serializeUrl(result as UrlTree)).toBe('/');
  });
});
