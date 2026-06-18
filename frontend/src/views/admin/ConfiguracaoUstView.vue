<template>
  <div>
    <h2 class="page-title">Configurações</h2>

    <div v-if="authStore.isAdmin" class="page-card inst-card">
      <h3 class="card-title">Identidade institucional</h3>
      <p class="card-subtitle">Nome do órgão e logo exibidos nos relatórios e no sistema</p>

      <div class="inst-grid">
        <div class="inst-form">
          <div class="field">
            <label for="nomeOrganizacao">Nome do órgão *</label>
            <InputText
              id="nomeOrganizacao"
              v-model="instForm.nomeOrganizacao"
              placeholder="Ex.: Ministério da Defesa"
              :invalid="!!instErrors.nomeOrganizacao"
              fluid
            />
            <small v-if="instErrors.nomeOrganizacao" class="p-error">{{ instErrors.nomeOrganizacao }}</small>
          </div>
          <Button
            label="Salvar nome"
            icon="pi pi-save"
            :loading="salvandoInst"
            @click="salvarInstitucional"
          />
        </div>

        <div class="logo-area">
          <label>Logo do órgão</label>
          <div
            class="logo-dropzone"
            :class="{ 'is-dragging': isDragging, 'has-logo': !!logoPreviewUrl }"
            @dragenter.prevent="isDragging = true"
            @dragover.prevent
            @dragleave.prevent="onDragLeave"
            @drop.prevent="onDrop"
          >
            <img v-if="logoPreviewUrl" :src="logoPreviewUrl" alt="Logo" class="logo-img" />
            <div v-else class="logo-placeholder">
              <i class="pi pi-cloud-upload"></i>
              <span>Arraste o logo aqui</span>
              <small>ou use o botão abaixo</small>
            </div>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/png,image/jpeg,image/webp"
            class="hidden-input"
            @change="onFileSelected"
          />
          <div class="logo-actions">
            <Button
              label="Selecionar arquivo"
              icon="pi pi-upload"
              severity="secondary"
              outlined
              :disabled="enviandoLogo"
              @click="fileInputRef?.click()"
            />
            <Button
              v-if="instConfig.possuiLogo"
              label="Remover"
              icon="pi pi-trash"
              severity="danger"
              text
              :loading="removendoLogo"
              @click="removerLogo"
            />
          </div>
          <small class="logo-hint">PNG, JPG ou WEBP — máx. 2 MB. Suporta arrastar e soltar.</small>
        </div>
      </div>
    </div>

    <div class="config-grid">
      <div class="page-card form-card">
        <h3 class="card-title">Parâmetros vigentes</h3>
        <p class="card-subtitle">Configure valor da UST, carga horária, encargos e BDI</p>

        <form class="config-form" @submit.prevent="salvar">
          <div class="field">
            <label for="valorUst">Valor UST (R$) *</label>
            <InputNumber
              id="valorUst"
              v-model="form.valorUst"
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              :min="0.01"
              :invalid="!!errors.valorUst"
              :disabled="!authStore.isAdmin"
              fluid
            />
            <small v-if="errors.valorUst" class="p-error">{{ errors.valorUst }}</small>
          </div>

          <div class="field">
            <label for="cargaHoraria">Carga Horária Mês (h) *</label>
            <InputNumber
              id="cargaHoraria"
              v-model="form.cargaHorariaMes"
              :min="1"
              :max="744"
              suffix=" h"
              :invalid="!!errors.cargaHorariaMes"
              :disabled="!authStore.isAdmin"
              fluid
            />
            <small v-if="errors.cargaHorariaMes" class="p-error">{{ errors.cargaHorariaMes }}</small>
          </div>

          <div class="field-row">
            <div class="field">
              <label for="encargos">Encargos (%) *</label>
              <InputNumber
                id="encargos"
                v-model="form.encargosPercentual"
                :min="0"
                :max="500"
                :min-fraction-digits="2"
                :max-fraction-digits="2"
                suffix="%"
                :invalid="!!errors.encargosPercentual"
                :disabled="!authStore.isAdmin"
                fluid
              />
              <small v-if="errors.encargosPercentual" class="p-error">{{ errors.encargosPercentual }}</small>
            </div>

            <div class="field">
              <label for="bdi">BDI (%) *</label>
              <InputNumber
                id="bdi"
                v-model="form.bdiPercentual"
                :min="0"
                :max="500"
                :min-fraction-digits="2"
                :max-fraction-digits="2"
                suffix="%"
                :invalid="!!errors.bdiPercentual"
                :disabled="!authStore.isAdmin"
                fluid
              />
              <small v-if="errors.bdiPercentual" class="p-error">{{ errors.bdiPercentual }}</small>
            </div>
          </div>

          <div class="field">
            <label for="vigenteDesde">Vigente desde</label>
            <DatePicker
              id="vigenteDesde"
              v-model="form.vigenteDesde"
              date-format="dd/mm/yy"
              show-icon
              :disabled="!authStore.isAdmin"
              fluid
            />
          </div>

          <Button
            v-if="authStore.isAdmin"
            type="submit"
            label="Salvar configuração"
            icon="pi pi-save"
            :loading="salvando"
          />
        </form>
      </div>

      <div class="page-card preview-card">
        <h3 class="card-title">Resumo financeiro</h3>
        <p class="card-subtitle">Valores calculados com base nos parâmetros</p>

        <div class="preview-items">
          <div class="preview-item">
            <span class="preview-label">Valor UST base</span>
            <span class="preview-value">{{ formatCurrency(form.valorUst) }}</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">Com encargos ({{ formatPercent(form.encargosPercentual) }})</span>
            <span class="preview-value">{{ formatCurrency(valorComEncargos) }}</span>
          </div>
          <div class="preview-item highlight">
            <span class="preview-label">Valor final UST (c/ BDI {{ formatPercent(form.bdiPercentual) }})</span>
            <span class="preview-value">{{ formatCurrency(valorFinalUst) }}</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">Carga horária mensal</span>
            <span class="preview-value">{{ form.cargaHorariaMes || 0 }} h</span>
          </div>
        </div>

        <Message severity="info" :closable="false" class="formula-msg">
          Valor financeiro = UST × Valor UST × (1 + Encargos%) × (1 + BDI%)
        </Message>
      </div>
    </div>

    <div v-if="authStore.isAdmin" class="page-card historico-card">
      <h3 class="card-title">Histórico de configurações</h3>
      <DataTable :value="historico" :loading="loadingHistorico" striped-rows paginator :rows="5">
        <Column field="vigenteDesde" header="Vigente desde">
          <template #body="{ data }">
            {{ formatDate(data.vigenteDesde) }}
          </template>
        </Column>
        <Column field="valorUst" header="Valor UST">
          <template #body="{ data }">
            {{ formatCurrency(data.valorUst) }}
          </template>
        </Column>
        <Column field="cargaHorariaMes" header="Horas/mês" />
        <Column field="encargosPercentual" header="Encargos">
          <template #body="{ data }">
            {{ formatPercent(data.encargosPercentual) }}
          </template>
        </Column>
        <Column field="bdiPercentual" header="BDI">
          <template #body="{ data }">
            {{ formatPercent(data.bdiPercentual) }}
          </template>
        </Column>
        <Column field="ativo" header="Status">
          <template #body="{ data }">
            <Tag :value="data.ativo ? 'Ativa' : 'Inativa'" :severity="data.ativo ? 'success' : 'secondary'" />
          </template>
        </Column>
        <template #empty>
          <p class="empty-historico">Nenhum histórico disponível</p>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import InputNumber from 'primevue/inputnumber'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import Message from 'primevue/message'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import InputText from 'primevue/inputtext'
