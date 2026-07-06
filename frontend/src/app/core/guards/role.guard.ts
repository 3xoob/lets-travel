import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const required: UserRole[] = route.data['roles'] ?? [];
  if (!auth.isLoggedIn) return router.createUrlTree(['/login']);
  const role = auth.currentUser?.role;
  if (!role) return router.createUrlTree(['/login']);
  const allowed = required.length === 0 || required.includes(role) ||
    (required.includes('MANAGER') && role === 'ADMIN');
  return allowed ? true : router.createUrlTree(['/']);
};
