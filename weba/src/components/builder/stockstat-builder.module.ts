import { NgModule } from '@angular/core';
import { CommonModule} from '@angular/common';
import { FormsModule, ReactiveFormsModule }   from '@angular/forms';

import { StockstatBuilderComponent } from "./stockstat-builder.component";
import { MarketComponent } from "./stockstat/stockstat.component";
import { MainPageComponent } from "./mainpage/mainpage.component";
import { ConfigTreeComponent } from "./configtree/configtree.component";
import { TreeViewComponent } from "./treeview/treeview.component";
import { StockstatBuilderService } from "./builder-services/stockstat-builder-service";

import { stockstatBuilderRouting } from './stockstat-builder.routes';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        stockstatBuilderRouting
    ],
    declarations: [
    		  MainPageComponent,
    		  MarketComponent,
    		  ConfigTreeComponent,
    		  TreeViewComponent,
        StockstatBuilderComponent
    ],
    providers: [
        StockstatBuilderService
    ]
})
export class StockstatBuilderModule { }
