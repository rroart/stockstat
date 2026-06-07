import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SettingsContainerComponent } from './settings';
import { MainComponent } from './main';
import { OAuth2CallbackComponent } from './shared/components/oauth2-callback.component';

const routes: Routes = [
  {
    path: 'callback',
    component: OAuth2CallbackComponent,
    data: { title: 'OAuth2 Callback' }
  },
  {
    path: '',
    //redirectTo: 'main',
    component: MainComponent,
    data: { title: 'anms.menu.main' },
    pathMatch: 'full'
  },
  {
    path: 'settings',
    component: SettingsContainerComponent,
    data: { title: 'anms.menu.settings' }
  },
  {
    path: '**',
    redirectTo: 'main'
  }
];

@NgModule({
  // useHash supports github.io demo page, remove in your app
  imports: [
    RouterModule.forRoot(routes, {
      useHash: true,
      scrollPositionRestoration: 'enabled'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
