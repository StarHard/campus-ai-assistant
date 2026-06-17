<template>
  <div class="mt-2 flex flex-wrap gap-2 animate-fade-in">
    <!-- 追问建议 -->
    <button
      v-for="(s, idx) in suggestions"
      :key="'sug-' + idx"
      @click="handleClick(s)"
      class="group px-3 py-1.5 text-[13px] bg-notion-canvas hover:bg-notion-canvas-soft border border-notion-hairline hover:border-notion-primary/30 rounded-notion-full transition-all flex items-center gap-1.5 text-notion-ink-muted hover:text-notion-ink"
    >
      <span class="text-[11px] opacity-60 group-hover:opacity-100 transition-opacity">
        {{ s.action === 'navigate' ? '↗' : '💡' }}
      </span>
      {{ s.text }}
    </button>

    <!-- 官方链接（单独一行，蓝色） -->
    <a
      v-for="(link, idx) in links"
      :key="'link-' + idx"
      :href="link.url"
      target="_blank"
      rel="noopener noreferrer"
      class="group px-3 py-1.5 text-[13px] bg-notion-accent-blue/5 hover:bg-notion-accent-blue/10 border border-notion-accent-blue/20 hover:border-notion-accent-blue/40 rounded-notion-full transition-all flex items-center gap-1.5 text-notion-accent-blue no-underline"
    >
      <svg class="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
      </svg>
      {{ link.text }}
    </a>

    <!-- 福工官网搜索（绿色，搜索图标） -->
    <a
      v-for="(sl, idx) in fjutSearchLinks"
      :key="'fjut-' + idx"
      :href="sl.url"
      target="_blank"
      rel="noopener noreferrer"
      class="group px-3 py-1.5 text-[13px] bg-notion-accent-green/5 hover:bg-notion-accent-green/10 border border-notion-accent-green/20 hover:border-notion-accent-green/40 rounded-notion-full transition-all flex items-center gap-1.5 text-notion-accent-green no-underline"
    >
      <svg class="w-3.5 h-3.5 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
      </svg>
      {{ sl.text }}
    </a>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  suggestions: {
    type: Array,
    default: () => [],
  },
  links: {
    type: Array,
    default: () => [],
  },
  fjutSearchLinks: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['select']);
const router = useRouter();

const handleClick = (suggestion) => {
  if (suggestion.action === 'navigate' && suggestion.target) {
    router.push(suggestion.target);
  } else {
    emit('select', suggestion.text);
  }
};
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-in;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
