

<script setup>
import { ref, computed } from 'vue'

import filledStar from '../assets/figma/919fb.svg'
import emptyStar from '../assets/figma/1dc29.svg'

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
      <img
        :src="star <= currentRating
          ? filledStar
          : emptyStar"
        alt=""
        class="star-icon"
      />
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
