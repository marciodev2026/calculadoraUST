import api from './api'

export const usuarioService = {
  listar() {
    return api.get('/usuarios')
  },

  criar(data) {
    return api.post('/usuarios', data)
  }
}
