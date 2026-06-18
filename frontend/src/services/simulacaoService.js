import api from './api'

export const simulacaoService = {
  listar() {
    return api.get('/simulacoes')
  },

  buscar(id) {
    return api.get(`/simulacoes/${id}`)
  },

  criar(data) {
    return api.post('/simulacoes', data)
  },

  atualizar(id, data) {
    return api.put(`/simulacoes/${id}`, data)
  },

  excluir(id) {
    return api.delete(`/simulacoes/${id}`)
  }
}
