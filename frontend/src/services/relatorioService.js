import api from './api'

const blobConfig = { responseType: 'blob' }

export const relatorioService = {
  listar(simulacaoId) {
    return api.get(`/simulacoes/${simulacaoId}/relatorios`)
  },

  carregarPreview(simulacaoId) {
    return api.get(`/simulacoes/${simulacaoId}/relatorios/preview`)
  },

  gerarPdf(simulacaoId) {
    return api.post(`/simulacoes/${simulacaoId}/relatorios/pdf`, null, blobConfig)
  },

  gerarExcel(simulacaoId) {
    return api.post(`/simulacoes/${simulacaoId}/relatorios/excel`, null, blobConfig)
  },

  download(simulacaoId, relatorioId) {
    return api.get(`/simulacoes/${simulacaoId}/relatorios/${relatorioId}/download`, blobConfig)
  },

  listarEnvios(simulacaoId) {
    return api.get(`/simulacoes/${simulacaoId}/relatorios/envios`)
  },

  enviarEmail(simulacaoId, payload) {
    return api.post(`/simulacoes/${simulacaoId}/relatorios/email`, payload)
  }
}
