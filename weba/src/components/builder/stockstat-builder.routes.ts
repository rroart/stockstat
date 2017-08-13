import { ModuleWithProviders } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MarketComponent}  from "./stockstat/stockstat.component";
import { ConfigTreeComponent } from "./configtree/configtree.component";
import { TreeViewComponent } from "./treeview/treeview.component";
import { MainPageComponent } from "./mainpage/mainpage.component";

export const stockstatBuilderRoutes: Routes = [
    {
        path: '',
        component: MainPageComponent,
//        component: MarketComponent,
//        children: [
//             {path:'', pathMatch: 'full', component: MarketComponent },
//        ]
    }
];

export const stockstatBuilderRouting: ModuleWithProviders = RouterModule.forChild(stockstatBuilderRoutes);
