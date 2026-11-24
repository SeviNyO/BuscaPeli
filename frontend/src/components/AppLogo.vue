<script setup>
import logoMark from '../assets/figma/beb93.svg'
</script>

<template>
  <div class="app-logo">
    <img
      :src="logoMark"
      alt=""
      class="logo-mark"
    />

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

