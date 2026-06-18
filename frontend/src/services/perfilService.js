import api from './api'

export const perfilService = {
  listar(apenasAtivos = false) {
    return api.get('/perfis', { params: { apenasAtivos } })
  },

  buscar(id) {
    return api.get(`/perfis/${id}`)
  },

  criar(data) {
    return api.post('/perfis', data)
  },

  atualizar(id, data) {
    return api.put(`/perfis/${id}`, data)
  },

  excluir(id) {
    return api.delete(`/perfis/${id}`)
  }
}
