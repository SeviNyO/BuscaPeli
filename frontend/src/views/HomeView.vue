<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MovieGrid from '../components/MovieGrid.vue'
import NavigationIcon from '../components/NavigationIcon.vue'
import { useMoviesStore } from '../stores/movies'
import '../assets/styles/views.css'

const router = useRouter()
const store = useMoviesStore()
const featured = ref(null)
const personalized = ref([])
const popular = ref([])
const topRated = ref([])
const genres = ['Acción', 'Comedia', 'Drama', 'Ciencia ficción', 'Animación', 'Suspenso']

onMounted(async () => {
  try {
    popular.value = await store.fetchPopular(12)
    topRated.value = await store.fetchTopRated(12)
    const result = await store.fetchForMe()
    personalized.value = result.items
    featured.value = personalized.value[0] || popular.value[0]
    if (featured.value) {
      const details = await store.fetchMovie(featured.value.id)
      featured.value = { ...featured.value, ...details, ...details.movie }
    }
  } catch {
    featured.value = popular.value[0] || null
  }
})
</script>

<template>
  <div class="view">
    <section v-if="featured" class="hero" :style="featured.backdrop_url || featured.poster_url ? {backgroundImage:`linear-gradient(90deg,#101116 25%,#101116aa),url(${featured.backdrop_url || featured.poster_url})`,backgroundSize:'cover',backgroundPosition:'center'} : {}">
      <span class="eyebrow">RECOMENDADA PARA TI</span><h1>{{featured.title}}</h1>
      <div class="actions"><span class="rating-text">★ {{featured.rating?.toFixed(1)}}</span><span>{{featured.year}}</span><span>{{featured.genres?.join(' · ')}}</span></div>
      <p>{{featured.overview || 'Una recomendación basada en tus calificaciones y en las comunidades globales de usuarios.'}}</p>
      <div class="actions"><button class="btn-primary" @click="router.push(`/app/movies/${featured.id}`)">Ver detalles</button><button class="btn-secondary favorite-action" @click="store.toggleSave(featured)"><NavigationIcon name="heart" class="action-icon" :class="{filled:store.savedIds.includes(featured.id)}" />{{store.savedIds.includes(featured.id)?'Quitar de favoritos':'Agregar a favoritos'}}</button></div>
    </section>
    <MovieGrid title="Recomendadas para ti" :movies="personalized" size="S" :columns="6" show-more :saved-ids="store.savedIds" @save="store.toggleSave" @view-more="router.push('/app/recommended')" />
    <MovieGrid title="Más populares" :movies="popular" size="S" :columns="6" :saved-ids="store.savedIds" @save="store.toggleSave" />
    <MovieGrid title="Mejor calificadas" :movies="topRated" size="S" :columns="6" :saved-ids="store.savedIds" @save="store.toggleSave" />
    <section class="section-stack"><h3>Explora otros géneros</h3><div class="chips"><button v-for="genre in genres" :key="genre" class="chip" @click="router.push({path:'/app/discover',query:{genre}})">{{genre}}</button></div></section>
    <p v-if="store.error" class="error" role="alert">{{store.error}}</p>
  </div>
</template>

<style scoped>
.favorite-action { display:inline-flex; align-items:center; gap:10px; }
.action-icon { width:20px; height:20px; flex-shrink:0; }
.action-icon.filled { fill:var(--color-brand-primary); }
</style>
