<template>
  <div class="app-logo">
    <svg class="logo-mark" viewBox="0 0 36 36" fill="none" aria-hidden="true">
      <path d="M4 6h28v24H4z" fill="var(--color-brand-primary)" />
      <path d="M11 7v22M25 7v22" stroke="#101116" stroke-width="1.5" />
      <path d="M7 10h2m-2 6h2m-2 6h2m-2 6h2m18-18h2m-2 6h2m-2 6h2m-2 6h2" stroke="#101116" stroke-width="2.5" stroke-linecap="round" />
      <path d="m15 12 8 6-8 6V12Z" fill="white" />
    </svg>

    <div class="wordmark">
      <span class="brand-white">Busca</span>
      <span class="brand-red">Peli</span>
    </div>
  </div>
</template>

<style scoped>
.app-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  width: max-content;
}

.logo-mark {
  display: block;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.wordmark {
  display: flex;
  font-family: 'Inter', sans-serif;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: normal;
  white-space: nowrap;
}

.brand-white {
  color: var(--color-text-primary);
}

.brand-red {
  color: var(--color-brand-primary);
}
</style>
