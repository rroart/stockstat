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
    console.log(`http://localhost:` + MainService.getPort() + url);
    return this.httpClient
      .post(`http://localhost:` + MainService.getPort() + url, param);
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
      console.log("devmode");
      console.log(isDevMode());
      if (!isDevMode()) {
        return 12345;
      } else {
        return 22345;
      }
    }
}
