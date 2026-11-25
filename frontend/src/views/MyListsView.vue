<script setup>
import {ref,computed} from 'vue'
import MovieGrid from '../components/MovieGrid.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const store=useMoviesStore(),active=ref('Favoritas'),filter=ref('Todas')
const lists=computed(()=>[{name:'Favoritas',icon:'♡',items:store.favorites},{name:'Quiero ver',icon:'＋',items:store.watchlist},{name:'Ya vistas',icon:'✓',items:store.ratedMovies}])
const filters=['Todas','Con calificación','Sin calificar']
const current=computed(()=>lists.value.find(l=>l.name===active.value))
const results=computed(()=>current.value.items.filter(m=>filter.value==='Todas'||(filter.value==='Con calificación'?!!store.ratings[m.id]:!store.ratings[m.id])))
</script>
<template><div class="view"><div class="view-heading"><h1>Mis listas</h1><p>Organiza tus películas según tu interés y tu actividad.</p></div><div class="list-types"><button v-for="list in lists" :key="list.name" class="panel list-type" :class="{active:active===list.name}" @click="active=list.name"><div class="list-icon">{{list.icon}}</div><h3>{{list.name}}</h3><p>{{list.items.length}} películas</p></button></div><div class="view-heading"><h2>{{active}}</h2><p>{{current.items.length}} películas</p></div><div class="chips"><button v-for="item in filters" :key="item" class="chip" :class="{active:filter===item}" @click="filter=item">{{item}}</button></div><MovieGrid :movies="results" size="M" :columns="5" :saved-ids="store.savedIds" @save="store.toggleSave"/><p v-if="!results.length" class="muted">Esta lista todavía está vacía.</p></div></template>
