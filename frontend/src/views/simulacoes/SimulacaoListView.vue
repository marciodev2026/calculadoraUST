<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">Simulações</h2>
      <Button
        v-if="authStore.canWrite"
        label="Nova Simulação"
        icon="pi pi-plus"
        @click="abrirDialog()"
      />
    </div>

    <div class="page-card">
      <DataTable
        :value="simulacoes"
        :loading="loading"
        striped-rows
        paginator
        :rows="10"
        :rows-per-page-options="[10, 25, 50]"
        sort-field="dataSimulacao"
        :sort-order="-1"
      >
        <Column field="nomeCompleto" header="Solicitante" sortable />
        <Column field="email" header="E-mail" />
        <Column field="orgao" header="Órgão" sortable />
        <Column field="departamento" header="Departamento" />
        <Column field="dataSimulacao" header="Data" sortable style="width: 120px">
          <template #body="{ data }">
            {{ formatDate(data.dataSimulacao) }}
          </template>
        </Column>
        <Column field="status" header="Status" style="width: 120px">
          <template #body="{ data }">
            <Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" />
          </template>
        </Column>
        <Column header="Ações" style="width: 180px">
          <template #body="{ data }">
            <div class="actions">
              <Button
                icon="pi pi-briefcase"
                text
                rounded
                severity="help"
                title="Projetos"
                @click="irParaProjetos(data)"
              />
              <Button
                icon="pi pi-eye"
                text
                rounded
                severity="info"
                title="Visualizar"
                @click="visualizar(data)"
              />
              <Button
                v-if="authStore.canWrite && data.status === 'RASCUNHO'"
                icon="pi pi-pencil"
                text
                rounded
                severity="secondary"
                title="Editar"
                @click="abrirDialog(data)"
              />
              <Button
                v-if="authStore.canWrite && data.status === 'RASCUNHO'"
                icon="pi pi-trash"
                text
                rounded
                severity="danger"
                title="Excluir"
                @click="confirmarExclusao(data)"
              />
            </div>
          </template>
        </Column>
        <template #empty>
          <div class="placeholder-content">
            <i class="pi pi-calculator"></i>
            <p>Nenhuma simulação cadastrada</p>
          </div>
        </template>
      </DataTable>
    </div>

    <!-- Dialog criar/editar -->
    <Dialog
      v-model:visible="dialogVisible"
      :header="editando ? 'Editar Simulação' : 'Nova Simulação — Etapa 1'"
      :style="{ width: '560px' }"
      modal
      :closable="!salvando"
    >
      <form class="simulacao-form" @submit.prevent="salvar">
        <div class="field">
          <label for="nomeCompleto">Nome Completo *</label>
          <InputText
            id="nomeCompleto"
            v-model="form.nomeCompleto"
            placeholder="Ex: Márcio Silva"
            :invalid="!!errors.nomeCompleto"
            fluid
          />
          <small v-if="errors.nomeCompleto" class="p-error">{{ errors.nomeCompleto }}</small>
        </div>

        <div class="field">
          <label for="email">E-mail *</label>
          <InputText
            id="email"
            v-model="form.email"
            type="email"
            placeholder="email@orgao.gov.br"
            :invalid="!!errors.email"
            fluid
          />
          <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
        </div>

        <div class="field-row">
          <div class="field">
            <label for="orgao">Órgão *</label>
            <InputText
              id="orgao"
              v-model="form.orgao"
              placeholder="Ex: Exército Brasileiro"
              :invalid="!!errors.orgao"
              fluid
            />
            <small v-if="errors.orgao" class="p-error">{{ errors.orgao }}</small>
          </div>
          <div class="field">
            <label for="departamento">Departamento *</label>
            <InputText
              id="departamento"
              v-model="form.departamento"
              placeholder="Ex: Centro de Desenvolvimento"
              :invalid="!!errors.departamento"
              fluid
            />
            <small v-if="errors.departamento" class="p-error">{{ errors.departamento }}</small>
          </div>
        </div>

        <div class="field-row">
          <div class="field">
            <label for="telefone">Telefone</label>
            <InputText
              id="telefone"
              v-model="form.telefone"
              placeholder="(00) 00000-0000"
              fluid
            />
          </div>
          <div class="field">
            <label for="dataSimulacao">Data da Simulação *</label>
            <DatePicker
              id="dataSimulacao"
              v-model="form.dataSimulacao"
              date-format="dd/mm/yy"
              show-icon
              :invalid="!!errors.dataSimulacao"
              fluid
            />
            <small v-if="errors.dataSimulacao" class="p-error">{{ errors.dataSimulacao }}</small>
          </div>
        </div>

        <div v-if="editando" class="field">
          <label for="status">Status</label>
          <Select
            id="status"
            v-model="form.status"
            :options="statusOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>
      </form>

      <template #footer>
        <Button label="Cancelar" severity="secondary" text :disabled="salvando" @click="dialogVisible = false" />
        <Button label="Salvar" icon="pi pi-check" :loading="salvando" @click="salvar" />
      </template>
    </Dialog>

    <!-- Dialog visualizar -->
    <Dialog
      v-model:visible="detalheVisible"
      header="Detalhes da Simulação"
      :style="{ width: '520px' }"
      modal
    >
      <div v-if="detalhe" class="detalhe-grid">
        <div class="detalhe-item">
          <span class="detalhe-label">Solicitante</span>
          <span>{{ detalhe.nomeCompleto }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">E-mail</span>
          <span>{{ detalhe.email }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Órgão</span>
          <span>{{ detalhe.orgao }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Departamento</span>
          <span>{{ detalhe.departamento }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Telefone</span>
          <span>{{ detalhe.telefone || '—' }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Data da Simulação</span>
          <span>{{ formatDate(detalhe.dataSimulacao) }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Status</span>
          <Tag :value="statusLabel(detalhe.status)" :severity="statusSeverity(detalhe.status)" />
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Responsável</span>
          <span>{{ detalhe.usuarioNome }}</span>
        </div>
        <div class="detalhe-item">
          <span class="detalhe-label">Criado em</span>
          <span>{{ formatDateTime(detalhe.createdAt) }}</span>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import { useAuthStore } from '@/stores/auth'
import { simulacaoService } from '@/services/simulacaoService'

const toast = useToast()
const confirm = useConfirm()
const authStore = useAuthStore()
const router = useRouter()

const simulacoes = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detalheVisible = ref(false)
const salvando = ref(false)
const editando = ref(false)
const simulacaoId = ref(null)
const detalhe = ref(null)

const statusOptions = [
  { label: 'Rascunho', value: 'RASCUNHO' },
  { label: 'Finalizada', value: 'FINALIZADA' }
]

const form = reactive({
  nomeCompleto: '',
  email: '',
  orgao: '',
  departamento: '',
  telefone: '',
  dataSimulacao: new Date(),
  status: 'RASCUNHO'
})

const errors = reactive({
  nomeCompleto: '',
  email: '',
  orgao: '',
  departamento: '',
  dataSimulacao: ''
})

function statusLabel(status) {
  return status === 'FINALIZADA' ? 'Finalizada' : 'Rascunho'
}

function statusSeverity(status) {
  return status === 'FINALIZADA' ? 'success' : 'warn'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('pt-BR')
}

function toIsoDate(date) {
  if (!date) return null
  const d = date instanceof Date ? date : new Date(date)
  return d.toISOString().split('T')[0]
}

function resetForm() {
  const user = authStore.user
  form.nomeCompleto = user?.nomeCompleto || ''
  form.email = user?.email || ''
  form.orgao = user?.orgao || ''
  form.departamento = user?.departamento || ''
  form.telefone = user?.telefone || ''
  form.dataSimulacao = new Date()
  form.status = 'RASCUNHO'
  errors.nomeCompleto = ''
  errors.email = ''
  errors.orgao = ''
  errors.departamento = ''
  errors.dataSimulacao = ''
  simulacaoId.value = null
  editando.value = false
}

function abrirDialog(simulacao = null) {
  resetForm()
  if (simulacao) {
    editando.value = true
    simulacaoId.value = simulacao.id
    form.nomeCompleto = simulacao.nomeCompleto
    form.email = simulacao.email
    form.orgao = simulacao.orgao
    form.departamento = simulacao.departamento
    form.telefone = simulacao.telefone || ''
    form.dataSimulacao = new Date(simulacao.dataSimulacao + 'T12:00:00')
    form.status = simulacao.status
  }
  dialogVisible.value = true
}

function visualizar(simulacao) {
  detalhe.value = simulacao
  detalheVisible.value = true
}

function irParaProjetos(simulacao) {
  router.push({ name: 'simulacao-projetos', params: { simulacaoId: simulacao.id } })
}

function validar() {
  errors.nomeCompleto = ''
  errors.email = ''
  errors.orgao = ''
  errors.departamento = ''
  errors.dataSimulacao = ''
  let valid = true

  if (!form.nomeCompleto?.trim()) {
    errors.nomeCompleto = 'Nome é obrigatório'
    valid = false
  }
  if (!form.email?.trim()) {
    errors.email = 'E-mail é obrigatório'
    valid = false
  }
  if (!form.orgao?.trim()) {
    errors.orgao = 'Órgão é obrigatório'
    valid = false
  }
  if (!form.departamento?.trim()) {
    errors.departamento = 'Departamento é obrigatório'
    valid = false
  }
  if (!form.dataSimulacao) {
    errors.dataSimulacao = 'Data é obrigatória'
    valid = false
  }

  return valid
}

async function loadSimulacoes() {
  loading.value = true
  try {
    const { data } = await simulacaoService.listar()
    simulacoes.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar simulações', life: 5000 })
  } finally {
    loading.value = false
  }
}

async function salvar() {
  if (!validar()) return

  salvando.value = true
  const payload = {
    nomeCompleto: form.nomeCompleto.trim(),
    email: form.email.trim(),
    orgao: form.orgao.trim(),
    departamento: form.departamento.trim(),
    telefone: form.telefone?.trim() || null,
    dataSimulacao: toIsoDate(form.dataSimulacao),
    status: form.status
  }

  try {
    if (editando.value) {
      await simulacaoService.atualizar(simulacaoId.value, payload)
      toast.add({ severity: 'success', summary: 'Simulação atualizada', life: 3000 })
    } else {
      await simulacaoService.criar(payload)
      toast.add({ severity: 'success', summary: 'Simulação criada', life: 3000 })
    }
    dialogVisible.value = false
    await loadSimulacoes()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar a simulação'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

function confirmarExclusao(simulacao) {
  confirm.require({
    message: `Deseja excluir a simulação de "${simulacao.nomeCompleto}"?`,
    header: 'Confirmar exclusão',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Excluir',
    acceptClass: 'p-button-danger',
    accept: () => excluir(simulacao.id)
  })
}

async function excluir(id) {
  try {
    await simulacaoService.excluir(id)
    toast.add({ severity: 'success', summary: 'Simulação excluída', life: 3000 })
    await loadSimulacoes()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível excluir a simulação'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  }
}

onMounted(loadSimulacoes)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.page-header .page-title {
  margin-bottom: 0;
}

.actions {
  display: flex;
  gap: 0.25rem;
}

.simulacao-form {
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

.detalhe-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detalhe-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detalhe-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--p-text-muted-color);
}

@media (max-width: 640px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
