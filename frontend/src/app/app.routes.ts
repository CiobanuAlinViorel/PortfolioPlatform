import { Routes } from '@angular/router';
import { RegisterPage } from '../features/auth/register/components/register.page';
import { HomePage } from '../features/home/components/home.component';

export const routes: Routes = [
    {
        path: '',
        component: HomePage
    },
    {
        path: 'register',
        component: RegisterPage
    }
];
