<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import AuthLayout from '../layouts/AuthLayout.vue'
import AppLogo from '../components/AppLogo.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const router=useRouter(), store=useMoviesStore()
const username=ref(''), password=ref(''), visible=ref(false), message=ref('')
function login(){store.username=username.value.trim();router.push('/app/home')}
</script>
<template>
<AuthLayout>
  <AppLogo />
  <div class="auth-heading"><h1>Inicia sesión</h1><p>Accede a tus recomendaciones personalizadas</p></div>
  <form class="auth-form" @submit.prevent="login">
    <label class="auth-field"><span class="field-icon" aria-hidden="true">♙</span><input v-model="username" required placeholder="Nombre de usuario" aria-label="Nombre de usuario" autocomplete="username"></label>
    <label class="auth-field"><span class="field-icon" aria-hidden="true">♧</span><input v-model="password" required :type="visible?'text':'password'" placeholder="Contraseña" aria-label="Contraseña" autocomplete="current-password"><button type="button" :aria-label="visible?'Ocultar contraseña':'Mostrar contraseña'" @click="visible=!visible">◉</button></label>
    <button class="btn-primary">Ingresar</button>
  </form>
  <div class="auth-foot"><button class="plain-button" @click="message='La recuperación requiere conectar el backend.'">¿Olvidaste tu contraseña?</button><p>¿No tienes una cuenta? <RouterLink to="/register">Regístrate</RouterLink></p><p v-if="message" role="status">{{message}}</p></div>
</AuthLayout>
</template>
