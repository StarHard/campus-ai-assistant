<template>
  <div v-if="videos.length > 0" class="mt-3 animate-fade-in">
    <div class="flex items-center gap-1.5 mb-2">
      <svg class="w-4 h-4 text-notion-accent-pink" viewBox="0 0 24 24" fill="#FB7299"><path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.854l-.02-.14c-.036-.506.16-.827.587-.96.426-.134.845.058 1.253.587L10.88 7.2h2.24l2.907-3.073c.409-.523.827-.712 1.254-.58.427.133.618.454.587.96z"/></svg>
      <span class="text-[13px] font-semibold text-notion-ink">B站推荐视频</span>
      <span class="text-[11px] text-notion-ink-faint ml-1">共{{ videos.length }}个</span>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
      <a
        v-for="(v, idx) in videos"
        :key="idx"
        :href="v.url"
        target="_blank"
        rel="noopener noreferrer"
        class="flex items-center gap-3 p-2.5 rounded-notion-md border border-notion-hairline hover:border-notion-accent-pink/30 bg-notion-canvas hover:bg-notion-accent-pink/[0.03] transition-all group no-underline"
      >
        <!-- B站logo占位 -->
        <div class="w-12 h-12 rounded-notion-sm shrink-0 flex items-center justify-center bg-notion-accent-pink/10">
          <svg class="w-6 h-6 text-notion-accent-pink" viewBox="0 0 24 24" fill="#FB7299">
            <path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.854l-.02-.14c-.036-.506.16-.827.587-.96.426-.134.845.058 1.253.587L10.88 7.2h2.24l2.907-3.073c.409-.523.827-.712 1.254-.58.427.133.618.454.587.96z"/>
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-[13px] font-medium text-notion-ink group-hover:text-notion-accent-pink transition-colors line-clamp-2 leading-snug">
            {{ v.title }}
          </div>
          <div class="flex items-center gap-2 mt-1">
            <span class="text-[11px] text-notion-ink-muted">{{ v.author }}</span>
            <span class="text-[11px] text-notion-ink-faint">·</span>
            <span class="text-[11px] text-notion-ink-faint">{{ v.views }}</span>
          </div>
        </div>
        <svg class="w-3.5 h-3.5 text-notion-ink-faint group-hover:text-notion-accent-pink shrink-0 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
        </svg>
      </a>
    </div>
  </div>
</template>

<script setup>
defineProps({
  videos: {
    type: Array,
    default: () => [],
  },
});
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-in;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
