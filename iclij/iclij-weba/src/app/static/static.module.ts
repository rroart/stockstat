import { NgModule } from '@angular/core';

import { SharedModule } from '@app/shared';

import { StaticRoutingModule } from './static-routing.module';

@NgModule({
  imports: [SharedModule, StaticRoutingModule],
  declarations: []
})
export class StaticModule {}
