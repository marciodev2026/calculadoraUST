import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const AppLayout = () => import('@/components/layout/AppLayout.vue')
const LoginView = () => import('@/views/auth/LoginView.vue')
const DashboardView = () => import('@/views/dashboard/DashboardView.vue')
const SimulacaoListView = () => import('@/views/simulacoes/SimulacaoListView.vue')
const ProjetoListView = () => import('@/views/projetos/ProjetoListView.vue')
const SquadMontagemView = () => import('@/views/squads/SquadMontagemView.vue')
const ConfiguracaoUstView = () => import('@/views/admin/ConfiguracaoUstView.vue')
const PerfisProfissionaisView = () => import('@/views/admin/PerfisProfissionaisView.vue')
const UsuariosView = () => import('@/views/admin/UsuariosView.vue')
const RelatorioView = () => import('@/views/relatorios/RelatorioView.vue')

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true, title: 'Login' }
  },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: { title: 'Dashboard', icon: 'pi pi-chart-bar' }
      },
      {
        path: 'simulacoes',
        name: 'simulacoes',
        component: SimulacaoListView,
        meta: { title: 'Simulações', icon: 'pi pi-calculator' }
      },
      {
        path: 'simulacoes/:simulacaoId/projetos',
        name: 'simulacao-projetos',
        component: ProjetoListView,
        meta: { title: 'Projetos', icon: 'pi pi-briefcase' }
      },
      {
        path: 'simulacoes/:simulacaoId/projetos/:projetoId/squad',
        name: 'projeto-squad',
        component: SquadMontagemView,
        meta: { title: 'Squad', icon: 'pi pi-users' }
      },
      {
        path: 'relatorios',
        name: 'relatorios',
        component: RelatorioView,
        meta: { title: 'Relatórios', icon: 'pi pi-file-pdf' }
      },
      {
        path: 'usuarios',
        name: 'usuarios',
        component: UsuariosView,
        meta: { title: 'Usuários', icon: 'pi pi-user-edit', roles: ['ADMIN', 'GESTOR'] }
      },
      {
        path: 'admin/configuracao-ust',
        name: 'configuracao-ust',
        component: ConfiguracaoUstView,
        meta: { title: 'Configurações', icon: 'pi pi-cog', roles: ['ADMIN'] }
      },
      {
        path: 'admin/perfis',
        name: 'perfis',
        component: PerfisProfissionaisView,
        meta: { title: 'Perfis Profissionais', icon: 'pi pi-users', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (authStore.isAuthenticated && to.name === 'login') {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.fetchMe()
    } catch {
      authStore.clearSession()
      return { name: 'login' }
    }
  }

  const requiredRoles = to.meta.roles
  if (requiredRoles && !requiredRoles.includes(authStore.user?.role)) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
