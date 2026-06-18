<template>
  <div>
    <div class="page-header">
      <div class="header-left">
        <Button icon="pi pi-arrow-left" text rounded @click="voltar" />
        <div>
          <h2 class="page-title">Projetos e Sustentações</h2>
          <p v-if="simulacao" class="simulacao-info">
            {{ simulacao.nomeCompleto }} — {{ simulacao.orgao }}
            <Tag
              :value="simulacao.status === 'FINALIZADA' ? 'Finalizada' : 'Rascunho'"
              :severity="simulacao.status === 'FINALIZADA' ? 'success' : 'warn'"
              class="ml-2"
            />
          </p>
        </div>
      </div>
      <Button
        v-if="authStore.canWrite && simulacao?.status === 'RASCUNHO'"
        label="Novo Projeto"
        icon="pi pi-plus"
        @click="abrirDialog()"
      />
    </div>

    <div class="page-card">
      <DataTable :value="projetos" :loading="loading" striped-rows paginator :rows="10">
        <Column field="nome" header="Nome" sortable />
        <Column field="tipo" header="Tipo" style="width: 140px">
          <template #body="{ data }">
            <Tag :value="tipoLabel(data.tipo)" :severity="tipoSeverity(data.tipo)" />
          </template>
        </Column>
        <Column field="semanas" header="Semanas" style="width: 100px" />
        <Column field="horasSemanais" header="H/Semana" style="width: 100px" />
        <Column field="descricao" header="Descrição">
          <template #body="{ data }">
            <span class="descricao-cell">{{ data.descricao || '—' }}</span>
          </template>
        </Column>
        <Column field="status" header="Status" style="width: 120px">
          <template #body="{ data }">
            <Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" />
          </template>
        </Column>
        <Column header="Ações" style="width: 160px">
          <template #body="{ data }">
            <div class="actions">
              <Button
                icon="pi pi-users"
                text
                rounded
                severity="help"
                title="Montar Squad"
                @click="irParaSquad(data)"
              />
              <template v-if="authStore.canWrite && simulacao?.status === 'RASCUNHO'">
                <Button
                  icon="pi pi-pencil"
                  text
                  rounded
                  severity="secondary"
                  @click="abrirDialog(data)"
                />
                <Button
                  icon="pi pi-trash"
                  text
                  rounded
                  severity="danger"
                  @click="confirmarExclusao(data)"
                />
              </template>
            </div>
          </template>
        </Column>
        <template #empty>
          <div class="placeholder-content">
            <i class="pi pi-briefcase"></i>
            <p>Nenhum projeto cadastrado nesta simulação</p>
          </div>
        </template>
      </DataTable>
    </div>

    <Dialog
      v-model:visible="dialogVisible"
      :header="editando ? 'Editar Projeto' : 'Novo Projeto — Etapa 4'"
      :style="{ width: '560px' }"
      modal
      :closable="!salvando"
    >
      <form class="projeto-form" @submit.prevent="salvar">
        <div class="field">
          <label for="nome">Nome do Projeto *</label>
          <InputText id="nome" v-model="form.nome" :invalid="!!errors.nome" fluid />
          <small v-if="errors.nome" class="p-error">{{ errors.nome }}</small>
        </div>

        <div class="field-row">
          <div class="field">
            <label for="tipo">Tipo *</label>
            <Select
              id="tipo"
              v-model="form.tipo"
              :options="tipoOptions"
              option-label="label"
              option-value="value"
              placeholder="Selecione"
              :invalid="!!errors.tipo"
              fluid
            />
            <small v-if="errors.tipo" class="p-error">{{ errors.tipo }}</small>
          </div>
          <div class="field">
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
        </div>

        <div class="field-row">
          <div class="field">
            <label for="semanas">Semanas *</label>
            <InputNumber id="semanas" v-model="form.semanas" :min="1" :invalid="!!errors.semanas" fluid />
            <small v-if="errors.semanas" class="p-error">{{ errors.semanas }}</small>
          </div>
          <div class="field">
            <label for="horasSemanais">Horas Semanais</label>
            <InputNumber id="horasSemanais" v-model="form.horasSemanais" :min="1" :max="168" suffix=" h" fluid />
          </div>
        </div>

        <div class="field">
          <label for="descricao">Descrição</label>
          <Textarea id="descricao" v-model="form.descricao" rows="4" fluid />
        </div>
      </form>

      <template #footer>
        <Button label="Cancelar" severity="secondary" text :disabled="salvando" @click="dialogVisible = false" />
        <Button label="Salvar" icon="pi pi-check" :loading="salvando" @click="salvar" />
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import { useAuthStore } from '@/stores/auth'
import { simulacaoService } from '@/services/simulacaoService'
import { projetoService } from '@/services/projetoService'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const authStore = useAuthStore()

const simulacaoId = route.params.simulacaoId
const simulacao = ref(null)
const projetos = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const salvando = ref(false)
const editando = ref(false)
const projetoId = ref(null)

