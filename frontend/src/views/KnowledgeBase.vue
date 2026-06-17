<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-[28px] font-bold text-notion-ink mb-8">知识库管理</h1>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">文档总数</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ stats.documentCount ?? '-' }}</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">总块数</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ stats.totalChunks ?? '-' }}</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">知识库大小</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ formatSize(stats.kbSize) }}</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">最后更新</div>
        <div class="text-[14px] font-semibold text-notion-ink leading-tight mt-2">{{ formatTime(stats.lastUpdated) }}</div>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="grid grid-cols-2 gap-6 mb-6">
      <!-- 上传文档 -->
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
        <h2 class="text-[18px] font-semibold text-notion-ink mb-4">上传文档</h2>
        <div
          class="border-2 border-dashed border-notion-hairline rounded-notion-md p-6 text-center hover:border-notion-primary/50 transition-colors cursor-pointer"
          @dragover.prevent @drop.prevent="handleDrop"
          @click="$refs.fileInput.click()"
        >
          <input ref="fileInput" type="file" accept=".txt,.md,.pdf,.doc,.docx" class="hidden" @change="handleFileSelect" />
          <svg class="w-10 h-10 mx-auto mb-3 text-notion-ink-faint" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
          </svg>
          <p class="text-[14px] text-notion-ink-muted">点击或拖拽文件到此处上传</p>
          <p class="text-[12px] text-notion-ink-faint mt-1">支持 .txt .md .pdf .doc .docx</p>
        </div>
        <div v-if="uploadingFile" class="mt-3 flex items-center justify-between text-[13px]">
          <span class="text-notion-ink-secondary truncate max-w-[200px]">{{ uploadingFile.name }}</span>
          <span class="text-notion-primary">上传中...</span>
        </div>
        <div v-if="uploadMsg" :class="uploadSuccess ? 'text-green-600' : 'text-red-500'" class="mt-2 text-[13px]">
          {{ uploadMsg }}
        </div>
      </div>

      <!-- 批量操作 -->
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
        <h2 class="text-[18px] font-semibold text-notion-ink mb-4">知识库操作</h2>
        <div class="space-y-3">
          <button
            @click="batchLoad"
            :disabled="loading"
            class="w-full py-2.5 bg-notion-canvas border border-notion-hairline text-[14px] font-medium text-notion-ink rounded-notion-sm hover:bg-notion-canvas-soft transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
            批量加载知识库目录
          </button>
          <button
            @click="clearKB"
            :disabled="loading"
            class="w-full py-2.5 bg-red-50 border border-red-200 text-[14px] font-medium text-red-600 rounded-notion-sm hover:bg-red-100 transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            清空知识库
          </button>
          <p class="text-[12px] text-notion-ink-faint">批量加载路径：./data/knowledge-base</p>
        </div>
        <div v-if="actionMsg" :class="actionSuccess ? 'text-green-600' : 'text-red-500'" class="mt-3 text-[13px]">
          {{ actionMsg }}
        </div>
      </div>
    </div>

    <!-- 检索测试 -->
    <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
      <h2 class="text-[18px] font-semibold text-notion-ink mb-4">检索测试</h2>
      <form @submit.prevent="testRetrieval" class="flex gap-3">
        <input
          v-model="retrievalQuery"
          type="text"
          placeholder="输入查询内容，测试知识库检索效果..."
          class="flex-1 px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary placeholder:text-notion-ink-faint"
        />
        <button
          type="submit"
          :disabled="loading || !retrievalQuery"
          class="px-6 py-2 bg-notion-primary text-white text-[14px] font-medium rounded-notion-full hover:bg-notion-primary-active transition-colors disabled:opacity-60"
        >
          检索
        </button>
      </form>

      <!-- 检索结果 -->
      <div v-if="retrievalResults.length > 0" class="mt-4 space-y-3">
        <div class="text-[13px] text-notion-ink-faint">检索到 {{ retrievalResults.length }} 条相关结果：</div>
        <div
          v-for="(item, idx) in retrievalResults"
          :key="idx"
          class="p-4 bg-notion-canvas-soft rounded-notion-md border border-notion-hairline"
        >
          <div class="flex items-start justify-between mb-2">
            <span class="inline-block px-2 py-0.5 text-[11px] font-medium rounded-notion-full bg-notion-accent-sky/10 text-notion-accent-sky">
              相关度 {{ item.score != null ? (item.score * 100).toFixed(1) + '%' : '-' }}
            </span>
            <span class="text-[12px] text-notion-ink-faint">#{{ idx + 1 }}</span>
          </div>
          <p class="text-[14px] text-notion-ink-secondary leading-relaxed whitespace-pre-wrap break-all">{{ item.content || item.text || JSON.stringify(item) }}</p>
        </div>
      </div>
      <div v-else-if="retrievalTested && !loading" class="mt-4 text-[14px] text-notion-ink-faint text-center py-4">
        未检索到相关结果
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ragApi } from '../api/chat';