import { useAuthStore } from '@/stores/auth'
import { configuracaoUstService } from '@/services/configuracaoUstService'
import { configuracaoInstitucionalService } from '@/services/configuracaoInstitucionalService'

const toast = useToast()
const authStore = useAuthStore()

const salvandoInst = ref(false)
const enviandoLogo = ref(false)
const removendoLogo = ref(false)
const isDragging = ref(false)
const fileInputRef = ref(null)
const logoPreviewUrl = ref(null)
const instConfig = ref({ possuiLogo: false })
const instForm = reactive({ nomeOrganizacao: '' })
const instErrors = reactive({ nomeOrganizacao: '' })

const salvando = ref(false)
const loadingHistorico = ref(false)
const historico = ref([])

const form = reactive({
  valorUst: 180,
  cargaHorariaMes: 160,
  encargosPercentual: 75,
  bdiPercentual: 25,
  vigenteDesde: new Date()
})

const errors = reactive({
  valorUst: '',
  cargaHorariaMes: '',
  encargosPercentual: '',
  bdiPercentual: ''
})

const valorComEncargos = computed(() => {
  const base = Number(form.valorUst) || 0
  const encargos = Number(form.encargosPercentual) || 0
  return base * (1 + encargos / 100)
})

const valorFinalUst = computed(() => {
  const bdi = Number(form.bdiPercentual) || 0
  return valorComEncargos.value * (1 + bdi / 100)
})

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value) || 0)
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

