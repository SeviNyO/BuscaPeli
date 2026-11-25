import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import App from '../App.vue'

describe('App', () => {
  it('renders the active route', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<h1>BuscaPeli</h1>' } }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(App, { global: { plugins: [router] } })
    expect(wrapper.find('h1').text()).toBe('BuscaPeli')
  })
})
