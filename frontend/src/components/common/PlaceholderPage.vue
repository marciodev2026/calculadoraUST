<template>
  <div>
    <h2 class="page-title">{{ title }}</h2>
    <div class="page-card">
      <div class="placeholder-content">
        <i :class="icon"></i>
        <p>{{ message }}</p>
        <Tag v-if="tag" :value="tag" severity="secondary" />
      </div>
    </div>
  </div>
</template>

<script setup>
import Tag from 'primevue/tag'

defineProps({
  title: { type: String, required: true },
  icon: { type: String, default: 'pi pi-info-circle' },
  message: { type: String, default: 'Módulo em desenvolvimento — aguarde o próximo bloco.' },
  tag: { type: String, default: 'Em breve' }
})
</script>
