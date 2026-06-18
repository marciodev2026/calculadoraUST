<template>
  <div>
    <h2 class="page-title">Relatórios</h2>

    <div class="page-card selector-card">
      <div class="field">
        <label for="simulacao">Simulação</label>
        <Select
          id="simulacao"
          v-model="simulacaoSelecionada"
          :options="simulacoes"
          option-label="label"
          option-value="value"
          placeholder="Selecione uma simulação"
          :loading="loadingSimulacoes"
          fluid
          @change="carregarDados"
        />
      </div>
    </div>

    <template v-if="simulacaoSelecionada">
      <div v-if="loadingPreview" class="page-card">
        <div class="placeholder-content">
          <i class="pi pi-spin pi-spinner"></i>
          <p>Carregando dados do relatório...</p>
        </div>
      </div>

      <template v-else-if="preview">
        <!-- Cabeçalho institucional -->
        <div class="page-card report-header">
          <div class="report-brand">
            <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="report-logo" />
            <div v-else class="report-logo-placeholder">
              <i class="pi pi-building"></i>
            </div>
            <div>
              <h3 class="report-org">{{ preview.institucional?.nomeOrganizacao || 'Governo Federal' }}</h3>
              <p class="report-subtitle">Relatório de Simulação UST</p>
            </div>
          </div>
        </div>

        <!-- Identificação -->
        <div class="page-card">
          <h3 class="card-title">Identificação</h3>
          <div class="info-grid">
            <div class="info-item"><span class="info-label">Solicitante</span><span>{{ preview.simulacao.nomeCompleto }}</span></div>
            <div class="info-item"><span class="info-label">E-mail</span><span>{{ preview.simulacao.email }}</span></div>
            <div class="info-item"><span class="info-label">Órgão</span><span>{{ preview.simulacao.orgao }}</span></div>
            <div class="info-item"><span class="info-label">Departamento</span><span>{{ preview.simulacao.departamento }}</span></div>
            <div class="info-item"><span class="info-label">Data</span><span>{{ formatDate(preview.simulacao.dataSimulacao) }}</span></div>
            <div class="info-item"><span class="info-label">Status</span><Tag :value="preview.simulacao.status" severity="info" /></div>
          </div>
        </div>

        <!-- Totais -->
        <div class="stats-row">
          <div class="stat-card">
            <span class="stat-value">{{ preview.totais.totalProjetos }}</span>
            <span class="stat-label">Projetos</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">{{ formatNumber(preview.totais.totalHoras) }}</span>
            <span class="stat-label">Horas</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">{{ formatNumber(preview.totais.totalUst) }}</span>
            <span class="stat-label">UST</span>
          </div>
          <div class="stat-card highlight">
            <span class="stat-value">{{ formatCurrency(preview.totais.valorTotal) }}</span>
            <span class="stat-label">Valor Total</span>
          </div>
        </div>

        <!-- Grupos por tipo -->
        <div class="page-card">
          <h3 class="card-title">Detalhamento por Tipo</h3>
          <Tabs v-if="preview.grupos?.length" v-model:value="abaAtiva">
            <TabList>
              <Tab v-for="grupo in preview.grupos" :key="grupo.tipo" :value="grupo.tipo">
                {{ grupo.tipoLabel }}
                <Badge :value="grupo.projetos.length" severity="secondary" class="tab-badge" />
              </Tab>
            </TabList>
            <TabPanels>
              <TabPanel v-for="grupo in preview.grupos" :key="grupo.tipo" :value="grupo.tipo">
                <div class="grupo-subtotais">
                  <span>{{ grupo.projetos.length }} item(ns)</span>
                  <span>{{ formatNumber(grupo.subtotais.totalHoras) }} h</span>
                  <span>{{ formatNumber(grupo.subtotais.totalUst) }} UST</span>
                  <strong>{{ formatCurrency(grupo.subtotais.valorTotal) }}</strong>
                </div>

                <DataTable
                  :value="grupo.projetos"
                  v-model:expandedRows="expandedRows"
                  data-key="id"
                  striped-rows
                >
                  <Column expander style="width: 3rem" />
                  <Column field="nome" header="Projeto" />
                  <Column field="semanas" header="Semanas" style="width: 90px" />
                  <Column field="horasSemanais" header="H/Sem" style="width: 80px" />
                  <Column header="Horas" style="width: 100px">
                    <template #body="{ data }">{{ formatNumber(data.totalHoras) }}</template>
                  </Column>
                  <Column header="UST" style="width: 100px">
                    <template #body="{ data }">{{ formatNumber(data.totalUst) }}</template>
                  </Column>
                  <Column header="Valor" style="width: 130px">
                    <template #body="{ data }">{{ formatCurrency(data.valorTotal) }}</template>
                  </Column>
                  <template #expansion="{ data }">
                    <div v-if="data.membros?.length" class="membros-expansion">
                      <h4>Composição do Squad</h4>
                      <DataTable :value="data.membros" size="small">
                        <Column field="perfilNome" header="Perfil" />
                        <Column field="quantidade" header="Qtd" style="width: 60px" />
                        <Column header="FCP" style="width: 70px">
                          <template #body="{ data: m }">{{ formatNumber(m.fcpAplicado) }}</template>
                        </Column>
                        <Column header="Horas" style="width: 90px">
                          <template #body="{ data: m }">{{ formatNumber(m.horasCalculadas) }}</template>
                        </Column>
                        <Column header="UST" style="width: 90px">
                          <template #body="{ data: m }">{{ formatNumber(m.ustCalculada) }}</template>
                        </Column>
                      </DataTable>
                    </div>
                    <p v-else class="sem-squad">Squad não montado</p>
                  </template>
                  <template #empty>
                    <p class="empty-msg">Nenhum projeto nesta categoria</p>
                  </template>
                </DataTable>
              </TabPanel>
            </TabPanels>
          </Tabs>
          <div v-else class="placeholder-content">
            <i class="pi pi-inbox"></i>
            <p>Nenhum projeto cadastrado nesta simulação</p>
          </div>
        </div>

        <!-- Exportação -->
        <div class="page-card export-card">
          <h3 class="card-title">Exportar Relatório</h3>
          <p class="card-subtitle">Gere o arquivo após conferir os dados acima</p>
          <div class="actions-row">
            <Button
              v-if="authStore.canWrite"
              label="Baixar PDF Executivo"
              icon="pi pi-file-pdf"
              severity="danger"
              :loading="gerandoPdf"
              @click="gerarPdf"
            />
            <Button
              v-if="authStore.canWrite"
              label="Baixar Excel Técnico"
              icon="pi pi-file-excel"
              severity="success"
              :loading="gerandoExcel"
              @click="gerarExcel"
            />
          </div>
        </div>

        <!-- Envio por e-mail -->
        <div v-if="authStore.canWrite" class="page-card email-card">
          <div class="email-card-header">
            <div>
              <h3 class="card-title">Enviar por E-mail</h3>
              <p class="card-subtitle">O relatório será gerado e enviado com anexo via Mailpit</p>
            </div>
            <a
              v-if="mailConfig?.enabled && mailConfig?.mailpitUiUrl"
              :href="mailConfig.mailpitUiUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="mailpit-link"
            >
              <i class="pi pi-inbox"></i>
              Abrir Mailpit
            </a>
          </div>

          <div class="email-form">
            <div class="field">
              <label for="email-destinatario">Destinatário</label>
              <InputText
                id="email-destinatario"
                v-model="emailForm.destinatario"
                type="email"
                placeholder="destinatario@orgao.gov.br"
                fluid
              />
            </div>
            <div class="field">
              <label for="email-tipo">Formato</label>
              <Select
                id="email-tipo"
                v-model="emailForm.tipo"
                :options="tiposRelatorio"
                option-label="label"
                option-value="value"
                fluid
              />
            </div>
            <div class="field">
              <label for="email-assunto">Assunto (opcional)</label>
              <InputText
                id="email-assunto"
                v-model="emailForm.assunto"
                placeholder="Assunto personalizado"
                fluid
              />
            </div>
            <div class="field field-full">
              <label for="email-mensagem">Mensagem (opcional)</label>
              <Textarea
                id="email-mensagem"
                v-model="emailForm.mensagem"
                rows="3"
                placeholder="Texto adicional no corpo do e-mail"
                fluid
                auto-resize
              />
            </div>
          </div>

          <div class="actions-row">
            <Button
              label="Enviar por E-mail"
              icon="pi pi-send"
              :loading="enviandoEmail"
              @click="enviarEmail()"
            />
          </div>
        </div>
      </template>
    </template>

    <div v-else class="page-card">
      <div class="placeholder-content">
        <i class="pi pi-file-pdf"></i>
        <p>Selecione uma simulação para visualizar o relatório</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Badge from 'primevue/badge'
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import { useAuthStore } from '@/stores/auth'
import { simulacaoService } from '@/services/simulacaoService'
import { relatorioService } from '@/services/relatorioService'
import { mailService } from '@/services/mailService'
import { configuracaoInstitucionalService } from '@/services/configuracaoInstitucionalService'
import { parseApiError, salvarRespostaArquivo } from '@/utils/fileDownload'

