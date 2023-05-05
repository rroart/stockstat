import { HttpClient, HttpHeaders, HttpResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AppService {
    constructor(protected http: HttpClient) { }
    getDeliveryStatus() {
    			const url = ""; //Client.geturl("/gettasks");
         return this.http.post(url, { observe: 'response' });
      }

}