import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/travels/travel-list/travel-list.component')
      .then(m => m.TravelListComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component')
      .then(m => m.RegisterComponent),
  },
  {
    path: 'travels/:id',
    loadComponent: () => import('./features/travels/travel-detail/travel-detail.component')
      .then(m => m.TravelDetailComponent),
  },
  {
    path: 'search',
    loadComponent: () => import('./features/search/search.component')
      .then(m => m.SearchComponent),
  },
  {
    path: 'recommendations',
    loadComponent: () => import('./features/recommendations/recommendations.component')
      .then(m => m.RecommendationsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/traveler-profile/traveler-profile.component')
      .then(m => m.TravelerProfileComponent),
    canActivate: [authGuard],
  },
  {
    path: 'subscriptions',
    loadComponent: () => import('./features/subscriptions/subscription-list/subscription-list.component')
      .then(m => m.SubscriptionListComponent),
    canActivate: [authGuard],
  },
  {
    path: 'managers/:id',
    loadComponent: () => import('./features/managers/manager-public-profile/manager-public-profile.component')
      .then(m => m.ManagerPublicProfileComponent),
  },
  {
    path: 'manager',
    canActivate: [roleGuard],
    data: { roles: ['MANAGER', 'ADMIN'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/manager/manager-dashboard/manager-dashboard.component')
          .then(m => m.ManagerDashboardComponent),
      },
      {
        path: 'travels',
        loadComponent: () => import('./features/manager/manager-travels/manager-travels.component')
          .then(m => m.ManagerTravelsComponent),
      },
      {
        path: 'travels/create',
        loadComponent: () => import('./features/travels/travel-create/travel-create.component')
          .then(m => m.TravelCreateComponent),
      },
      {
        path: 'travels/:id/edit',
        loadComponent: () => import('./features/travels/travel-edit/travel-edit.component')
          .then(m => m.TravelEditComponent),
      },
      {
        path: 'travels/:travelId/subscribers',
        loadComponent: () => import('./features/manager/subscriber-list/subscriber-list.component')
          .then(m => m.SubscriberListComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component')
          .then(m => m.AdminDashboardComponent),
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/admin/admin-reports/admin-reports.component')
          .then(m => m.AdminReportsComponent),
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/admin-users/admin-users.component')
          .then(m => m.AdminUsersComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: '' },
];
