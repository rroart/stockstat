import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

import { selectIsAuthenticated } from './auth.selectors';
import { AppState } from '../core.state';
import { OAuth2Service } from './oauth2.service';

@Injectable()
export class AuthGuardService implements CanActivate {
  constructor(
    private store: Store<AppState>,
    private oauth2Service: OAuth2Service,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.store.pipe(
      select(selectIsAuthenticated),
      tap(isAuthenticated => {
        if (!isAuthenticated) {
          this.router.navigate(['/'], {
            queryParams: { returnUrl: state.url }
          });
        }
      }),
      map(isAuthenticated => isAuthenticated)
    );
  }

  /**
   * Check if the user is OAuth2 authenticated
   */
  isOAuth2Authenticated(): boolean {
    return this.oauth2Service.isAuthenticated();
  }
}
