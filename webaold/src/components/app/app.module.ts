import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';

import { StockstatAppComponent }  from './app.component';
import { HeaderComponent } from './header.component';
import { MarketComponent } from "../builder/stockstat/stockstat.component";
import { ConfigTreeComponent } from "../builder/configtree/configtree.component";
import { TreeViewComponent } from "../builder/treeview/treeview.component";

import { StartModule } from '../start/start.module';
import { FinishModule } from '../finish/finish.module';
import { ServicesModule } from '../../services/services.module';

import { ModalModule } from 'angular2-modal';
import { BootstrapModalModule } from 'angular2-modal/plugins/bootstrap';

import {routing} from './app.routes';

@NgModule({
  imports: [
    BrowserModule,
    HttpModule,
    StartModule,
    FinishModule,
    routing,
    ModalModule.forRoot(),
    BootstrapModalModule,
    ServicesModule],
  declarations: [
    StockstatAppComponent,
    HeaderComponent,
    ],
  bootstrap: [StockstatAppComponent]
})
export class AppModule { }
