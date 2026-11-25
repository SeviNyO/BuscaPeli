
<script setup>
import { computed, ref } from 'vue'
import { RouterView } from 'vue-router'
import { useRouter } from 'vue-router'
import { useMoviesStore } from '../stores/movies'

import AppSidebar from '../components/AppSidebar.vue'
import AppHeader from '../components/AppHeader.vue'
import NavigationIcon from '../components/NavigationIcon.vue'

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

    <Transition name="fade">
      <div
        v-if="sidebarOpen"
        class="mobile-overlay"
        @click="closeSidebar"
      ></div>
    </Transition>

    <div
      class="sidebar-container"
      :class="{ 'sidebar-open': sidebarOpen }"
    >
      <AppSidebar @logout="handleLogout" />
    </div>

    <div class="main-container">

      <div class="header-container">

        <button
          class="menu-toggle"
          type="button"
          aria-label="Abrir menú de navegación"
          :aria-expanded="sidebarOpen"
          @click="toggleSidebar"
        >
          <NavigationIcon name="menu" class="menu-icon" />
        </button>

        <AppHeader :username="username" @profile="router.push('/app/profile')" />

      </div>

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


.sidebar-container {
  position: sticky;
  top: 0;

  width: 248px;
  height: 100vh;

  flex-shrink: 0;

  z-index: 30;
}


.main-container {
  display: flex;
  flex-direction: column;

  flex: 1;
  min-width: 0;
  min-height: 100vh;
}


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


.main-content {
  flex: 1;

  width: 100%;
  min-width: 0;

  padding: 32px 40px 48px;
}


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
.menu-icon { width:22px; height:22px; }

.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: 25;

  background: rgba(0, 0, 0, 0.65);
}


.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}


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
