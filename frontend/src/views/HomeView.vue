<script setup>
import {useRouter} from 'vue-router'
import MovieGrid from '../components/MovieGrid.vue'
import {pick,genres,interstellar} from '../data/movies'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const router=useRouter(), store=useMoviesStore(), featured=pick(1)[0]
const sections=[{title:'Películas recomendadas para ti',movies:pick(2,3,4,5,6,15)},{title:'A otros usuarios con gustos similares les gustó…',movies:pick(7,8,9,10,11,15)},{title:'Porque calificaste bien películas de ciencia ficción…',movies:pick(1,12,11,5,2,16)}]
</script>
<template>
<div class="view">
 <section class="hero"><span class="eyebrow">DESTACADA PARA TI</span><h1>Interstellar</h1><div class="actions"><span class="rating-text">★ 4.7</span><span>2014</span><span>Ciencia ficción · Aventura</span><span>2 h 49 min</span></div><p>{{interstellar.synopsis}}</p><div class="actions"><button class="btn-primary" @click="router.push('/app/movies/1')">Ver detalles</button><button class="btn-secondary" @click="store.toggleSave(featured)">{{store.savedIds.includes(1)?'Quitar de mi lista':'Agregar a mi lista'}}</button></div></section>
 <MovieGrid v-for="section in sections" :key="section.title" :title="section.title" :movies="section.movies" size="S" :columns="6" show-more :saved-ids="store.savedIds" @save="store.toggleSave" @view-more="router.push('/app/recommended')" />
 <section class="section-stack"><h3>Explora otros géneros</h3><div class="chips"><button v-for="genre in ['Acción','Comedia','Drama','Ciencia ficción','Animación','Suspenso']" :key="genre" class="chip" @click="router.push({path:'/app/discover',query:{genre}})">{{genre}}</button></div></section>
</div>
</template>
