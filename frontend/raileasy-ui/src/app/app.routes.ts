import { Routes } from '@angular/router';

import { Search } from './features/search/search';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { SeatSelection } from './features/booking/seat-selection';
import { BookingConfirmation } from './features/booking/booking-confirmation';
import { MyTickets } from './features/booking/my-tickets';
import { Admin } from './features/admin/admin';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: Search, title: 'RailEasy — Search trains' },
  { path: 'login', component: Login, title: 'RailEasy — Sign in' },
  { path: 'register', component: Register, title: 'RailEasy — Create account' },
  {
    path: 'book/:scheduleId',
    component: SeatSelection,
    title: 'RailEasy — Select seats',
    canActivate: [authGuard],
  },
  {
    path: 'booking-confirmation',
    component: BookingConfirmation,
    title: 'RailEasy — Booking confirmed',
    canActivate: [authGuard],
  },
  {
    path: 'my-tickets',
    component: MyTickets,
    title: 'RailEasy — My tickets',
    canActivate: [authGuard],
  },
  {
    path: 'admin',
    component: Admin,
    title: 'RailEasy — Admin',
    canActivate: [adminGuard],
  },
  { path: '**', redirectTo: '' },
];
