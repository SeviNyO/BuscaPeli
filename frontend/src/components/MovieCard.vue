
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import glowS from '../assets/figma/da223.svg'
import orbS from '../assets/figma/97736.svg'
import ridgeS from '../assets/figma/0e972.svg'

import glowM from '../assets/figma/fdfb1.svg'
import orbM from '../assets/figma/20eb5.svg'
import ridgeM from '../assets/figma/04982.svg'

import bookmarkIcon from '../assets/figma/1302e.svg'
import starIcon from '../assets/figma/b13a4.svg'

const props = defineProps({
  movie: {
    type: Object,
    required: true
  },

  size: {
    type: String,
    default: 'S',
    validator: value => ['S', 'M'].includes(value)
  },

  saved: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save'])

const router = useRouter()

const assets = computed(() => {
  return props.size === 'M'
    ? {
        glow: glowM,
        orb: orbM,
        ridge: ridgeM
      }
    : {
        glow: glowS,
        orb: orbS,
        ridge: ridgeS
      }
})

const movieTitle = computed(() =>
  props.movie.title || 'Sin título'
)

const posterTitle = computed(() =>
  props.movie.posterTitle || movieTitle.value
)

const genre = computed(() => {
  if (Array.isArray(props.movie.genres)) {
    return props.movie.genres[0] || ''
  }

  return props.movie.genre ||
    props.movie.genres || ''
})

const rating = computed(() => {
  const value = props.movie.rating ??
    props.movie.average_rating

  return value != null && Number.isFinite(Number(value))
    ? Number(value).toFixed(1)
    : '—'
})

const posterStyle = computed(() => ({
  backgroundImage: props.movie.gradient ||
    'linear-gradient(135.855deg, #2a4a6b 14.286%, #0b1626 85.714%)'
}))

function openMovie() {
  const id = props.movie.id ?? props.movie.movie_id

  if (id != null) {
    router.push(`/app/movies/${id}`)
  }
}
</script>

<template>
  <article
    class="movie-card"
    :class="size === 'M' ? 'medium' : 'small'"
  >
    <div
      class="poster"
      :style="posterStyle"
    >
      <!-- Póster real cuando se conecte TMDB -->
      <img
        v-if="movie.poster"
        :src="movie.poster"
        :alt="movieTitle"
        class="real-poster"
      />

      <!-- Ilustración original del componente Figma -->
      <template v-else>
        <div class="glow">
          <img :src="assets.glow" alt="" />
        </div>

        <img
          :src="assets.orb"
          alt=""
          class="orb"
        />

        <img
          :src="assets.ridge"
          alt=""
          class="ridge"
        />

        <div class="scrim"></div>

        <span class="poster-title">
          {{ posterTitle }}
        </span>
      </template>

      <button
        class="poster-link"
        type="button"
        :aria-label="`Ver detalles de ${movieTitle}`"
        @click="openMovie"
      ></button>

      <button
        class="save-button"
        :class="{ saved }"
        type="button"
        :aria-label="saved
          ? 'Quitar de mi lista'
          : 'Agregar a mi lista'"
        :aria-pressed="saved"
        @click.stop="emit('save', movie)"
      >
        <img :src="bookmarkIcon" alt="" />
      </button>
    </div>

    <button
      class="movie-title"
      type="button"
      :title="movieTitle"
      @click="openMovie"
    >
      {{ movieTitle }}
    </button>

    <div class="metadata">
      <img
        :src="starIcon"
        alt=""
        class="star-icon"
      />

      <span class="rating">
        {{ rating }}
      </span>

      <span class="year-genre">
        {{ movie.year }} · {{ genre }}
      </span>
    </div>
  </article>
</template>

<style scoped>
.movie-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;

  width: var(--card-width);
  max-width: 100%;
  min-width: 0;
}

.small {
  --card-width: 176px;
  --poster-radius: 12px;
  --poster-font: 14px;
  --title-top: 86.74%;
}

.medium {
  --card-width: 208px;
  --poster-radius: 15px;
  --poster-font: 17px;
  --title-top: 87.5%;
}

.poster {
  width: 100%;
  aspect-ratio: 2 / 3;
  position: relative;
  overflow: hidden;

  border-radius: var(--poster-radius);
}

.real-poster {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.glow {
  position: absolute;
  left: 2%;
  top: 10%;

  width: 95%;
  aspect-ratio: 1;
}

.glow img {
  position: absolute;
  display: block;
  inset: -21.05%;
  width: 142.1%;
  height: 142.1%;
}

.orb {
  position: absolute;
  left: 33%;
  top: 20%;

  width: 34%;
  aspect-ratio: 1;
}

.ridge {
  position: absolute;
  top: 64%;
  left: 0;

  width: 100%;
  height: 36%;
}

.scrim {
  position: absolute;
  bottom: 0;
  left: 0;

  width: 100%;
  height: 50%;

  background: linear-gradient(
    to bottom,
    transparent,
    rgba(0, 0, 0, 0.85)
  );
}

.poster-title {
  position: absolute;
  top: var(--title-top);
  left: 12px;
  right: 12px;

  text-align: center;
  color: rgba(255, 255, 255, 0.95);

  font-size: var(--poster-font);
  font-weight: 800;
  letter-spacing: 1.2px;
  line-height: normal;
  text-transform: uppercase;
}

.poster-link {
  position: absolute;
  inset: 0;
  z-index: 1;

  width: 100%;
  height: 100%;

  background: transparent;
  border: none;
}

.save-button {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 32px;
  height: 32px;

  background: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 16px;
}

.save-button img {
  width: 16px;
  height: 16px;
}

.save-button.saved,
.save-button:hover {
  background: var(--color-brand-primary);
}

.movie-title {
  display: block;
  width: 100%;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  background: transparent;
  border: none;
  padding: 0;

  text-align: left;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.movie-title:hover {
  text-decoration: underline;
}

.metadata {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
}

.star-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.rating {
  color: var(--color-text-primary);
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
}

.year-genre {
  color: var(--color-text-secondary);
  font-size: 12px;
  line-height: 18px;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

