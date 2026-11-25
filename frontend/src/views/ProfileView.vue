<script setup>
import { useMoviesStore } from '../stores/movies'
import '../assets/styles/views.css'

const store = useMoviesStore()
</script>

<template>
  <div class="profile-view">
    <div class="view-heading">
      <h1>Mi perfil</h1>
      <p class="muted">La información con la que creaste tu cuenta.</p>
    </div>
    <section class="profile-card panel" aria-label="Datos de la cuenta">
      <div class="profile-avatar" aria-hidden="true">{{ store.user?.name?.charAt(0).toUpperCase() }}</div>
      <div>
        <h2>{{ store.user?.name }}</h2>
        <p class="muted">Cuenta de BuscaPeli</p>
      </div>
      <div class="profile-field">
        <span>Nombre</span>
        <strong>{{ store.user?.name }}</strong>
      </div>
      <div class="profile-field">
        <span>Correo electrónico</span>
        <strong>{{ store.user?.email }}</strong>
      </div>
    </section>
  </div>
</template>

<style scoped>
.profile-view { display: flex; flex-direction: column; gap: 28px; max-width: 720px; margin: 0 auto; }
.view-heading { display: flex; flex-direction: column; gap: 8px; }
.muted,.profile-field span { color: var(--color-text-secondary); }
.profile-card { display: flex; flex-direction: column; align-items: flex-start; gap: 16px; padding: 32px; }
.profile-avatar { display: grid; place-items: center; width: 72px; height: 72px; border-radius: 50%; background: var(--color-brand-primary); color: white; font-size: 28px; font-weight: 700; }
.profile-field { display: flex; flex-direction: column; gap: 6px; width: 100%; padding-top: 16px; border-top: 1px solid var(--color-border-default); }
.profile-field strong { overflow-wrap: anywhere; }
@media (max-width: 600px) { .profile-card { padding: 20px; } }
</style>
