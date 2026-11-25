

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },

  readonly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change'
])

const hoveredRating = ref(0)

const currentRating = computed(() =>
  hoveredRating.value || props.modelValue
)

function selectRating(value) {
  if (props.readonly) return

  emit('update:modelValue', value)
  emit('change', value)
}

function previewRating(value) {
  if (!props.readonly) {
    hoveredRating.value = value
  }
}
</script>

<template>
  <div
    class="star-rating"
    :class="{ readonly }"
    :aria-label="`Calificación: ${modelValue} de 5`"
    @mouseleave="hoveredRating = 0"
  >
    <button
      v-for="star in 5"
      :key="star"
      type="button"
      class="star-button"
      :disabled="readonly"
      :aria-label="`Calificar con ${star} estrellas`"
      :aria-pressed="modelValue === star"
      @mouseenter="previewRating(star)"
      @click="selectRating(star)"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true" class="star-icon" :class="{ filled: star <= currentRating }">
        <path d="M12 2.5 14.9 8.4l6.5 1-4.7 4.6 1.1 6.5L12 17.4l-5.8 3.1 1.1-6.5-4.7-4.6 6.5-1L12 2.5Z" />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.star-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  width: max-content;
}

.star-button {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 26px;
  height: 26px;
  flex-shrink: 0;

  padding: 0;
  border: none;
  background: transparent;

  cursor: pointer;
}

.star-icon {
  display: block;
  width: 26px;
  height: 26px;
  fill: transparent;
  stroke: var(--color-text-secondary);
  stroke-width: 1.5;
}
.star-icon.filled {
  fill: var(--color-star);
  stroke: var(--color-star);
}

.star-button:disabled {
  cursor: default;
  opacity: 1;
}

.star-button:focus-visible {
  outline: 1px solid var(--color-brand-primary);
  border-radius: 4px;
}
</style>
