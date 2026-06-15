<template>
  <div class="h-full flex flex-col bg-gray-50/50">
    <header class="bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <div>
            <h1 class="text-lg font-semibold text-gray-800">校园智能助手</h1>
            <p class="text-xs text-gray-500">智能问答 · 知识检索</p>
          </div>
        </div>
        
        <div class="flex items-center gap-2">
          <button
            class="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
            @click="startNewSession"
          >
            新会话
          </button>
          <div class="flex items-center gap-2 px-3 py-1.5 bg-green-50 text-green-700 rounded-lg">
            <span class="w-2 h-2 bg-green-500 rounded-full"></span>
            <span class="text-xs">知识库在线</span>
          </div>
        </div>
      </div>
    </header>

    <main ref="messagesContainer" class="flex-1 overflow-y-auto px-4 py-4">
      <div class="max-w-4xl mx-auto space-y-4">
        <div v-if="messages.length === 0" class="text-center py-12">
          <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center">
            <svg class="w-10 h-10 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-gray-800 mb-2">欢迎使用校园智能助手</h2>
          <p class="text-gray-500 mb-6">我可以帮助您查询校园信息、解答问题</p>
          <div class="flex flex-wrap justify-center gap-2">
            <button
              v-for="question in welcomeQuestions"
              :key="question"
              class="px-4 py-2 text-sm bg-white hover:bg-blue-50 text-gray-700 hover:text-blue-600 rounded-full shadow-sm transition-colors"
              @click="sendQuickQuestion(question)"
            >
              {{ question }}
            </button>
          </div>
        </div>

        <MessageBubble
          v-for="(msg, index) in messages"
          :key="index"
          :content="msg.content"
          :is-user="msg.isUser"
          :timestamp="msg.timestamp"
          :is-typing="msg.isTyping"
          :sources="msg.sources"
        />
      </div>
    </main>

    <ChatInput @send="handleSend" />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import MessageBubble from './MessageBubble.vue';
import ChatInput from './ChatInput.vue';
import { chatApi } from '../api/chat';

const messages = ref([]);
const messagesContainer = ref(null);
const sessionId = ref('session-' + Date.now());

const welcomeQuestions = [
  '图书馆开放时间',
  '课程表查询',
  '校园卡充值',
  '食堂特色',
  '考试安排',
];

const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const handleSend = async (question) => {
  messages.value.push({
    content: question,
    isUser: true,
    timestamp: Date.now(),
    isTyping: false,
    sources: [],
  });

  await scrollToBottom();

  const typingIndex = messages.value.push({
    content: '',
    isUser: false,
    timestamp: Date.now(),
    isTyping: true,
    sources: [],
  }) - 1;

  await scrollToBottom();

  try {
    const response = await chatApi.ask(question, sessionId.value);
    
    if (response && response.code === 200 && response.data) {
      const data = response.data;
      messages.value[typingIndex] = {
        content: data.answer || '抱歉，我无法回答这个问题。',
        isUser: false,
        timestamp: Date.now(),
        isTyping: false,
        sources: data.sources || [],
      };
    } else {
      messages.value[typingIndex] = {
        content: response.message || '请求失败，请重试。',
        isUser: false,
        timestamp: Date.now(),
        isTyping: false,
        sources: [],
      };
    }
  } catch (error) {
    console.error('API调用失败:', error);
    messages.value[typingIndex] = {
      content: '网络错误，请检查后端服务是否启动。',
      isUser: false,
      timestamp: Date.now(),
      isTyping: false,
      sources: [],
    };
  }

  await scrollToBottom();
};

const sendQuickQuestion = (question) => {
  handleSend(question);
};

const startNewSession = () => {
  sessionId.value = 'session-' + Date.now();
  messages.value = [];
};
</script>
