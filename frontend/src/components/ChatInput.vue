<template>
  <div class="bg-notion-canvas border-t border-notion-hairline p-4">
    <div class="flex items-end gap-3 max-w-3xl mx-auto">
      <div class="flex-1 relative">
        <textarea
          v-model="message"
          placeholder="输入您的问题..."
          class="w-full px-4 py-3 pr-16 bg-notion-canvas-soft border border-notion-hairline rounded-notion-md text-notion-ink text-[15px] outline-none resize-none focus:border-notion-primary focus:shadow-notion-1 placeholder:text-notion-ink-faint transition-all"
          rows="1"
          maxlength="2000"
          @keydown.enter.exact.prevent="sendMessage"
          @input="adjustHeight"
          ref="textareaRef"
        ></textarea>
        
        <span class="absolute right-3 bottom-3 text-[12px] text-notion-ink-faint">
          {{ message.length }}/2000
        </span>
      </div>

      <button
        class="flex-shrink-0 px-4 py-3 bg-notion-primary text-white rounded-notion-md hover:bg-notion-primary-active transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="!message.trim() || isSending || props.disabled"
        @click="sendMessage"
      >
        <svg v-if="!isSending" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
        <svg v-else class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </button>
    </div>

    <div class="flex flex-wrap gap-2 mt-3 max-w-3xl mx-auto">
      <button
        v-for="question in quickQuestions"
        :key="question"
        class="px-3 py-1.5 text-[12px] bg-notion-canvas hover:bg-notion-canvas-soft text-notion-ink-muted hover:text-notion-ink border border-notion-hairline rounded-notion-md transition-colors"
        @click="sendQuickQuestion(question)"
      >
        {{ question }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  disabled: { type: Boolean, default: false },
});
const emit = defineEmits(['send']);

const message = ref('');
const isSending = ref(false);
const textareaRef = ref(null);

const quickQuestions = [
  '图书馆开放时间是什么时候？',
  '如何查询课程表？',
  '校园卡充值在哪里办理？',
  '食堂有哪些特色窗口？',
  '期末考试安排在哪里查看？',
];

const adjustHeight = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto';
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px';
  }
};

const sendMessage = async () => {
  if (!message.value.trim() || isSending.value) return;
  
  isSending.value = true;
  const content = message.value.trim();
  message.value = '';
  
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto';
  }
  
  emit('send', content);
  isSending.value = false;
};

const sendQuickQuestion = (question) => {
  message.value = question;
  sendMessage();
};


</script>
