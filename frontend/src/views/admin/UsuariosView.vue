<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">{{ tituloPagina }}</h2>
      <Button
        label="Novo usuário"
        icon="pi pi-user-plus"
        @click="abrirDialog"
      />
    </div>

    <div class="page-card">
      <p v-if="authStore.isGestor" class="page-hint">
        Como gestor, você pode cadastrar e visualizar apenas usuários analistas.
      </p>

      <DataTable :value="usuarios" :loading="loading" striped-rows paginator :rows="10">
        <Column field="nomeCompleto" header="Nome" />
        <Column field="email" header="E-mail" />
        <Column field="role" header="Perfil">
          <template #body="{ data }">
            <Tag :value="roleLabel(data.role)" :severity="roleSeverity(data.role)" />
          </template>
        </Column>
        <Column field="orgao" header="Órgão" />
        <Column field="departamento" header="Departamento" />
        <Column field="ativo" header="Status">
          <template #body="{ data }">
            <Tag :value="data.ativo ? 'Ativo' : 'Inativo'" :severity="data.ativo ? 'success' : 'danger'" />
          </template>
        </Column>
        <template #empty>
          <div class="placeholder-content">
            <i class="pi pi-users"></i>
            <p>Nenhum usuário encontrado</p>
          </div>
        </template>
      </DataTable>
    </div>

    <Dialog
      v-model:visible="dialogVisivel"
      :header="dialogTitulo"
      modal
      :style="{ width: '520px' }"
      @hide="resetForm"
    >
      <form class="user-form" @submit.prevent="salvar">
        <div class="field">
          <label for="nome">Nome completo *</label>
          <InputText id="nome" v-model="form.nomeCompleto" :invalid="!!errors.nomeCompleto" fluid />
          <small v-if="errors.nomeCompleto" class="p-error">{{ errors.nomeCompleto }}</small>
        </div>

        <div class="field">
          <label for="email">E-mail *</label>
          <InputText id="email" v-model="form.email" type="email" :invalid="!!errors.email" fluid />
          <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
        </div>

        <div class="field">
          <label for="senha">Senha *</label>
          <Password
            id="senha"
            v-model="form.senha"
            :feedback="false"
            toggle-mask
            :invalid="!!errors.senha"
            fluid
          />
          <small v-if="errors.senha" class="p-error">{{ errors.senha }}</small>
        </div>

        <div class="field-row">
          <div class="field">
            <label for="orgao">Órgão</label>
            <InputText id="orgao" v-model="form.orgao" fluid />
          </div>
          <div class="field">
            <label for="departamento">Departamento</label>
            <InputText id="departamento" v-model="form.departamento" fluid />
          </div>
        </div>

        <div class="field">
          <label for="telefone">Telefone</label>
          <InputText id="telefone" v-model="form.telefone" fluid />
        </div>

        <div v-if="authStore.isAdmin" class="field">
          <label for="role">Perfil de acesso *</label>
          <Select
            id="role"
            v-model="form.role"
            :options="roleOptionsAdmin"
            option-label="label"
            option-value="value"
            placeholder="Selecione o perfil"
            :invalid="!!errors.role"
            fluid
          />
          <small v-if="errors.role" class="p-error">{{ errors.role }}</small>
        </div>

        <div class="dialog-actions">
          <Button label="Cancelar" severity="secondary" text @click="dialogVisivel = false" />
          <Button type="submit" label="Salvar" icon="pi pi-save" :loading="salvando" />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Select from 'primevue/select'
import { useAuthStore } from '@/stores/auth'
import { usuarioService } from '@/services/usuarioService'

const toast = useToast()
const authStore = useAuthStore()

const usuarios = ref([])
const loading = ref(false)
const salvando = ref(false)
const dialogVisivel = ref(false)

const form = reactive({
  nomeCompleto: '',
  email: '',
  senha: '',
  orgao: '',
  departamento: '',
  telefone: '',
  role: 'ANALISTA'
})

const errors = reactive({
  nomeCompleto: '',
  email: '',
  senha: '',
  role: ''
})

const roleLabels = {
  ADMIN: 'Administrador',
  GESTOR: 'Gestor',
  ANALISTA: 'Analista',
  CONSULTA: 'Consulta'
}

const roleSeverities = {
  ADMIN: 'danger',
  GESTOR: 'warn',
  ANALISTA: 'info',
  CONSULTA: 'secondary'
}

const roleOptionsAdmin = [
  { label: 'Administrador', value: 'ADMIN' },
  { label: 'Gestor', value: 'GESTOR' },
  { label: 'Analista', value: 'ANALISTA' },
  { label: 'Consulta', value: 'CONSULTA' }
]

const tituloPagina = computed(() =>
  authStore.isGestor ? 'Usuários Analistas' : 'Usuários'
)

const dialogTitulo = computed(() =>
  authStore.isGestor ? 'Novo analista' : 'Novo usuário'
)

function roleLabel(role) {
  return roleLabels[role] || role
}

function roleSeverity(role) {
  return roleSeverities[role] || 'secondary'
}

function abrirDialog() {
  form.role = authStore.isGestor ? 'ANALISTA' : null
  dialogVisivel.value = true
}

function resetForm() {
  form.nomeCompleto = ''
  form.email = ''
  form.senha = ''
  form.orgao = ''
  form.departamento = ''
  form.telefone = ''
  form.role = authStore.isGestor ? 'ANALISTA' : null
  errors.nomeCompleto = ''
  errors.email = ''
  errors.senha = ''
  errors.role = ''
}

function validar() {
  errors.nomeCompleto = ''
  errors.email = ''
  errors.senha = ''
  errors.role = ''
  let valid = true

  if (!form.nomeCompleto?.trim()) {
    errors.nomeCompleto = 'Nome é obrigatório'
    valid = false
  }
  if (!form.email?.trim()) {
    errors.email = 'E-mail é obrigatório'
    valid = false
  }
  if (!form.senha || form.senha.length < 6) {
    errors.senha = 'Senha deve ter no mínimo 6 caracteres'
    valid = false
  }
  if (authStore.isAdmin && !form.role) {
    errors.role = 'Selecione o perfil'
    valid = false
  }

  return valid
}

async function loadUsuarios() {
  loading.value = true
  try {
    const { data } = await usuarioService.listar()
    usuarios.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar usuários', life: 5000 })
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
    senha: form.senha,
    orgao: form.orgao?.trim() || null,
    departamento: form.departamento?.trim() || null,
    telefone: form.telefone?.trim() || null,
    role: authStore.isGestor ? 'ANALISTA' : form.role,
    ativo: true
  }

  try {
    await usuarioService.criar(payload)
    toast.add({ severity: 'success', summary: 'Usuário criado com sucesso', life: 3000 })
    dialogVisivel.value = false
    await loadUsuarios()
  } catch (error) {
    const message = error.response?.data?.detalhe || 'Não foi possível criar o usuário'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    salvando.value = false
  }
}

onMounted(loadUsuarios)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.page-header .page-title {
  margin: 0;
}

.page-hint {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.user-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
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

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

@media (max-width: 600px) {
  .field-row {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
