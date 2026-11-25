<script setup>
defineProps({ name: { type: String, required: true } })
</script>

<template>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <template v-if="name === 'home'">
      <path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V10Z" />
      <path d="M9 21v-7h6v7" />
    </template>
    <template v-else-if="name === 'recommended'">
      <path d="m12 2 2.9 6 6.6 1-4.8 4.7 1.1 6.6-5.8-3.1-5.8 3.1 1.1-6.6L2.5 9l6.6-1L12 2Z" />
    </template>
    <template v-else-if="name === 'discover'">
      <circle cx="12" cy="12" r="9" />
      <path d="m15.5 8.5-2.2 4.8-4.8 2.2 2.2-4.8 4.8-2.2Z" />
    </template>
    <template v-else-if="name === 'lists'">
      <path d="M9 6h12M9 12h12M9 18h12" />
      <path d="m3 6 1 1 2-2M3 12l1 1 2-2M3 18l1 1 2-2" />
    </template>
    <template v-else-if="name === 'profile'">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21v-2a8 8 0 0 1 16 0v2H4Z" />
    </template>
    <template v-else-if="name === 'logout'">
      <path d="M10 4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h5" />
      <path d="M14 8l4 4-4 4M8 12h10" />
    </template>
    <template v-else-if="name === 'heart'">
      <path d="M20.8 8.4c0 4-4.6 7.8-8.8 11-4.2-3.2-8.8-7-8.8-11a4.7 4.7 0 0 1 8.8-2.2 4.7 4.7 0 0 1 8.8 2.2Z" />
    </template>
    <template v-else-if="name === 'chevron-down'">
      <path d="m6 9 6 6 6-6" />
    </template>
    <template v-else-if="name === 'bookmark'">
      <path d="M6 4h12v17l-6-4-6 4V4Z" />
    </template>
    <template v-else-if="name === 'check'">
      <path d="m4 12 5 5L20 6" />
    </template>
    <template v-else-if="name === 'plus'">
      <path d="M12 4v16M4 12h16" />
    </template>
    <template v-else-if="name === 'mail'">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="m4 7 8 6 8-6" />
    </template>
    <template v-else-if="name === 'lock'">
      <rect x="5" y="10" width="14" height="11" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </template>
    <template v-else-if="name === 'eye'">
      <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z" />
      <circle cx="12" cy="12" r="3" />
    </template>
    <template v-else-if="name === 'eye-off'">
      <path d="M3 3 21 21M10 5.2A11 11 0 0 1 12 5c6 0 10 7 10 7a16 16 0 0 1-3.2 3.8M6.3 6.3C3.6 8.1 2 12 2 12s4 7 10 7c1.1 0 2.2-.2 3.2-.6" />
      <path d="M10 10a3 3 0 0 0 4 4" />
    </template>
    <template v-else-if="name === 'menu'">
      <path d="M4 6h16M4 12h16M4 18h16" />
    </template>
    <template v-else-if="name === 'search'">
      <circle cx="10.8" cy="10.8" r="6.8" />
      <path d="m16 16 4.5 4.5" />
    </template>
  </svg>
</template>
