import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MainComponent } from '../main/main.component';

const routes: Routes = [
  {
    path: 'main',
    component: MainComponent,
    data: { title: 'anms.menu.main' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StaticRoutingModule {}
