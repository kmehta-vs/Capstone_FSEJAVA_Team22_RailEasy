import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { SearchResultsComponent } from './features/search/search-results/search-results.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'search', component: SearchResultsComponent },
  { path: '**', redirectTo: '' }
];
