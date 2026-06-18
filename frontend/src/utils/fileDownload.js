export async function parseApiError(error) {
  const data = error.response?.data

  if (data instanceof Blob) {
    try {
      const text = await data.text()
      const json = JSON.parse(text)
      return json.detalhe || json.titulo || 'Não foi possível concluir a operação'
    } catch {
      return 'Não foi possível concluir a operação'
    }
  }

  if (data && typeof data === 'object') {
    return data.detalhe || data.titulo || 'Não foi possível concluir a operação'
  }

  return error.message || 'Não foi possível concluir a operação'
}

export function extractFilename(contentDisposition, fallback) {
  if (!contentDisposition) {
    return fallback
  }

  const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1].trim())
  }

  const asciiMatch = /filename="?([^";]+)"?/i.exec(contentDisposition)
  return asciiMatch?.[1]?.trim() || fallback
}

export function saveBlob(blob, filename, mimeType) {
  const file = blob instanceof Blob
    ? blob
    : new Blob([blob], { type: mimeType || 'application/octet-stream' })

  if (!file.size) {
    throw new Error('Arquivo vazio ou indisponível')
  }

  const url = window.URL.createObjectURL(file)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.setTimeout(() => window.URL.revokeObjectURL(url), 2000)
}

export function salvarRespostaArquivo(response, fallbackFilename, fallbackMimeType) {
  const filename = extractFilename(response.headers['content-disposition'], fallbackFilename)
  const mimeType = response.headers['content-type'] || fallbackMimeType
  saveBlob(response.data, filename, mimeType)
}
