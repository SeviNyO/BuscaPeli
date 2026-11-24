<script setup>
import {ref,computed,watch} from 'vue'
import {useRoute} from 'vue-router'
import MovieGrid from '../components/MovieGrid.vue'
import {movies,genres,pick} from '../data/movies'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const route=useRoute(),store=useMoviesStore(),query=ref(''),genre=ref('Todos'),year=ref(''),sort=ref('default'),limit=ref(15)
watch(()=>route.query,q=>{query.value=String(q.search||'');genre.value=genres.includes(q.genre)?q.genre:'Todos'},{immediate:true})
watch([query,genre,year,sort],()=>limit.value=15)
const ordered=[...pick(7,8,9,10,1,12,11,13,14,15,2,3,4,5,6,16,17,18,19)]
const results=computed(()=>{let r=ordered.filter(m=>m.title.toLowerCase().includes(query.value.toLowerCase())&&(genre.value==='Todos'||m.genre===genre.value)&&(!year.value||m.year===Number(year.value)));if(sort.value==='rating')r.sort((a,b)=>b.rating-a.rating);if(sort.value==='year')r.sort((a,b)=>b.year-a.year);return r})
const years=[...new Set(movies.map(m=>m.year))].sort((a,b)=>b-a)
</script>
<template><div class="view"><div class="view-heading"><h1>Descubre películas</h1><p>Explora el catálogo completo por título, género y año de estreno.</p></div><div class="filters"><div class="filter-row"><input v-model="query" placeholder="Buscar por título…" aria-label="Buscar por título"><select v-model="year" aria-label="Año de estreno"><option value="">Año de estreno</option><option v-for="y in years" :key="y" :value="y">{{y}}</option></select><select v-model="sort" aria-label="Ordenar"><option value="default">Ordenar: Catálogo</option><option value="rating">Ordenar: Puntuación</option><option value="year">Ordenar: Año</option></select></div><div class="chips"><button v-for="g in genres" :key="g" class="chip" :class="{active:genre===g}" @click="genre=g">{{g}}</button></div></div><p class="muted">Mostrando {{Math.min(limit,results.length)}} de {{results.length}} películas</p><MovieGrid :movies="results.slice(0,limit)" :saved-ids="store.savedIds" size="M" :columns="5" @save="store.toggleSave"/><button v-if="limit<results.length" class="btn-secondary center-button" @click="limit+=15">Cargar más películas</button></div></template>
