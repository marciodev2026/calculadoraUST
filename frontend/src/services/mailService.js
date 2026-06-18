import api from './api'

export const mailService = {
  obterConfig() {
    return api.get('/mail/config')
  }
}
