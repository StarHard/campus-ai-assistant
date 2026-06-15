<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-500 via-purple-500 to-indigo-600 flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white">校园智能助手</h1>
        <p class="text-white/70 mt-1">欢迎回来，请登录您的账号</p>
      </div>

      <div class="bg-white rounded-2xl shadow-2xl p-8">
        <form @submit.prevent="handleLogin">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
              required
            />
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              class="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
              required
            />
          </div>

          <div v-if="errorMsg" class="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-xl">
            {{ errorMsg }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-medium rounded-xl hover:shadow-lg transition-all disabled:opacity-60"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          还没有账号？
          <router-link to="/register" class="text-blue-600 hover:text-blue-700 font-medium">立即注册</router-link>
        </p>

        <div class="mt-4 text-center">
          <router-link to="/" class="text-sm text-gray-400 hover:text-gray-600">
            返回首页
          </router-link>
        </div>
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