const tiposRelatorio = [
  { label: 'PDF Executivo', value: 'PDF' },
  { label: 'Excel Técnico', value: 'EXCEL' }
]

const toast = useToast()
const authStore = useAuthStore()

const simulacoes = ref([])
const simulacaoSelecionada = ref(null)
const preview = ref(null)
const mailConfig = ref(null)
const logoUrl = ref(null)
const abaAtiva = ref(null)
const expandedRows = ref({})

const emailForm = ref({
  destinatario: '',
  tipo: 'PDF',
  assunto: '',
  mensagem: ''
})

const loadingSimulacoes = ref(false)
const loadingPreview = ref(false)
const gerandoPdf = ref(false)
const gerandoExcel = ref(false)
const enviandoEmail = ref(false)

watch(preview, (value) => {
  if (value?.simulacao?.email && !emailForm.value.destinatario) {
    emailForm.value.destinatario = value.simulacao.email
  }
})

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(Number(value) || 0)
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value) || 0)
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

async function carregarLogo() {
  revogarLogo()
  try {
    const { data } = await configuracaoInstitucionalService.obterLogo()
    logoUrl.value = URL.createObjectURL(data)
  } catch {
    logoUrl.value = null
  }
}

function revogarLogo() {
  if (logoUrl.value) {
    URL.revokeObjectURL(logoUrl.value)
    logoUrl.value = null
  }
}

