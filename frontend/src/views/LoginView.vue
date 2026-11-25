<script setup>
import {onMounted,ref} from 'vue'
import {useRouter} from 'vue-router'
import AuthLayout from '../layouts/AuthLayout.vue'
import AppLogo from '../components/AppLogo.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const router=useRouter(), store=useMoviesStore()
const email=ref(''), password=ref(''), visible=ref(false), message=ref(''), loading=ref(false)
onMounted(()=>store.loadSession().then(session=>{if(session?.authenticated)router.push('/app/home')}))
async function login(){loading.value=true;message.value='';try{await store.login(email.value,password.value);router.push('/app/home')}catch(error){message.value=error.message}finally{loading.value=false}}
</script>
<template>
<AuthLayout>
  <AppLogo />
  <div class="auth-heading"><h1>Inicia sesión</h1><p>Accede a tus recomendaciones personalizadas</p></div>
  <form class="auth-form" @submit.prevent="login">
    <label class="auth-field"><span class="field-icon" aria-hidden="true">♙</span><input v-model="email" required type="email" placeholder="Correo electrónico" aria-label="Correo electrónico" autocomplete="username"></label>
    <label class="auth-field"><span class="field-icon" aria-hidden="true">♧</span><input v-model="password" required :type="visible?'text':'password'" placeholder="Contraseña" aria-label="Contraseña" autocomplete="current-password"><button type="button" :aria-label="visible?'Ocultar contraseña':'Mostrar contraseña'" @click="visible=!visible">◉</button></label>
    <button class="btn-primary" :disabled="loading">{{loading?'Ingresando…':'Ingresar'}}</button>
  </form>
  <div class="auth-foot"><button class="plain-button" @click="message='La recuperación requiere conectar el backend.'">¿Olvidaste tu contraseña?</button><p>¿No tienes una cuenta? <RouterLink to="/register">Regístrate</RouterLink></p><p v-if="message" role="status">{{message}}</p></div>
</AuthLayout>
</template>