function toIsoDate(date) {
  if (!date) return null
  const d = date instanceof Date ? date : new Date(date)
  return d.toISOString().split('T')[0]
}

function preencherForm(config) {
  form.valorUst = Number(config.valorUst)
  form.cargaHorariaMes = config.cargaHorariaMes
  form.encargosPercentual = Number(config.encargosPercentual)
  form.bdiPercentual = Number(config.bdiPercentual)
  form.vigenteDesde = config.vigenteDesde ? new Date(config.vigenteDesde + 'T12:00:00') : new Date()
}

function validar() {
  errors.valorUst = ''
  errors.cargaHorariaMes = ''
  errors.encargosPercentual = ''
  errors.bdiPercentual = ''
  let valid = true

  if (!form.valorUst || form.valorUst <= 0) {
    errors.valorUst = 'Valor deve ser maior que zero'
    valid = false
  }
  if (!form.cargaHorariaMes || form.cargaHorariaMes < 1) {
    errors.cargaHorariaMes = 'Carga horária inválida'
    valid = false
  }
  if (form.encargosPercentual == null || form.encargosPercentual < 0) {
    errors.encargosPercentual = 'Encargos inválido'
    valid = false
  }
  if (form.bdiPercentual == null || form.bdiPercentual < 0) {
    errors.bdiPercentual = 'BDI inválido'
    valid = false
  }

  return valid
}

async function carregarInstitucional() {
  try {
    const { data } = await configuracaoInstitucionalService.buscar()
    instConfig.value = data
    instForm.nomeOrganizacao = data.nomeOrganizacao || ''
    await carregarLogoPreview(data.possuiLogo)
  } catch {
    toast.add({ severity: 'warn', summary: 'Aviso', detail: 'Configuração institucional não encontrada', life: 5000 })
  }
}

async function carregarLogoPreview(possuiLogo) {
  if (logoPreviewUrl.value) {
    URL.revokeObjectURL(logoPreviewUrl.value)
    logoPreviewUrl.value = null
  }
  if (!possuiLogo) return
  try {
    const { data } = await configuracaoInstitucionalService.obterLogo()
    logoPreviewUrl.value = URL.createObjectURL(data)
  } catch {
    logoPreviewUrl.value = null
  }
}

function validarInstitucional() {
  instErrors.nomeOrganizacao = ''
  if (!instForm.nomeOrganizacao?.trim()) {
    instErrors.nomeOrganizacao = 'Nome do órgão é obrigatório'
    return false
  }
  return true
}

async function salvarInstitucional() {
  if (!validarInstitucional()) return
  salvandoInst.value = true
  try {
    const { data } = await configuracaoInstitucionalService.salvar({
      nomeOrganizacao: instForm.nomeOrganizacao.trim()
    })
    instConfig.value = data
    toast.add({ severity: 'success', summary: 'Nome do órgão salvo', life: 3000 })
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvandoInst.value = false
  }
}

