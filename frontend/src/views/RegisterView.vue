<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import AuthLayout from '../layouts/AuthLayout.vue'
import AppLogo from '../components/AppLogo.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const router=useRouter(), store=useMoviesStore()
const username=ref(''), email=ref(''), password=ref(''), confirm=ref(''), visible=ref(false), error=ref(''), loading=ref(false)
async function register(){error.value='';if(!/^(?=.*[a-zA-Z])(?=.*\d).{8,}$/.test(password.value)){error.value='Usa al menos 8 caracteres, letras y números.';return}if(password.value!==confirm.value){error.value='Las contraseñas no coinciden.';return}loading.value=true;try{await store.registerAccount(username.value.trim(),email.value,password.value);router.push('/onboarding')}catch(e){error.value=e.message}finally{loading.value=false}}
</script>
<template>
<AuthLayout>
 <AppLogo />
 <div class="auth-heading"><h1>Crea tu cuenta</h1><p>Comienza a descubrir películas pensadas para ti</p></div>
 <form class="auth-form" @submit.prevent="register">
  <label class="auth-field"><span class="field-icon">♙</span><input v-model="username" required placeholder="Nombre de usuario" aria-label="Nombre de usuario" autocomplete="username"></label>
  <label class="auth-field"><span class="field-icon">✉</span><input v-model="email" required type="email" placeholder="Correo electrónico" aria-label="Correo electrónico" autocomplete="email"></label>
  <label class="auth-field"><span class="field-icon">♧</span><input v-model="password" required :type="visible?'text':'password'" placeholder="Contraseña" aria-label="Contraseña" autocomplete="new-password"><button type="button" aria-label="Cambiar visibilidad" @click="visible=!visible">◉</button></label>
  <small class="muted">Mínimo 8 caracteres. Usa letras y números.</small>
  <label class="auth-field"><span class="field-icon">♧</span><input v-model="confirm" required :type="visible?'text':'password'" placeholder="Confirmar contraseña" aria-label="Confirmar contraseña" autocomplete="new-password"></label>
  <p v-if="error" class="error" role="alert">{{error}}</p>
  <button class="btn-primary" :disabled="loading">{{loading?'Creando cuenta…':'Registrarme'}}</button>
 </form>
 <div class="auth-foot"><p>¿Ya tienes una cuenta? <RouterLink to="/login">Inicia sesión</RouterLink></p></div>
</AuthLayout>
</template>
