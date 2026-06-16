<template>
  <div 
    class="flex message-bubble" 
    :class="[
      isUser ? 'justify-end' : 'justify-start',
      isTyping ? 'typing-indicator' : ''
    ]"
  >
    <div 
      class="max-w-[70%] px-4 py-3 rounded-2xl shadow-lg"
      :class="[
        isUser 
          ? 'bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-br-md' 
          : 'bg-white text-gray-800 rounded-bl-md'
      ]"
    >
      <div v-if="!isUser && sources && sources.length > 0" class="mb-2">
        <span class="inline-block px-2 py-0.5 text-xs rounded-full bg-emerald-100 text-emerald-700">
          📚 基于知识库
        </span>
      </div>

      <div class="text-sm leading-relaxed whitespace-pre-wrap">
        {{ content }}<span v-if="isTyping" class="inline-block w-2 h-4 ml-0.5 bg-gray-400 align-middle animate-pulse"></span>
      </div>

      <div 
        class="mt-1 text-xs opacity-60"
        :class="isUser ? 'text-blue-200' : 'text-gray-400'"
      >
        {{ formatTime(timestamp) }}
      </div>
    </div>
  </div>
</template>

<script setup>
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
});

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