async function enviarLogo(arquivo) {
  if (!arquivo) return
  enviandoLogo.value = true
  try {
    const { data } = await configuracaoInstitucionalService.uploadLogo(arquivo)
    instConfig.value = data
    await carregarLogoPreview(data.possuiLogo)
    toast.add({ severity: 'success', summary: 'Logo enviado', life: 3000 })
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível enviar o logo'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    enviandoLogo.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

function onDragLeave(event) {
  if (!event.currentTarget.contains(event.relatedTarget)) {
    isDragging.value = false
  }
}

function onDrop(event) {
  isDragging.value = false
  const arquivo = event.dataTransfer?.files?.[0]
  if (arquivo) enviarLogo(arquivo)
}

function onFileSelected(event) {
  const arquivo = event.target.files?.[0]
  if (arquivo) enviarLogo(arquivo)
}

async function removerLogo() {
  removendoLogo.value = true
  try {
    const { data } = await configuracaoInstitucionalService.removerLogo()
    instConfig.value = data
    if (logoPreviewUrl.value) {
      URL.revokeObjectURL(logoPreviewUrl.value)
      logoPreviewUrl.value = null
    }
    toast.add({ severity: 'success', summary: 'Logo removido', life: 3000 })
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível remover o logo'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    removendoLogo.value = false
  }
}

async function carregarAtiva() {
  try {
    const { data } = await configuracaoUstService.buscarAtiva()
    preencherForm(data)
  } catch {
    toast.add({ severity: 'warn', summary: 'Aviso', detail: 'Configuração UST não encontrada', life: 5000 })
  }
}

async function carregarHistorico() {
  if (!authStore.isAdmin) return
  loadingHistorico.value = true
  try {
    const { data } = await configuracaoUstService.listarHistorico()
    historico.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar histórico', life: 5000 })
  } finally {
    loadingHistorico.value = false
  }
}

async function salvar() {
  if (!validar()) return

  salvando.value = true
  const payload = {
    valorUst: form.valorUst,
    cargaHorariaMes: form.cargaHorariaMes,
    encargosPercentual: form.encargosPercentual,
    bdiPercentual: form.bdiPercentual,
    vigenteDesde: toIsoDate(form.vigenteDesde)
  }

  try {
    const { data } = await configuracaoUstService.salvar(payload)
    preencherForm(data)
    toast.add({ severity: 'success', summary: 'Configuração salva', life: 3000 })
    await carregarHistorico()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar a configuração'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

onMounted(async () => {
  if (authStore.isAdmin) await carregarInstitucional()
  await carregarAtiva()
  await carregarHistorico()
})
</script>

<style scoped>
.inst-card {
  margin-bottom: 1.5rem;
}

.inst-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  align-items: start;
}

.inst-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.logo-area {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.logo-area > label {
  font-weight: 500;
  font-size: 0.875rem;
}

.logo-dropzone {
  border: 2px dashed var(--p-surface-300);
  border-radius: var(--p-border-radius-lg);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 140px;
  background: var(--p-surface-50);
  transition: border-color 0.2s, background 0.2s;
}

.logo-dropzone.is-dragging {
  border-color: var(--ust-accent, #3b82f6);
  background: rgba(59, 130, 246, 0.06);
}

.logo-dropzone.has-logo {
  border-style: solid;
  border-color: var(--p-surface-200);
}

.hidden-input {
  display: none;
}

.logo-img {
  max-height: 80px;
  max-width: 200px;
  object-fit: contain;
}

.logo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  color: var(--p-text-muted-color);
  text-align: center;
}

.logo-placeholder i {
  font-size: 2.25rem;
  opacity: 0.5;
  color: var(--ust-primary);
}

.logo-placeholder small {
  font-size: 0.75rem;
  opacity: 0.8;
}

.logo-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.logo-hint {
  color: var(--p-text-muted-color);
  font-size: 0.75rem;
}

.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ust-primary);
  margin-bottom: 0.25rem;
}

.card-subtitle {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  margin-bottom: 1.5rem;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field label {
  font-weight: 500;
  font-size: 0.875rem;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.preview-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--p-surface-200);
}

.preview-item.highlight {
  background: var(--p-surface-50);
  padding: 0.75rem;
  border-radius: var(--p-border-radius-md);
  border-bottom: none;
}

.preview-label {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.preview-value {
  font-weight: 600;
  font-size: 1rem;
}

.preview-item.highlight .preview-value {
  color: var(--ust-primary);
  font-size: 1.25rem;
}

.formula-msg {
  margin-top: 0.5rem;
}

.historico-card {
  margin-top: 0;
}

.empty-historico {
  text-align: center;
  padding: 1rem;
  color: var(--p-text-muted-color);
}

@media (max-width: 900px) {
  .inst-grid {
    grid-template-columns: 1fr;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
