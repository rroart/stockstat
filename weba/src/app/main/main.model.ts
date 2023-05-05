import { AppState } from '@app/core';

export type Language = 'en';

export interface MainState {
  count: number;
  tabs: any;
  startdate: string;
  enddate: string;
  market: string;
  market2: string;
  markets: Array<string>;
  tasks: Array<string>;
  config: any;
  config2: any;
}

export interface State extends AppState {
  main: MainState;
}
