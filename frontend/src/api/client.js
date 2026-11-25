const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1'
let csrfToken = null

async function getCsrfToken() {
  if (!csrfToken) {
    const response = await fetch(`${API_BASE}/auth/csrf`, { credentials: 'include' })
    if (!response.ok) throw new Error('No se pudo iniciar una sesión segura')
    csrfToken = (await response.json()).token
  }
  return csrfToken
}

export async function api(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = new Headers(options.headers || {})
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) headers.set('X-XSRF-TOKEN', await getCsrfToken())
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    method,
    headers,
    credentials: 'include',
  })
  const body = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) throw new Error(body?.detail || body?.message || `Error ${response.status}`)
  return body
}
