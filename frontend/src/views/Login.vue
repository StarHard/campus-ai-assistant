<template>
  <div class="min-h-[calc(100vh-56px)] flex items-center justify-center bg-notion-canvas-soft px-4">
    <div class="w-full max-w-sm bg-notion-canvas rounded-notion-xl shadow-notion-1 p-8">
      <div class="text-center mb-8">
        <div class="w-10 h-10 mx-auto mb-4 rounded-notion-md bg-notion-primary flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-notion-ink">校园智能助手</h1>
        <p class="text-notion-ink-muted mt-1 text-[14px]">欢迎回来，请登录您的账号</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label class="block text-[14px] font-medium text-notion-ink-secondary mb-1">用户名</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary transition-colors placeholder:text-notion-ink-faint"
            required
          />
        </div>

        <div class="mb-6">
          <label class="block text-[14px] font-medium text-notion-ink-secondary mb-1">密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary transition-colors placeholder:text-notion-ink-faint"
            required
          />
        </div>

        <div v-if="errorMsg" class="mb-4 text-[13px] text-red-500">
          {{ errorMsg }}
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full py-2.5 bg-notion-primary text-white font-medium rounded-notion-full hover:bg-notion-primary-active transition-colors disabled:opacity-60"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <p class="mt-6 text-center text-[14px] text-notion-ink-muted">
        还没有账号？
        <router-link to="/register" class="text-notion-primary hover:underline">立即注册</router-link>
      </p>

      <div class="mt-4 text-center">
        <router-link to="/" class="text-[14px] text-notion-ink-faint hover:text-notion-ink-muted transition-colors">
          返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const errorMsg = ref('');

const form = reactive({
  username: '',
  password: '',
});

const handleLogin = async () => {
  errorMsg.value = '';
  loading.value = true;
  try {
    const result = await userStore.login(form.username, form.password);
    if (result.success) {
      router.push('/');
    } else {
      errorMsg.value = result.message;
    }
  } catch (e) {
    errorMsg.value = '网络错误，请检查后端服务是否启动。';
  } finally {
    loading.value = false;
  }
};
</script>
