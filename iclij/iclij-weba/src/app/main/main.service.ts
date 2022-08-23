import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Http, Response, Headers, RequestOptions } from '@angular/http';

import { isDevMode } from '@angular/core';

import { environment as myenv } from '@env/myenvironment';

@Injectable()
export class MainService {
  constructor(private httpClient: HttpClient) {}

  retrieve(url: string, config: any): Observable<any> {
    //let headers = new Headers({ 'Content-Type': 'application/json' });
    //let options = new RequestOptions({ headers: headers });
    let param = config;
    console.log(
      'http://' + MainService.getIHost() + ':' + MainService.getIPort() + url
    );
    return this.httpClient.post(
      'http://' + MainService.getIHost() + ':' + MainService.getIPort() + url,
      param
    );
    //.pipe(
    //map((res: Response) => { res.json() } ));
    //.catch(MainService.handleError)
    //;
  }

  retrieve0(url: string, config: any): Observable<any> {
    //let headers = new Headers({ 'Content-Type': 'application/json' });
    //let options = new RequestOptions({ headers: headers });
    let param = config;
    console.log(
      'http://' + MainService.getHost() + ':' + MainService.getPort() + url
    );
    return this.httpClient.post(
      'http://' + MainService.getHost() + ':' + MainService.getPort() + url,
      param
    );
    //.pipe(
    //map((res: Response) => { res.json() } ));
    //.catch(MainService.handleError)
    //;
  }

  retrieve2(url: string, config: any): Observable<any> {
    //let headers = new Headers({ 'Content-Type': 'application/json' });
    //let options = new RequestOptions({ headers: headers });
    let param = config;
    console.log(
      'http://' + MainService.getAHost() + ':' + MainService.getAPort() + url
    );
    return this.httpClient.post(
      'http://' + MainService.getAHost() + ':' + MainService.getAPort() + url,
      param
    );
    //.pipe(
    //map((res: Response) => { res.json() } ));
    //.catch(MainService.handleError)
    //;
  }

  static handleError(error: Response) {
    console.log('Error ' + error);
    return Observable.throw(error || 'Server error');
  }

  static getPort() {
    if (typeof myenv.MYPORT !== 'undefined' && myenv.MYPORT !== '') {
      return myenv.MYPORT;
    }
    return 80;
  }

  static getHost() {
    console.log('pppp');
    console.log(myenv);
    if (typeof myenv.MYSERVER !== 'undefined' && myenv.MYSERVER !== '') {
      return myenv.MYSERVER;
    }
    return 'localhost';
  }

  static getIPort() {
    if (typeof myenv.MYIPORT !== 'undefined' && myenv.MYIPORT !== '') {
      return myenv.MYIPORT;
    }
    return 80;
  }

  static getIHost() {
    console.log('pppp');
    console.log(myenv);
    if (typeof myenv.MYISERVER !== 'undefined' && myenv.MYISERVER !== '') {
      return myenv.MYISERVER;
    }
    return 'localhost';
  }
  
  static getAPort() {
    if (typeof myenv.MYAPORT !== 'undefined' && myenv.MYAPORT !== '') {
      return myenv.MYAPORT;
    }
    return 80;
  }

  static getAHost() {
    console.log('pppp');
    console.log(myenv);
    if (typeof myenv.MYASERVER !== 'undefined' && myenv.MYASERVER !== '') {
      return myenv.MYASERVER;
    }
    return 'localhost';
  }
}
