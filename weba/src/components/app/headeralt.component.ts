import {Component} from '@angular/core';
import {Router, Event } from '@angular/router';

@Component({
  selector: 'headeralt',
  template: `<div class="navbar-header">
                <h1>Stock Statistics Go</h1>
             </div>`
})
export class HeaderComponentAlt {
  private subscription: any;
  constructor(private router: Router) {}
}
