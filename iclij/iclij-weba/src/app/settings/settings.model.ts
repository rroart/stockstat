import { AppState } from '@app/core';

export const NIGHT_MODE_THEME = 'BLACK-THEME';

export type Language = 'en';

export interface SettingsState {
  language: string;
  theme: string;
  autoNightMode: boolean;
  stickyHeader: boolean;
  pageAnimations: boolean;
  pageAnimationsDisabled: boolean;
  elementsAnimations: boolean;
}

export interface State extends AppState {
  settings: SettingsState;
}
