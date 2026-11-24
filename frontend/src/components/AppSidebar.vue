
<script setup>
import { useRoute, useRouter } from 'vue-router'
import AppLogo from './AppLogo.vue'

import homeIcon from '../assets/figma/f8e05.svg'
import homeActive from '../assets/figma/d242d.svg'

import sparklesIcon from '../assets/figma/2b44b.svg'
import sparklesActive from '../assets/figma/48741.svg'

import compassIcon from '../assets/figma/9e7b4.svg'
import compassActive from '../assets/figma/9f1dd.svg'

import listIcon from '../assets/figma/88a19.svg'
import listActive from '../assets/figma/5c811.svg'

import userIcon from '../assets/figma/91670.svg'
import userActive from '../assets/figma/2b8b2.svg'

import logoutIcon from '../assets/figma/0a1ad.svg'

const route = useRoute()
const router = useRouter()

const emit = defineEmits(['logout', 'profile'])

const navigation = [
  {
    label: 'Home',
    path: '/app/home',
    icon: homeIcon,
    activeIcon: homeActive
  },
  {
    label: 'Recomendadas',
    path: '/app/recommended',
    icon: sparklesIcon,
    activeIcon: sparklesActive
  },
  {
    label: 'Descubrir',
    path: '/app/discover',
    icon: compassIcon,
    activeIcon: compassActive
  },
  {
    label: 'Mis listas',
    path: '/app/lists',
    icon: listIcon,
    activeIcon: listActive
  },
  {
    label: 'Mi perfil',
    path: '/app/profile',
    icon: userIcon,
    activeIcon: userActive
  }
]

function isActive(path) {
  return route.path === path ||
    route.path.startsWith(path + '/')
}

function navigate(item) {
  if (item.path === '/app/profile') {
    emit('profile')
    return
  }

  router.push(item.path)
}

function logout() {
  emit('logout')
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="logo-wrapper">
      <AppLogo />
    </div>

    <nav class="navigation">
      <button
        v-for="item in navigation"
        :key="item.path"
        type="button"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        :aria-current="isActive(item.path) ? 'page' : undefined"
        @click="navigate(item)"
      >
        <img
          :src="isActive(item.path)
            ? item.activeIcon
            : item.icon"
          alt=""
          class="nav-icon"
        />

        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="spacer"></div>

    <button
      type="button"
      class="logout"
      @click="logout"
    >
      <img
        :src="logoutIcon"
        alt=""
        class="nav-icon"
      />

      <span>Cerrar sesión</span>
    </button>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 248px;
  height: 100vh;
  position: sticky;
  top: 0;
  flex-shrink: 0;

  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;

  padding: 28px 16px 24px;

  background: var(--color-bg-app);
  border-right: 1px solid var(--color-border-default);

  overflow-y: auto;
}

.logo-wrapper {
  padding-left: 8px;
  padding-bottom: 28px;
}

.navigation {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item,
.logout {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;

  padding: 13px 16px;

  background: transparent;
  border: none;
  text-align: left;
  border-radius: 12px;

  color: var(--color-text-secondary);

  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  white-space: nowrap;

  transition: background 0.2s;
}

.nav-item:hover {
  background: var(--color-bg-surface);
}

.nav-item.active {
  background: var(--color-brand-primary);
  color: var(--color-text-primary);
}

.nav-icon {
  display: block;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.spacer {
  flex: 1;
}

.logout {
  color: var(--color-text-muted);
}

.logout:hover {
  background: var(--color-bg-surface);
  color: var(--color-text-primary);
}
</style>

