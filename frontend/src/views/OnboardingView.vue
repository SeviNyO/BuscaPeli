<script setup>
import {ref,computed,watch} from 'vue'
import {useRouter} from 'vue-router'
import AuthLayout from '../layouts/AuthLayout.vue'
import AppLogo from '../components/AppLogo.vue'
import StarRating from '../components/StarRating.vue'
import MoviePoster from '../components/MoviePoster.vue'
import NavigationIcon from '../components/NavigationIcon.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const store=useMoviesStore(),router=useRouter(),query=ref('')
const selected=ref([])
const pending=ref(0), error=ref('')
const count=computed(()=>selected.value.filter(m=>store.ratings[m.id]>0).length)
const results=computed(()=>query.value.trim()?store.catalog.filter(m=>!selected.value.some(s=>s.id===m.id)).slice(0,8):[])
let timer
watch(query,value=>{clearTimeout(timer);timer=setTimeout(()=>{if(value.trim())store.fetchMovies({search:value.trim(),page:1,pageSize:12}).catch(()=>{})},250)})
function add(movie){if(selected.value.length<5&&!selected.value.some(item=>item.id===movie.id))selected.value.push(movie);query.value=''}
async function rate(movie, value){
  pending.value++
  error.value=''
  try { await store.rate(movie.id,value) }
  catch(e){ error.value=e.message }
  finally { pending.value-- }
}
async function remove(movie){
  pending.value++
  try { await store.rate(movie.id,0);selected.value=selected.value.filter(m=>m.id!==movie.id) }
  catch(e){ error.value=e.message }
  finally { pending.value-- }
}
async function continueToHome(){
  if(pending.value || count.value!==5) return
  pending.value++
  try {
    await store.refreshRatedMovies()
    if(selected.value.every(movie=>store.ratings[movie.id]>0)) await router.push('/app/home')
    else error.value='No se guardaron todas las calificaciones. Intenta nuevamente.'
  } catch(e){ error.value=e.message }
  finally { pending.value-- }
}
</script>
<template>
<AuthLayout :show-card="false" :show-collage="false">
 <section class="onboarding">
  <AppLogo />
  <div class="view-heading"><h1>Califica 5 películas que hayas visto</h1><p>Así podremos encontrar películas que realmente te gusten. Has calificado {{count}} de 5.</p></div>
  <div class="progress"><span :style="{width:`${count*20}%`}"></span></div>
   <div class="onboarding-search"><NavigationIcon name="search" class="onboarding-search-icon" /><input v-model="query" placeholder="Buscar película por título…" :disabled="selected.length>=5" aria-label="Buscar película"><div v-if="results.length" class="onboarding-results"><button v-for="movie in results" :key="movie.id" @click="add(movie)">{{movie.title}} ({{movie.year || 'año desconocido'}})</button></div></div>
  <div class="onboarding-cards">
    <div v-for="movie in selected" :key="movie.id" class="onboarding-item"><button class="onboarding-remove" aria-label="Quitar película" :disabled="pending>0" @click="remove(movie)">×</button><MoviePoster class="onboarding-poster" :movie="movie"><template #fallback><span>{{movie.posterTitle}}</span></template></MoviePoster><strong>{{movie.title}}</strong><StarRating :model-value="store.ratings[movie.id]||0" :readonly="pending>0" @update:model-value="rate(movie,$event)"/></div>
    <div v-for="i in 5-selected.length" :key="`empty-${i}`" class="empty-poster"><NavigationIcon name="plus" class="empty-poster-icon" /><span>Busca y elige tu película</span></div>
  </div>
   <p v-if="error" class="error" role="alert">{{error}}</p><p class="muted">{{pending?'Guardando calificaciones…':count===5?'¡Todo listo!':`Califica ${5-count} película(s) más para continuar`}}</p><button class="btn-primary" style="width:280px" :disabled="count!==5 || pending>0" @click="continueToHome">Continuar</button>
 </section>
</AuthLayout>
</template>

<style scoped>
.onboarding-search { position:relative; }
.onboarding-search-icon { position:absolute; left:14px; top:16px; width:20px; height:20px; color:var(--color-text-secondary); pointer-events:none; }
.onboarding-search input { padding-left:44px; }
.empty-poster-icon { display:block; width:28px; height:28px; margin:0 auto 8px; }
</style>
