
<script setup>
import MovieCard from './MovieCard.vue'

defineProps({
  title: {
    type: String,
    default: ''
  },

  movies: {
    type: Array,
    default: () => []
  },

  size: {
    type: String,
    default: 'M',
    validator: value => ['S', 'M'].includes(value)
  },

  columns: {
    type: Number,
    default: 5
  },

  showMore: {
    type: Boolean,
    default: false
  },

  savedIds: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'view-more',
  'save'
])

function movieId(movie) {
  return movie.id ?? movie.movie_id
}
</script>

<template>
  <section class="movie-section">
    <div
      v-if="title || showMore"
      class="section-header"
    >
      <h3>{{ title }}</h3>

      <button
        v-if="showMore"
        class="view-more"
        type="button"
        @click="emit('view-more')"
      >
        Ver más
        <span aria-hidden="true">›</span>
      </button>
    </div>

    <div
      v-if="movies.length"
      class="movie-grid"
      :style="{
        '--columns': columns,
        '--card-width': size === 'S'
          ? '176px'
          : '208px'
      }"
    >
      <MovieCard
        v-for="movie in movies"
        :key="movieId(movie)"
        :movie="movie"
        :size="size"
        :saved="savedIds.includes(movieId(movie))"
        @save="emit('save', $event)"
      />
    </div>

    <p
      v-else
      class="empty-state"
    >
      No se encontraron películas.
    </p>
  </section>
</template>

<style scoped>
.movie-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  min-width: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.section-header h3 {
  margin: 0;

  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 26px;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 6px;

  padding: 0;
  border: none;
  background: transparent;

  color: var(--color-brand-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  white-space: nowrap;
}

.view-more span {
  font-size: 24px;
  line-height: 20px;
}

.movie-grid {
  display: grid;

  grid-template-columns:
    repeat(var(--columns), minmax(0, var(--card-width)));

  gap: 28px 18px;
  justify-content: space-between;
}

.empty-state {
  padding: 32px 0;
  text-align: center;
  color: var(--color-text-secondary);
}

@media (max-width: 1200px) {
  .movie-grid {
    grid-template-columns:
      repeat(auto-fill, minmax(min(100%, var(--card-width)), 1fr));
  }
}

@media (max-width: 600px) {
  .movie-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px 12px;
  }
}
</style>
