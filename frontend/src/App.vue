<script setup>
import { useUserStore } from './stores/user';
import { useRouter, useRoute } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const isAuthPage = () => {
  return route.path === '/login' || route.path === '/register';
};

const handleLogout = () => {
  userStore.logout();
  router.push('/');
};
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-600 via-purple-600 to-indigo-700">
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-white/10 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-white/10 rounded-full blur-3xl"></div>
      <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
    </div>

    <div v-if="!isAuthPage()" class="relative z-10 bg-white/10 backdrop-blur-sm border-b border-white/10">
      <div class="max-w-4xl mx-auto flex items-center justify-between px-4 py-2">
        <router-link to="/" class="flex items-center gap-2 text-white hover:opacity-80">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
          <span class="font-medium text-sm">校园智能助手</span>
        </router-link>

        <div v-if="userStore.isLoggedIn" class="flex items-center gap-3">
          <span class="text-white/80 text-sm">{{ userStore.user?.realName || userStore.user?.username }}</span>
          <button @click="handleLogout" class="px-3 py-1.5 text-sm text-white/80 hover:text-white bg-white/10 hover:bg-white/20 rounded-lg transition-all">
            退出
          </button>
        </div>
        <div v-else class="flex items-center gap-2">
          <router-link to="/login" class="px-4 py-1.5 text-sm text-white/80 hover:text-white bg-white/10 hover:bg-white/20 rounded-lg transition-all">
            登录
          </router-link>
          <router-link to="/register" class="px-4 py-1.5 text-sm text-white bg-white/20 hover:bg-white/30 rounded-lg transition-all">
            注册
          </router-link>
        </div>
      </div>
    </div>

    <div :class="isAuthPage() ? '' : 'relative z-10 h-[calc(100vh-45px)]'">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
</style>
