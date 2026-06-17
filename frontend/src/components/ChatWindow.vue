<template>
  <div class="h-full flex">
    <!-- 左侧会话列表 - 白色背景 -->
    <div class="w-60 bg-notion-canvas border-r border-notion-hairline flex flex-col shrink-0">
      <!-- 新会话按钮 -->
      <div class="p-3">
        <button
          @click="startNewSession"
          class="w-full px-3 py-2 text-[14px] text-notion-ink-secondary hover:bg-notion-canvas-soft rounded-notion-sm transition-colors flex items-center gap-2"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          新会话
        </button>
      </div>
      <!-- 会话列表 -->
      <div class="flex-1 overflow-y-auto px-2 space-y-0.5">
        <div
          v-for="s in sessionList"
          :key="s.id"
          @click="switchSession(s.id)"
          class="px-3 py-1.5 rounded-notion-sm cursor-pointer transition-colors group flex items-center justify-between"
          :class="s.id === sessionId ? 'bg-notion-canvas-soft text-notion-ink' : 'text-notion-ink-muted hover:bg-notion-canvas-soft'"
        >
          <span class="text-[13px] truncate flex-1">{{ s.title }}</span>
          <button
            @click.stop="deleteSession(s.id)"
            class="opacity-0 group-hover:opacity-100 text-notion-ink-faint hover:text-notion-ink-muted ml-1 text-xs"
          >
            ×
          </button>
        </div>
      </div>
      <!-- 知识库统计 -->
      <div class="p-3 border-t border-notion-hairline">
        <div class="text-[12px] text-notion-ink-faint mb-1">知识库状态</div>
        <div class="flex items-center gap-2">
          <span class="w-1.5 h-1.5 rounded-full" :class="ragStats.status === 'active' ? 'bg-notion-accent-green' : 'bg-notion-ink-faint'"></span>
          <span class="text-[12px] text-notion-ink-muted">{{ ragStats.documentCount }} 篇文档</span>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区 -->
    <div class="flex-1 flex flex-col min-w-0 bg-notion-canvas-soft">
      <!-- 顶部栏 -->
      <header class="bg-notion-canvas border-b border-notion-hairline px-6 py-2.5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <h1 class="text-[15px] font-semibold text-notion-ink">校园智能助手</h1>
            <span class="text-[12px] text-notion-ink-faint">智能问答 · 知识检索</span>
          </div>
          <div class="flex items-center gap-2">
            <button
              @click="streamMode = !streamMode"
              class="px-2.5 py-1 text-[12px] rounded-notion-full transition-colors border"
              :class="streamMode ? 'bg-notion-primary/5 text-notion-primary border-notion-primary/20' : 'bg-notion-canvas text-notion-ink-muted border-notion-hairline hover:border-notion-ink-faint'"
            >
              {{ streamMode ? '流式输出' : '同步输出' }}
            </button>
          </div>
        </div>
      </header>

      <!-- 消息区 -->
      <main ref="messagesContainer" class="flex-1 overflow-y-auto px-6 py-6">
        <div class="max-w-3xl mx-auto space-y-4">
          <!-- 空状态 -->
          <div v-if="messages.length === 0" class="text-center py-16">
            <div class="w-12 h-12 mx-auto mb-4 rounded-notion-lg bg-notion-canvas-soft flex items-center justify-center">
              <svg class="w-6 h-6 text-notion-ink-faint" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 class="text-xl font-bold text-notion-ink mb-2">欢迎使用校园智能助手</h2>
            <p class="text-notion-ink-muted mb-8 text-[15px]">我可以帮助您查询校园信息、解答问题</p>
            <div class="flex flex-wrap justify-center gap-2">
              <button
                v-for="question in welcomeQuestions"
                :key="question"
                class="px-3 py-1.5 text-[13px] bg-notion-canvas hover:bg-notion-canvas-soft text-notion-ink-muted hover:text-notion-ink border border-notion-hairline rounded-notion-md transition-colors"
                @click="sendQuickQuestion(question)"
              >
                {{ question }}
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
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

      <ChatInput @send="handleSend" :disabled="isSending" />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue';
import MessageBubble from './MessageBubble.vue';
import ChatInput from './ChatInput.vue';
import { chatApi, ragApi } from '../api/chat';

const messages = ref([]);
const messagesContainer = ref(null);
const sessionId = ref('session-' + Date.now());
const streamMode = ref(true);
const isSending = ref(false);
const sessionList = ref([]);
const ragStats = ref({ documentCount: 0, status: 'inactive' });

const welcomeQuestions = [
  '图书馆开放时间',
  '课程表查询',
  '校园卡充值',
  '食堂特色',
  '考试安排',
];

// ===== 会话历史管理 =====
const SESSIONS_KEY = 'chat_sessions';

const loadSessionList = () => {
  try {
    const saved = localStorage.getItem(SESSIONS_KEY);
    sessionList.value = saved ? JSON.parse(saved) : [];
  } catch {
    sessionList.value = [];
  }
};

const saveSessionList = () => {
  localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessionList.value));
};

