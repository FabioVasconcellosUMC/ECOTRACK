import axios from 'axios'

const api = axios.create({
  baseURL: 'https://ecotrack-d5i0.onrender.com',
})

// Injeta o token JWT em todas as requisições
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Trata erros globais de autenticação e permissão
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      // Token expirado ou inválido — desloga e manda pro login
      localStorage.clear()
      window.location.href = '/'
    }

    if (status === 403) {
      // Perfil sem permissão para essa ação
      error.mensagemAmigavel = 'Seu perfil não tem permissão para realizar essa ação.'
    }

    return Promise.reject(error)
  }
)

export default api