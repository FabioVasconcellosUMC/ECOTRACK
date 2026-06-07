import api from './api'

const TTL_PADRAO_MS = 90_000
const LIMITE_AQUECIMENTO = 20
const LIMITE_AUXILIAR = 100
const cache = new Map()

const chaveCache = (url, params = {}) =>
  `${url}?${JSON.stringify(params)}`

export const buscarComCache = async (url, params = {}, ttlMs = TTL_PADRAO_MS) => {
  const chave = chaveCache(url, params)
  const agora = Date.now()
  const entrada = cache.get(chave)

  if (entrada && agora - entrada.criadoEm < ttlMs) {
    return entrada.dados
  }

  if (entrada?.promise) {
    return entrada.promise
  }

  const promise = api.get(url, { params }).then((resposta) => {
    cache.set(chave, { dados: resposta.data, criadoEm: Date.now() })
    return resposta.data
  }).catch((erro) => {
    cache.delete(chave)
    throw erro
  })

  cache.set(chave, { promise, criadoEm: agora })
  return promise
}

export const invalidarCacheDados = (...prefixos) => {
  const alvos = prefixos.length ? prefixos : ['/empresas', '/lotes', '/transportes']
  for (const chave of cache.keys()) {
    if (alvos.some(prefixo => chave.startsWith(prefixo))) {
      cache.delete(chave)
    }
  }
}

export const aquecerCacheOperacional = () => {
  const consultas = [
    buscarComCache('/dashboard/resumo'),
    buscarComCache('/relatorios/resumo'),
    buscarComCache('/empresas', { limit: LIMITE_AQUECIMENTO, page: 0 }),
    buscarComCache('/lotes', { limit: LIMITE_AQUECIMENTO, page: 0 }),
    buscarComCache('/transportes', { limit: LIMITE_AQUECIMENTO, page: 0 }),
    buscarComCache('/empresas', { limit: LIMITE_AUXILIAR }),
    buscarComCache('/lotes', { limit: LIMITE_AUXILIAR }),
  ]

  Promise.allSettled(consultas).catch(() => {})
}
