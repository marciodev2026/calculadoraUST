import api from './api'

export const projetoService = {
  listar(simulacaoId) {
    return api.get(`/simulacoes/${simulacaoId}/projetos`)
  },

  buscar(simulacaoId, id) {
    return api.get(`/simulacoes/${simulacaoId}/projetos/${id}`)
  },

  criar(simulacaoId, data) {
    return api.post(`/simulacoes/${simulacaoId}/projetos`, data)
  },

  atualizar(simulacaoId, id, data) {
    return api.put(`/simulacoes/${simulacaoId}/projetos/${id}`, data)
  },

  excluir(simulacaoId, id) {
    return api.delete(`/simulacoes/${simulacaoId}/projetos/${id}`)
  }
}
