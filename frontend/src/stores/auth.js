import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'

const TOKEN_KEY = 'ust_token'
const USER_KEY = 'ust_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || null)
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isGestor = computed(() => user.value?.role === 'GESTOR')
  const isAnalista = computed(() => user.value?.role === 'ANALISTA')
  const isConsulta = computed(() => user.value?.role === 'CONSULTA')
  const canWrite = computed(() => isAdmin.value || isGestor.value || isAnalista.value)
  const canManageUsers = computed(() => isAdmin.value || isGestor.value)

  function setSession(loginResponse) {
    token.value = loginResponse.token
    user.value = loginResponse.usuario
    localStorage.setItem(TOKEN_KEY, loginResponse.token)
    localStorage.setItem(USER_KEY, JSON.stringify(loginResponse.usuario))
  }

  function clearSession() {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(email, senha) {
    const { data } = await authService.login(email, senha)
    setSession(data)
    return data
  }

  async function fetchMe() {
    const { data } = await authService.me()
    user.value = data
    localStorage.setItem(USER_KEY, JSON.stringify(data))
    return data
  }

  function logout() {
    clearSession()
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    isGestor,
    isAnalista,
    isConsulta,
    canWrite,
    canManageUsers,
    login,
    fetchMe,
    logout,
    clearSession,
    setSession
  }
})
