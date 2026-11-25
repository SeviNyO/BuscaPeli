<script setup>
import {ref,watch} from 'vue'
import MovieGrid from '../components/MovieGrid.vue'
import {useMoviesStore} from '../stores/movies'
import '../assets/styles/views.css'
const store=useMoviesStore(),tabs=['Para ti','Afinidad','Por género'],tab=ref(tabs[0]),results=ref([]),reason=ref('')
async function load(){
  try {
    if(tab.value==='Por género'){
      const favorite=store.ratedMovies.find(movie=>store.ratings[movie.id]>=4)
      if(!favorite){results.value=[];reason.value='Califica una película para obtener recomendaciones por género.';return}
      const result=await store.fetchRecommendations(favorite.id,'genre');results.value=result.items;reason.value=`Géneros compartidos con ${favorite.title}.`
    } else {
      const result=await store.fetchForMe();results.value=result.items
      reason.value=result.cold_start?'Aún no hay suficientes calificaciones: mostramos las más populares.':'Ordenadas por costo de afinidad de menor a mayor.'
    }
  } catch(error){results.value=[];reason.value=error.message}
}
watch(tab,load,{immediate:true})
</script>
<template><div class="view"><div class="view-heading"><h1>Recomendadas para ti</h1><p>Descubre películas seleccionadas según tus gustos y calificaciones.</p></div><div class="chips"><button v-for="item in tabs" :key="item" class="chip" :class="{active:tab===item}" @click="tab=item">{{item}}</button></div><p class="muted">{{reason}}</p><MovieGrid :movies="results" :saved-ids="store.savedIds" size="M" :columns="5" @save="store.toggleSave"/><p v-if="!results.length" class="muted">Todavía no hay recomendaciones para mostrar.</p></div></template>
