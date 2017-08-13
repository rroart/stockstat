import {Component} from '@angular/core';
import {Router, Event } from '@angular/router';

@Component({
  selector: 'header',
  template: `<div class="navbar-header">
                <h1>Stock Statistics</h1>
             </div>`
})
export class HeaderComponent {
  private subscription: any;
  constructor(private router: Router) {}
}