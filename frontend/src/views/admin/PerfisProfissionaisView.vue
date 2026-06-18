<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">Perfis Profissionais</h2>
      <Button
        v-if="authStore.isAdmin"
        label="Novo Perfil"
        icon="pi pi-plus"
        @click="abrirDialog()"
      />
    </div>

    <div class="page-card">
      <DataTable
        :value="perfis"
        :loading="loading"
        striped-rows
        paginator
        :rows="10"
        :rows-per-page-options="[10, 25, 50]"
        sort-field="fcp"
        :sort-order="1"
      >
        <Column field="nome" header="Perfil" sortable />
        <Column field="descricao" header="Descrição" />
        <Column field="fcp" header="FCP" sortable style="width: 100px">
          <template #body="{ data }">
            <Tag :value="formatFcp(data.fcp)" severity="info" />
          </template>
        </Column>
        <Column field="ativo" header="Status" style="width: 110px">
          <template #body="{ data }">
            <Tag
              :value="data.ativo ? 'Ativo' : 'Inativo'"
              :severity="data.ativo ? 'success' : 'danger'"
            />
          </template>
        </Column>
        <Column v-if="authStore.isAdmin" header="Ações" style="width: 120px">
          <template #body="{ data }">
            <div class="actions">
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
            </div>
          </template>
        </Column>
        <template #empty>
          <div class="placeholder-content">
            <i class="pi pi-users"></i>
            <p>Nenhum perfil cadastrado</p>
          </div>
        </template>
      </DataTable>
    </div>

    <Dialog
      v-model:visible="dialogVisible"
      :header="editando ? 'Editar Perfil' : 'Novo Perfil'"
      :style="{ width: '480px' }"
      modal
      :closable="!salvando"
    >
      <form class="perfil-form" @submit.prevent="salvar">
        <div class="field">
          <label for="nome">Nome *</label>
          <InputText
            id="nome"
            v-model="form.nome"
            placeholder="Ex: Desenvolvedor Pleno"
            :invalid="!!errors.nome"
            fluid
          />
          <small v-if="errors.nome" class="p-error">{{ errors.nome }}</small>
        </div>

        <div class="field">
          <label for="descricao">Descrição</label>
          <Textarea
            id="descricao"
            v-model="form.descricao"
            rows="3"
            placeholder="Descrição do perfil"
            fluid
          />
        </div>

        <div class="field">
          <label for="fcp">FCP (Fator de Complexidade Profissional) *</label>
          <InputNumber
            id="fcp"
            v-model="form.fcp"
            :min-fraction-digits="2"
            :max-fraction-digits="2"
            :min="0.01"
            :max="9.99"
            :step="0.1"
            :invalid="!!errors.fcp"
            fluid
          />
          <small v-if="errors.fcp" class="p-error">{{ errors.fcp }}</small>
        </div>

        <div class="field-checkbox">
          <Checkbox v-model="form.ativo" input-id="ativo" :binary="true" />
          <label for="ativo">Perfil ativo</label>
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
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
import { useAuthStore } from '@/stores/auth'
import { perfilService } from '@/services/perfilService'

const toast = useToast()
const confirm = useConfirm()
const authStore = useAuthStore()

const perfis = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const salvando = ref(false)
const editando = ref(false)
const perfilId = ref(null)

const form = reactive({
  nome: '',
  descricao: '',
  fcp: 1.0,
  ativo: true
})

const errors = reactive({
  nome: '',
  fcp: ''
})

function formatFcp(fcp) {
  return Number(fcp).toFixed(2)
}

function resetForm() {
  form.nome = ''
  form.descricao = ''
  form.fcp = 1.0
  form.ativo = true
  errors.nome = ''
  errors.fcp = ''
  perfilId.value = null
  editando.value = false
}

function abrirDialog(perfil = null) {
  resetForm()
  if (perfil) {
    editando.value = true
    perfilId.value = perfil.id
    form.nome = perfil.nome
    form.descricao = perfil.descricao || ''
    form.fcp = Number(perfil.fcp)
    form.ativo = perfil.ativo
  }
  dialogVisible.value = true
}

function validar() {
  errors.nome = ''
  errors.fcp = ''
  let valid = true

  if (!form.nome?.trim()) {
    errors.nome = 'Nome é obrigatório'
    valid = false
  }
  if (!form.fcp || form.fcp <= 0) {
    errors.fcp = 'FCP deve ser maior que zero'
    valid = false
  }

  return valid
}

async function loadPerfis() {
  loading.value = true
  try {
    const { data } = await perfilService.listar()
    perfis.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar perfis', life: 5000 })
  } finally {
    loading.value = false
  }
}

async function salvar() {
  if (!validar()) return

  salvando.value = true
  const payload = {
    nome: form.nome.trim(),
    descricao: form.descricao?.trim() || null,
    fcp: form.fcp,
    ativo: form.ativo
  }

  try {
    if (editando.value) {
      await perfilService.atualizar(perfilId.value, payload)
      toast.add({ severity: 'success', summary: 'Perfil atualizado', life: 3000 })
    } else {
      await perfilService.criar(payload)
      toast.add({ severity: 'success', summary: 'Perfil criado', life: 3000 })
    }
    dialogVisible.value = false
    await loadPerfis()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível salvar o perfil'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

function confirmarExclusao(perfil) {
  confirm.require({
    message: `Deseja excluir o perfil "${perfil.nome}"?`,
    header: 'Confirmar exclusão',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Excluir',
    acceptClass: 'p-button-danger',
    accept: () => excluir(perfil.id)
  })
}

async function excluir(id) {
  try {
    await perfilService.excluir(id)
    toast.add({ severity: 'success', summary: 'Perfil excluído', life: 3000 })
    await loadPerfis()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível excluir o perfil'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  }
}

onMounted(loadPerfis)
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

.perfil-form {
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

.field-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
