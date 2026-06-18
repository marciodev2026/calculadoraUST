import api from './api'

export const configuracaoInstitucionalService = {
  buscar() {
    return api.get('/configuracoes/institucional')
  },

  salvar(data) {
    return api.put('/configuracoes/institucional', data)
  },

  uploadLogo(arquivo) {
    const formData = new FormData()
    formData.append('arquivo', arquivo)
    return api.post('/configuracoes/institucional/logo', formData)
  },

  removerLogo() {
    return api.delete('/configuracoes/institucional/logo')
  },

  obterLogo() {
    return api.get('/configuracoes/institucional/logo', { responseType: 'blob' })
  }
}
