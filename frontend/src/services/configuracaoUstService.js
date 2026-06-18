import api from './api'

export const configuracaoUstService = {
  buscarAtiva() {
    return api.get('/configuracoes/ust')
  },

  listarHistorico() {
    return api.get('/configuracoes/ust/historico')
  },

  salvar(data) {
    return api.put('/configuracoes/ust', data)
  }
}
