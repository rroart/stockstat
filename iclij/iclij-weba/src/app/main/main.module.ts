import { NgModule } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { SharedModule } from '@app/shared';
import { environment } from '@env/environment';

import { FEATURE_NAME } from './main.state';
import { mainReducer } from './main.reducer';
import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main.component';
//import { TodosContainerComponent } from './todos/components/todos-container.component';
//import { TodosEffects } from './todos/todos.effects';
//import { StockMarketContainerComponent } from './stock-market/components/stock-market-container.component';
import { MainEffects } from './main.effects';
import { MainService } from './main.service';
//import { ParentComponent } from './theming/parent/parent.component';
//import { ChildComponent } from './theming/child/child.component';
//import { CrudComponent } from './crud/components/crud.component';
//import { BooksEffects } from './crud/books.effects';
//import { FormComponent } from './form/components/form.component';
//import { FormEffects } from './form/form.effects';
//import { AuthenticatedComponent } from './authenticated/authenticated.component';
//import { NotificationsComponent } from './notifications/components/notifications.component';

import { MarketComponent } from './market/market.component';
import { MarketbarComponent } from './market/marketbar.component';
import { EvolvebarComponent } from './market/evolvebar.component';
import { ConfigurationComponent } from './configuration/configuration.component';
import { ConfigtreeComponent } from './configuration/configtree.component';
import { TreeviewComponent } from './configuration/treeview.component';
import { ControlPanelComponent } from './controlpanel/controlpanel.component';
import { TaskListComponent } from './controlpanel/tasklist.component';
import { MytableComponent } from './table/mytable.component';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';

@NgModule({
  imports: [
    MatSortModule,
    MatTableModule,
    SharedModule,
    MainRoutingModule,
    StoreModule.forFeature(FEATURE_NAME, mainReducer),
    EffectsModule.forFeature([
      MainEffects
    ])
  ],
  declarations: [
    MainComponent,
    MarketComponent,
    MarketbarComponent,
    EvolvebarComponent,
    ConfigurationComponent,
    ConfigtreeComponent,
    TreeviewComponent,
    ControlPanelComponent,
    TaskListComponent,
    MytableComponent,
    /*
    TodosContainerComponent,
    StockMarketContainerComponent,
    ParentComponent,
    ChildComponent,
    AuthenticatedComponent,
    CrudComponent,
    FormComponent,
    NotificationsComponent
    */
  ],
  providers: [MainService]
})
export class MainModule {
  constructor() {}
}
