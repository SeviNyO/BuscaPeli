<script setup>
import { ref, computed } from 'vue'
import MovieGrid from '../components/MovieGrid.vue'
import NavigationIcon from '../components/NavigationIcon.vue'
import { useMoviesStore } from '../stores/movies'
import '../assets/styles/views.css'

const store = useMoviesStore()
const active = ref('Favoritas')
const filter = ref('Todas')
const lists = computed(() => [
  { name: 'Favoritas', icon: 'heart', items: store.favorites },
  { name: 'Quiero ver', icon: 'bookmark', items: store.watchlist },
  { name: 'Ya vistas', icon: 'check', items: store.ratedMovies },
])
const filters = ['Todas', 'Con calificación', 'Sin calificar']
const current = computed(() => lists.value.find(list => list.name === active.value))
const results = computed(() => current.value.items.filter(movie =>
  filter.value === 'Todas' || (filter.value === 'Con calificación' ? !!store.ratings[movie.id] : !store.ratings[movie.id])
))
</script>

<template>
  <div class="view">
    <div class="view-heading"><h1>Mis listas</h1><p>Organiza tus películas según tu interés y tu actividad.</p></div>
    <div class="list-types">
      <button v-for="list in lists" :key="list.name" class="panel list-type" :class="{ active: active === list.name }" @click="active = list.name">
        <div class="list-icon"><NavigationIcon :name="list.icon" class="list-symbol" /></div>
        <h3>{{ list.name }}</h3>
        <p>{{ list.items.length }} películas</p>
      </button>
    </div>
    <div class="view-heading"><h2>{{ active }}</h2><p>{{ current.items.length }} películas</p></div>
    <div class="chips"><button v-for="item in filters" :key="item" class="chip" :class="{ active: filter === item }" @click="filter = item">{{ item }}</button></div>
    <MovieGrid :movies="results" size="M" :columns="5" :saved-ids="store.savedIds" @save="store.toggleSave" />
    <p v-if="!results.length" class="muted">Esta lista todavía está vacía.</p>
  </div>
</template>

<style scoped>
.list-symbol { width:24px; height:24px; }
</style>
