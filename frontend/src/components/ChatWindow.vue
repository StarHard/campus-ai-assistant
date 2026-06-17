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
          :key="s.sessionId || s.session_id"
          @click="switchSession(s.sessionId || s.session_id)"
          class="px-3 py-1.5 rounded-notion-sm cursor-pointer transition-colors group flex items-center justify-between"
          :class="(s.sessionId || s.session_id) === sessionId ? 'bg-notion-canvas-soft text-notion-ink' : 'text-notion-ink-muted hover:bg-notion-canvas-soft'"
        >
          <span class="text-[13px] truncate flex-1">{{ s.title || '新会话' }}</span>
          <button
            @click.stop="deleteSession(s.sessionId || s.session_id)"
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
            :suggestions="msg.suggestions || []"
            :links="msg.links || []"
            :fjut-search-links="msg.fjutSearchLinks || []"
            :videos="msg.videos || []"
            @select-suggestion="handleSuggestionClick"
          />
        </div>
      </main>

      <ChatInput @send="handleSend" :disabled="isSending" />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue';
import MessageBubble from './MessageBubble.vue';
import ChatInput from './ChatInput.vue';
import { chatApi, ragApi } from '../api/chat';
import { generateSuggestions } from '../utils/suggestionEngine';
import { searchBilibili, detectCourseIntent, bilibiliSearchUrl, getCourseVideos } from '../utils/bilibiliService';

const messages = ref([]);
const messagesContainer = ref(null);
const sessionId = ref(null);  // 后端管理sessionId
const streamMode = ref(true);
const isSending = ref(false);
const sessionList = ref([]);
const ragStats = ref({ documentCount: 0, status: 'inactive' });
const loadingSessions = ref(false);

const welcomeQuestions = [
  '图书馆开放时间',
  '课程表查询',
  '校园卡充值',
  '食堂特色',
  '考试安排',
];

// ===== 会话管理（后端持久化） =====

// 从后端加载会话列表
const loadSessionList = async () => {
  loadingSessions.value = true;
  try {
    const res = await chatApi.getSessions();
    if (res.code === 200 && res.data) {
      sessionList.value = res.data;
    }
  } catch {
    console.warn('加载会话列表失败');
    ElMessage.warning('加载会话列表失败，请检查后端服务');
  } finally {
    loadingSessions.value = false;
  }
};

// 从后端加载指定会话的历史消息
const loadHistory = async (sid) => {
  if (!sid) return;
  try {
    const res = await chatApi.getSessionHistory(sid);
    if (res.code === 200 && res.data) {
      messages.value = res.data.map(msg => ({
        content: msg.content || '',
        isUser: msg.role === 'user',
        timestamp: msg.timestamp ? new Date(msg.timestamp).getTime() : Date.now(),
        isTyping: false,
        sources: msg.sources || [],
      }));
    } else {
      messages.value = [];
    }
  } catch {
    messages.value = [];
  }
  await scrollToBottom();
};

// 切换会话
const switchSession = async (sid) => {
  if (sid === sessionId.value) return;
  sessionId.value = sid;
  messages.value = [];
  await loadHistory(sid);
};

// 删除会话
const deleteSession = async (sid) => {
  try {
    await chatApi.clearSession(sid);
  } catch {
    console.warn('删除会话失败');
  }
  sessionList.value = sessionList.value.filter(s => {
    return s.sessionId !== sid && s.session_id !== sid;
  });
  if (sid === sessionId.value) {
    await startNewSession();
  }
};

// 创建新会话
const startNewSession = async () => {
  messages.value = [];
  try {
    const res = await chatApi.createSession('新会话');
    if (res.code === 200 && res.data) {
      const newId = typeof res.data === 'string' ? res.data : res.data.sessionId;
      sessionId.value = newId;
      // 同步刷新列表
      await loadSessionList();
    } else {
      sessionId.value = null;
    }
  } catch {
    console.warn('创建会话失败，使用本地模式');
    sessionId.value = 'session-' + Date.now();
  }
};

// 更新会话列表中的标题（对话后标题可能变化）
const refreshSessionTitle = async () => {
  try {
    const res = await chatApi.getSessions();
    if (res.code === 200 && res.data) {
      sessionList.value = res.data;
    }
  } catch { /* ignore */ }
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

  if (streamMode.value) {
    await handleStreamAsk(question, typingIndex);
  } else {
    await handleSyncAsk(question, typingIndex);
  }

  // 刷新会话列表（标题可能更新）
  refreshSessionTitle();
};

const handleSyncAsk = async (question, typingIndex) => {
  isSending.value = true;
  // 如果没有会话，先创建一个
  if (!sessionId.value) {
    await startNewSession();
  }
  try {
    const response = await chatApi.ask(question, sessionId.value);
    if (response && response.code === 200 && response.data) {
      const data = response.data;
      // 使用后端返回的sessionId（确保一致）
      if (data.sessionId && data.sessionId !== sessionId.value) {
        sessionId.value = data.sessionId;
        refreshSessionTitle();
      }
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
  isSending.value = false;
  // 生成追问建议
  _generateSuggestions(typingIndex, question);
  await scrollToBottom();
};

const handleStreamAsk = async (question, typingIndex) => {
  isSending.value = true;
  let fullContent = '';
  let sources = [];

  // 如果没有会话，先创建一个
  if (!sessionId.value) {
    await startNewSession();
  }

  try {
    await chatApi.streamAsk(
      question,
      sessionId.value,
      (data) => {
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
          // 捕获后端返回的sessionId
          if (data.sessionId && data.sessionId !== sessionId.value) {
            sessionId.value = data.sessionId;
            refreshSessionTitle();
          }
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
        messages.value[typingIndex] = {
          content: fullContent || '回复完成',
          isUser: false,
          timestamp: Date.now(),
          isTyping: false,
          sources: sources,
        };
        isSending.value = false;
        // 生成追问建议
        _generateSuggestions(typingIndex, question);
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
        // 生成追问建议
        _generateSuggestions(typingIndex, question);
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
    // 生成追问建议
    _generateSuggestions(typingIndex, question);
    await scrollToBottom();
  }
};

const _generateSuggestions = (typingIndex, question) => {
  const aiMsg = messages.value[typingIndex];
  if (!aiMsg) return;
  const aiAnswer = aiMsg.content || '';
  const result = generateSuggestions(question, aiAnswer);
  aiMsg.suggestions = result.suggestions || [];
  aiMsg.links = result.links || [];
  aiMsg.fjutSearchLinks = result.fjutSearchLinks || [];

  // 精品课程视频（静态库，立刻返回）
  const courseName = detectCourseIntent(question);
  aiMsg.videos = courseName ? getCourseVideos(question) : [];

  // 异步补充B站动态搜索结果
  if (courseName) {
    searchBilibili(question).then(more => {
      if (more.length > (aiMsg.videos || []).length) {
        aiMsg.videos = more;
      }
    }).catch(() => {});
  }
};

const sendQuickQuestion = (question) => {
  handleSend(question);
};

// 处理追问建议点击
const handleSuggestionClick = (text) => {
  handleSend(text);
};

// ===== 初始化 =====
onMounted(async () => {
  await loadRagStats();
  await loadSessionList();
  // 如果已有会话，切换到最新一个；否则创建新会话
  if (sessionList.value.length > 0) {
    const latest = sessionList.value[0];
    const sid = latest.sessionId || latest.session_id;
    sessionId.value = sid;
    await loadHistory(sid);
  } else {
    await startNewSession();
  }
});
</script>
