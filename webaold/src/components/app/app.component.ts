import {Component, ViewContainerRef} from '@angular/core';
import { Overlay } from 'angular2-modal';

@Component({
  selector: 'stockstat-app',
  template: `<div>
                <div>
                  <header></header>
                </div>
             </div>
             <div>
                <router-outlet></router-outlet>
             </div>`
})
export class StockstatAppComponent {
  constructor(overlay: Overlay, viewContainer: ViewContainerRef) {
    overlay.defaultViewContainer = viewContainer;
  }
}
