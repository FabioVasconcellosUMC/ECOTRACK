import axios from 'axios'

const api = axios.create({
  baseURL: 'https://ecotrack-d5i0.onrender.com',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }

    if (status === 403) {
      error.mensagemAmigavel = 'Seu perfil não tem permissão para realizar essa ação.'
    }

    return Promise.reject(error)
  }
)

export default api