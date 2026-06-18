import api from './api'

export const squadService = {
  buscar(simulacaoId, projetoId) {
    return api.get(`/simulacoes/${simulacaoId}/projetos/${projetoId}/squad`)
  },

  salvar(simulacaoId, projetoId, data) {
    return api.put(`/simulacoes/${simulacaoId}/projetos/${projetoId}/squad`, data)
  }
}
