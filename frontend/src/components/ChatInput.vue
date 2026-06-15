<template>
  <div class="bg-white border-t border-gray-100 px-4 py-3">
    <div class="flex items-end gap-3 max-w-4xl mx-auto">
      <button 
        class="flex-shrink-0 w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
        @click="toggleVoice"
      >
        <svg v-if="!isRecording" class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        <svg v-else class="w-5 h-5 text-red-500 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
        </svg>
      </button>

      <div class="flex-1 relative">
        <textarea
          v-model="message"
          placeholder="输入您的问题..."
          class="w-full px-4 py-3 pr-12 bg-gray-50 rounded-2xl border-none outline-none resize-none focus:ring-2 focus:ring-blue-500/20 transition-all"
          rows="1"
          maxlength="2000"
          @keydown.enter.exact.prevent="sendMessage"
          @input="adjustHeight"
          ref="textareaRef"
        ></textarea>
        
        <span class="absolute right-3 bottom-3 text-xs text-gray-400">
          {{ message.length }}/2000
        </span>
      </div>

      <button
        class="flex-shrink-0 w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white flex items-center justify-center shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="!message.trim() || isSending"
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

    <div class="flex flex-wrap gap-2 mt-3 max-w-4xl mx-auto">
      <button
        v-for="question in quickQuestions"
        :key="question"
        class="px-3 py-1.5 text-xs bg-gray-100 hover:bg-blue-100 text-gray-600 hover:text-blue-600 rounded-full transition-colors"
        @click="sendQuickQuestion(question)"
      >
        {{ question }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['send']);

const message = ref('');
const isSending = ref(false);
const isRecording = ref(false);
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

const toggleVoice = () => {
  isRecording.value = !isRecording.value;
};
</script>
