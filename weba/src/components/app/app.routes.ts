import { ModuleWithProviders } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { StartComponent } from '../start/start.component';
import { FinishComponent } from '../finish/finish.component';

const stockstatBuilderRoutes: Routes = [
  {
    path: 'stockstat',
    loadChildren: 'dist/components/builder/stockstat-builder.module#StockstatBuilderModule'
  }
];

export const routes: Routes = [
  { path: 'start', component: StartComponent },
  { path: 'finish', component: FinishComponent },
    ...stockstatBuilderRoutes,
  { path: '**', redirectTo: '/start' }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(routes);
