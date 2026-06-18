<template>
  <div class="login-page">
    <div class="login-panel login-panel-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <i class="pi pi-building"></i>
        </div>
        <h1>UST Gov</h1>
        <p class="brand-tagline">Simulação de esforço e custo em Unidade de Serviço Técnico para órgãos governamentais</p>
        <ul class="brand-features">
          <li><i class="pi pi-check-circle"></i> Cálculo automatizado de UST</li>
          <li><i class="pi pi-check-circle"></i> Gestão de squads e perfis</li>
          <li><i class="pi pi-check-circle"></i> Relatórios PDF e Excel</li>
        </ul>
      </div>
    </div>

    <div class="login-panel login-panel-form">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>Bem-vindo</h2>
          <p>Acesse com suas credenciais institucionais</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="field">
            <label for="email">E-mail</label>
            <span class="p-input-icon-left w-full">
              <i class="pi pi-envelope" />
              <InputText
                id="email"
                v-model="form.email"
                type="email"
                placeholder="seu@email.gov.br"
                :invalid="!!errors.email"
                fluid
              />
            </span>
            <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
          </div>

          <div class="field">
            <label for="senha">Senha</label>
            <Password
              id="senha"
              v-model="form.senha"
              placeholder="Sua senha"
              :feedback="false"
              toggle-mask
              :invalid="!!errors.senha"
              fluid
            />
            <small v-if="errors.senha" class="p-error">{{ errors.senha }}</small>
          </div>

          <Button
            type="submit"
            label="Entrar no sistema"
            icon="pi pi-sign-in"
            :loading="loading"
            fluid
            class="login-btn"
          />
        </form>

        <p class="login-footer">UST Gov &copy; {{ anoAtual }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()

const loading = ref(false)
const anoAtual = computed(() => new Date().getFullYear())

const form = reactive({
  email: '',
  senha: ''
})
const errors = reactive({
  email: '',
  senha: ''
})

function validate() {
  errors.email = ''
  errors.senha = ''
  let valid = true

  if (!form.email) {
    errors.email = 'E-mail é obrigatório'
    valid = false
  }
  if (!form.senha) {
    errors.senha = 'Senha é obrigatória'
    valid = false
  }

  return valid
}

async function handleLogin() {
  if (!validate()) return

  loading.value = true
  try {
    await authStore.login(form.email, form.senha)
    toast.add({ severity: 'success', summary: 'Login realizado', life: 3000 })
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    const message = error.response?.data?.detalhe || 'E-mail ou senha incorretos'
    toast.add({ severity: 'error', summary: 'Falha no login', detail: message, life: 5000 })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.login-panel-brand {
  background: linear-gradient(160deg, var(--ust-primary-dark) 0%, var(--ust-primary) 45%, #1a5c52 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
  overflow: hidden;
}

.login-panel-brand::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 80%, rgba(59, 130, 246, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.brand-content {
  position: relative;
  max-width: 420px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  backdrop-filter: blur(8px);
}

.brand-logo i {
  font-size: 2rem;
}

.brand-content h1 {
  font-size: 2.5rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-bottom: 0.75rem;
}

.brand-tagline {
  font-size: 1rem;
  line-height: 1.6;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.brand-features {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.9rem;
  opacity: 0.85;
}

.brand-features i {
  color: #5eead4;
}

.login-panel-form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: var(--p-surface-50);
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 2rem;
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--ust-primary);
  margin-bottom: 0.35rem;
}

.form-header p {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
}

.login-form {
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
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--ust-primary);
}

.w-full {
  width: 100%;
}

.p-input-icon-left {
  display: block;
  position: relative;
}

.p-input-icon-left > i {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--p-text-muted-color);
  z-index: 1;
}

.p-input-icon-left :deep(.p-inputtext) {
  padding-left: 2.5rem;
}

.login-btn {
  margin-top: 0.5rem;
}

.login-footer {
  text-align: center;
  color: var(--p-text-muted-color);
  font-size: 0.8rem;
  margin-top: 2rem;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-panel-brand {
    padding: 2rem;
    min-height: auto;
  }

  .brand-content h1 {
    font-size: 2rem;
  }

  .brand-features {
    display: none;
  }
}
</style>