const loading = ref(false);
const stats = ref({});
const uploadingFile = ref(null);
const uploadMsg = ref('');
const uploadSuccess = ref(false);
const actionMsg = ref('');
const actionSuccess = ref(false);
const retrievalQuery = ref('');
const retrievalResults = ref([]);
const retrievalTested = ref(false);

const formatSize = (size) => {
  if (size == null) return '-';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
  return (size / 1024 / 1024).toFixed(1) + ' MB';
};

const formatTime = (t) => {
  if (!t) return '-';
  return String(t).replace('T', ' ').substring(0, 19);
};

// 加载统计
const loadStats = async () => {
  try {
    const res = await ragApi.getStats();
    if (res.code === 200 && res.data) {
      stats.value = res.data;
    }
  } catch {}
};

// 文件上传
const handleFileSelect = (e) => {
  const file = e.target.files?.[0];
  if (file) uploadFile(file);
};

const handleDrop = (e) => {
  const file = e.dataTransfer?.files?.[0];
  if (file) uploadFile(file);
};

const uploadFile = async (file) => {
  uploadingFile.value = file;
  uploadMsg.value = '';
  loading.value = true;
  try {
    const res = await ragApi.uploadDocument(file);
    if (res.code === 200) {
      uploadMsg.value = `上传成功：${file.name}`;
      uploadSuccess.value = true;
      loadStats();
    } else {
      uploadMsg.value = res.message || '上传失败';
      uploadSuccess.value = false;
    }
  } catch (e) {
    uploadMsg.value = '网络错误，请检查后端服务';
    uploadSuccess.value = false;
  } finally {
    uploadingFile.value = null;
    loading.value = false;
  }
};

// 批量加载
const batchLoad = async () => {
  actionMsg.value = '';
  loading.value = true;
  try {
    const res = await ragApi.batchLoadDocuments();
    if (res.code === 200) {
      actionMsg.value = '批量加载完成';
      actionSuccess.value = true;
      loadStats();
    } else {
      actionMsg.value = res.message || '批量加载失败';
      actionSuccess.value = false;
    }
  } catch (e) {
    actionMsg.value = '网络错误，请检查后端服务';
    actionSuccess.value = false;
  } finally {
    loading.value = false;
  }
};

// 清空知识库
const clearKB = async () => {
  if (!confirm('确定要清空整个知识库吗？此操作不可恢复！')) return;
  actionMsg.value = '';
  loading.value = true;
  try {
    const res = await ragApi.clearKnowledgeBase();
    if (res.code === 200) {
      actionMsg.value = '知识库已清空';
      actionSuccess.value = true;
      loadStats();
    } else {
      actionMsg.value = res.message || '清空失败';
      actionSuccess.value = false;
    }
  } catch (e) {
    actionMsg.value = '网络错误，请检查后端服务';
    actionSuccess.value = false;
  } finally {
    loading.value = false;
  }
};

// 检索测试
const testRetrieval = async () => {
  if (!retrievalQuery.value.trim()) return;
  loading.value = true;
  retrievalResults.value = [];
  retrievalTested.value = false;
  try {
    const res = await ragApi.testRetrieval(retrievalQuery.value.trim());
    if (res.code === 200 && res.data) {
      const data = Array.isArray(res.data) ? res.data : [res.data];
      retrievalResults.value = data;
    }
    retrievalTested.value = true;
  } catch (e) {
    retrievalTested.value = true;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadStats();
});
</script>
