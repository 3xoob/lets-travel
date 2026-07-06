import { createAction, props } from '@ngrx/store';
import { AuthResponse, UserDto } from '../../core/models';

export const login = createAction('[Auth] Login', props<{ email: string; password: string }>());
export const loginSuccess = createAction('[Auth] Login Success', props<{ response: AuthResponse }>());
export const loginFailure = createAction('[Auth] Login Failure', props<{ error: string }>());

export const register = createAction('[Auth] Register',
  props<{ email: string; password: string; firstName: string; lastName: string }>());
export const registerSuccess = createAction('[Auth] Register Success', props<{ response: AuthResponse }>());
export const registerFailure = createAction('[Auth] Register Failure', props<{ error: string }>());

export const logout = createAction('[Auth] Logout');
export const setUser = createAction('[Auth] Set User', props<{ user: UserDto }>());
export const clearError = createAction('[Auth] Clear Error');
