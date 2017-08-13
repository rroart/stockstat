import { Injectable } from '@angular/core';

import { MyConfig } from '../../../services/config';
import { StockstatService } from "../../../services/stockstat-service";

@Injectable()
export class StockstatBuilderService {
    buildingConfig: MyConfig;
    
    constructor(public stockstatService:StockstatService){    				console.log("here1");
}

    startBuildingExisting(name: string){
    				console.log("here0");
            return this.stockstatService.getConfig()
    }
}
