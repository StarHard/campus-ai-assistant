<template>
  <div 
    class="flex message-bubble" 
    :class="[
      isUser ? 'justify-end' : 'justify-start',
      isTyping ? 'typing-indicator' : ''
    ]"
  >
    <div 
      class="max-w-[80%] px-4 py-3 rounded-notion-lg"
      :class="[
        isUser 
          ? 'bg-notion-primary/8 text-notion-ink' 
          : 'bg-notion-canvas border border-notion-hairline shadow-notion-1 text-notion-ink'
      ]"
    >
      <div v-if="!isUser && sources && sources.length > 0" class="mb-2">
        <span class="inline-block px-2 py-0.5 text-[12px] rounded-notion-full bg-notion-primary/5 text-notion-primary">
          📚 基于知识库
        </span>
      </div>

      <div class="text-[15px] leading-relaxed whitespace-pre-wrap">
        {{ content }}<span v-if="isTyping" class="inline-block w-2 h-4 ml-0.5 bg-notion-ink-faint align-middle animate-pulse"></span>
      </div>

      <SuggestionCards
        v-if="!isUser && !isTyping && (suggestions?.length || links?.length || fjutSearchLinks?.length)"
        :suggestions="suggestions || []"
        :links="links || []"
        :fjut-search-links="fjutSearchLinks || []"
        @select="suggestionText => $emit('select-suggestion', suggestionText)"
      />

      <div 
        class="mt-1 text-[12px] text-notion-ink-faint"
      >
        {{ formatTime(timestamp) }}
      </div>
    </div>
  </div>
</template>

<script setup>
import SuggestionCards from './SuggestionCards.vue';

defineProps({
  content: {
    type: String,
    default: '',
  },
  isUser: {
    type: Boolean,
    default: false,
  },
  timestamp: {
    type: Number,
    default: Date.now,
  },
  isTyping: {
    type: Boolean,
    default: false,
  },
  sources: {
    type: Array,
    default: () => [],
  },
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

defineEmits(['select-suggestion']);

const formatTime = (time) => {
  const date = new Date(time);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};
</script>

<style scoped>
.message-bubble {
  margin-bottom: 12px;
}

.typing-indicator {
  justify-content: flex-start !important;
}
</style>
