<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">

    <header class="flex items-end justify-between flex-wrap gap-4">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Gestão · Lotes de resíduos</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Pipeline de lotes
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ lotes.length }} lotes · {{ contagens.AGUARDANDO_COLETA }} aguardando ·
          {{ contagens.EM_TRANSITO }} em trânsito · {{ contagens.DESCARTADO }} descartados
        </p>
      </div>

      <div class="flex items-center gap-2">
        <div class="flex p-1 rounded-md bg-bg-elevated border border-bg-line">
          <button
            @click="vista = 'kanban'"
            class="px-3 py-1.5 rounded-[5px] text-[11px] font-bold tracking-wider transition-colors flex items-center gap-1.5"
            :class="vista === 'kanban' ? 'bg-bg-panel text-ink' : 'text-ink-3 hover:text-ink'"
          >
            <Columns3 :size="13" /> KANBAN
          </button>
          <button
            @click="vista = 'tabela'"
            class="px-3 py-1.5 rounded-[5px] text-[11px] font-bold tracking-wider transition-colors flex items-center gap-1.5"
            :class="vista === 'tabela' ? 'bg-bg-panel text-ink' : 'text-ink-3 hover:text-ink'"
          >
            <Rows3 :size="13" /> TABELA
          </button>
        </div>

        <button
          @click="abrirModal"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base
                 text-[12.5px] font-bold tracking-[0.08em] hover:bg-cyan/90 transition-colors"
        >
          <Plus :size="16" /> NOVO LOTE
        </button>
      </div>
    </header>

    <div v-if="carregando" class="grid grid-cols-4 gap-4">
      <div v-for="i in 4" :key="i" class="h-[440px] skeleton rounded-2xl" />
    </div>

    <section
      v-else-if="vista === 'kanban'"
      class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4"
    >
      <div
        v-for="(col, idx) in colunas"
        :key="col.status"
        class="rounded-2xl bg-bg-panel border border-bg-line-strong flex flex-col min-h-[500px] overflow-hidden fade-up"
        :style="{ animationDelay: `${idx * 80}ms` }"
      >
        <div
          class="px-5 py-4 flex items-center justify-between border-b border-bg-line"
          :style="{ borderTop: `2px solid ${col.cor}` }"
        >
          <div class="flex items-center gap-2.5">
            <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: col.cor }" />
            <p class="eyebrow text-ink">{{ col.label }}</p>
          </div>
          <span
            class="mono-tag px-2 py-0.5 rounded text-[10px] font-bold"
            :style="{ backgroundColor: `${col.cor}20`, color: col.cor }"
          >
            {{ contagens[col.status] || 0 }}
          </span>
        </div>

        <div class="flex-1 p-3 space-y-2.5 overflow-y-auto">
          <div
            v-if="lotesPorStatus(col.status).length === 0"
            class="flex flex-col items-center justify-center py-12 text-center"
          >
            <Package :size="22" class="text-ink-4 mb-2" />
            <p class="mono-tag text-ink-4 text-[11px]">Sem lotes</p>
          </div>

          <article
            v-for="l in lotesPorStatus(col.status)"
            :key="l.id"
            class="group rounded-xl bg-bg-elevated border border-bg-line p-4 cursor-pointer
                   card-hover relative overflow-hidden"
          >
            <span class="absolute left-0 top-0 bottom-0 w-[2px]" :style="{ backgroundColor: col.cor }" />

            <div class="flex items-start justify-between mb-3">
              <span class="mono-tag text-cyan text-[12px]">
                #{{ String(l.id).padStart(4, '0') }}
              </span>
              <span class="mono-tag text-ink-3 text-[10px]">
                {{ formatarData(l.criadoEm) }}
              </span>
            </div>

            <p class="text-[13px] text-ink font-semibold leading-snug line-clamp-2 mb-1">
              {{ l.descricao || 'Sem descrição' }}
            </p>
            <p class="eyebrow text-[9.5px] mb-3">{{ l.tipoResiduo || 'Tipo não informado' }}</p>

            <div class="flex items-baseline gap-1.5 pt-3 border-t border-bg-line">
              <span class="scoreboard text-[30px] text-ink leading-none tabular-nums">
                {{ l.quantidade }}
              </span>
              <span class="font-display text-cyan text-[15px]"
                    style="font-weight: 500; font-style: italic; letter-spacing: 0.04em;">{{ l.unidade }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section
      v-else
      class="rounded-2xl bg-bg-panel border border-bg-line-strong overflow-hidden helmet-stripe fade-up"
    >
      <table class="w-full text-left">
        <thead class="bg-bg-elevated border-b border-bg-line">
          <tr>
            <th class="px-5 py-3 eyebrow">#</th>
            <th class="px-5 py-3 eyebrow">ID</th>
            <th class="px-5 py-3 eyebrow">Descrição</th>
            <th class="px-5 py-3 eyebrow">Resíduo</th>
            <th class="px-5 py-3 eyebrow text-right">Qtd</th>
            <th class="px-5 py-3 eyebrow">Status</th>
            <th class="px-5 py-3 eyebrow">Data</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="lotes.length === 0">
            <td colspan="7" class="px-6 py-16 text-center">
              <Package :size="28" class="mx-auto text-ink-4 mb-2" />
              <p class="mono-tag text-ink-3">Nenhum lote registrado</p>
            </td>
          </tr>
          <tr
            v-for="(l, i) in lotes"
            :key="l.id"
            class="border-b border-bg-line hover:bg-bg-elevated transition-colors"
          >
            <td class="px-5 py-3.5 mono-tag text-ink-4 text-[11px]">
              {{ String(i + 1).padStart(2, '0') }}
            </td>
            <td class="px-5 py-3.5 mono-tag text-cyan text-[12px]">
              #{{ String(l.id).padStart(4, '0') }}
            </td>
            <td class="px-5 py-3.5 text-[13px] text-ink">{{ l.descricao || '—' }}</td>
            <td class="px-5 py-3.5">
              <span class="px-2 py-0.5 rounded-full text-[10.5px] font-bold tracking-wider
                           bg-info-soft border border-info/30 text-info">
                {{ l.tipoResiduo || '—' }}
              </span>
            </td>
            <td class="px-5 py-3.5 text-right">
              <span class="scoreboard text-[20px] text-ink tabular-nums">{{ l.quantidade }}</span>
              <span class="mono-tag text-ink-3 ml-1">{{ l.unidade }}</span>
            </td>
            <td class="px-5 py-3.5">
              <span
                class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10.5px] font-bold tracking-wider border"
                :style="statusChipStyle(l.status)"
              >
                <span class="w-1.5 h-1.5 rounded-full" :style="{ backgroundColor: statusCor(l.status) }" />
                {{ statusLabel(l.status) }}
              </span>
            </td>
            <td class="px-5 py-3.5 mono-tag text-ink-3 text-[11px]">
              {{ formatarData(l.criadoEm) }}
            </td>
          </tr>
        </tbody>
      </table>
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
              <h2 class="section-title text-[28px] mt-2">Registrar lote</h2>
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
            <FormField v-model="form.descricao"   label="Descrição"      placeholder="Ex: Resíduo do laboratório B-12" />
            <FormField v-model="form.tipoResiduo" label="Tipo de resíduo" placeholder="Ex: Químico, Eletrônico, Orgânico" />

            <div class="grid grid-cols-3 gap-3">
              <div class="col-span-2">
                <label class="eyebrow block mb-1.5">Quantidade</label>
                <div class="flex items-center px-3 h-10 rounded-md bg-bg-base border border-bg-line focus-within:border-cyan/40 transition-colors">
                  <Hash :size="14" class="text-ink-3" />
                  <input
                    v-model="form.quantidade"
                    type="number"
                    placeholder="0"
                    class="ml-2 flex-1 bg-transparent outline-none text-[13px] text-ink mono-tag placeholder:text-ink-4"
                  />
                </div>
              </div>
              <div>
                <label class="eyebrow block mb-1.5">Unidade</label>
                <select
                  v-model="form.unidade"
                  class="w-full px-3 h-10 rounded-md bg-bg-base border border-bg-line text-[13px] text-ink mono-tag outline-none focus:border-cyan/40"
                >
                  <option value="KG">KG</option>
                  <option value="TON">TON</option>
                  <option value="L">L</option>
                </select>
              </div>
            </div>

            <div>
              <label class="eyebrow block mb-1.5">Empresa geradora · ID</label>
              <div class="flex items-center px-3 h-10 rounded-md bg-bg-base border border-bg-line focus-within:border-cyan/40 transition-colors">
                <Building2 :size="14" class="text-ink-3" />
                <input
                  v-model="form.empresaGeradoraId"
                  type="number"
                  placeholder="ID da empresa geradora"
                  class="ml-2 flex-1 bg-transparent outline-none text-[13px] text-ink mono-tag placeholder:text-ink-4"
                />
              </div>
            </div>
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
              @click="salvarLote"
              :disabled="salvando"
              class="flex items-center gap-2 px-5 h-10 rounded-md text-[11.5px] font-bold tracking-wider
                     bg-cyan text-bg-base hover:bg-cyan/90 transition-colors disabled:opacity-50"
            >
              <Loader2 v-if="salvando" :size="13" class="animate-spin" />
              <Check v-else :size="13" />
              {{ salvando ? 'SALVANDO...' : 'CRIAR LOTE' }}
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
  Plus, Package, X, Check, AlertCircle, Loader2, Hash, Building2,
  Columns3, Rows3,
} from 'lucide-vue-next'
import api from '../services/api'

