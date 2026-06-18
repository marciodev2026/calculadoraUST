import api from './api'

export const authService = {
  login(email, senha) {
    return api.post('/auth/login', { email, senha })
  },

  me() {
    return api.get('/auth/me')
  }
}
