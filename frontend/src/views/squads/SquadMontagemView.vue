<template>
  <div>
    <div class="page-header">
      <div class="header-left">
        <Button icon="pi pi-arrow-left" text rounded @click="voltar" />
        <div>
          <h2 class="page-title">Montagem de Squad — Etapa 5</h2>
          <p v-if="squad" class="projeto-info">
            {{ squad.projetoNome }}
            <span class="meta">· {{ squad.semanas }} semanas · {{ squad.horasSemanais }}h/semana</span>
            <Tag
              v-if="simulacao"
              :value="simulacao.status === 'FINALIZADA' ? 'Finalizada' : 'Rascunho'"
              :severity="simulacao.status === 'FINALIZADA' ? 'success' : 'warn'"
              class="ml-2"
            />
          </p>
        </div>
      </div>
      <Button
        v-if="podeEditar"
        label="Salvar Squad"
        icon="pi pi-save"
        :loading="salvando"
        @click="salvar"
      />
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <span class="summary-label">Total de Horas</span>
        <span class="summary-value">{{ formatNumber(squad?.totalHoras) }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">Total UST</span>
        <span class="summary-value">{{ formatNumber(squad?.totalUst) }}</span>
      </div>
      <div class="summary-card highlight">
        <span class="summary-label">Valor Total</span>
        <span class="summary-value">{{ formatCurrency(squad?.valorTotal) }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">Valor UST</span>
        <span class="summary-value">{{ formatCurrency(squad?.valorUst) }}</span>
        <span class="summary-hint">
          Encargos {{ formatPercent(squad?.encargosPercentual) }} · BDI {{ formatPercent(squad?.bdiPercentual) }}
        </span>
      </div>
    </div>

    <div class="page-card">
      <div class="card-toolbar">
        <h3 class="card-title">Composição da Squad</h3>
        <Button
          v-if="podeEditar"
          label="Adicionar Perfil"
          icon="pi pi-plus"
          size="small"
          @click="adicionarMembro"
        />
      </div>

      <DataTable :value="membros" :loading="loading" striped-rows>
        <Column header="Perfil" style="min-width: 220px">
          <template #body="{ data, index }">
            <Select
              v-if="podeEditar"
              v-model="data.perfilId"
              :options="perfisDisponiveis(index)"
              option-label="nome"
              option-value="id"
              placeholder="Selecione o perfil"
              :invalid="!!errors[index]?.perfilId"
              fluid
              @change="onPerfilChange(data)"
            >
              <template #option="{ option }">
                <span>{{ option.nome }}</span>
                <span class="fcp-badge">FCP {{ formatNumber(option.fcp) }}</span>
              </template>
            </Select>
            <span v-else>{{ data.perfilNome }}</span>
            <small v-if="errors[index]?.perfilId" class="p-error">{{ errors[index].perfilId }}</small>
          </template>
        </Column>
        <Column header="FCP" style="width: 90px">
          <template #body="{ data }">
            {{ formatNumber(data.fcpAplicado) }}
          </template>
        </Column>
        <Column header="Qtd" style="width: 120px">
          <template #body="{ data, index }">
            <InputNumber
              v-if="podeEditar"
              v-model="data.quantidade"
              :min="1"
              :max="99"
              :invalid="!!errors[index]?.quantidade"
              fluid
              @update:model-value="recalcularLocal"
            />
            <span v-else>{{ data.quantidade }}</span>
          </template>
        </Column>
        <Column header="Horas" style="width: 110px">
          <template #body="{ data }">
            {{ formatNumber(data.horasCalculadas) }}
          </template>
        </Column>
        <Column header="UST" style="width: 110px">
          <template #body="{ data }">
            {{ formatNumber(data.ustCalculada) }}
          </template>
        </Column>
        <Column v-if="podeEditar" header="" style="width: 60px">
          <template #body="{ index }">
            <Button
              icon="pi pi-trash"
              text
              rounded
              severity="danger"
              @click="removerMembro(index)"
            />
          </template>
        </Column>
        <template #empty>
          <div class="placeholder-content">
            <i class="pi pi-users"></i>
            <p>Nenhum membro na squad</p>
            <Button
              v-if="podeEditar"
              label="Adicionar primeiro perfil"
              icon="pi pi-plus"
              size="small"
              @click="adicionarMembro"
            />
          </div>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Select from 'primevue/select'
import InputNumber from 'primevue/inputnumber'
import { useAuthStore } from '@/stores/auth'
import { simulacaoService } from '@/services/simulacaoService'
import { squadService } from '@/services/squadService'
import { perfilService } from '@/services/perfilService'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const authStore = useAuthStore()

const simulacaoId = route.params.simulacaoId
const projetoId = route.params.projetoId

const simulacao = ref(null)
const squad = ref(null)
const perfis = ref([])
const membros = ref([])
const loading = ref(false)
const salvando = ref(false)
const errors = ref({})

const podeEditar = computed(() =>
  authStore.canWrite && simulacao.value?.status === 'RASCUNHO'
)

function voltar() {
  router.push({ name: 'simulacao-projetos', params: { simulacaoId } })
}

function formatNumber(value) {
  if (value == null) return '—'
  return Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatCurrency(value) {
  if (value == null) return '—'
  return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatPercent(value) {
  if (value == null) return '—'
  return `${Number(value).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}%`
}

function perfisDisponiveis(indexAtual) {
  const usados = membros.value
    .map((m, i) => (i !== indexAtual ? m.perfilId : null))
    .filter(Boolean)
  return perfis.value.filter((p) => !usados.includes(p.id))
}

function onPerfilChange(membro) {
  const perfil = perfis.value.find((p) => p.id === membro.perfilId)
  if (perfil) {
    membro.perfilNome = perfil.nome
    membro.fcpAplicado = perfil.fcp
  }
  recalcularLocal()
}

function adicionarMembro() {
  membros.value.push({
    perfilId: null,
    perfilNome: '',
    quantidade: 1,
    fcpAplicado: null,
    horasCalculadas: null,
    ustCalculada: null
  })
}

function removerMembro(index) {
  membros.value.splice(index, 1)
  recalcularLocal()
}

function recalcularLocal() {
  if (!squad.value) return

  const horasSemanais = squad.value.horasSemanais || 0
  const semanas = squad.value.semanas || 0
  let totalHoras = 0
  let totalUst = 0

  membros.value.forEach((membro) => {
    if (!membro.quantidade || !membro.fcpAplicado) {
      membro.horasCalculadas = null
      membro.ustCalculada = null
      return
    }

    const horas = membro.quantidade * horasSemanais * semanas
    const ust = horas * Number(membro.fcpAplicado)

    membro.horasCalculadas = horas
    membro.ustCalculada = ust
    totalHoras += horas
    totalUst += ust
  })

  squad.value.totalHoras = totalHoras || null
  squad.value.totalUst = totalUst || null

  if (squad.value.valorUst != null) {
    const encargos = 1 + Number(squad.value.encargosPercentual || 0) / 100
    const bdi = 1 + Number(squad.value.bdiPercentual || 0) / 100
    squad.value.valorTotal = totalUst * Number(squad.value.valorUst) * encargos * bdi
  }
}

function validar() {
  errors.value = {}
  let valid = true

  membros.value.forEach((membro, index) => {
    const rowErrors = {}
    if (!membro.perfilId) {
      rowErrors.perfilId = 'Perfil obrigatório'
      valid = false
    }
    if (!membro.quantidade || membro.quantidade < 1) {
      rowErrors.quantidade = 'Quantidade inválida'
      valid = false
    }
    if (Object.keys(rowErrors).length) {
      errors.value[index] = rowErrors
    }
  })

  return valid
}

async function carregarSimulacao() {
  try {
    const { data } = await simulacaoService.buscar(simulacaoId)
    simulacao.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Simulação não encontrada', life: 5000 })
    voltar()
  }
}

async function carregarDados() {
  loading.value = true
  try {
    const [squadRes, perfisRes] = await Promise.all([
      squadService.buscar(simulacaoId, projetoId),
      perfilService.listar(true)
    ])

    squad.value = squadRes.data
    perfis.value = perfisRes.data
    membros.value = (squadRes.data.membros || []).map((m) => ({ ...m }))
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar a squad', life: 5000 })
  } finally {
    loading.value = false
  }
}

async function salvar() {
  if (!validar()) return

  salvando.value = true
  const payload = {
    membros: membros.value.map((m) => ({
      perfilId: m.perfilId,
      quantidade: m.quantidade
    }))
  }

  try {
    const { data } = await squadService.salvar(simulacaoId, projetoId, payload)
    squad.value = data
    membros.value = (data.membros || []).map((m) => ({ ...m }))
    toast.add({ severity: 'success', summary: 'Squad salva', life: 3000 })
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar a squad'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

onMounted(async () => {
  await carregarSimulacao()
  await carregarDados()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  gap: 1rem;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.page-header .page-title {
  margin-bottom: 0.25rem;
}

.projeto-info {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.meta {
  color: var(--p-text-muted-color);
}

.ml-2 {
  margin-left: 0.25rem;
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

.summary-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--p-text-muted-color);
}

.summary-value {
  font-size: 1.5rem;
  font-weight: 600;
}

.summary-hint {
  font-size: 0.75rem;
  color: var(--p-text-muted-color);
}

.card-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.card-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.fcp-badge {
  margin-left: 0.5rem;
  font-size: 0.75rem;
  color: var(--p-text-muted-color);
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
