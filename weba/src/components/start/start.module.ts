import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { FormsModule }   from '@angular/forms';

import { StartComponent }  from './start.component';
import { MarketComponent } from "../builder/stockstat/stockstat.component";
import { ConfigTreeComponent } from "../builder/configtree/configtree.component";
import { TreeViewComponent } from "../builder/treeview/treeview.component";

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        RouterModule
],
    declarations: [StartComponent ],
    exports: [StartComponent ],
})
export class StartModule { }
