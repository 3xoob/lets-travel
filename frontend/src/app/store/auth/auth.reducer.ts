import { createReducer, on } from '@ngrx/store';
import { initialAuthState } from './auth.state';
import * as AuthActions from './auth.actions';

export const authReducer = createReducer(
  initialAuthState,
  on(AuthActions.login, AuthActions.register, state => ({ ...state, loading: true, error: null })),
  on(AuthActions.loginSuccess, AuthActions.registerSuccess, (state, { response }) => ({
    ...state, loading: false, user: response.user, error: null,
  })),
  on(AuthActions.loginFailure, AuthActions.registerFailure, (state, { error }) => ({
    ...state, loading: false, error,
  })),
  on(AuthActions.logout, () => ({ ...initialAuthState })),
  on(AuthActions.setUser, (state, { user }) => ({ ...state, user })),
  on(AuthActions.clearError, state => ({ ...state, error: null })),
);
