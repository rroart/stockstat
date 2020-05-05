import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {Http, Response, Headers, RequestOptions} from '@angular/http';

import { isDevMode } from '@angular/core';

//import { Stock } from './stock-market.model';

@Injectable()
export class MainService {
  constructor(private httpClient: HttpClient) {}

  retrieve(url: string, config: any): Observable<any> {
    //let headers = new Headers({ 'Content-Type': 'application/json' });
    //let options = new RequestOptions({ headers: headers }); 
    let param = config;
    console.log("http://" + MainService.getIHost() + ":" + MainService.getIPort() + url);
    return this.httpClient
      .post("http://" + MainService.getIHost() + ":" + MainService.getIPort() + url, param);
      //.pipe(
      //map((res: Response) => { res.json() } ));
      //.catch(MainService.handleError)
      //;
    }  

  retrieve0(url: string, config: any): Observable<any> {
    //let headers = new Headers({ 'Content-Type': 'application/json' });
    //let options = new RequestOptions({ headers: headers }); 
    let param = config;
    console.log("http://" + MainService.getHost() + ":" + MainService.getPort() + url);
    return this.httpClient
      .post("http://" + MainService.getHost() + ":" + MainService.getPort() + url, param);
      //.pipe(
      //map((res: Response) => { res.json() } ));
      //.catch(MainService.handleError)
      //;
    }  

    static handleError(error: Response) {
        console.log("Error " + error);
        return Observable.throw(error || 'Server error');
    }

static getPort() {
    console.log(process.env.NODE_ENV);
    if (typeof process.env.MYPORT !== 'undefined') {
        return process.env.MYPORT;
    }
    return 80;
}

static getHost() {
    console.log("pppp");
    console.log(process.env);
    if (typeof process.env.MYSERVER !== 'undefined') {
        return process.env.MYSERVER;
    }
    return "localhost";
}

static getIPort() {
    console.log(process.env.NODE_ENV);
    if (typeof process.env.MYIPORT !== 'undefined') {
        return process.env.MYIPORT;
    }
    return 80;
}

static getIHost() {
    console.log("pppp");
    console.log(process.env);
    if (typeof process.env.MYISERVER !== 'undefined') {
        return process.env.MYISERVER;
    }
    return "localhost";
}

}