const tipoOptions = [
  { label: 'Projeto', value: 'PROJETO' },
  { label: 'Sustentação', value: 'SUSTENTACAO' },
  { label: 'Evolução', value: 'EVOLUCAO' },
  { label: 'Correção', value: 'CORRECAO' }
]

const statusOptions = [
  { label: 'Ativo', value: 'ATIVO' },
  { label: 'Concluído', value: 'CONCLUIDO' },
  { label: 'Cancelado', value: 'CANCELADO' }
]

const form = reactive({
  nome: '',
  tipo: null,
  semanas: 12,
  horasSemanais: 40,
  descricao: '',
  status: 'ATIVO'
})

const errors = reactive({ nome: '', tipo: '', semanas: '' })

const tipoLabels = {
  PROJETO: 'Projeto',
  SUSTENTACAO: 'Sustentação',
  EVOLUCAO: 'Evolução',
  CORRECAO: 'Correção'
}

const tipoSeverities = {
  PROJETO: 'info',
  SUSTENTACAO: 'secondary',
  EVOLUCAO: 'success',
  CORRECAO: 'warn'
}

const statusLabels = { ATIVO: 'Ativo', CONCLUIDO: 'Concluído', CANCELADO: 'Cancelado' }
const statusSeverities = { ATIVO: 'success', CONCLUIDO: 'info', CANCELADO: 'danger' }

function tipoLabel(tipo) { return tipoLabels[tipo] || tipo }
function tipoSeverity(tipo) { return tipoSeverities[tipo] || 'secondary' }
function statusLabel(status) { return statusLabels[status] || status }
function statusSeverity(status) { return statusSeverities[status] || 'secondary' }

function voltar() {
  router.push({ name: 'simulacoes' })
}

function irParaSquad(projeto) {
  router.push({
    name: 'projeto-squad',
    params: { simulacaoId, projetoId: projeto.id }
  })
}

function resetForm() {
  form.nome = ''
  form.tipo = null
  form.semanas = 12
  form.horasSemanais = 40
  form.descricao = ''
  form.status = 'ATIVO'
  errors.nome = ''
  errors.tipo = ''
  errors.semanas = ''
  projetoId.value = null
  editando.value = false
}

function abrirDialog(projeto = null) {
  resetForm()
  if (projeto) {
    editando.value = true
    projetoId.value = projeto.id
    form.nome = projeto.nome
    form.tipo = projeto.tipo
    form.semanas = projeto.semanas
    form.horasSemanais = projeto.horasSemanais
    form.descricao = projeto.descricao || ''
    form.status = projeto.status
  }
  dialogVisible.value = true
}

function validar() {
  errors.nome = ''
  errors.tipo = ''
  errors.semanas = ''
  let valid = true

  if (!form.nome?.trim()) { errors.nome = 'Nome é obrigatório'; valid = false }
  if (!form.tipo) { errors.tipo = 'Tipo é obrigatório'; valid = false }
  if (!form.semanas || form.semanas < 1) { errors.semanas = 'Semanas inválido'; valid = false }

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

async function loadProjetos() {
  loading.value = true
  try {
    const { data } = await projetoService.listar(simulacaoId)
    projetos.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar projetos', life: 5000 })
  } finally {
    loading.value = false
  }
}

async function salvar() {
  if (!validar()) return

  salvando.value = true
  const payload = {
    nome: form.nome.trim(),
    tipo: form.tipo,
    semanas: form.semanas,
    horasSemanais: form.horasSemanais,
    descricao: form.descricao?.trim() || null,
    status: form.status
  }

  try {
    if (editando.value) {
      await projetoService.atualizar(simulacaoId, projetoId.value, payload)
      toast.add({ severity: 'success', summary: 'Projeto atualizado', life: 3000 })
    } else {
      await projetoService.criar(simulacaoId, payload)
      toast.add({ severity: 'success', summary: 'Projeto criado', life: 3000 })
    }
    dialogVisible.value = false
    await loadProjetos()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar o projeto'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

function confirmarExclusao(projeto) {
  confirm.require({
    message: `Deseja excluir o projeto "${projeto.nome}"?`,
    header: 'Confirmar exclusão',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Excluir',
    acceptClass: 'p-button-danger',
    accept: () => excluir(projeto.id)
  })
}

async function excluir(id) {
  try {
    await projetoService.excluir(simulacaoId, id)
    toast.add({ severity: 'success', summary: 'Projeto excluído', life: 3000 })
    await loadProjetos()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível excluir o projeto'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  }
}

onMounted(async () => {
  await carregarSimulacao()
  await loadProjetos()
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

.simulacao-info {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.ml-2 {
  margin-left: 0.25rem;
}

.actions {
  display: flex;
  gap: 0.25rem;
}

.descricao-cell {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-width: 300px;
}

.text-muted {
  color: var(--p-text-muted-color);
}

.projeto-form {
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

@media (max-width: 640px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
