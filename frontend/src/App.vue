<template>
  <div class="min-h-screen bg-notion-canvas-soft font-notion">
    <!-- 顶部导航栏 -->
    <nav class="bg-notion-canvas border-b border-notion-hairline sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between">
        <!-- 左侧Logo -->
        <router-link to="/" class="flex items-center gap-2 hover:opacity-80 transition-opacity">
          <div class="w-7 h-7 bg-notion-primary rounded-notion-md flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <span class="text-notion-ink font-semibold text-[15px]">校园智能助手</span>
        </router-link>

        <!-- 中间导航 -->
        <div class="flex items-center gap-1">
          <router-link
            to="/"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path === '/' ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            AI问答
          </router-link>
          <router-link
            to="/courses"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path.startsWith('/courses') ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            课程查询
          </router-link>
          <router-link
            to="/classrooms"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path === '/classrooms' ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            教室查询
          </router-link>
          <router-link
            to="/schedule"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path === '/schedule' ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            我的课表
          </router-link>
          <router-link
            to="/dashboard"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path === '/dashboard' ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            数据看板
          </router-link>
          <router-link
            to="/knowledge-base"
            class="px-3 py-1.5 text-[14px] rounded-notion-sm transition-colors"
            :class="$route.path === '/knowledge-base' ? 'text-notion-primary bg-notion-primary/5' : 'text-notion-ink-muted hover:text-notion-ink hover:bg-notion-canvas-soft'"
          >
            知识库
          </router-link>
        </div>

        <!-- 右侧用户 -->
        <div class="flex items-center gap-3">
          <template v-if="userStore.isLoggedIn">
            <router-link
              to="/profile"
              class="text-[14px] text-notion-ink-secondary hover:text-notion-primary transition-colors"
              :class="$route.path === '/profile' ? 'text-notion-primary' : ''"
            >
              {{ userStore.user?.realName || userStore.user?.username }}
            </router-link>
            <button
              @click="handleLogout"
              class="text-[14px] text-notion-ink-faint hover:text-notion-ink-muted transition-colors"
            >
              退出
            </button>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="text-[14px] text-notion-ink-muted hover:text-notion-ink transition-colors"
            >
              登录
            </router-link>
            <router-link
              to="/register"
              class="px-4 py-1.5 bg-notion-primary text-white text-[14px] font-medium rounded-notion-full hover:bg-notion-primary-active transition-colors"
            >
              注册
            </router-link>
          </template>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <main class="h-[calc(100vh-56px)]">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useUserStore } from './stores/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();

const handleLogout = async () => {
  await userStore.logout();
  router.push('/login');
};
</script>
