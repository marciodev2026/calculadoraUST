<template>
  <header class="topbar">
    <div class="topbar-left">
      <h1 class="page-heading">{{ pageTitle }}</h1>
    </div>
    <div class="topbar-right">
      <span class="user-email">{{ authStore.user?.email }}</span>
      <Button
        icon="pi pi-sign-out"
        label="Sair"
        severity="secondary"
        outlined
        size="small"
        @click="handleLogout"
      />
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const pageTitle = computed(() => route.meta.title || 'UST Gov')

function handleLogout() {
  authStore.logout()
  toast.add({ severity: 'info', summary: 'Sessão encerrada', life: 3000 })
  router.push({ name: 'login' })
}
</script>

<style scoped>
.page-heading {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--ust-primary-dark);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  background: var(--p-surface-0);
  border-bottom: 1px solid var(--p-surface-200);
  border-top: 3px solid var(--ust-accent);
  min-height: 64px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-email {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

@media (max-width: 640px) {
  .user-email {
    display: none;
  }
}
</style>
