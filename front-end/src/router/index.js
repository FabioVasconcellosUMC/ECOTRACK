import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layouts/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import EmpresasView from '../views/EmpresasView.vue'
import LotesView from '../views/LotesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardView,
        },
        {
          path: 'empresas',
          name: 'empresas',
          component: EmpresasView,
        },
        {
          path: 'lotes',
          name: 'lotes',
          component: LotesView,
        }
      ]
    }
  ],
})

export default router