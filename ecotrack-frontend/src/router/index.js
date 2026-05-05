import { createRouter, createWebHistory } from 'vue-router'
import LoginView       from '../views/LoginView.vue'
import MainLayout      from '../layouts/MainLayout.vue'
import DashboardView   from '../views/DashboardView.vue'
import EmpresasView    from '../views/EmpresasView.vue'
import LotesView       from '../views/LotesView.vue'
import TransportesView from '../views/TransportesView.vue'
import RelatoriosView  from '../views/RelatoriosView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'login', component: LoginView },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        { path: 'dashboard',   name: 'dashboard',   component: DashboardView   },
        { path: 'empresas',    name: 'empresas',    component: EmpresasView    },
        { path: 'lotes',       name: 'lotes',       component: LotesView       },
        { path: 'transportes', name: 'transportes', component: TransportesView },
        { path: 'relatorios',  name: 'relatorios',  component: RelatoriosView  },
      ],
    },
  ],
})

// Guarda de navegação — redireciona para login se não houver token
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }
  // Se já estiver logado e tentar acessar o login, vai pro dashboard
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
})

export default router
