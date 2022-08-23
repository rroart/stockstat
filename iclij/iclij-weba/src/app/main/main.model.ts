import { AppState } from '@app/core';

export type Language = 'en';

export interface MainState {
  count: number;
  tabs: any;
  startdate: string;
  enddate: string;
  market: string;
  markets: Array<string>;
  tasks: Array<string>;
  config: any;
}

export interface State extends AppState {
  main: MainState;
}
