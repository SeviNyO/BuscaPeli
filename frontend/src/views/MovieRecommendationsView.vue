<script setup>
import {ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {useMoviesStore} from '../stores/movies'
import MoviePoster from '../components/MoviePoster.vue'
import NavigationIcon from '../components/NavigationIcon.vue'
import '../assets/styles/views.css'
const store=useMoviesStore(),route=useRoute(),router=useRouter(),tab=ref('affinity'),movie=ref(null),results=ref([])
watch([()=>route.params.id,tab],async([id,selected])=>{
  try { const result=await store.fetchRecommendations(id,selected==='genre'?'genre':'collaborative');movie.value=result.movie;results.value=result.items }
  catch { movie.value=null;results.value=[] }
},{immediate:true})
</script>
<template>
  <div v-if="movie" class="view">
    <button class="back" @click="router.push(`/app/movies/${movie.id}`)">← Volver al detalle</button>
    <div class="view-heading"><h1>Porque te interesó {{movie.title}}</h1></div>
    <section class="panel movie-row">
      <MoviePoster class="row-poster" :movie="movie"><template #fallback>{{movie.posterTitle}}</template></MoviePoster>
      <div class="row-copy"><h2>{{movie.title}}</h2><div class="chips"><span class="chip outline">{{movie.genre}}</span><span class="chip outline">{{movie.year}}</span></div></div>
    </section>
    <h2>Películas que podrían gustarte</h2>
    <div class="chips"><button class="chip" :class="{active:tab==='affinity'}" @click="tab='affinity'">Usuarios con gustos similares</button><button class="chip" :class="{active:tab==='genre'}" @click="tab='genre'">Por género</button></div>
    <div class="row-stack">
      <article v-for="m in results" :key="m.id" class="movie-row">
        <button class="row-poster" :aria-label="`Ver detalles de ${m.title}`" @click="router.push(`/app/movies/${m.id}`)"><MoviePoster class="row-poster-image" :movie="m"><template #fallback>{{m.posterTitle}}</template></MoviePoster></button>
        <div class="row-copy"><RouterLink :to="`/app/movies/${m.id}`"><h3>{{m.title}}</h3></RouterLink><p>{{m.genre}}</p><div class="actions"><span class="rating-text">★ {{m.rating}}</span><span class="muted">{{m.year}}</span></div><p>{{tab==='genre'?'Géneros compartidos: '+(m.shared_genres||[]).join(', '):`${m.recommended_by_users||0} usuarios · costo de afinidad ${m.affinity_cost??'—'}`}}</p></div>
        <button class="btn-secondary" :aria-label="store.savedIds.includes(m.id)?'Quitar de favoritos':'Agregar a favoritos'" :aria-pressed="store.savedIds.includes(m.id)" @click="store.toggleSave(m)"><NavigationIcon name="heart" class="action-icon" :class="{ filled: store.savedIds.includes(m.id) }" /></button>
      </article>
      <p v-if="!results.length" class="muted">No hay recomendaciones para mostrar.</p>
    </div>
  </div>
  <div v-else class="view"><h1>Película no encontrada</h1><RouterLink to="/app/discover">Volver al catálogo</RouterLink></div>
</template>

<style scoped>
.row-poster { overflow: hidden; padding: 0; color: white; border: 0; }
.row-poster-image { width: 100%; height: 100%; }
.action-icon { width:20px; height:20px; }
.action-icon.filled { fill:var(--color-brand-primary); }
</style>
