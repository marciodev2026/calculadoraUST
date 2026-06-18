import api from './api'

export const dashboardService = {
  obterIndicadores() {
    return api.get('/dashboard')
  }
}