async function carregarSimulacoes() {
  loadingSimulacoes.value = true
  try {
    const { data } = await simulacaoService.listar()
    simulacoes.value = data.map((s) => ({
      value: s.id,
      label: `${s.nomeCompleto} — ${s.orgao} (${formatDate(s.dataSimulacao)})`
    }))
  } catch {
    toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar simulações', life: 5000 })
  } finally {
    loadingSimulacoes.value = false
  }
}

async function carregarPreview() {
  if (!simulacaoSelecionada.value) {
    preview.value = null
    return
  }
  loadingPreview.value = true
  try {
    const { data } = await relatorioService.carregarPreview(simulacaoSelecionada.value)
    preview.value = data
    abaAtiva.value = data.grupos?.[0]?.tipo ?? null
    expandedRows.value = {}
  } catch (error) {
    preview.value = null
    const message = error.response?.data?.detalhe || 'Não foi possível carregar o relatório'
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    loadingPreview.value = false
  }
}

async function carregarMailConfig() {
  try {
    const { data } = await mailService.obterConfig()
    mailConfig.value = data
  } catch {
    mailConfig.value = null
  }
}

async function carregarDados() {
  await carregarPreview()
  if (preview.value?.simulacao?.email) {
    emailForm.value.destinatario = preview.value.simulacao.email
  }
}

async function gerarPdf() {
  gerandoPdf.value = true
  try {
    const response = await relatorioService.gerarPdf(simulacaoSelecionada.value)
    salvarRespostaArquivo(response, 'relatorio.pdf', 'application/pdf')
    toast.add({ severity: 'success', summary: 'PDF gerado', life: 3000 })
  } catch (error) {
    const message = await parseApiError(error)
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    gerandoPdf.value = false
  }
}

