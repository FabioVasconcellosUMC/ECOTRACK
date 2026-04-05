<template>
  <div class="flex flex-col gap-6">

    <div class="grid grid-cols-4 gap-4">
      <div v-for="card in kpis" :key="card.label"
        class="flex flex-col gap-3 p-5 rounded-xl border border-bg-border bg-bg-surface"
        style="border-top: 2px solid #004C54;">
        <div class="flex items-center justify-between">
          <component :is="card.icon" :size="20" class="text-accent" />
          <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-brand text-text-primary">
            {{ card.variacao }}
          </span>
        </div>
        <div>
          <p class="text-3xl font-bold text-text-primary">{{ card.valor }}</p>
          <p class="text-sm text-text-secondary mt-1">{{ card.label }}</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-3 gap-4">
      <div class="col-span-2 p-5 rounded-xl border border-bg-border bg-bg-surface">
        <p class="text-text-primary font-semibold mb-4">Transportes por Mês</p>
        <Bar :data="chartData" :options="chartOptions" />
      </div>

      <div class="p-5 rounded-xl border border-bg-border bg-bg-surface flex flex-col gap-4">
        <p class="text-text-primary font-semibold">Atividades Recentes</p>
        <div v-for="item in atividades" :key="item.texto" class="flex items-start gap-3">
          <span class="w-2 h-2 rounded-full mt-1.5 shrink-0" :style="{ backgroundColor: item.cor }"></span>
          <div>
            <p class="text-sm text-text-primary">{{ item.texto }}</p>
            <p class="text-xs text-text-muted mt-0.5">{{ item.tempo }}</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip } from 'chart.js'
import { Building2, Package, Truck, Scale } from 'lucide-vue-next'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip)

const kpis = [
  { label: 'Empresas Cadastradas', valor: '84', variacao: '+12%', icon: Building2 },
  { label: 'Lotes Ativos', valor: '217', variacao: '+8%', icon: Package },
  { label: 'Em Transporte', valor: '43', variacao: '+15%', icon: Truck },
  { label: 'Total Resíduos', valor: '1.840 ton', variacao: '+5%', icon: Scale },
]

const atividades = [
  { texto: 'Lote LT-2026-0318 iniciou transporte', tempo: '2 min atrás', cor: '#60A5FA' },
  { texto: 'Lote LT-2026-0315 foi descartado', tempo: '15 min atrás', cor: '#00A8B8' },
  { texto: 'Novo lote LT-2026-0319 criado', tempo: '1 hora atrás', cor: '#EAB308' },
  { texto: 'Transporte TR-2026-0142 cancelado', tempo: '2 horas atrás', cor: '#F87171' },
  { texto: 'Lote LT-2026-0312 processado com sucesso', tempo: '3 horas atrás', cor: '#60A5FA' },
]

const chartData = {
  labels: ['Out', 'Nov', 'Dez', 'Jan', 'Fev', 'Mar'],
  datasets: [
    {
      data: [44, 50, 46, 62, 55, 68],
      backgroundColor: 'rgba(0, 76, 84, 0.8)',
      borderRadius: 6,
      hoverBackgroundColor: '#00A8B8',
    }
  ]
}

const chartOptions = {
  responsive: true,
  plugins: { legend: { display: false } },
  scales: {
    x: {
      ticks: { color: '#4D7A80' },
      grid: { color: '#143040' },
    },
    y: {
      ticks: { color: '#4D7A80' },
      grid: { color: '#143040' },
    }
  }
}
</script>