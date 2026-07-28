import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  fromStation = '';
  toStation = '';
  journeyDate = '';

  constructor(private router: Router) {}

  onSearch(): void {
    if (this.fromStation && this.toStation && this.journeyDate) {
      this.router.navigate(['/search'], {
        queryParams: {
          from: this.fromStation,
          to: this.toStation,
          date: this.journeyDate
        }
      });
    }
  }
}
