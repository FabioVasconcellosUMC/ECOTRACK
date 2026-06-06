<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">

    <section class="relative rounded-2xl overflow-hidden helmet-stripe border border-bg-line-strong">
      <div
        class="absolute inset-0 bg-cover pointer-events-none"
        style="background-image: url('/photos/empresas-bg.jpg'); background-position: center 35%;"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: linear-gradient(105deg,
          rgba(7, 24, 30, 0.96) 0%,
          rgba(7, 24, 30, 0.86) 35%,
          rgba(7, 24, 30, 0.78) 65%,
          rgba(7, 24, 30, 0.88) 100%);"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: radial-gradient(700px 500px at 12% 50%, rgba(0, 76, 84, 0.32), transparent 60%);"
      />
      <div class="absolute inset-0 wing-pattern opacity-25 pointer-events-none" />

      <div class="relative flex items-end justify-between flex-wrap gap-4 p-7">
        <div>
          <p class="eyebrow-italic text-cyan mb-2">Cadastro de empresas</p>
          <h1 class="display-title text-[48px] leading-[0.98]">Empresas</h1>
          <p class="text-ink-3 text-[13px] mt-2 mono-tag">
            {{ totalEmpresasReal }} empresas · {{ contagensPorTipo.GERADORA }} geradoras ·
            {{ contagensPorTipo.TRANSPORTADORA }} transportadoras ·
            {{ contagensPorTipo.RECEPTORA }} receptoras
            <span v-if="totalEmpresasReal > empresas.length"> · {{ empresas.length }} carregadas</span>
          </p>
        </div>

        <div class="flex items-center gap-2">
          <button
            @click="exportarTodas"
            class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[12.5px] font-bold tracking-[0.08em] hover:border-cyan/40 hover:text-cyan transition-colors"
          >
            <Download :size="15" /> EXPORTAR
          </button>
          <button
            @click="abrirModalCadastro"
            class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base text-[12.5px] font-bold tracking-[0.08em] hover:bg-cyan/90 transition-colors"
          >
            <Plus :size="16" /> NOVA EMPRESA
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-2xl bg-bg-panel border border-bg-line-strong flex flex-col helmet-stripe overflow-hidden">
      <div class="px-5 pt-5 pb-4 border-b border-bg-line flex flex-wrap items-center gap-4 justify-between">
        <div class="flex items-center gap-2 px-3 h-10 rounded-md bg-bg-base border border-bg-line focus-within:border-cyan/40 transition-colors flex-1 min-w-[260px] max-w-[420px]">
          <Search :size="14" class="text-ink-3" />
          <input
            v-model="termoBusca"
            type="text"
            placeholder="Buscar por razão social..."
            class="flex-1 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4"
          />
        </div>

        <div class="flex flex-wrap gap-1.5">
          <button
            v-for="filtro in FILTROS"
            :key="filtro.valor"
            @click="filtroAtivo = filtro.valor"
            class="px-3 py-1 rounded-full text-[11px] font-semibold border tracking-wide transition-colors"
            :class="filtroAtivo === filtro.valor
              ? 'bg-cyan/10 border-cyan/40 text-cyan'
              : 'bg-bg-elevated border-bg-line text-ink-2 hover:border-bg-line-strong'"
          >
            {{ filtro.rotulo }} ·
            <span class="mono-tag text-[10px]">{{ contadorParaFiltro(filtro.valor) }}</span>
          </button>
        </div>
      </div>

      <div class="flex-1">
        <div v-if="empresasFiltradas.length === 0" class="px-6 py-16 text-center">
          <Building2 :size="32" class="mx-auto text-ink-4 mb-3" />
          <p class="eyebrow">Nenhuma empresa</p>
          <p class="text-ink-3 text-[12px] mt-2">Ajuste os filtros ou cadastre uma nova.</p>
        </div>

        <button
          v-for="(empresa, indice) in empresasFiltradas"
          :key="chavePublica(empresa)"
          @click="abrirDetalhes(empresa)"
          class="group w-full flex items-center gap-3 px-5 py-4 border-b border-bg-line text-left transition-colors relative cursor-pointer hover:bg-bg-elevated/60"
        >
          <span class="mono-tag text-ink-4 text-[11px] w-6 shrink-0">
            {{ formatarIndice(indice) }}
          </span>

          <div
            class="w-10 h-10 rounded-md flex items-center justify-center text-[12px] font-bold shrink-0"
            :style="estiloAvatar(empresa.tipo)"
          >
            {{ iniciais(empresa.razaoSocial) }}
          </div>

          <div class="flex-1 min-w-0">
            <p class="text-[13.5px] font-semibold text-ink truncate">{{ empresa.razaoSocial }}</p>
            <p class="mono-tag text-ink-3 text-[10.5px] truncate mt-0.5">{{ empresa.cnpj }}</p>
          </div>

          <div class="hidden md:flex flex-col items-end min-w-0 max-w-[260px]">
            <p class="text-[12px] text-ink-2 truncate w-full text-right">{{ empresa.email || '—' }}</p>
            <p class="mono-tag text-ink-3 text-[10.5px] truncate w-full text-right mt-0.5">{{ empresa.telefone || '—' }}</p>
          </div>

          <span
            class="px-2 py-0.5 rounded text-[10px] font-bold tracking-wider shrink-0"
            :style="estiloChip(empresa.tipo)"
          >
            {{ rotuloTipoCurto(empresa.tipo) }}
          </span>

          <ChevronRight :size="16" class="text-ink-4 group-hover:text-cyan transition-colors shrink-0" />
        </button>
      </div>
    </section>

    <div
      v-if="temMaisEmpresas || carregandoMais"
      class="flex items-center justify-center"
    >
      <button
        @click="carregarMaisEmpresas"
        :disabled="carregandoMais"
        class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[11.5px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors disabled:opacity-50"
      >
        <Loader2 v-if="carregandoMais" :size="14" class="animate-spin" />
        <Plus v-else :size="14" />
        {{ carregandoMais ? 'CARREGANDO...' : 'CARREGAR MAIS EMPRESAS' }}
      </button>
    </div>

    <Transition name="page">
      <div
        v-if="empresaSelecionada"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="fecharDetalhes"
      >
        <div class="w-full max-w-2xl rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-start gap-5">
            <div
              class="w-16 h-16 rounded-xl flex items-center justify-center text-[20px] font-bold shrink-0"
              :style="estiloAvatar(empresaSelecionada.tipo)"
            >
              {{ iniciais(empresaSelecionada.razaoSocial) }}
            </div>

            <div class="flex-1 min-w-0">
              <p class="eyebrow" :style="{ color: corDoTipo(empresaSelecionada.tipo) }">
                {{ empresaSelecionada.tipo }}
              </p>
              <h2 class="section-title text-[28px] mt-1.5">{{ empresaSelecionada.razaoSocial }}</h2>
              <p class="mono-tag text-ink-3 text-[11px] mt-2">CNPJ {{ empresaSelecionada.cnpj }}</p>
            </div>

            <button
              @click="fecharDetalhes"
              class="shrink-0 w-9 h-9 rounded-md flex items-center justify-center text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="grid grid-cols-2 gap-4 mt-7">
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">E-mail</p>
              <p class="text-[13px] text-ink mt-2 break-words">{{ empresaSelecionada.email || '—' }}</p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Telefone</p>
              <p class="text-[13px] text-ink mt-2">{{ empresaSelecionada.telefone || '—' }}</p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4 col-span-2">
              <p class="eyebrow">Endereço</p>
              <p class="text-[13px] text-ink mt-2 break-words">{{ empresaSelecionada.endereco || '—' }}</p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Cadastrado em</p>
              <p class="mono-tag text-[13px] text-ink mt-2">{{ formatarData(empresaSelecionada.criadoEm) }}</p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Identificador</p>
              <p class="mono-tag text-cyan text-[13px] mt-2">#{{ formatarIdentificador(empresaSelecionada) }}</p>
            </div>
          </div>

          <div class="mt-7 pt-5 border-t border-bg-line flex items-center justify-between gap-4">
            <span
              class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10.5px] font-bold tracking-wider"
              :class="empresaSelecionada.ativa
                ? 'bg-success-soft border border-success/30 text-success'
                : 'bg-danger-soft border border-danger/30 text-danger'"
            >
              <span
                class="w-1.5 h-1.5 rounded-full"
                :class="empresaSelecionada.ativa ? 'bg-success' : 'bg-danger'"
              />
              {{ empresaSelecionada.ativa ? 'ATIVA' : 'INATIVA' }}
            </span>

            <button
              @click="exportarSelecionada"
              class="shrink-0 flex items-center gap-2 h-9 px-3 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[11px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
            >
              <Download :size="14" /> EXPORTAR EMPRESA
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="page">
      <div
        v-if="modalCadastroAberto"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="fecharModalCadastro"
      >
        <div class="w-full max-w-xl rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-center justify-between mb-6">
            <div>
              <p class="eyebrow text-cyan">Novo registro</p>
              <h2 class="section-title text-[28px] mt-2">Cadastrar empresa</h2>
            </div>
            <button
              @click="fecharModalCadastro"
              class="w-9 h-9 rounded-md flex items-center justify-center text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="space-y-4">
            <FormField
              v-model="formulario.cnpj"
              label="CNPJ"
              placeholder="00.000.000/0000-00"
              mask="cnpj"
              :erro="erros.cnpj"
              @update:modelValue="limparErro('cnpj')"
              @blur="validarCampo('cnpj')"
            >
              <template #prefix>
                <Hash :size="14" class="text-ink-3" />
              </template>
              <template #suffix>
                <button
                  @click="buscarDadosPorCnpj"
                  :disabled="buscandoCnpj"
                  class="flex items-center gap-1.5 px-3.5 h-10 rounded-md bg-cyan/10 border border-cyan/30 text-cyan text-[11.5px] font-bold tracking-wider hover:bg-cyan/20 transition-colors disabled:opacity-50"
                >
                  <Loader2 v-if="buscandoCnpj" :size="13" class="animate-spin" />
                  <Search v-else :size="13" />
                  BUSCAR
                </button>
              </template>
            </FormField>

            <FormField
              v-model="formulario.razaoSocial"
              label="Razão social"
              placeholder="Razão social da empresa"
              :erro="erros.razaoSocial"
              @update:modelValue="limparErro('razaoSocial')"
              @blur="validarCampo('razaoSocial')"
            />

            <div>
              <label class="eyebrow block mb-1.5">Tipo</label>
              <div v-if="usuarioAdmin" class="grid grid-cols-3 gap-1.5">
                <button
                  v-for="opcao in TIPOS_CADASTRO"
                  :key="opcao.valor"
                  @click="formulario.tipo = opcao.valor"
                  class="px-3 py-2.5 rounded-md text-[11.5px] font-bold tracking-wider border transition-colors"
                  :class="formulario.tipo === opcao.valor
                    ? 'border-cyan/40 text-cyan bg-cyan/10'
                    : 'bg-bg-elevated border-bg-line text-ink-2 hover:border-bg-line-strong'"
                >
                  {{ opcao.rotulo }}
                </button>
              </div>
              <div
                v-else
                class="px-3 py-2.5 rounded-md text-[11.5px] font-bold tracking-wider border border-cyan/40 text-cyan bg-cyan/10 text-center"
              >
                {{ rotuloTipoCadastro(formulario.tipo) }}
              </div>
            </div>

            <FormField
              v-model="formulario.email"
              label="E-mail"
              placeholder="contato@empresa.com"
              type="email"
              :erro="erros.email"
              @update:modelValue="limparErro('email')"
              @blur="validarCampo('email')"
            />

            <FormField
              v-model="formulario.telefone"
              label="Telefone"
              placeholder="(00) 00000-0000"
              mask="telefone"
              :erro="erros.telefone"
              @update:modelValue="limparErro('telefone')"
              @blur="validarCampo('telefone')"
            />

            <FormField
              v-model="formulario.endereco"
              label="Endereço"
              placeholder="Rua, número, cidade — UF"
              :erro="erros.endereco"
              @update:modelValue="limparErro('endereco')"
              @blur="validarCampo('endereco')"
            />
          </div>

          <p
            v-if="mensagemErro"
            class="flex items-center gap-2 text-[12px] text-danger bg-danger-soft border border-danger/30 rounded-md px-3 py-2 mt-4"
          >
            <AlertCircle :size="14" /> {{ mensagemErro }}
          </p>

          <div class="flex justify-end gap-2 mt-6 pt-5 border-t border-bg-line">
            <button
              @click="fecharModalCadastro"
              class="px-4 h-10 rounded-md text-[11.5px] font-bold tracking-wider bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong"
            >
              CANCELAR
            </button>
            <button
              @click="salvarEmpresa"
              :disabled="salvando"
              class="flex items-center gap-2 px-5 h-10 rounded-md text-[11.5px] font-bold tracking-wider bg-cyan text-bg-base hover:bg-cyan/90 transition-colors disabled:opacity-50"
            >
              <Loader2 v-if="salvando" :size="13" class="animate-spin" />
              <Check v-else :size="13" />
              {{ salvando ? 'SALVANDO...' : 'SALVAR REGISTRO' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Search, Plus, Building2, Hash, X, Check,
  AlertCircle, Loader2, Download, ChevronRight,
} from 'lucide-vue-next'
import api from '../services/api'
import { buscarComCache, invalidarCacheDados } from '../services/dataCache'
import FormField from '../components/ui/FormField.vue'
import { exportCsv } from '../utils/exportCsv'
import { removerMascara } from '../composables/useMascara'
import {
  validarObrigatorio, validarTamanhoMinimo, validarEmail, validarTelefone, validarCnpj,
} from '../composables/useValidacao'

const TIPOS = {
  GERADORA:       'GERADORA',
  TRANSPORTADORA: 'TRANSPORTADORA',
  RECEPTORA:      'RECEPTORA',
}

const FILTROS = [
  { valor: 'TODOS',               rotulo: 'Todas' },
  { valor: TIPOS.GERADORA,        rotulo: 'Geradoras' },
  { valor: TIPOS.TRANSPORTADORA,  rotulo: 'Transportadoras' },
  { valor: TIPOS.RECEPTORA,       rotulo: 'Receptoras' },
]

const TIPOS_CADASTRO = [
  { valor: TIPOS.GERADORA,       rotulo: 'GERADORA' },
  { valor: TIPOS.TRANSPORTADORA, rotulo: 'TRANSPORT.' },
  { valor: TIPOS.RECEPTORA,      rotulo: 'RECEPTORA' },
]

const CORES_POR_TIPO = {
  [TIPOS.GERADORA]:       '#38BDF8',
  [TIPOS.TRANSPORTADORA]: '#F59E0B',
  [TIPOS.RECEPTORA]:      '#10B981',
}

const ROTULOS_CURTOS = {
  [TIPOS.GERADORA]:       'GER',
  [TIPOS.TRANSPORTADORA]: 'TRANS',
  [TIPOS.RECEPTORA]:      'RECEP',
}

const COR_PADRAO = '#A5ACAF'
const TAMANHO_ID = 4
const TAMANHO_INDICE = 2
const CNPJ_DIGITOS = 14
const LIMITE_LISTAGEM = 20
const ATRASO_BUSCA_MS = 350
let timeoutBusca = null

const perfilUsuario = computed(() =>
  (localStorage.getItem('perfil') || '').toUpperCase(),
)

const usuarioAdmin = computed(() => perfilUsuario.value === 'ADMIN')

const tipoPermitidoParaPerfil = (perfil) => ({
  GERADORA:       TIPOS.GERADORA,
  TRANSPORTADORA: TIPOS.TRANSPORTADORA,
  RECEPTORA:      TIPOS.RECEPTORA,
}[perfil] || TIPOS.GERADORA)

const formularioVazio = () => ({
  cnpj: '',
  razaoSocial: '',
  tipo: tipoPermitidoParaPerfil(perfilUsuario.value),
  email: '',
  telefone: '',
  endereco: '',
})

const errosVazio = () => ({
  cnpj: '',
  razaoSocial: '',
  email: '',
  telefone: '',
  endereco: '',
})

const empresas = ref([])
const totalEmpresasReal = ref(0)
const totaisPorTipoReal = ref({
  [TIPOS.GERADORA]: 0,
  [TIPOS.TRANSPORTADORA]: 0,
  [TIPOS.RECEPTORA]: 0,
})
const paginaAtual = ref(0)
const temMaisEmpresas = ref(false)
const carregandoMais = ref(false)
const empresaSelecionada = ref(null)
const modalCadastroAberto = ref(false)
const buscandoCnpj = ref(false)
const salvando = ref(false)
const mensagemErro = ref('')
const termoBusca = ref('')
const filtroAtivo = ref('TODOS')
const formulario = ref(formularioVazio())
const erros = ref(errosVazio())

const validarCampo = (campo) => {
  const valor = formulario.value[campo]
  if (campo === 'cnpj')        erros.value.cnpj        = validarCnpj(valor)
  if (campo === 'razaoSocial') erros.value.razaoSocial = validarTamanhoMinimo(valor, 3, 'Razão social')
  if (campo === 'email')       erros.value.email       = validarEmail(valor)
  if (campo === 'telefone')    erros.value.telefone    = validarTelefone(valor)
  if (campo === 'endereco')    erros.value.endereco    = validarObrigatorio(valor, 'Endereço')
}

const limparErro = (campo) => { erros.value[campo] = '' }

const validarFormularioCompleto = () => {
  validarCampo('cnpj')
  validarCampo('razaoSocial')
  validarCampo('email')
  validarCampo('telefone')
  validarCampo('endereco')
  return Object.values(erros.value).every(mensagem => mensagem === '')
}

const contagensPorTipo = computed(() => {
  if (totalEmpresasReal.value > 0 || empresas.value.length === 0) {
    return totaisPorTipoReal.value
  }

  return empresas.value.reduce((contagem, empresa) => {
    if (contagem[empresa.tipo] !== undefined) contagem[empresa.tipo] += 1
    return contagem
  }, { [TIPOS.GERADORA]: 0, [TIPOS.TRANSPORTADORA]: 0, [TIPOS.RECEPTORA]: 0 })
})

const empresasFiltradas = computed(() => {
  let lista = empresas.value
  if (filtroAtivo.value !== 'TODOS') {
    lista = lista.filter(empresa => empresa.tipo === filtroAtivo.value)
  }
  const termo = termoBusca.value.trim().toLowerCase()
  if (termo) {
    const termoDigitos = removerMascara(termo)
    lista = lista.filter(empresa => {
      const razaoSocial = (empresa.razaoSocial || '').toLowerCase()
      const email       = (empresa.email       || '').toLowerCase()
      const cnpjDigitos = removerMascara(empresa.cnpj || '')

      if (razaoSocial.includes(termo) || email.includes(termo)) return true
      if (termoDigitos && cnpjDigitos.includes(termoDigitos))   return true
      return false
    })
  }
  return lista
})

const contadorParaFiltro = (valor) =>
  valor === 'TODOS' ? totalEmpresasReal.value : (contagensPorTipo.value[valor] || 0)

const corDoTipo = (tipo) => CORES_POR_TIPO[tipo] || COR_PADRAO

const estiloAvatar = (tipo) => {
  const cor = corDoTipo(tipo)
  return { backgroundColor: `${cor}1A`, border: `1px solid ${cor}40`, color: cor }
}

const estiloChip = (tipo) => {
  const cor = corDoTipo(tipo)
  return { backgroundColor: `${cor}1A`, color: cor, border: `1px solid ${cor}30` }
}

const rotuloTipoCurto = (tipo) => ROTULOS_CURTOS[tipo] || tipo
const rotuloTipoCadastro = (tipo) =>
  TIPOS_CADASTRO.find(opcao => opcao.valor === tipo)?.rotulo || tipo

const iniciais = (texto) =>
  (texto || 'EM')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(palavra => palavra[0])
    .join('')
    .toUpperCase()

const formatarData = (data) =>
  data
    ? new Date(data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
    : '—'

const chavePublica = (registro) => registro?.publicId || registro?.id || ''
const formatarIdentificador = (registro) => {
  const valor = chavePublica(registro)
  if (!valor) return ''.padStart(TAMANHO_ID, '0')
  if (typeof valor === 'string' && valor.includes('-')) return valor.slice(0, 8).toUpperCase()
  return String(valor).padStart(TAMANHO_ID, '0')
}
const formatarIndice = (indice) => String(indice + 1).padStart(TAMANHO_INDICE, '0')

const empresaParaCsv = (empresa) => ({
  id: chavePublica(empresa),
  razaoSocial: empresa.razaoSocial || '',
  cnpj: empresa.cnpj || '',
  tipo: empresa.tipo || '',
  email: empresa.email || '',
  telefone: empresa.telefone || '',
  endereco: empresa.endereco || '',
  status: empresa.ativa ? 'ATIVA' : 'INATIVA',
})

const exportarTodas = () => {
  const base = empresasFiltradas.value.length ? empresasFiltradas.value : empresas.value
  exportCsv('ecotrack-empresas.csv', base.map(empresaParaCsv))
}

const exportarSelecionada = () => {
  if (!empresaSelecionada.value) return
  exportCsv(
    `ecotrack-empresa-${chavePublica(empresaSelecionada.value)}.csv`,
    [empresaParaCsv(empresaSelecionada.value)],
  )
}

const normalizarPaginaEmpresas = (resposta) => {
  if (Array.isArray(resposta)) {
    return {
      itens: resposta,
      hasNext: false,
      page: 0,
      total: resposta.length,
      totalGeradoras: resposta.filter(empresa => empresa.tipo === TIPOS.GERADORA).length,
      totalTransportadoras: resposta.filter(empresa => empresa.tipo === TIPOS.TRANSPORTADORA).length,
      totalReceptoras: resposta.filter(empresa => empresa.tipo === TIPOS.RECEPTORA).length,
    }
  }

  return {
    itens: resposta?.itens || [],
    hasNext: Boolean(resposta?.hasNext),
    page: Number(resposta?.page) || 0,
    total: Number(resposta?.total) || 0,
    totalGeradoras: Number(resposta?.totalGeradoras) || 0,
    totalTransportadoras: Number(resposta?.totalTransportadoras) || 0,
    totalReceptoras: Number(resposta?.totalReceptoras) || 0,
  }
}

const atualizarResumoEmpresas = (pagina) => {
  totalEmpresasReal.value = pagina.total
  totaisPorTipoReal.value = {
    [TIPOS.GERADORA]: pagina.totalGeradoras,
    [TIPOS.TRANSPORTADORA]: pagina.totalTransportadoras,
    [TIPOS.RECEPTORA]: pagina.totalReceptoras,
  }
}

const carregarEmpresas = async ({ acrescentar = false } = {}) => {
  const pagina = acrescentar ? paginaAtual.value + 1 : 0
  if (acrescentar) carregandoMais.value = true

  try {
    const resposta = await buscarComCache('/empresas', {
      limit: LIMITE_LISTAGEM,
      page: pagina,
      q: termoBusca.value.trim() || undefined,
    })
    const paginaEmpresas = normalizarPaginaEmpresas(resposta)
    empresas.value = acrescentar
      ? [...empresas.value, ...paginaEmpresas.itens]
      : paginaEmpresas.itens
    paginaAtual.value = paginaEmpresas.page
    temMaisEmpresas.value = paginaEmpresas.hasNext
    atualizarResumoEmpresas(paginaEmpresas)
  } catch (erro) {
    console.error('Erro ao carregar empresas:', erro)
  } finally {
    carregandoMais.value = false
  }
}

const carregarMaisEmpresas = () => carregarEmpresas({ acrescentar: true })

const abrirDetalhes = (empresa) => { empresaSelecionada.value = empresa }
const fecharDetalhes = () => { empresaSelecionada.value = null }

const buscarDadosPorCnpj = async () => {
  if (buscandoCnpj.value) return
  buscandoCnpj.value = true
  mensagemErro.value = ''
  try {
    const cnpjLimpo = removerMascara(formulario.value.cnpj)
    if (cnpjLimpo.length !== CNPJ_DIGITOS) {
      mensagemErro.value = 'CNPJ deve ter 14 dígitos.'
      return
    }
    const resposta = await fetch(`https://brasilapi.com.br/api/cnpj/v1/${cnpjLimpo}`)
    if (!resposta.ok) throw new Error('CNPJ não encontrado')
    const dados = await resposta.json()
    formulario.value.razaoSocial = dados.razao_social || ''
    formulario.value.endereco = [dados.logradouro, dados.numero, dados.municipio, dados.uf]
      .filter(Boolean)
      .join(', ')
  } catch {
    mensagemErro.value = 'CNPJ não encontrado na Receita Federal.'
  } finally {
    buscandoCnpj.value = false
  }
}

const abrirModalCadastro = () => {
  modalCadastroAberto.value = true
  mensagemErro.value = ''
  formulario.value = formularioVazio()
  erros.value = errosVazio()
}

const fecharModalCadastro = () => { modalCadastroAberto.value = false }

const salvarEmpresa = async () => {
  if (salvando.value) return
  if (!validarFormularioCompleto()) {
    mensagemErro.value = 'Corrija os campos destacados antes de salvar.'
    return
  }
  salvando.value = true
  mensagemErro.value = ''
  try {
    await api.post('/empresas', formulario.value)
    invalidarCacheDados('/empresas', '/lotes', '/transportes')
    fecharModalCadastro()
    await carregarEmpresas()
  } catch (erro) {
    mensagemErro.value = erro.mensagemAmigavel || 'Erro ao salvar empresa. Verifique os dados.'
  } finally {
    salvando.value = false
  }
}

watch(termoBusca, () => {
  clearTimeout(timeoutBusca)
  paginaAtual.value = 0
  temMaisEmpresas.value = false
  timeoutBusca = setTimeout(carregarEmpresas, ATRASO_BUSCA_MS)
})

onMounted(carregarEmpresas)
</script>
