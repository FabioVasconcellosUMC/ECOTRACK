<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">
    <header class="flex items-end justify-between flex-wrap gap-4">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Módulo preparado</p>
        <h1 class="display-title text-[48px] leading-[0.98]">Transportes</h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          Área reservada para manifestos, rotas e responsáveis pela coleta dos resíduos
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="abrirChecklist"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[12px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
        >
          <ListChecks :size="15" /> CHECKLIST
        </button>
        <button
          @click="exportarModelo"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base text-[12px] font-bold tracking-wider hover:bg-cyan/90 transition-colors"
        >
          <Download :size="15" /> EXPORTAR MODELO
        </button>
      </div>
    </header>

    <section class="grid grid-cols-12 gap-5">
      <div
        v-for="card in cards"
        :key="card.label"
        class="col-span-12 md:col-span-4 rounded-2xl bg-bg-panel border border-bg-line-strong p-6 helmet-stripe"
      >
        <div class="flex items-center justify-between gap-3">
          <div class="w-11 h-11 rounded-xl bg-cyan/10 border border-cyan/20 flex items-center justify-center text-cyan">
            <component :is="card.icon" :size="20" />
          </div>
          <span class="mono-tag text-[10px] px-2 py-0.5 rounded-full bg-bg-elevated border border-bg-line text-ink-3">
            {{ card.status }}
          </span>
        </div>
        <p class="eyebrow mt-6">{{ card.label }}</p>
        <h2 class="section-title text-[28px] mt-2">{{ card.value }}</h2>
        <p class="text-[13px] text-ink-3 mt-3 leading-relaxed">{{ card.description }}</p>
      </div>
    </section>

    <section class="grid grid-cols-12 gap-5">
      <div class="col-span-12 lg:col-span-8 rounded-2xl bg-bg-panel border border-bg-line-strong overflow-hidden helmet-stripe">
        <div class="px-6 py-5 border-b border-bg-line flex items-center justify-between">
          <div>
            <p class="eyebrow text-cyan">Estrutura futura</p>
            <h2 class="section-title text-[28px] mt-2">Manifestos de transporte</h2>
          </div>
          <span class="px-3 py-1 rounded-full bg-warning-soft border border-amber/30 text-amber text-[10px] font-bold tracking-wider">
            EM PLANEJAMENTO
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-bg-elevated border-b border-bg-line">
              <tr>
                <th class="px-5 py-3 eyebrow">Manifesto</th>
                <th class="px-5 py-3 eyebrow">Lote</th>
                <th class="px-5 py-3 eyebrow">Transportadora</th>
                <th class="px-5 py-3 eyebrow">Status</th>
                <th class="px-5 py-3 eyebrow">Previsão</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in manifestos"
                :key="item.manifesto"
                class="border-b border-bg-line hover:bg-bg-elevated transition-colors"
              >
                <td class="px-5 py-4 mono-tag text-cyan text-[12px]">{{ item.manifesto }}</td>
                <td class="px-5 py-4 text-[13px] text-ink">{{ item.lote }}</td>
                <td class="px-5 py-4 text-[13px] text-ink-2">{{ item.transportadora }}</td>
                <td class="px-5 py-4">
                  <span class="px-2 py-0.5 rounded-full bg-cyan/10 border border-cyan/30 text-cyan text-[10px] font-bold tracking-wider">
                    {{ item.status }}
                  </span>
                </td>
                <td class="px-5 py-4 mono-tag text-ink-3 text-[11px]">{{ item.previsao }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="col-span-12 lg:col-span-4 rounded-2xl bg-bg-panel border border-bg-line-strong p-6 helmet-stripe">
        <p class="eyebrow text-cyan">Como defender na banca</p>
        <h2 class="section-title text-[26px] mt-2">Por que manter esta tela?</h2>
        <p class="editorial text-[13px] mt-4">
          A página demonstra que a arquitetura visual já prevê expansão do sistema. O módulo aparece preparado, mas separado das funcionalidades já concluídas.
        </p>

        <div class="mt-6 space-y-3">
          <div
            v-for="item in proximosPassos"
            :key="item"
            class="flex items-start gap-3 rounded-xl bg-bg-elevated border border-bg-line p-3"
          >
            <CheckCircle2 :size="15" class="text-success shrink-0 mt-0.5" />
            <p class="text-[13px] text-ink-2 leading-snug">{{ item }}</p>
          </div>
        </div>
      </div>
    </section>

    <Transition name="page">
      <div
        v-if="checklistAberto"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="checklistAberto = false"
      >
        <div class="w-full max-w-lg rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-center justify-between mb-6">
            <div>
              <p class="eyebrow text-cyan">Checklist</p>
              <h2 class="section-title text-[28px] mt-2">Funcionalidades previstas</h2>
            </div>
            <button
              @click="checklistAberto = false"
              class="w-9 h-9 rounded-md flex items-center justify-center text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="space-y-3">
            <label
              v-for="item in checklist"
              :key="item.texto"
              class="flex items-center gap-3 rounded-xl bg-bg-elevated border border-bg-line p-3 cursor-pointer hover:border-cyan/30 transition-colors"
            >
              <input v-model="item.ativo" type="checkbox" class="accent-cyan" />
              <span class="text-[13px] text-ink-2">{{ item.texto }}</span>
            </label>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  Download, ListChecks, CheckCircle2, X, Truck, Route, ClipboardCheck,
} from 'lucide-vue-next'
import { exportCsv } from '../utils/exportCsv'

const checklistAberto = ref(false)

const cards = [
  {
    label: 'Manifestos',
    value: 'MTR',
    status: 'FUTURO',
    description: 'Cadastro de manifesto para conectar lote, transportadora, origem e destino.',
    icon: ClipboardCheck,
  },
  {
    label: 'Rotas',
    value: 'Rastreio',
    status: 'FUTURO',
    description: 'Acompanhamento de rota, previsão de chegada e status do transporte.',
    icon: Route,
  },
  {
    label: 'Coletas',
    value: 'Operação',
    status: 'FUTURO',
    description: 'Registro da coleta e confirmação de entrega no destino final.',
    icon: Truck,
  },
]

const manifestos = [
  { manifesto: 'MTR-2026-001', lote: 'LT-2026-0319', transportadora: 'EcoMove Transportes', status: 'Modelo', previsao: '10/05/2026' },
  { manifesto: 'MTR-2026-002', lote: 'LT-2026-0320', transportadora: 'VerdeLog Ambiental', status: 'Modelo', previsao: '12/05/2026' },
  { manifesto: 'MTR-2026-003', lote: 'LT-2026-0321', transportadora: 'Rota Limpa', status: 'Modelo', previsao: '15/05/2026' },
]

const proximosPassos = [
  'Criar endpoint de cadastro de transporte no back-end.',
  'Vincular transporte a um lote existente.',
  'Registrar mudança de status durante a coleta.',
  'Gerar comprovante ou manifesto para auditoria.',
]

const checklist = ref(proximosPassos.map((texto) => ({ texto, ativo: false })))

const abrirChecklist = () => {
  checklistAberto.value = true
}

const exportarModelo = () => {
  exportCsv('ecotrack-modelo-transportes.csv', manifestos)
}
</script>
