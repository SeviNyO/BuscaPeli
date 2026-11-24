
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

import searchIcon from '../assets/figma/89de3.svg'
import bellIcon from '../assets/figma/a5a56.svg'
import chevronIcon from '../assets/figma/21bd4.svg'

const props = defineProps({
  username: {
    type: String,
    default: 'paula'
  }
})

const emit = defineEmits([
  'notifications',
  'profile'
])

const router = useRouter()
const search = ref('')

const initial = computed(() =>
  props.username.charAt(0).toUpperCase()
)

function handleSearch() {
  const query = search.value.trim()

  if (!query) return

  router.push({
    path: '/app/discover',
    query: { search: query }
  })
}
</script>

<template>
  <header class="app-header">
    <form
      class="search-box"
      role="search"
      @submit.prevent="handleSearch"
    >
      <img
        :src="searchIcon"
        alt=""
        class="search-icon"
      />

      <input
        v-model="search"
        type="search"
        aria-label="Buscar películas"
        placeholder="Buscar películas, actores o géneros…"
      />
    </form>

    <div class="spacer"></div>

    <button
      class="notification-button"
      type="button"
      aria-label="Notificaciones"
      @click="emit('notifications')"
    >
      <img :src="bellIcon" alt="" />
    </button>

    <button
      class="profile-button"
      type="button"
      @click="emit('profile')"
    >
      <div class="avatar">
        {{ initial }}
      </div>

      <span class="username">
        {{ username }}
      </span>

      <img
        :src="chevronIcon"
        alt=""
        class="chevron"
      />
    </button>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  gap: 16px;

  width: 100%;
  height: 76px;
  padding: 0 40px;

  background: var(--color-bg-app);
  border-bottom: 1px solid var(--color-border-default);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;

  width: 520px;
  height: 44px;
  min-width: 0;
  padding: 0 16px;

  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: 22px;
}

.search-box:focus-within {
  border-color: var(--color-brand-primary);
}

.search-icon {
  display: block;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.search-box input {
  width: 100%;
  height: 100%;
  min-height: 0;
  min-width: 0;

  padding: 0;

  border: none;
  background: transparent;
  outline: none;

  color: var(--color-text-primary);
  font-size: 14px;
  line-height: 22px;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
}

.spacer {
  flex: 1;
}

.notification-button {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 44px;
  height: 44px;
  flex-shrink: 0;

  border: none;
  border-radius: 22px;
  background: var(--color-bg-surface);
}

.notification-button img {
  width: 20px;
  height: 20px;
}

.profile-button {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;

  padding: 6px 14px 6px 6px;

  border: none;
  border-radius: 26px;
  background: var(--color-bg-surface);

  color: var(--color-text-primary);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 32px;
  height: 32px;

  background: var(--color-brand-primary);
  border-radius: 50%;

  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.chevron {
  width: 16px;
  height: 16px;
}

@media (max-width: 800px) {
  .app-header {
    padding: 0 16px;
    gap: 10px;
  }

  .search-box {
    flex: 1;
  }

  .spacer,
  .username {
    display: none;
  }
}
</style>

