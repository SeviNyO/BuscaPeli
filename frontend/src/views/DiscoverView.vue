<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import MovieGrid from '../components/MovieGrid.vue'
import { useMoviesStore } from '../stores/movies'
import '../assets/styles/views.css'

const genres = ['Todos', 'Acción', 'Aventura', 'Animación', 'Comedia', 'Crimen', 'Documental', 'Drama', 'Familia', 'Fantasía', 'Cine negro', 'Terror', 'Música', 'Misterio', 'Romance', 'Ciencia ficción', 'Suspenso', 'Guerra', 'Western']

const route = useRoute()
const store = useMoviesStore()
const query = ref('')
const genre = ref('Todos')
const year = ref('')
const sort = ref('title')
const page = ref(1)
const total = ref(0)
const suggested = ref(false)
let timer
const results = computed(() => store.catalog)

function load(append = false) {
  clearTimeout(timer)
  timer = setTimeout(async () => {
    try {
      const result = await store.fetchMovies({
        search: query.value.trim() || undefined,
        genre: genre.value === 'Todos' ? undefined : genre.value,
        year: year.value || undefined,
        sort: sort.value,
        page: append ? page.value : 1,
        pageSize: 100,
      }, append)
      total.value = result.total
      suggested.value = result.suggested
      if (!append) page.value = 1
    } catch { total.value = 0 }
  }, query.value ? 250 : 0)
}

watch(() => route.query, value => {
  query.value = String(value.search || '')
  genre.value = genres.includes(value.genre) ? value.genre : 'Todos'
}, { immediate: true })
watch([query, genre, year, sort], () => { page.value = 1; load() })

async function loadMore() {
  page.value += 1
  load(true)
}
</script>

<template>
  <div class="view">
    <div class="view-heading"><h1>Descubre películas</h1><p>Explora el catálogo por título, género y año.</p></div>
    <div class="filters">
      <div class="filter-row">
        <input v-model="query" placeholder="Buscar por título…" aria-label="Buscar por título">
        <input v-model="year" type="number" min="1900" max="2026" placeholder="Año de estreno" aria-label="Año de estreno">
        <select v-model="sort" aria-label="Ordenar"><option value="title">Título</option><option value="popular">Más calificadas</option><option value="top_rated">Mejor calificadas</option></select>
      </div>
      <div class="chips"><button v-for="g in genres" :key="g" class="chip" :class="{active:genre===g}" @click="genre=g">{{g}}</button></div>
    </div>
    <p v-if="suggested && results.length" class="muted">No encontramos una coincidencia exacta. ¿Quisiste decir <strong>{{results[0].title}}</strong>?</p>
    <p class="muted">{{store.loading ? 'Buscando…' : `Mostrando ${results.length} de ${total} películas`}}</p>
    <MovieGrid :movies="results" :saved-ids="store.savedIds" size="M" :columns="5" @save="store.toggleSave" />
    <button v-if="results.length<total" class="btn-secondary center-button" @click="loadMore">Cargar más películas</button>
    <p v-if="store.error" class="error" role="alert">{{store.error}}</p>
  </div>
</template>
