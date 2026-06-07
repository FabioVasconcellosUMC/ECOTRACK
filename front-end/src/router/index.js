import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layouts/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import EmpresasView from '../views/EmpresasView.vue'
import LotesView from '../views/LotesView.vue'
import TransportesView from '../views/TransportesView.vue'
import RelatoriosView from '../views/RelatoriosView.vue'

const PERFIS_OPERACIONAIS = ['ADMIN', 'GERADORA', 'TRANSPORTADORA', 'RECEPTORA']

const tokenExpirado = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp && payload.exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

const limparSessao = () => {
  localStorage.clear()
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'login', component: LoginView },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { allowedRoles: PERFIS_OPERACIONAIS } },
        { path: 'empresas', name: 'empresas', component: EmpresasView, meta: { allowedRoles: PERFIS_OPERACIONAIS } },
        { path: 'lotes', name: 'lotes', component: LotesView, meta: { allowedRoles: PERFIS_OPERACIONAIS } },
        { path: 'transportes', name: 'transportes', component: TransportesView, meta: { allowedRoles: PERFIS_OPERACIONAIS } },
        { path: 'relatorios', name: 'relatorios', component: RelatoriosView, meta: { allowedRoles: PERFIS_OPERACIONAIS } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const perfil = (localStorage.getItem('perfil') || '').toUpperCase()

  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }

  if (to.meta.requiresAuth && tokenExpirado(token)) {
    limparSessao()
    return { name: 'login' }
  }

  if (to.meta.allowedRoles && !to.meta.allowedRoles.includes(perfil)) {
    return { name: 'dashboard' }
  }

  if (to.name === 'login' && token) {
    if (tokenExpirado(token)) {
      limparSessao()
      return
    }
    return { name: 'dashboard' }
  }
})

export default router
