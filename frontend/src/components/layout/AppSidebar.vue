<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <div class="brand">
        <i class="pi pi-building brand-icon"></i>
        <span v-if="!collapsed" class="brand-text">UST Gov</span>
      </div>
      <Button
        :icon="collapsed ? 'pi pi-angle-right' : 'pi pi-angle-left'"
        text
        rounded
        severity="secondary"
        class="toggle-btn"
        @click="collapsed = !collapsed"
      />
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.name"
        :to="{ name: item.name }"
        class="nav-item"
        :title="item.label"
      >
        <i :class="item.icon"></i>
        <span v-if="!collapsed">{{ item.label }}</span>
      </router-link>

      <div v-if="adminItems.length && !collapsed" class="nav-section">Administração</div>
      <div v-if="adminItems.length && collapsed" class="nav-divider"></div>

      <router-link
        v-for="item in adminItems"
        :key="item.name"
        :to="{ name: item.name }"
        class="nav-item"
        :title="item.label"
      >
        <i :class="item.icon"></i>
        <span v-if="!collapsed">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div v-if="!collapsed" class="user-info">
        <span class="user-name">{{ authStore.user?.nomeCompleto }}</span>
        <Tag :value="roleLabel" severity="info" />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const collapsed = ref(false)

const mainItems = [
  { name: 'dashboard', label: 'Dashboard', icon: 'pi pi-chart-bar' },
  { name: 'simulacoes', label: 'Simulações', icon: 'pi pi-calculator' },
  { name: 'relatorios', label: 'Relatórios', icon: 'pi pi-file-pdf' }
]

const adminRoutes = [
  { name: 'configuracao-ust', label: 'Configurações', icon: 'pi pi-cog' },
  { name: 'perfis', label: 'Perfis Profissionais', icon: 'pi pi-users' },
  { name: 'usuarios', label: 'Usuários', icon: 'pi pi-user-edit' }
]

const menuItems = computed(() => {
  const items = [...mainItems]
  if (authStore.isGestor) {
    items.push({ name: 'usuarios', label: 'Usuários', icon: 'pi pi-user-edit' })
  }
  return items
})

const adminItems = computed(() => {
  if (!authStore.isAdmin) return []
  return adminRoutes
})

const roleLabel = computed(() => {
  const roles = {
    ADMIN: 'Administrador',
    GESTOR: 'Gestor',
    ANALISTA: 'Analista',
    CONSULTA: 'Consulta'
  }
  return roles[authStore.user?.role] || authStore.user?.role
})
</script>

<style scoped>
.sidebar {
  width: var(--ust-sidebar-width);
  background: linear-gradient(180deg, var(--ust-primary-dark) 0%, var(--ust-primary) 55%, #1a5c52 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.brand-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
  color: var(--ust-accent-light);
}

.brand-text {
  font-weight: 700;
  font-size: 1rem;
  white-space: nowrap;
}

.toggle-btn {
  color: #fff !important;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0.5rem;
  overflow-y: auto;
}

.nav-section {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  opacity: 0.6;
  padding: 1rem 0.75rem 0.5rem;
}

.nav-divider {
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  margin: 0.5rem 0.75rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: var(--p-border-radius-md);
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  margin-bottom: 0.25rem;
  transition: background 0.15s;
  white-space: nowrap;
}

.nav-item i {
  font-size: 1.1rem;
  width: 1.25rem;
  text-align: center;
  flex-shrink: 0;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-weight: 600;
  border-left: 3px solid var(--ust-accent-light);
  padding-left: calc(0.75rem - 3px);
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.user-name {
  font-size: 0.85rem;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
