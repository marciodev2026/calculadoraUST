<template>
  <div>
    <h2 class="page-title">Dashboard Executivo</h2>

    <div v-if="loading" class="loading-state">
      <ProgressSpinner />
    </div>

    <template v-else-if="dashboard">
      <div class="summary-grid">
        <div class="summary-card">
          <i class="pi pi-calculator summary-icon"></i>
          <span class="summary-label">Simulações</span>
          <span class="summary-value">{{ dashboard.totalSimulacoes }}</span>
        </div>
        <div class="summary-card">
          <i class="pi pi-briefcase summary-icon"></i>
          <span class="summary-label">Projetos</span>
          <span class="summary-value">{{ dashboard.totalProjetos }}</span>
        </div>
        <div class="summary-card">
          <i class="pi pi-clock summary-icon"></i>
          <span class="summary-label">Total UST</span>
          <span class="summary-value">{{ formatNumber(dashboard.totalUst) }}</span>
        </div>
        <div class="summary-card highlight">
          <i class="pi pi-dollar summary-icon"></i>
          <span class="summary-label">Valor Total</span>
          <span class="summary-value">{{ formatCurrency(dashboard.valorTotal) }}</span>
        </div>
      </div>

      <div class="charts-grid">
        <div class="page-card chart-card">
          <h3 class="card-title">UST por Projeto</h3>
          <Chart
            v-if="ustPorProjetoChart"
            type="bar"
            :data="ustPorProjetoChart"
            :options="barOptions"
            class="chart"
          />
          <div v-else class="chart-empty">Nenhum projeto com squad montada</div>
        </div>

        <div class="page-card chart-card">
          <h3 class="card-title">Distribuição por Perfil</h3>
          <Chart
            v-if="perfilChart"
            type="doughnut"
            :data="perfilChart"
            :options="doughnutOptions"
            class="chart"
          />
          <div v-else class="chart-empty">Nenhum perfil alocado</div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="page-card chart-card">
          <h3 class="card-title">Valor por Tipo</h3>
          <Chart
            v-if="tipoChart"
            type="pie"
            :data="tipoChart"
            :options="pieOptions"
            class="chart"
          />
          <div v-else class="chart-empty">Nenhum dado por tipo</div>
        </div>

        <div class="page-card chart-card">
          <h3 class="card-title">Resumo de Horas</h3>
          <div class="hours-summary">
            <div class="hours-total">
              <span class="hours-value">{{ formatNumber(dashboard.totalHoras) }}</span>
              <span class="hours-label">horas totais</span>
            </div>
            <div class="hours-meta">
              <span>{{ dashboard.totalSquads }} squads montadas</span>
            </div>
          </div>
        </div>
      </div>

      <div class="page-card">
        <h3 class="card-title">Simulações Recentes</h3>
        <DataTable :value="dashboard.simulacoesRecentes" striped-rows>
          <Column field="nomeCompleto" header="Solicitante" />
          <Column field="orgao" header="Órgão" />
          <Column field="dataSimulacao" header="Data" style="width: 120px">
            <template #body="{ data }">
              {{ formatDate(data.dataSimulacao) }}
            </template>
          </Column>
          <Column field="status" header="Status" style="width: 120px">
            <template #body="{ data }">
              <Tag
                :value="data.status === 'FINALIZADA' ? 'Finalizada' : 'Rascunho'"
                :severity="data.status === 'FINALIZADA' ? 'success' : 'warn'"
              />
            </template>
          </Column>
          <Column field="totalProjetos" header="Projetos" style="width: 100px" />
          <Column field="totalUst" header="UST" style="width: 120px">
            <template #body="{ data }">
              {{ formatNumber(data.totalUst) }}
            </template>
          </Column>
          <Column field="valorTotal" header="Valor" style="width: 140px">
            <template #body="{ data }">
              {{ formatCurrency(data.valorTotal) }}
            </template>
          </Column>
          <template #empty>
            <div class="placeholder-content">
              <i class="pi pi-inbox"></i>
              <p>Nenhuma simulação cadastrada</p>
            </div>
          </template>
        </DataTable>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import Chart from 'primevue/chart'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import ProgressSpinner from 'primevue/progressspinner'
import { dashboardService } from '@/services/dashboardService'

const toast = useToast()
const dashboard = ref(null)
const loading = ref(false)

const CHART_COLORS = [
  '#1e3a5f', '#2d5a8e', '#3d7ab8', '#5a9fd4', '#7eb8e0',
  '#4a6741', '#6b8f5e', '#8fb87a', '#c4a35a', '#d4845a'
]

const tipoLabels = {
  PROJETO: 'Projeto',
  SUSTENTACAO: 'Sustentação',
  EVOLUCAO: 'Evolução',
  CORRECAO: 'Correção'
}

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { beginAtZero: true, title: { display: true, text: 'UST' } }
  }
}

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'right' } }
}

const pieOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' } }
}

const ustPorProjetoChart = computed(() => {
  const items = dashboard.value?.ustPorProjeto?.slice(0, 10) || []
  if (!items.length) return null

  return {
    labels: items.map((i) => i.projetoNome),
    datasets: [{
      data: items.map((i) => Number(i.totalUst)),
      backgroundColor: CHART_COLORS.slice(0, items.length),
      borderRadius: 4
    }]
  }
})

const perfilChart = computed(() => {
  const items = dashboard.value?.distribuicaoPorPerfil || []
  if (!items.length) return null

  return {
    labels: items.map((i) => i.perfilNome),
    datasets: [{
      data: items.map((i) => Number(i.totalUst)),
      backgroundColor: CHART_COLORS.slice(0, items.length)
    }]
  }
})

const tipoChart = computed(() => {
  const items = dashboard.value?.valorPorTipo || []
  if (!items.length) return null

  return {
    labels: items.map((i) => tipoLabels[i.tipo] || i.tipo),
    datasets: [{
      data: items.map((i) => Number(i.valorTotal)),
      backgroundColor: CHART_COLORS.slice(0, items.length)
    }]
  }
})

function formatNumber(value) {
  if (value == null) return '0,00'
  return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatCurrency(value) {
  if (value == null) return 'R$ 0,00'
  return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

async function carregar() {
  loading.value = true
  try {
    const { data } = await dashboardService.obterIndicadores()
    dashboard.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar o dashboard', life: 5000 })
  } finally {
    loading.value = false
  }
}

onMounted(carregar)
</script>

<style scoped>
.loading-state {
  display: flex;
  justify-content: center;
  padding: 4rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.summary-card {
  background: var(--p-surface-0);
  border: 1px solid var(--p-surface-200);
  border-radius: var(--p-border-radius-lg);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.summary-card.highlight {
  border-color: var(--p-primary-200);
  background: var(--p-primary-50);
}

.summary-icon {
  font-size: 1.25rem;
  color: var(--ust-primary, #1e3a5f);
  opacity: 0.7;
}

.summary-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--p-text-muted-color);
}

.summary-value {
  font-size: 1.75rem;
  font-weight: 600;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.chart-card {
  min-height: 320px;
}

.card-title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ust-primary, #1e3a5f);
}

.chart {
  height: 260px;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 260px;
  color: var(--p-text-muted-color);
}

.hours-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 260px;
  gap: 1rem;
}

.hours-total {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.hours-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--ust-primary, #1e3a5f);
}

.hours-label {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.hours-meta {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

@media (max-width: 900px) {
  .summary-grid,
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
