<script setup>
import {ref,computed} from 'vue'
import MovieGrid from '../components/MovieGrid.vue'
import {pick} from '../data/movies'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const store=useMoviesStore(),tabs=['Para ti','Usuarios con gustos similares','Basadas en tus favoritas','Por género'],tab=ref(tabs[0])
const sets=[pick(2,3,4,5,6,7,8,9,10,15,1,12,11,13,16),pick(7,8,9,10,11,15,2,4),pick(1,2,4,5,8,12,16),pick(1,2,5,12,3,4,16)]
const results=computed(()=>sets[tabs.indexOf(tab.value)])
</script>
<template><div class="view"><div class="view-heading"><h1>Recomendadas para ti</h1><p>Descubre películas seleccionadas según tus gustos y calificaciones.</p></div><div class="chips"><button v-for="item in tabs" :key="item" class="chip" :class="{active:tab===item}" @click="tab=item">{{item}}</button></div><p class="muted">Basadas en tus calificaciones de Inception, Interstellar y The Dark Knight.</p><MovieGrid :movies="results" :saved-ids="store.savedIds" size="M" :columns="5" @save="store.toggleSave"/></div></template>
