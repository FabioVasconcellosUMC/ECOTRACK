<template>
  <div class="max-w-[1600px] mx-auto fade-up">
    <header class="mb-8">
      <p class="eyebrow-italic text-cyan mb-2">Manifestos · Transporte de resíduos</p>
      <h1 class="display-title text-[48px] leading-[0.98]">
        Transportes
      </h1>
      <p class="text-ink-3 text-[13px] mt-2 mono-tag">
        Rastreamento de manifestos · CTR · MTR · em conformidade com PNRS
      </p>
    </header>

    <section
      class="relative rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe
             p-12 lg:p-16 overflow-hidden min-h-[480px] flex flex-col items-center justify-center text-center"
    >

      <div class="absolute inset-0 wing-pattern opacity-30 pointer-events-none" />

      <div
        class="absolute inset-0 pointer-events-none"
        style="background: radial-gradient(500px 350px at 50% 0%, rgba(45, 212, 191, 0.08), transparent 70%);"
      />

      <div class="relative">
        <div
          class="w-20 h-20 rounded-2xl mx-auto mb-6 flex items-center justify-center
                 bg-bg-elevated border border-bg-line-strong"
        >
          <Truck :size="32" :stroke-width="1.5" class="text-cyan" />
        </div>

        <p class="eyebrow text-cyan mb-3">Módulo em desenvolvimento</p>
        <h2 class="section-title text-[36px] mb-4">
          Manifestos de transporte
        </h2>
        <p class="editorial text-[15px] max-w-md mx-auto">
          Acompanhamento em tempo real dos manifestos de transporte de resíduos,
          integração com sistemas estaduais e geração automatizada de CTRs.
        </p>

        <div class="mt-8 flex flex-wrap items-center justify-center gap-2">
          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full
                       bg-bg-elevated border border-bg-line text-ink-2 text-[11px]">
            <Clock :size="11" /> Próxima entrega · Q2
          </span>
          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full
                       bg-info-soft border border-info/30 text-info text-[11px] font-semibold">
            ROADMAP
          </span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { Truck, Clock } from 'lucide-vue-next'
</script>
