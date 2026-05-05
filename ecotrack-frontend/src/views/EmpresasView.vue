<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">

    <header class="flex items-end justify-between flex-wrap gap-4">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Cadastro · Empresas parceiras</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Roster operacional
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ empresas.length }} empresas · {{ countByType.GERADORA }} geradoras ·
          {{ countByType.TRANSPORTADORA }} transportadoras · {{ countByType.RECEPTORA }} receptoras
        </p>
      </div>

      <button
        @click="abrirModal"
        class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base
               text-[12.5px] font-bold tracking-[0.08em] hover:bg-cyan/90 transition-colors"
      >
        <Plus :size="16" /> NOVA EMPRESA
      </button>
    </header>

    <section class="grid grid-cols-12 gap-5 min-h-[640px]">

      <div
        class="col-span-12 lg:col-span-5 rounded-2xl bg-bg-panel border border-bg-line-strong
               flex flex-col helmet-stripe overflow-hidden"
      >
        <div class="px-5 pt-5 pb-4 border-b border-bg-line">
          <div class="flex items-center gap-2 px-3 h-10 rounded-md bg-bg-base border border-bg-line
                      focus-within:border-cyan/40 transition-colors">
            <Search :size="14" class="text-ink-3" />
            <input
              v-model="busca"
              type="text"
              placeholder="Buscar por razão social, CNPJ ou e-mail..."
              class="flex-1 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4"
            />
          </div>

          <div class="flex flex-wrap gap-1.5 mt-3">
            <button
              v-for="f in filtros"
              :key="f.value"
              @click="filtroTipo = f.value"
              class="px-3 py-1 rounded-full text-[11px] font-semibold border tracking-wide transition-colors"
              :class="filtroTipo === f.value
                ? 'bg-cyan/10 border-cyan/40 text-cyan'
                : 'bg-bg-elevated border-bg-line text-ink-2 hover:border-bg-line-strong'"
            >
              {{ f.label }} ·
              <span class="mono-tag text-[10px]">
                {{ f.value === 'TODOS' ? empresas.length : countByType[f.value] || 0 }}
              </span>
            </button>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto">
          <div v-if="empresasFiltradas.length === 0" class="px-6 py-16 text-center">
            <Building2 :size="32" class="mx-auto text-ink-4 mb-3" />
            <p class="eyebrow">Nenhuma empresa</p>
            <p class="text-ink-3 text-[12px] mt-2">Ajuste os filtros ou cadastre uma nova.</p>
          </div>

          <button
            v-for="(e, i) in empresasFiltradas"
            :key="e.id"
            @click="selecionada = e"
            class="group w-full flex items-center gap-3 px-5 py-3 border-b border-bg-line
                   text-left transition-colors relative"
            :class="selecionada?.id === e.id
              ? 'bg-bg-elevated'
              : 'hover:bg-bg-elevated/60'"
          >
            <span
              v-if="selecionada?.id === e.id"
              class="absolute left-0 top-2 bottom-2 w-[2px] bg-cyan rounded-r"
            />

            <span class="mono-tag text-ink-4 text-[11px] w-6 shrink-0">
              {{ String(i + 1).padStart(2, '0') }}
            </span>

            <div
              class="w-9 h-9 rounded-md flex items-center justify-center text-[11.5px] font-bold shrink-0"
              :style="avatarStyle(e.tipo)"
            >
              {{ inicials(e.razaoSocial) }}
            </div>

            <div class="flex-1 min-w-0">
              <p class="text-[13px] font-semibold text-ink truncate">{{ e.razaoSocial }}</p>
              <p class="mono-tag text-ink-3 text-[10.5px] truncate">{{ e.cnpj }}</p>
            </div>

            <span
              class="px-1.5 py-0.5 rounded text-[9.5px] font-bold tracking-wider shrink-0"
              :style="chipStyle(e.tipo)"
            >
              {{ tipoCurto(e.tipo) }}
            </span>
          </button>
        </div>
      </div>

      <div class="col-span-12 lg:col-span-7">
        <div
          v-if="selecionada"
          :key="selecionada.id"
          class="relative rounded-2xl bg-bg-panel border border-bg-line-strong p-7
                 helmet-stripe fade-up overflow-hidden"
        >
          <svg
            class="absolute -right-20 -top-20 w-[360px] h-[360px] opacity-[0.045] pointer-events-none"
            viewBox="0 0 600 600" fill="none"
          >
            <path d="M0 600 L300 0 L600 600 L500 600 L300 200 L100 600 Z" fill="#2DD4BF" />
          </svg>

          <div class="flex items-start gap-5 relative">
            <div
              class="w-16 h-16 rounded-xl flex items-center justify-center text-[20px] font-bold shrink-0"
              :style="avatarStyle(selecionada.tipo)"
            >
              {{ inicials(selecionada.razaoSocial) }}
            </div>

            <div class="flex-1 min-w-0">
              <span class="eyebrow" :style="{ color: tipoColor(selecionada.tipo) }">
                {{ selecionada.tipo }}
              </span>
              <h2 class="section-title text-[30px] mt-2">
                {{ selecionada.razaoSocial }}
              </h2>
              <p class="mono-tag text-ink-3 text-[11px] mt-2">
                CNPJ {{ selecionada.cnpj }}
              </p>
            </div>

            <span
              class="px-2.5 py-1 rounded-full text-[10.5px] font-bold tracking-wider flex items-center gap-1.5"
              :class="selecionada.ativa
                ? 'bg-success-soft border border-success/30 text-success'
                : 'bg-danger-soft  border border-danger/30  text-danger'"
            >
              <span class="w-1.5 h-1.5 rounded-full" :class="selecionada.ativa ? 'bg-success' : 'bg-danger'" />
              {{ selecionada.ativa ? 'ATIVA' : 'INATIVA' }}
            </span>
          </div>

          <dl class="mt-7 grid grid-cols-2 gap-x-6 gap-y-5">
            <Field icon="Mail"     label="E-mail"         :value="selecionada.email" />
            <Field icon="Phone"    label="Telefone"       :value="selecionada.telefone" />
            <Field icon="MapPin"   label="Endereço"       :value="selecionada.endereco" cols="2" />
            <Field icon="Calendar" label="Cadastrado em"  :value="formatarData(selecionada.criadoEm)" />
            <Field icon="Tag"      label="Identificador"  :value="`#${String(selecionada.id).padStart(4, '0')}`" mono />
          </dl>

          <div class="mt-7 flex flex-wrap gap-2 pt-5 border-t border-bg-line">
            <button class="flex items-center gap-2 px-3.5 py-2 rounded-md text-[11.5px] font-semibold tracking-wide
                           bg-bg-elevated border border-bg-line text-ink-2
                           hover:border-cyan/40 hover:text-cyan transition-colors">
              <FileText :size="13" /> VER LOTES ASSOCIADOS
            </button>
            <button class="flex items-center gap-2 px-3.5 py-2 rounded-md text-[11.5px] font-semibold tracking-wide
                           bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong transition-colors">
              <Edit3 :size="13" /> EDITAR CADASTRO
            </button>
            <button class="flex items-center gap-2 px-3.5 py-2 rounded-md text-[11.5px] font-semibold tracking-wide
                           bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong transition-colors">
              <Download :size="13" /> EXPORTAR FICHA
            </button>
          </div>
        </div>

        <div
          v-else
          class="rounded-2xl bg-bg-panel border border-bg-line-strong p-12 text-center
                 min-h-[400px] flex flex-col items-center justify-center"
        >
          <div class="w-16 h-16 rounded-2xl bg-bg-elevated border border-bg-line flex items-center justify-center mb-4">
            <MousePointerClick :size="22" class="text-ink-3" />
          </div>
          <p class="eyebrow">Selecione uma empresa</p>
          <p class="text-ink-3 text-[13px] mt-2 max-w-xs">
            Escolha uma empresa do roster ao lado para visualizar os detalhes completos do cadastro.
          </p>
        </div>
      </div>
    </section>

    <Transition name="page">
      <div
        v-if="modalAberto"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="fecharModal"
      >
        <div class="w-full max-w-xl rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-center justify-between mb-6">
            <div>
              <p class="eyebrow text-cyan">Novo registro</p>
              <h2 class="section-title text-[28px] mt-2">Cadastrar empresa</h2>
            </div>
            <button
              @click="fecharModal"
              class="w-9 h-9 rounded-md flex items-center justify-center text-ink-3
                     hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="space-y-4">
            <div>
              <label class="eyebrow block mb-1.5">CNPJ</label>
              <div class="flex gap-2">
                <div class="flex items-center gap-2 px-3 h-10 rounded-md bg-bg-base border border-bg-line flex-1
                            focus-within:border-cyan/40 transition-colors">
                  <Hash :size="14" class="text-ink-3" />
                  <input
                    v-model="form.cnpj"
                    placeholder="00.000.000/0000-00"
                    class="flex-1 bg-transparent outline-none text-[13px] text-ink mono-tag placeholder:text-ink-4"
                  />
                </div>
                <button
                  @click="consultarCnpj"
                  :disabled="buscandoCnpj"
                  class="flex items-center gap-1.5 px-3.5 h-10 rounded-md bg-cyan/10 border border-cyan/30
                         text-cyan text-[11.5px] font-bold tracking-wider hover:bg-cyan/20 transition-colors disabled:opacity-50"
                >
                  <Loader2 v-if="buscandoCnpj" :size="13" class="animate-spin" />
                  <Search v-else :size="13" />
                  BUSCAR
                </button>
              </div>
            </div>

            <FormField v-model="form.razaoSocial" label="Razão social" placeholder="Razão social da empresa" />

            <div>
              <label class="eyebrow block mb-1.5">Tipo</label>
              <div class="grid grid-cols-3 gap-1.5">
                <button
                  v-for="t in tiposCad"
                  :key="t.value"
                  @click="form.tipo = t.value"
                  class="px-3 py-2.5 rounded-md text-[11.5px] font-bold tracking-wider border transition-colors"
                  :class="form.tipo === t.value
                    ? 'border-cyan/40 text-cyan bg-cyan/10'
                    : 'bg-bg-elevated border-bg-line text-ink-2 hover:border-bg-line-strong'"
                >
                  {{ t.label }}
                </button>
              </div>
            </div>

            <FormField v-model="form.email"    label="E-mail"   placeholder="contato@empresa.com" type="email" />
            <FormField v-model="form.telefone" label="Telefone" placeholder="(00) 00000-0000" />
            <FormField v-model="form.endereco" label="Endereço" placeholder="Rua, número, cidade — UF" />
          </div>

          <p
            v-if="erro"
            class="flex items-center gap-2 text-[12px] text-danger
                   bg-danger-soft border border-danger/30 rounded-md px-3 py-2 mt-4"
          >
            <AlertCircle :size="14" /> {{ erro }}
          </p>

          <div class="flex justify-end gap-2 mt-6 pt-5 border-t border-bg-line">
            <button
              @click="fecharModal"
              class="px-4 h-10 rounded-md text-[11.5px] font-bold tracking-wider
                     bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong"
            >
              CANCELAR
            </button>
            <button
              @click="salvarEmpresa"
              :disabled="salvando"
              class="flex items-center gap-2 px-5 h-10 rounded-md text-[11.5px] font-bold tracking-wider
                     bg-cyan text-bg-base hover:bg-cyan/90 transition-colors disabled:opacity-50"
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
import { ref, computed, onMounted, h } from 'vue'
import {
  Search, Plus, Building2, Mail, Phone, MapPin, Calendar, Tag, Hash, X, Check,
  AlertCircle, Loader2, FileText, Edit3, Download, MousePointerClick,
} from 'lucide-vue-next'
import api from '../services/api'

