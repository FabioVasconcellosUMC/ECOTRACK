<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">
    <header class="flex items-end justify-between flex-wrap gap-4">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Indicadores e auditoria</p>
        <h1 class="display-title text-[48px] leading-[0.98]">Relatórios</h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          Exportação demonstrativa dos principais dados ambientais e operacionais
        </p>
      </div>

      <button
        @click="exportarRelatorio"
        class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base text-[12px] font-bold tracking-wider hover:bg-cyan/90 transition-colors"
      >
        <Download :size="15" /> EXPORTAR CSV
      </button>
    </header>

    <section class="grid grid-cols-12 gap-5">
      <div
        v-for="indicador in indicadores"
        :key="indicador.label"
        class="col-span-12 md:col-span-3 rounded-2xl bg-bg-panel border border-bg-line-strong p-6 helmet-stripe"
      >
        <p class="eyebrow">{{ indicador.label }}</p>
        <div class="flex items-end gap-2 mt-3">
          <span class="scoreboard text-[42px] text-ink">{{ indicador.value }}</span>
          <span class="font-display text-cyan text-[15px] mb-1" style="font-weight: 500; font-style: italic; letter-spacing: 0.04em;">
            {{ indicador.unit }}
          </span>
        </div>
        <p class="text-[12px] text-ink-3 mt-3 leading-relaxed">{{ indicador.description }}</p>
      </div>
    </section>

    <section class="grid grid-cols-12 gap-5">
      <div class="col-span-12 lg:col-span-8 rounded-2xl bg-bg-panel border border-bg-line-strong overflow-hidden helmet-stripe">
        <div class="px-6 py-5 border-b border-bg-line flex items-center justify-between gap-3">
          <div>
            <p class="eyebrow text-cyan">Resumo demonstrativo</p>
            <h2 class="section-title text-[28px] mt-2">Relatório mensal</h2>
          </div>
          <span class="px-3 py-1 rounded-full bg-cyan/10 border border-cyan/30 text-cyan text-[10px] font-bold tracking-wider">
            MAIO 2026
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-bg-elevated border-b border-bg-line">
              <tr>
                <th class="px-5 py-3 eyebrow">Métrica</th>
                <th class="px-5 py-3 eyebrow">Valor</th>
                <th class="px-5 py-3 eyebrow">Status</th>
                <th class="px-5 py-3 eyebrow">Observação</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="linha in linhasRelatorio"
                :key="linha.metrica"
                class="border-b border-bg-line hover:bg-bg-elevated transition-colors"
              >
                <td class="px-5 py-4 text-[13px] text-ink font-semibold">{{ linha.metrica }}</td>
                <td class="px-5 py-4 mono-tag text-cyan text-[12px]">{{ linha.valor }}</td>
                <td class="px-5 py-4">
                  <span class="px-2 py-0.5 rounded-full bg-success-soft border border-success/30 text-success text-[10px] font-bold tracking-wider">
                    {{ linha.status }}
                  </span>
                </td>
                <td class="px-5 py-4 text-[13px] text-ink-3">{{ linha.observacao }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="col-span-12 lg:col-span-4 rounded-2xl bg-bg-panel border border-bg-line-strong p-6 helmet-stripe">
        <p class="eyebrow text-cyan">Explicação para banca</p>
        <h2 class="section-title text-[26px] mt-2">Relatórios sem complicar o escopo</h2>
        <p class="editorial text-[13px] mt-4">
          A tela já demonstra como o sistema poderá consolidar dados operacionais. A exportação CSV funciona no front e serve para provar a intenção de auditoria e tomada de decisão.
        </p>

        <button
          @click="mostrarResumo = !mostrarResumo"
          class="mt-6 w-full flex items-center justify-center gap-2 h-10 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[12px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
        >
          <Eye :size="15" /> {{ mostrarResumo ? 'OCULTAR RESUMO' : 'VER RESUMO' }}
        </button>

        <div v-if="mostrarResumo" class="mt-4 rounded-xl bg-bg-elevated border border-bg-line p-4 fade-up">
          <p class="text-[13px] text-ink-2 leading-relaxed">
            Neste mês, o EcoTrack consolidou dados de resíduos rastreados, empresas participantes, lotes ativos e operações pendentes. O próximo passo é conectar esses indicadores diretamente ao banco de dados.
          </p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Download, Eye } from 'lucide-vue-next'
import { exportCsv } from '../utils/exportCsv'

const mostrarResumo = ref(false)

const indicadores = [
  { label: 'Resíduos', value: '1.840', unit: 'TON', description: 'Volume total rastreado no painel demonstrativo.' },
  { label: 'Empresas', value: '84', unit: '', description: 'Participantes cadastrados na cadeia operacional.' },
  { label: 'Lotes', value: '217', unit: '', description: 'Registros ativos para acompanhamento.' },
  { label: 'Pendências', value: '12', unit: '', description: 'Itens que exigem validação operacional.' },
]

const linhasRelatorio = [
  { metrica: 'Volume rastreado', valor: '1.840 TON', status: 'OK', observacao: 'Consolidado para apresentação.' },
  { metrica: 'Empresas cadastradas', valor: '84', status: 'OK', observacao: 'Base preparada para filtros.' },
  { metrica: 'Lotes ativos', valor: '217', status: 'OK', observacao: 'Kanban e tabela disponíveis.' },
  { metrica: 'Lotes em trânsito', valor: '43', status: 'OK', observacao: 'Integração futura com transportes.' },
]

const exportarRelatorio = () => {
  exportCsv('ecotrack-relatorio-mensal.csv', linhasRelatorio)
}
</script>