const lotes       = ref([])
const carregando  = ref(false)
const modalAberto = ref(false)
const salvando    = ref(false)
const erro        = ref('')
const vista       = ref('kanban')

const form = ref({
  descricao: '', tipoResiduo: '', quantidade: '', unidade: 'KG', empresaGeradoraId: '',
})

const colunas = [
  { status: 'AGUARDANDO_COLETA', label: 'Aguardando',   cor: '#F59E0B' },
  { status: 'EM_TRANSITO',       label: 'Em trânsito',  cor: '#38BDF8' },
  { status: 'DESCARTADO',        label: 'Descartado',   cor: '#10B981' },
  { status: 'CANCELADO',         label: 'Cancelado',    cor: '#DC2626' },
]

const contagens = computed(() => {
  const c = { AGUARDANDO_COLETA: 0, EM_TRANSITO: 0, DESCARTADO: 0, CANCELADO: 0 }
  lotes.value.forEach(l => { if (c[l.status] !== undefined) c[l.status]++ })
  return c
})

const lotesPorStatus = (s) => lotes.value.filter(l => l.status === s)
const statusCor   = (s) => colunas.find(c => c.status === s)?.cor   || '#A5ACAF'
const statusLabel = (s) => colunas.find(c => c.status === s)?.label || s

