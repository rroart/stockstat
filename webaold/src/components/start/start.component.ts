import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';

import { StockstatService } from "../../services/stockstat-service";
import { ServiceParam } from "../../services/serviceresult";
import { MarketComponent } from "../builder/stockstat/stockstat.component";

@Component({
  selector: 'start',
  templateUrl: '/src/components/start/start.html'
})

export class StartComponent implements OnInit, OnDestroy{
    public notFound:boolean = false;
    public searchTerm: string;
    private subscription:any;
    private subscription2:any;
    private stockstatResult:ServiceParam;
    private stockstatResult2:ServiceParam;
    
    constructor(private router:Router,
                private stockstatService:StockstatService) {
    }

    ngOnInit() {
    console.log("hi");
        this.subscription = this.stockstatService.getMarkets()
            .subscribe(
                stockstatResult => this.stockstatResult = stockstatResult,
                (err:any) => console.error(err)
            );
        this.subscription2 = this.stockstatService.getConfig()
            .subscribe(
                stockstatResult => this.stockstatResult2 = stockstatResult,
                (err:any) => console.error(err)
            );
    }

    onSelect() {
        console.log("hi2");
        this.router.navigate(['/stockstat']);
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
