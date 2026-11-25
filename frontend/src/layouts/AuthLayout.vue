
<script setup>
defineProps({
  showCollage: {
    type: Boolean,
    default: true
  },
  showCard: {
    type: Boolean,
    default: true
  }
})

const gradients = [
  ['#2a4a6b', '#0b1626'],
  ['#7a3b14', '#1a0b05'],
  ['#1f5a4a', '#08171a'],
  ['#5a2a6b', '#140a1e'],
  ['#6b2a2a', '#1a0808'],
  ['#3a4a1f', '#0e1408'],
  ['#1f3a6b', '#080e1a'],
  ['#6b5a1f', '#1a1408']
]

const columns = Array.from({ length: 7 }, (_, column) => ({
  id: column,
  left: -60 + column * 250,
  top: [2, 4, 6].includes(column) ? -20 : -150,
  posters: Array.from({ length: 4 }, (_, row) => ({
    id: row,
    gradient: gradients[(column * 4 + row) % gradients.length]
  }))
}))
</script>

<template>
  <main class="auth-layout">

    <div
      v-if="showCollage"
      class="collage"
      aria-hidden="true"
    >
      <div class="collage-canvas">
        <div
          v-for="column in columns"
          :key="column.id"
          class="collage-column"
          :style="{
            left: `${column.left}px`,
            top: `${column.top}px`
          }"
        >
          <div
            v-for="poster in column.posters"
            :key="poster.id"
            class="collage-poster"
            :style="{
              backgroundImage: `linear-gradient(
                135.855deg,
                ${poster.gradient[0]} 14.286%,
                ${poster.gradient[1]} 85.714%
              )`
            }"
          ></div>
        </div>
      </div>

      <div class="collage-overlay"></div>
    </div>

    <div
      class="auth-content"
      :class="{ 'auth-card': showCard }"
    >
      <slot />
    </div>

  </main>
</template>

<style scoped>
.auth-layout {
  position: relative;
  isolation: isolate;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;

  padding: 32px 20px;

  background: var(--color-bg-app);
}


.collage {
  position: absolute;
  inset: 0;
  z-index: -1;

  overflow: hidden;
  pointer-events: none;
}

.collage-canvas {
  position: absolute;

  width: 1440px;
  height: 900px;

  top: 50%;
  left: 50%;

  transform: translate(-50%, -50%);
}

.collage-column {
  position: absolute;

  display: flex;
  flex-direction: column;
  gap: 50px;
}

.collage-poster {
  width: 220px;
  height: 330px;

  border-radius: 16px;

  opacity: 0.85;
  transform: rotate(-12deg);
}

.collage-overlay {
  position: absolute;
  inset: 0;

  background: linear-gradient(
    to right,
    rgba(16, 17, 22, 0.72) 0%,
    rgba(16, 17, 22, 0.92) 50%,
    rgba(16, 17, 22, 0.72) 100%
  );
}


.auth-content {
  position: relative;

  display: flex;
  flex-direction: column;
  align-items: center;

  width: 100%;
  max-width: 1200px;

  gap: 20px;
}


.auth-card {
  width: 460px;
  max-width: 100%;

  padding: 40px 40px 36px;

  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: 24px;

  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
}


@media (max-width: 480px) {
  .auth-layout {
    padding: 24px 16px;
  }

  .auth-card {
    padding: 32px 24px;
    border-radius: 20px;
  }
}
</style>
