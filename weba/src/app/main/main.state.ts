import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';
import { AppState } from '@app/core';

import { mainReducer } from './main.reducer';
//import { todosReducer } from './todos/todos.reducer';
//import { TodosState } from './todos/todos.model';
//import { stockMarketReducer } from './stock-market/stock-market.reducer';
//import { StockMarketState } from './stock-market/stock-market.model';
//import { bookReducer } from './crud/books.reducer';
//import { formReducer } from './form/form.reducer';
//import { FormState } from './form/form.model';
//import { Book, BookState } from './crud/books.model';

export const FEATURE_NAME = 'main';
export const selectMain = createFeatureSelector<State, MainState>(
  FEATURE_NAME
);

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
