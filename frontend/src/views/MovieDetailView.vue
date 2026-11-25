<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MovieGrid from '../components/MovieGrid.vue'
import StarRating from '../components/StarRating.vue'
import NavigationIcon from '../components/NavigationIcon.vue'
import { useMoviesStore } from '../stores/movies'
import '../assets/styles/views.css'

const route=useRoute(), router=useRouter(), store=useMoviesStore()
const detail=ref(null), related=ref([]), byGenre=ref([]), loading=ref(true), ratingPanel=ref(null), tag=ref(''), editing=ref(false)
const movie=computed(()=>detail.value?.movie)
const info=computed(()=>detail.value)
watch(()=>route.params.id,async id=>{
  loading.value=true
  try {
    detail.value=await store.fetchMovie(id)
    const [collaborative,genre] = await Promise.allSettled([
      store.fetchRecommendations(id,'collaborative'),store.fetchRecommendations(id,'genre')
    ])
    related.value=collaborative.status==='fulfilled'?collaborative.value.items.slice(0,6):[]
    byGenre.value=genre.status==='fulfilled'?genre.value.items.slice(0,6):[]
  } catch(error) { detail.value=null; store.error=error.message }
  finally { loading.value=false }
},{immediate:true})
async function addTag(){await store.addTag(movie.value.id,tag.value);tag.value='';editing.value=false}
function more(){router.push(`/app/movies/${movie.value.id}/recommendations`)}
</script>
<template>
  <div v-if="movie" :key="movie.id" class="view">
    <button class="back" @click="router.push('/app/discover')">← Volver</button>
    <section class="hero" :style="info.backdrop_url || info.poster_url ? {backgroundImage:`linear-gradient(90deg,#101116 20%,#10111699),url(${info.backdrop_url || info.poster_url})`,backgroundSize:'cover',backgroundPosition:'center'} : {}">
      <h1>{{movie.title}}</h1><div class="actions"><span>{{movie.year}}</span><span v-if="info.runtime">{{info.runtime}} min</span></div>
      <div class="chips"><span v-for="genre in movie.genres" :key="genre" class="chip outline">{{genre}}</span></div>
      <p><span class="rating-text">★ {{movie.rating.toFixed(1)}}</span> · {{movie.rating_count}} calificaciones</p>
      <div class="actions"><button class="btn-secondary" @click="ratingPanel?.scrollIntoView({behavior:'smooth'})">Calificar</button><button class="btn-secondary icon-action" @click="store.toggleSave(movie)"><NavigationIcon name="heart" class="action-icon" :class="{filled:store.savedIds.includes(movie.id)}" />{{store.savedIds.includes(movie.id)?'Quitar de favoritos':'Agregar a favoritos'}}</button><button class="btn-secondary icon-action" @click="store.toggleWatch(movie)"><NavigationIcon name="bookmark" class="action-icon" :class="{filled:store.watchIds.includes(movie.id)}" />{{store.watchIds.includes(movie.id)?'Quitar de Quiero ver':'Agregar a Quiero ver'}}</button></div>
    </section>
    <section class="detail-info"><div class="section-stack"><h2>Sinopsis</h2><p class="muted">{{info.overview||'Sinopsis no disponible en TMDB.'}}</p></div><div class="panel facts"><div><small>FECHA DE ESTRENO</small>{{info.release_date||movie.year||'—'}}</div><div><small>DURACIÓN</small>{{info.runtime?`${info.runtime} min`:'—'}}</div><div><small>GÉNEROS</small>{{movie.genres.join(', ')||'—'}}</div><div><small>CALIFICACIÓN</small>{{movie.rating.toFixed(1)}} / 5</div></div></section>
    <section v-if="info.cast?.length" class="section-stack"><h2>Reparto principal</h2><div class="cast"><div v-for="person in info.cast" :key="person.name" class="cast-person"><div class="cast-avatar">{{person.name.split(' ').map(x=>x[0]).slice(0,2).join('')}}</div><strong>{{person.name}}</strong><p class="muted">{{person.character}}</p></div></div></section>
    <section ref="ratingPanel" class="panel section-stack"><h3>Tu calificación</h3><StarRating :model-value="store.ratings[movie.id]||0" @update:model-value="store.rate(movie.id,$event)"/><h3>Tus tags personales</h3><div class="chips"><span v-for="t in store.tags[movie.id]||[]" :key="t" class="chip">{{t}}</span><button class="chip outline" @click="editing=!editing">+ Añadir tag</button></div><form v-if="editing" class="filter-row" @submit.prevent="addTag"><input v-model="tag" placeholder="Nuevo tag" required><button class="btn-primary">Añadir</button></form></section>
    <section class="section-stack"><h2>Usuarios con gustos similares también calificaron bien…</h2><MovieGrid :movies="related" size="S" :columns="6" :saved-ids="store.savedIds" @save="store.toggleSave"/><button class="btn-secondary center-button" @click="more">Ver más recomendaciones</button></section>
    <MovieGrid title="Películas del mismo género" :movies="byGenre" size="S" :columns="6" show-more :saved-ids="store.savedIds" @save="store.toggleSave" @view-more="more"/>
    <p v-if="store.error" class="error" role="alert">{{store.error}}</p>
  </div>
  <div v-else-if="loading" class="view"><p>Cargando película…</p></div>
  <div v-else class="view"><h1>Película no encontrada</h1><RouterLink to="/app/discover">Volver al catálogo</RouterLink></div>
</template>

<style scoped>
.icon-action { display:inline-flex; align-items:center; gap:10px; }
.action-icon { width:20px; height:20px; flex-shrink:0; }
.action-icon.filled { fill:var(--color-brand-primary); }
</style>
