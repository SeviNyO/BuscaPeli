
<script setup>
import { computed, ref } from 'vue'
import { RouterView } from 'vue-router'
import { useRouter } from 'vue-router'
import { useMoviesStore } from '../stores/movies'

import AppSidebar from '../components/AppSidebar.vue'
import AppHeader from '../components/AppHeader.vue'

const router = useRouter()
const store = useMoviesStore()
const username = computed(() => store.username)
const sidebarOpen = ref(false)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}

async function handleLogout() {
  closeSidebar()
  await store.logout()
  router.push('/login')
}
</script>

<template>
  <div class="main-layout">

    <!-- Fondo del menú en dispositivos móviles -->
    <Transition name="fade">
      <div
        v-if="sidebarOpen"
        class="mobile-overlay"
        @click="closeSidebar"
      ></div>
    </Transition>

    <!-- Sidebar -->
    <div
      class="sidebar-container"
      :class="{ 'sidebar-open': sidebarOpen }"
    >
      <AppSidebar @logout="handleLogout" />
    </div>

    <!-- Área principal -->
    <div class="main-container">

      <!-- Header -->
      <div class="header-container">

        <button
          class="menu-toggle"
          type="button"
          aria-label="Abrir menú de navegación"
          :aria-expanded="sidebarOpen"
          @click="toggleSidebar"
        >
          ☰
        </button>

        <AppHeader :username="username" />

      </div>

      <!-- Contenido dinámico -->
      <main class="main-content">
        <RouterView />
      </main>

    </div>

  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  align-items: flex-start;

  width: 100%;
  min-height: 100vh;

  background: var(--color-bg-app);
  color: var(--color-text-primary);
}

/* Sidebar */

.sidebar-container {
  position: sticky;
  top: 0;

  width: 248px;
  height: 100vh;

  flex-shrink: 0;

  z-index: 30;
}

/* Contenedor principal */

.main-container {
  display: flex;
  flex-direction: column;

  flex: 1;
  min-width: 0;
  min-height: 100vh;
}

/* Header */

.header-container {
  position: sticky;
  top: 0;
  z-index: 20;

  display: flex;
  align-items: center;

  width: 100%;
  min-height: 76px;

  background: var(--color-bg-app);
}

/* Contenido de las vistas */

.main-content {
  flex: 1;

  width: 100%;
  min-width: 0;

  padding: 32px 40px 48px;
}

/* Menú responsive */

.menu-toggle {
  display: none;

  align-items: center;
  justify-content: center;

  width: 40px;
  height: 40px;

  margin-left: 16px;
  flex-shrink: 0;

  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: 10px;

  color: var(--color-text-primary);
  font-size: 22px;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: 25;

  background: rgba(0, 0, 0, 0.65);
}

/* Animaciones */

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Tablet y móvil */

@media (max-width: 1024px) {
  .sidebar-container {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;

    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }

  .sidebar-container.sidebar-open {
    transform: translateX(0);
  }

  .menu-toggle {
    display: flex;
  }

  .main-content {
    padding: 24px;
  }
}

@media (max-width: 600px) {
  .main-content {
    padding: 20px 16px 32px;
  }
}
</style>
