import { Routes } from '@angular/router';
import { RegisterPage } from '../features/auth/register/components/register.page';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'register',
        pathMatch: 'full'
    },
    {
        path: 'register',
        component: RegisterPage
    }
];
