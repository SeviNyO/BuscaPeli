
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import NavigationIcon from './NavigationIcon.vue'

defineProps({
  username: {
    type: String,
    default: 'paula'
  }
})

const emit = defineEmits(['profile'])

const router = useRouter()
const search = ref('')

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
      <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true">
        <circle cx="10.8" cy="10.8" r="6.8" />
        <path d="m16 16 4.5 4.5" />
      </svg>

      <input
        v-model="search"
        type="search"
        aria-label="Buscar películas"
        placeholder="Buscar películas, actores o géneros…"
      />
    </form>

    <div class="spacer"></div>

    <button
      class="profile-button"
      type="button"
      aria-label="Abrir mi perfil"
      @click="emit('profile')"
    >
      <div class="avatar"><NavigationIcon name="profile" class="profile-icon" /></div>

      <span class="username">
        {{ username }}
      </span>

      <NavigationIcon name="chevron-down" class="chevron" />
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
.profile-icon { width: 20px; height: 20px; }

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
