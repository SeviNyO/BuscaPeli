<script setup>
import {ref,computed,watch} from 'vue'
import {useRouter} from 'vue-router'
import AuthLayout from '../layouts/AuthLayout.vue'
import AppLogo from '../components/AppLogo.vue'
import StarRating from '../components/StarRating.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const store=useMoviesStore(),router=useRouter(),query=ref('')
const selected=ref([])
const count=computed(()=>selected.value.filter(m=>store.ratings[m.id]>0).length)
const results=computed(()=>query.value.trim()?store.catalog.filter(m=>!selected.value.some(s=>s.id===m.id)).slice(0,8):[])
let timer
watch(query,value=>{clearTimeout(timer);timer=setTimeout(()=>{if(value.trim())store.fetchMovies({search:value.trim(),page:1,pageSize:12}).catch(()=>{})},250)})
function add(movie){if(selected.value.length<5&&!selected.value.some(item=>item.id===movie.id))selected.value.push(movie);query.value=''}
function remove(movie){selected.value=selected.value.filter(m=>m.id!==movie.id);store.rate(movie.id,0)}
</script>
<template>
<AuthLayout :show-card="false" :show-collage="false">
 <section class="onboarding">
  <AppLogo />
  <div class="view-heading"><h1>Califica 5 películas que hayas visto</h1><p>Así podremos encontrar películas que realmente te gusten. Has calificado {{count}} de 5.</p></div>
  <div class="progress"><span :style="{width:`${count*20}%`}"></span></div>
  <div class="onboarding-search"><input v-model="query" placeholder="Buscar película por título…" :disabled="selected.length>=5" aria-label="Buscar película"><div v-if="results.length" class="onboarding-results"><button v-for="movie in results" :key="movie.id" @click="add(movie)">{{movie.title}} ({{movie.year || 'año desconocido'}})</button></div></div>
  <div class="onboarding-cards">
   <div v-for="movie in selected" :key="movie.id" class="onboarding-item"><button class="onboarding-remove" aria-label="Quitar película" @click="remove(movie)">×</button><div class="onboarding-poster" :style="{backgroundImage:movie.gradient}"><span>{{movie.posterTitle}}</span></div><strong>{{movie.title}}</strong><StarRating :model-value="store.ratings[movie.id]||0" @update:model-value="store.rate(movie.id,$event)"/></div>
   <div v-for="i in 5-selected.length" :key="`empty-${i}`" class="empty-poster">＋<br>Busca y elige tu película</div>
  </div>
  <p class="muted">{{count===5?'¡Todo listo!':`Califica ${5-count} película(s) más para continuar`}}</p><button class="btn-primary" style="width:280px" :disabled="count!==5" @click="router.push('/app/home')">Continuar</button>
 </section>
</AuthLayout>
</template>