const empresas     = ref([])
const selecionada  = ref(null)
const modalAberto  = ref(false)
const buscandoCnpj = ref(false)
const salvando     = ref(false)
const erro         = ref('')
const busca        = ref('')
const filtroTipo   = ref('TODOS')

const filtros = [
  { value: 'TODOS',          label: 'Todas' },
  { value: 'GERADORA',       label: 'Geradoras' },
  { value: 'TRANSPORTADORA', label: 'Transportadoras' },
  { value: 'RECEPTORA',      label: 'Receptoras' },
]

const tiposCad = [
  { value: 'GERADORA',       label: 'GERADORA' },
  { value: 'TRANSPORTADORA', label: 'TRANSPORT.' },
  { value: 'RECEPTORA',      label: 'RECEPTORA' },
]

const form = ref({
  cnpj: '', razaoSocial: '', tipo: 'GERADORA', email: '', telefone: '', endereco: '',
})

const countByType = computed(() => {
  const c = { GERADORA: 0, TRANSPORTADORA: 0, RECEPTORA: 0 }
  empresas.value.forEach(e => { if (c[e.tipo] !== undefined) c[e.tipo]++ })
  return c
})

const empresasFiltradas = computed(() => {
  let l = empresas.value
  if (filtroTipo.value !== 'TODOS') l = l.filter(e => e.tipo === filtroTipo.value)
  if (busca.value.trim()) {
    const q = busca.value.toLowerCase()
    l = l.filter(e =>
      (e.razaoSocial || '').toLowerCase().includes(q) ||
      (e.cnpj        || '').toLowerCase().includes(q) ||
      (e.email       || '').toLowerCase().includes(q)
    )
  }
  return l
})