async function gerarExcel() {
  gerandoExcel.value = true
  try {
    const response = await relatorioService.gerarExcel(simulacaoSelecionada.value)
    salvarRespostaArquivo(
      response,
      'relatorio.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    )
    toast.add({ severity: 'success', summary: 'Excel gerado', life: 3000 })
  } catch (error) {
    const message = await parseApiError(error)
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    gerandoExcel.value = false
  }
}

async function enviarEmail() {
  if (!emailForm.value.destinatario?.trim()) {
    toast.add({ severity: 'warn', summary: 'Atenção', detail: 'Informe o e-mail do destinatário', life: 4000 })
    return
  }

  enviandoEmail.value = true
  try {
    const { data } = await relatorioService.enviarEmail(simulacaoSelecionada.value, {
      destinatario: emailForm.value.destinatario.trim(),
      tipo: emailForm.value.tipo,
      assunto: emailForm.value.assunto?.trim() || null,
      mensagem: emailForm.value.mensagem?.trim() || null
    })

    toast.add({
      severity: 'success',
      summary: 'E-mail enviado',
      detail: `Relatório enviado para ${data.destinatario}`,
      life: 5000
    })
  } catch (error) {
    const message = await parseApiError(error)
    toast.add({ severity: 'error', summary: 'Erro', detail: message, life: 5000 })
  } finally {
    enviandoEmail.value = false
  }
}

onMounted(async () => {
  await carregarSimulacoes()
  await carregarLogo()
  await carregarMailConfig()
})

onUnmounted(revogarLogo)
</script>

<style scoped>
.selector-card {
  margin-bottom: 1.5rem;
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

.card-title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ust-primary);
}

.card-subtitle {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  margin: -0.5rem 0 1rem;
}

.report-header {
  margin-bottom: 1.5rem;
}

.report-brand {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.report-logo {
  max-height: 64px;
  max-width: 160px;
  object-fit: contain;
}

.report-logo-placeholder {
  width: 64px;
  height: 64px;
  background: var(--p-surface-100);
  border-radius: var(--p-border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ust-primary);
  font-size: 1.75rem;
}

.report-org {
  margin: 0;
  font-size: 1.25rem;
  color: var(--ust-primary);
}

.report-subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
}

.info-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--p-text-muted-color);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: var(--p-surface-0);
  border: 1px solid var(--p-surface-200);
  border-radius: var(--p-border-radius-lg);
  padding: 1.25rem;
  text-align: center;
  border-top: 3px solid var(--ust-accent);
}

.stat-card:nth-child(2) {
  border-top-color: #6366f1;
}

.stat-card:nth-child(3) {
  border-top-color: var(--ust-accent-warm);
}

.stat-card.highlight {
  background: linear-gradient(135deg, var(--ust-primary-dark) 0%, #1a5c52 100%);
  border-color: transparent;
  border-top: none;
  color: #fff;
}

.stat-card.highlight .stat-label {
  color: rgba(255, 255, 255, 0.8);
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  display: block;
  font-size: 0.8rem;
  color: var(--p-text-muted-color);
  margin-top: 0.35rem;
}

.grupo-subtotais {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  padding: 0.75rem 0 1rem;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.grupo-subtotais strong {
  color: var(--ust-primary);
}

.tab-badge {
  margin-left: 0.5rem;
}

.membros-expansion {
  padding: 0.5rem 1rem 1rem 3rem;
}

.membros-expansion h4 {
  margin: 0 0 0.75rem;
  font-size: 0.875rem;
  color: var(--ust-primary);
}

.sem-squad,
.empty-msg {
  padding: 1rem;
  color: var(--p-text-muted-color);
  font-size: 0.875rem;
}

.export-card {
  margin: 1.5rem 0;
  border-left: 4px solid var(--ust-accent);
  background: var(--ust-accent-soft);
}

.email-card {
  border-left: 4px solid #6366f1;
  background: #f8f9ff;
}

.email-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.mailpit-link {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.875rem;
  color: #6366f1;
  text-decoration: none;
  white-space: nowrap;
}

.mailpit-link:hover {
  text-decoration: underline;
}

.email-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.field-full {
  grid-column: 1 / -1;
}

.actions-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.page-card + .page-card,
.stats-row + .page-card {
  margin-top: 0;
}

.page-card {
  margin-bottom: 1.5rem;
}
</style>
