<template>
  <svg width="13" height="13" viewBox="0 0 24 24">
    <defs>
      <linearGradient id="half" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="50%" stop-color="#F59E0B"/>
        <stop offset="50%" stop-color="#E5E7EB"/>
      </linearGradient>
    </defs>
    <polygon
      :fill="getFill(rating)"
      points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
    />
  </svg>
</template>

<script setup lang="ts">
const props = defineProps<{ rating: number }>()

function getFill(r: number): string {
  if (r >= 1) return '#F59E0B'       // pleine
  if (r === 0.5) return 'url(#half)' // demi
  return '#E5E7EB'                   // vide
}
</script>