const tipoColor = (t) => ({
  GERADORA:       '#38BDF8',
  TRANSPORTADORA: '#F59E0B',
  RECEPTORA:      '#10B981',
}[t] || '#A5ACAF')

const avatarStyle = (t) => {
  const c = tipoColor(t)
  return { backgroundColor: `${c}1A`, border: `1px solid ${c}40`, color: c }
}

const chipStyle = (t) => {
  const c = tipoColor(t)
  return { backgroundColor: `${c}1A`, color: c, border: `1px solid ${c}30` }
}

const tipoCurto = (t) => ({
  GERADORA: 'GER', TRANSPORTADORA: 'TRANS', RECEPTORA: 'RECEP',
}[t] || t)

const inicials = (str) =>
  (str || 'EM').split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase()

const formatarData = (d) =>
  d ? new Date(d).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'

const carregar = async () => {
  try {
    const r = await api.get('/empresas')
    empresas.value = r.data
    if (!selecionada.value && empresas.value.length) selecionada.value = empresas.value[0]
  } catch (e) {
    console.error('Erro ao carregar empresas:', e)
  }
}

const consultarCnpj = async () => {
  if (buscandoCnpj.value) return
  buscandoCnpj.value = true
  erro.value = ''
  try {
    const cnpj = form.value.cnpj.replace(/\D/g, '')
    if (cnpj.length !== 14) {
      erro.value = 'CNPJ deve ter 14 dígitos.'
      return
    }
    // Usa a BrasilAPI (pública, sem autenticação) para buscar dados do CNPJ
    const res = await fetch(`https://brasilapi.com.br/api/cnpj/v1/${cnpj}`)
    if (!res.ok) throw new Error('CNPJ não encontrado')
    const data = await res.json()
    form.value.razaoSocial = data.razao_social || ''
    const partes = [data.logradouro, data.numero, data.municipio, data.uf].filter(Boolean)
    form.value.endereco = partes.join(', ')
  } catch {
    erro.value = 'CNPJ não encontrado na Receita Federal.'
  } finally {
    buscandoCnpj.value = false
  }
}