const saveCurrentSession = () => {
  if (messages.value.length === 0) return;
  const firstUserMsg = messages.value.find(m => m.isUser);
  const title = firstUserMsg ? firstUserMsg.content.slice(0, 20) : '新会话';
  const idx = sessionList.value.findIndex(s => s.id === sessionId.value);
  if (idx >= 0) {
    sessionList.value[idx].title = title;
  } else {
    sessionList.value.unshift({ id: sessionId.value, title });
  }
  saveSessionList();
};

const saveMessages = () => {
  localStorage.setItem('chat_messages_' + sessionId.value, JSON.stringify(messages.value));
};

const loadMessages = (sid) => {
  try {
    const saved = localStorage.getItem('chat_messages_' + sid);
    messages.value = saved ? JSON.parse(saved) : [];
  } catch {
    messages.value = [];
  }
};

const switchSession = (sid) => {
  saveCurrentSession();
  saveMessages();
  sessionId.value = sid;
  loadMessages(sid);
  scrollToBottom();
};

const deleteSession = (sid) => {
  sessionList.value = sessionList.value.filter(s => s.id !== sid);
  localStorage.removeItem('chat_messages_' + sid);
  saveSessionList();
  if (sid === sessionId.value) {
    startNewSession();
  }
};

const startNewSession = () => {
  saveCurrentSession();
  saveMessages();
  sessionId.value = 'session-' + Date.now();
  messages.value = [];
};

// ===== 知识库统计 =====
const loadRagStats = async () => {
  try {
    const res = await ragApi.getStats();
    if (res.code === 200 && res.data) {
      ragStats.value = res.data;
    }
  } catch {
    ragStats.value = { documentCount: 0, status: 'inactive' };
  }
};

// ===== 发送消息 =====
const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const handleSend = async (question) => {
  if (isSending.value) return;

  messages.value.push({
    content: question,
    isUser: true,
    timestamp: Date.now(),
    isTyping: false,
    sources: [],
  });

  const typingIndex = messages.value.push({
    content: '',
    isUser: false,
    timestamp: Date.now(),
    isTyping: true,
    sources: [],
  }) - 1;

  await scrollToBottom();
  saveCurrentSession();
  saveMessages();

  if (streamMode.value) {
    await handleStreamAsk(question, typingIndex);
  } else {
    await handleSyncAsk(question, typingIndex);
  }
};

const handleSyncAsk = async (question, typingIndex) => {
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
        content: response?.message || '请求失败，请重试。',
        isUser: false,
        timestamp: Date.now(),
        isTyping: false,
        sources: [],
      };
    }
  } catch (error) {
    messages.value[typingIndex] = {
      content: '网络错误，请检查后端服务是否启动。',
      isUser: false,
      timestamp: Date.now(),
      isTyping: false,
      sources: [],
    };
  }
  saveMessages();
  await scrollToBottom();
};

const handleStreamAsk = async (question, typingIndex) => {
  isSending.value = true;
  let fullContent = '';
  let sources = [];

  try {
    await chatApi.streamAsk(
      question,
      sessionId.value,
      (data) => {
        // 收到流式数据片段
        if (data.type === 'message' && data.content) {
          fullContent += data.content;
          messages.value[typingIndex] = {
            content: fullContent,
            isUser: false,
            timestamp: Date.now(),
            isTyping: true,
            sources: [],
          };
          scrollToBottom();
        } else if (data.type === 'done') {
          messages.value[typingIndex] = {
            content: fullContent || '回复完成',
            isUser: false,
            timestamp: Date.now(),
            isTyping: false,
            sources: sources,
          };
        } else if (data.sources) {
          sources = data.sources;
        } else if (data.answer) {
          fullContent = data.answer;
          messages.value[typingIndex] = {
            content: fullContent,
            isUser: false,
            timestamp: Date.now(),
            isTyping: false,
            sources: data.sources || sources,
          };
        }
      },
      () => {
        // 流结束
        messages.value[typingIndex] = {
          content: fullContent || '回复完成',
          isUser: false,
          timestamp: Date.now(),
          isTyping: false,
          sources: sources,
        };
        isSending.value = false;
        saveMessages();
        scrollToBottom();
      },
      (error) => {
        console.error('SSE错误:', error);
        messages.value[typingIndex] = {
          content: fullContent || '流式请求失败，请切换同步模式重试。',
          isUser: false,
          timestamp: Date.now(),
          isTyping: false,
          sources: [],
        };
        isSending.value = false;
        saveMessages();
        scrollToBottom();
      }
    );
  } catch (error) {
    messages.value[typingIndex] = {
      content: '流式请求失败，请切换同步模式重试。',
      isUser: false,
      timestamp: Date.now(),
      isTyping: false,
      sources: [],
    };
    isSending.value = false;
    saveMessages();
    await scrollToBottom();
  }
};

const sendQuickQuestion = (question) => {
  handleSend(question);
};

// ===== 初始化 =====
onMounted(() => {
  loadSessionList();
  loadMessages(sessionId.value);
  loadRagStats();
  // 把当前会话加入列表
  if (messages.value.length > 0) {
    const firstUserMsg = messages.value.find(m => m.isUser);
    const title = firstUserMsg ? firstUserMsg.content.slice(0, 20) : '新会话';
    if (!sessionList.value.find(s => s.id === sessionId.value)) {
      sessionList.value.unshift({ id: sessionId.value, title });
      saveSessionList();
    }
  }
});
</script>
