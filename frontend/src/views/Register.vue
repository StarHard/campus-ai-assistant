<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-500 via-purple-500 to-indigo-600 flex items-center justify-center px-4 py-8">
    <div class="w-full max-w-lg">
      <div class="text-center mb-8">
        <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white">创建账号</h1>
        <p class="text-white/70 mt-1">注册后即可使用校园智能服务</p>
      </div>

      <div class="bg-white rounded-2xl shadow-2xl p-8">
        <form @submit.prevent="handleRegister">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">用户名 <span class="text-red-500">*</span></label>
              <input v-model="form.username" type="text" placeholder="登录账号" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">密码 <span class="text-red-500">*</span></label>
              <input v-model="form.password" type="password" placeholder="至少6位" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" required minlength="6" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 mt-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">真实姓名 <span class="text-red-500">*</span></label>
              <input v-model="form.realName" type="text" placeholder="您的姓名" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
              <select v-model="form.gender" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm bg-white">
                <option :value="null">请选择</option>
                <option :value="1">男</option>
                <option :value="0">女</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 mt-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
              <input v-model="form.phone" type="tel" placeholder="11位手机号" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
              <input v-model="form.email" type="email" placeholder="example@mail.com" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 mt-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">院系</label>
              <input v-model="form.department" type="text" placeholder="所在院系" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">专业</label>
              <input v-model="form.major" type="text" placeholder="所学专业" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" />
            </div>
          </div>

          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">年级</label>
            <input v-model="form.grade" type="text" placeholder="如：2025级" class="w-full px-3 py-2.5 rounded-xl border border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all text-sm" />
          </div>

          <div v-if="errorMsg" class="mt-4 p-3 bg-red-50 text-red-600 text-sm rounded-xl">
            {{ errorMsg }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full mt-6 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-medium rounded-xl hover:shadow-lg transition-all disabled:opacity-60"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          已有账号？
          <router-link to="/login" class="text-blue-600 hover:text-blue-700 font-medium">立即登录</router-link>
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
  realName: '',
  gender: null,
  phone: '',
  email: '',
  department: '',
  major: '',
  grade: '',
});

const handleRegister = async () => {
  errorMsg.value = '';
  loading.value = true;
  try {
    const userInfo = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      userType: 2,
    };
    if (form.gender !== null) userInfo.gender = form.gender;
    if (form.phone) userInfo.phone = form.phone;
    if (form.email) userInfo.email = form.email;
    if (form.department) userInfo.department = form.department;
    if (form.major) userInfo.major = form.major;
    if (form.grade) userInfo.grade = form.grade;

    const result = await userStore.register(userInfo);
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
