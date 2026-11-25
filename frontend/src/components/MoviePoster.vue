<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getPoster } from '../api/movies'

const posterRequests = new Map()
const props = defineProps({
  movie: { type: Object, required: true },
})

const container = ref(null)
const visible = ref(false)
const fetched = ref('')
const failed = ref(false)
const poster = computed(() => failed.value ? '' : (props.movie.poster || props.movie.poster_url || fetched.value))
let observer

watch(() => props.movie.id ?? props.movie.movie_id, () => {
  fetched.value = ''
  failed.value = false
})

watch([visible, () => props.movie.id ?? props.movie.movie_id], async ([isVisible, id]) => {
  if (!isVisible || !id || !props.movie.tmdb_id || props.movie.poster || props.movie.poster_url) return
  if (!posterRequests.has(id)) posterRequests.set(id, getPoster(id).then(result => result.poster_url || '').catch(() => ''))
  const url = await posterRequests.get(id)
  if ((props.movie.id ?? props.movie.movie_id) === id) fetched.value = url
})

onMounted(() => {
  if (!('IntersectionObserver' in window)) { visible.value = true; return }
  observer = new IntersectionObserver(entries => {
    if (entries[0].isIntersecting) {
      visible.value = true
      observer.disconnect()
    }
  }, { rootMargin: '200px' })
  observer.observe(container.value)
})
onBeforeUnmount(() => observer?.disconnect())

</script>

<template>
  <div ref="container">
    <img v-if="poster" :src="poster" :alt="`Póster de ${movie.title}`" class="movie-poster-image" @error="failed = true" />
    <slot v-else name="fallback" />
    <slot />
  </div>
</template>

<style scoped>
.movie-poster-image { display: block; width: 100%; height: 100%; object-fit: cover; }
</style>