const statusChipStyle = (s) => {
  const c = statusCor(s)
  return { backgroundColor: `${c}1A`, color: c, borderColor: `${c}30` }
}

const formatarData = (d) =>
  d ? new Date(d).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }) : '—'

const carregar = async () => {
  carregando.value = true
  try {
    const r = await api.get('/lotes')
    lotes.value = r.data
  } catch (e) {
    console.error('Erro ao carregar lotes:', e)
  } finally {
    carregando.value = false
  }
}

const abrirModal = () => {
  modalAberto.value = true
  erro.value = ''
  form.value = { descricao: '', tipoResiduo: '', quantidade: '', unidade: 'KG', empresaGeradoraId: '' }
}

const fecharModal = () => { modalAberto.value = false }

const salvarLote = async () => {
  if (salvando.value) return
  salvando.value = true
  erro.value = ''
  try {
    await api.post('/lotes', {
      descricao:    form.value.descricao,
      tipoResiduo:  form.value.tipoResiduo,
      quantidade:   form.value.quantidade,
      unidade:      form.value.unidade,
      empresaGeradora: { id: form.value.empresaGeradoraId },
    })
    fecharModal()
    await carregar()
  } catch (e) {
    erro.value = e.mensagemAmigavel || 'Erro ao salvar lote. Verifique os dados.'
  } finally {
    salvando.value = false
  }
}

onMounted(carregar)

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
