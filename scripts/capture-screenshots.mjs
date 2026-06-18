import { chromium } from 'playwright'
import { mkdir } from 'fs/promises'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = join(__dirname, '..')
const IMG_DIR = join(ROOT, 'img')
const BASE_URL = process.env.APP_URL || 'http://localhost:5173'
const API_URL = process.env.API_URL || 'http://localhost:8080/api'

const SIM_ID = process.env.SIM_ID || 'fcd753c5-9251-49a2-8f17-0f5761d50637'
const PROJ_ID = process.env.PROJ_ID || 'f13b4c3e-573b-4b2b-8b03-1e3f6a43496b'

async function login(email, senha) {
  const res = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, senha })
  })
  if (!res.ok) throw new Error(`Login failed: ${email}`)
  return res.json()
}

async function injectSession(page, session) {
  await page.goto(`${BASE_URL}/login`)
  await page.evaluate(({ token, user }) => {
    localStorage.setItem('ust_token', token)
    localStorage.setItem('ust_user', JSON.stringify(user))
  }, { token: session.token, user: session.usuario })
}

async function screenshot(page, name, path, { waitMs = 1500, action } = {}) {
  await page.goto(`${BASE_URL}${path}`, { waitUntil: 'networkidle' })
  if (action) await action(page)
  await page.waitForTimeout(waitMs)
  await page.screenshot({ path: join(IMG_DIR, name), fullPage: true })
  console.log(`OK ${name}`)
}

await mkdir(IMG_DIR, { recursive: true })

const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  deviceScaleFactor: 1
})
const page = await context.newPage()

// Login (tela pública)
await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' })
await page.waitForTimeout(1000)
await page.screenshot({ path: join(IMG_DIR, '01-login.png'), fullPage: true })
console.log('OK 01-login.png')

const admin = await login('admin@ust.gov.br', 'admin123')
await injectSession(page, admin)

await screenshot(page, '02-dashboard.png', '/dashboard')
await screenshot(page, '03-simulacoes.png', '/simulacoes')
await screenshot(page, '04-projetos.png', `/simulacoes/${SIM_ID}/projetos`)
await screenshot(page, '05-squad.png', `/simulacoes/${SIM_ID}/projetos/${PROJ_ID}/squad`)

await screenshot(page, '06-relatorios.png', '/relatorios', {
  waitMs: 2500,
  action: async (p) => {
    const select = p.locator('#simulacao')
    if (await select.count()) {
      await select.click()
      await p.locator('.p-select-option').first().click({ timeout: 5000 }).catch(() => {})
      await p.waitForTimeout(2000)
    }
  }
})

await screenshot(page, '07-usuarios.png', '/usuarios')
await screenshot(page, '08-configuracoes.png', '/admin/configuracao-ust')
await screenshot(page, '09-perfis.png', '/admin/perfis')

// Gestor — visão do menu Usuários (sem Administração)
const gestor = await login('gestor@ust.gov.br', 'gestor123')
await injectSession(page, gestor)
await screenshot(page, '10-dashboard-gestor.png', '/dashboard')
await screenshot(page, '11-usuarios-gestor.png', '/usuarios')

await browser.close()
console.log(`\nScreenshots salvos em: ${IMG_DIR}`)
