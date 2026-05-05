# EcoTrack — Design System & Decisões

Sistema de rastreamento de resíduos sólidos em conformidade com a PNRS (Lei 12.305/2010).
Frontend Vue 3 + Tailwind v4. Visual de "centro de operações" institucional, não dashboard SaaS genérico.

---

## Filosofia visual

**Não é um SaaS, é um console operacional.**
A linguagem mistura tipografia técnica (mono) e display (Bebas Neue) para criar densidade institucional, com voz mais próxima de painel de torre de controle / terminal Bloomberg do que de Stripe ou Linear.

---

## Arquitetura de navegação

**Header de duas faixas (84px) substitui sidebar + topbar.**
Foi a decisão estrutural mais importante: 5 rotas não justificam um sidebar fixo, e horizontal libera 100% da largura da viewport pro conteúdo operacional — onde realmente importa.

### Faixa 1 — Utility (28px)

Voz de console técnico. Tipografia mono, densa.

```
v1.0 · PNRS Lei 12.305/2010 · PROD · <página>      SINCRONIZADO · API 42ms · 14:32:08 BRT · admin
```

Existe pra dar **personalidade institucional** e diferenciar o produto de um SaaS qualquer. Status sincronizado pulsa em verde (live), latência da API simula telemetria, hora atualiza em tempo real.

### Faixa 2 — Operacional (56px)

```
[🍃 EcoTrack]  |  Dashboard  Empresas  Lotes  Transportes  Relatórios  |  🔔  Operador  [O]
                                            ────────
                                            magic-line
```

- Logo: ícone Lucide Leaf (folha original) com gradiente brand → ciano
- Nav: ícone Lucide + label, magic-line ciana animada no item ativo
- Notificações: sininho com dot ciano
- Usuário: nome + avatar com inicial. Click no avatar = logout

---

## Detalhes de capricho

### 1. Magic-line (HeaderNav.vue)

A linha ciana sob o item ativo desliza fluidamente entre tabs. Implementação:
- Cada `<RouterLink>` se registra via callback `:ref`
- `getBoundingClientRect()` mede posição relativa ao container nav
- `watch(route.path)` + `nextTick()` recalcula posição ao trocar de rota
- `ResizeObserver` recalcula em mudanças de viewport / fonte
- Transição CSS: `left 360ms cubic-bezier(0.65,0,0.35,1)`
- Hover preview: linha mais fina aparece sob o item em hover

### 2. Route sweep (MainLayout.vue + main.css)

Toda mudança de rota dispara uma linha ciana que atravessa o viewport logo abaixo do header (~720ms). Sinaliza "trocando de canal" no centro de operações.

Implementação: classe `.is-sweeping` é toggleada via JS no `watch(route.path)`. Reflow forçado (`void el.offsetWidth`) reinicia animação CSS.

### 3. Page transition fluida

```css
.page-leave-active { position: absolute; transition: opacity 160ms; }
.page-enter-active { transition: opacity 360ms, transform 420ms; }
.page-enter-from   { opacity: 0; transform: translateY(14px); }
```

Conteúdo antigo fica `position: absolute` durante a saída → não há "salto" no layout. Novo entra com fade + translateY discreto.

### 4. Helmet stripe

Hairline ciana de 2px no topo de painéis principais. Sutil, mas dá ritmo institucional consistente.

### 5. Wing pattern

Diagonais a 135° muito sutis (~3% opacity) sobre fotos.

---

## Tokens

### Surfaces — 6 níveis
```
bg-base        #04141A   ← body
bg-panel       #07181E   ← cards principais
bg-elevated    #0B2026   ← buttons, hover states
bg-raised      #102932   ← raised elements
bg-line        #173640   ← borders normais
bg-line-strong #1F4751   ← borders enfáticos
```

### Brand
```
brand-deep   #00343A
brand        #004C54
brand-bright #006D78
```

### Acentos
- **cyan** `#2DD4BF` — primary action, active, live
- **silver** `#A5ACAF` — neutral metadata
- **amber** `#F59E0B` — aguardando
- **success** `#10B981` — descartado
- **info** `#38BDF8` — em trânsito
- **danger** `#DC2626` — cancelado, erro

### Texto — 4 níveis
```
ink     #F1F5F4   ← primário
ink-2   #B8C2C5   ← secundário
ink-3   #6E7779   ← terciário
ink-4   #3B464C   ← quaternário
```

### Tipografia
- **display**: Bebas Neue — números grandes, títulos hero
- **body**: Manrope — UI geral
- **mono**: JetBrains Mono — IDs, telemetria, datas

---

## Estrutura

```
src/
├── assets/main.css                ← tokens, animações, transitions, sweep
├── components/
│   ├── AppHeader.vue              ← header de duas faixas
│   ├── HeaderNav.vue              ← nav com magic-line
│   └── LogoMark.vue               ← Lucide Leaf wrapping
├── layouts/MainLayout.vue         ← header + sweep + RouterView
├── views/
│   ├── LoginView.vue
│   ├── DashboardView.vue          ← hero photo + chart + feed + meters
│   ├── EmpresasView.vue           ← master-detail
│   ├── LotesView.vue              ← kanban + tabela
│   ├── TransportesView.vue        ← stub
│   └── RelatoriosView.vue         ← stub
└── router/index.js
```

---

## Fotos

`public/photos/`:
- `login-bg.jpg` — sucata + floresta (login)
- `dashboard-bg.jpg` — rodovia + caminhões (hero KPI)
- `empresas-bg.jpg` — disponível pra uso futuro

Sempre com 4-5 layers de overlay pra legibilidade.

---

## API integração (preservada)

Backend Java/Spring em `https://ecotrack-d5i0.onrender.com`:
- `POST /auth/login`, `POST /auth/cadastro`
- `GET/POST /empresas`
- `GET /empresas/cnpj/:cnpj`
- `GET/POST /lotes` (POST espera `empresaGeradora: { id }`)

JWT salvo em `localStorage` como `token`, junto com `nome`, `perfil`, `email`.

---

## Como rodar

```bash
npm install
npm run dev
```

Login de teste: `admin@ecotrack.com` / `admin123`
