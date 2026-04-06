# EcoTrack 🌿

Sistema web responsivo para gestão e rastreabilidade de resíduos sólidos, desenvolvido em conformidade com a Política Nacional de Resíduos Sólidos (PNRS — Lei nº 12.305/2010).

##  Acesso ao Sistema

- **Frontend:** https://ecotrack-khaki.vercel.app
- **Backend API:** https://ecotrack-d5i0.onrender.com

##  Credenciais de Acesso

| Email | Senha | Perfil |
|-------|-------|--------|
| admin@ecotrack.com | admin123 | Administrador |

##  Tecnologias

- **Backend:** Java 17, Spring Boot, Spring Security, JWT
- **Frontend:** Vue.js 3, Tailwind CSS, Axios
- **Banco de dados:** PostgreSQL (Supabase)
- **Deploy:** Render (backend) + Vercel (frontend)

##  Funcionalidades Implementadas

- ✅ Autenticação JWT (Login/Logout)
- ✅ Dashboard Gerencial
- ✅ Gestão de Empresas (Geradora, Transportadora, Receptora)
- ✅ Módulo de Lotes com ciclo de status completo
- ✅ Rastreabilidade auditada de movimentações
- ✅ Histórico imutável de alterações de status

## 🗃️ Banco de Dados

Para popular o banco com dados de exemplo, execute o script:
`scripts/dados_exemplo.sql` no Supabase SQL Editor.

## 👥 Equipe

- **Fabio Vasconcellos** — Back-end (API, autenticação, módulo de lotes)
- **Micael Humberto** — Front-end (Vue.js, Dashboard, interfaces)

## 📁 Branches

- `Codigo-Inicial` — Branch principal da entrega
- `frontend-deploy` — Branch do deploy na Vercel