const abrirModal = () => {
  modalAberto.value = true
  erro.value = ''
  form.value = { cnpj: '', razaoSocial: '', tipo: 'GERADORA', email: '', telefone: '', endereco: '' }
}

const fecharModal = () => { modalAberto.value = false }

const salvarEmpresa = async () => {
  if (salvando.value) return
  salvando.value = true
  erro.value = ''
  try {
    await api.post('/empresas', form.value)
    fecharModal()
    await carregar()
  } catch (e) {
    erro.value = e.mensagemAmigavel || 'Erro ao salvar empresa. Verifique os dados.'
  } finally {
    salvando.value = false
  }
}

onMounted(carregar)

const iconMap = { Mail, Phone, MapPin, Calendar, Tag }

const Field = (props) => {
  const Icon = iconMap[props.icon] || Tag
  return h('div', { class: props.cols === '2' ? 'col-span-2' : '' }, [
    h('p', { class: 'eyebrow flex items-center gap-1.5' }, [
      h(Icon, { size: 11 }), props.label,
    ]),
    h('p', {
      class: ['mt-1.5 text-[13px] break-words',
              props.mono ? 'mono-tag text-cyan text-[13px]' : 'text-ink'],
    }, props.value || '—'),
  ])
}
Field.props = ['icon', 'label', 'value', 'cols', 'mono']

const FormField = (props, { emit }) => h('div', {}, [
  h('label', { class: 'eyebrow block mb-1.5' }, props.label),
  h('div', {
    class: 'flex items-center px-3 h-10 rounded-md bg-bg-base border border-bg-line ' +
           'focus-within:border-cyan/40 transition-colors',
  }, [
    h('input', {
      type: props.type || 'text',
      value: props.modelValue,
      placeholder: props.placeholder,
      onInput: (e) => emit('update:modelValue', e.target.value),
      class: 'flex-1 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4',
    }),
  ]),
])
FormField.props = ['modelValue', 'label', 'placeholder', 'type']
FormField.emits = ['update:modelValue']
</script>
