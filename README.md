# EcoTrack 🌿 — Gestão e Rastreabilidade de Resíduos

Sistema web responsivo para gestão e rastreabilidade de resíduos sólidos, desenvolvido em conformidade com a **Política Nacional de Resíduos Sólidos (PNRS — Lei nº 12.305/2010)**.

## 🚀 Links de Acesso (Ambiente de Produção)

- **Frontend (Vercel):** [https://ecotrack-khaki.vercel.app](https://ecotrack-khaki.vercel.app)
- **Backend API (Render):** [https://ecotrack-d5i0.onrender.com](https://ecotrack-d5i0.onrender.com)
    - *Nota: A raiz da API possui um Health Check para validação de status.*

## 👤 Credenciais de Acesso

| Perfil | Email | Senha |
| :--- | :--- | :--- |
| **Administrador** | `admin@ecotrack.com` | `admin123` |

## 🛠️ Tecnologias & Padrões

- **Backend:** Java 17, Spring Boot 3, Spring Security, JWT (Autenticação Stateless).
- **Frontend:** Vue.js 3, Tailwind CSS, Axios.
- **Banco de Dados:** PostgreSQL (Instância Supabase).
- **Infra:** Render (Backend CI/CD) + Vercel (Frontend CI/CD).

## 📋 Funcionalidades Validadas (Entrega 06/04)

- ✅ **Autenticação JWT:** Login seguro com geração de token e proteção de rotas.
- ✅ **Dashboard Gerencial:** Visualização de indicadores integrada à API.
- ✅ **Gestão de Lotes:** CRUD completo e transição de status de resíduos.
- ✅ **Rastreabilidade Auditada:** Registro imutável de movimentações e histórico.

## 🕹️ Documentação da API (Endpoints para Teste)

Para validar o Back-end via **Postman/Insomnia**, siga o fluxo abaixo:

### 1. Autenticação (Pública)
- **POST** `https://ecotrack-d5i0.onrender.com/auth/login`
    - **Body (JSON):** `{"email": "admin@ecotrack.com", "senha": "admin123"}`

### 2. Gestão de Lotes (Requer Header `Authorization: Bearer <TOKEN>`)
- **Criar Lote:** `POST` `https://ecotrack-d5i0.onrender.com/lotes`
- **Buscar por ID:** `GET` `https://ecotrack-d5i0.onrender.com/lotes/1`
- **Alterar Status:** `PATCH` `https://ecotrack-d5i0.onrender.com/lotes/1/status?novoStatus=EM_TRANSITO&observacao=Lote coletado 
- pela transportadora`
- **Histórico do Lote:** `GET` `https://ecotrack-d5i0.onrender.com/lotes/1/historico`

## 🗃️ Banco de Dados

O banco de dados PostgreSQL está integrado e ativo no **Supabase**.
- **Script de População:** Para verificar a modelagem ou subir um ambiente local, utilize o arquivo em `scripts/dados_exemplo.sql`.

## 👥 Equipe & Responsabilidades

- **Fabio Vasconcellos** — Back-end (API, Segurança JWT, Módulo de Lotes e Infra).
- **Micael Humberto** — Front-end (Vue.js, Dashboard, Interfaces e Integração).

## 📁 Estrutura de Branches

- `Codigo-Inicial` — **Branch principal da entrega.**
- `frontend-deploy` — Configurações de ambiente Vercel.

---
