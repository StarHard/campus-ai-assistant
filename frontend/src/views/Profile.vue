<template>
  <div class="max-w-3xl mx-auto px-6 py-8">
    <h1 class="text-[28px] font-bold text-notion-ink mb-8">个人中心</h1>

    <!-- 基本信息 -->
    <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6 mb-6">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-[18px] font-semibold text-notion-ink">基本信息</h2>
        <button
          v-if="!editing"
          @click="editing = true"
          class="text-[14px] text-notion-primary hover:text-notion-primary-active transition-colors"
        >
          编辑
        </button>
        <template v-else>
          <button @click="cancelEdit" class="text-[14px] text-notion-ink-faint hover:text-notion-ink-muted mr-3">取消</button>
          <button @click="saveProfile" :disabled="saving" class="text-[14px] text-notion-primary hover:text-notion-primary-active disabled:opacity-60">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </template>
      </div>

      <div class="grid grid-cols-2 gap-x-8 gap-y-5">
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">用户名</label>
          <p v-if="!editing" class="text-[15px] text-notion-ink">{{ userInfo.username || '-' }}</p>
          <input v-else v-model="form.username" type="text" class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary" />
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">姓名</label>
          <p v-if="!editing" class="text-[15px] text-notion-ink">{{ userInfo.realName || '-' }}</p>
          <input v-else v-model="form.realName" type="text" class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary" />
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">邮箱</label>
          <p v-if="!editing" class="text-[15px] text-notion-ink">{{ userInfo.email || '-' }}</p>
          <input v-else v-model="form.email" type="email" class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary" />
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">手机号</label>
          <p v-if="!editing" class="text-[15px] text-notion-ink">{{ userInfo.phone || '-' }}</p>
          <input v-else v-model="form.phone" type="text" class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary" />
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">院系</label>
          <p class="text-[15px] text-notion-ink">{{ userInfo.department || '-' }}</p>
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">年级</label>
          <p class="text-[15px] text-notion-ink">{{ userInfo.grade || '-' }}</p>
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">角色</label>
          <span class="inline-block px-2.5 py-0.5 text-[12px] font-medium rounded-notion-full bg-notion-accent-sky/10 text-notion-accent-sky">
            {{ roleLabel }}
          </span>
        </div>
        <div>
          <label class="block text-[12px] font-medium text-notion-ink-muted mb-1 uppercase tracking-wide">注册时间</label>
          <p class="text-[15px] text-notion-ink">{{ formatTime(userInfo.createTime) }}</p>
        </div>
      </div>
    </div>

    <!-- 修改密码 -->
    <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
      <h2 class="text-[18px] font-semibold text-notion-ink mb-6">修改密码</h2>
      <form @submit.prevent="handleChangePassword" class="max-w-md space-y-4">
        <div>
          <label class="block text-[14px] font-medium text-notion-ink-secondary mb-1">当前密码</label>
          <input
            v-model="pwdForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary placeholder:text-notion-ink-faint"
            required
          />
        </div>
        <div>
          <label class="block text-[14px] font-medium text-notion-ink-secondary mb-1">新密码</label>
          <input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            minlength="6"
            class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary placeholder:text-notion-ink-faint"
            required
          />
        </div>
        <div>
          <label class="block text-[14px] font-medium text-notion-ink-secondary mb-1">确认新密码</label>
          <input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            minlength="6"
            class="w-full px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[15px] text-notion-ink outline-none focus:border-notion-primary placeholder:text-notion-ink-faint"
            required
          />
        </div>
        <div v-if="pwdMsg" :class="pwdMsgType === 'error' ? 'text-red-500' : 'text-green-600'" class="text-[13px]">
          {{ pwdMsg }}
        </div>
        <button
          type="submit"
          :disabled="changingPwd"
          class="px-6 py-2 bg-notion-primary text-white text-[14px] font-medium rounded-notion-full hover:bg-notion-primary-active transition-colors disabled:opacity-60"
        >
          {{ changingPwd ? '修改中...' : '确认修改' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { useUserStore } from '../stores/user';
import { userApi } from '../api/user';

const userStore = useUserStore();
const editing = ref(false);
const saving = ref(false);
const changingPwd = ref(false);
const pwdMsg = ref('');
const pwdMsgType = ref('');

const userInfo = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  grade: '',
  role: '',
  createTime: '',
});

const form = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
});

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' };
  return map[userInfo.role] || userInfo.role || '-';
});

const formatTime = (t) => {
  if (!t) return '-';
  return t.replace('T', ' ').substring(0, 16);
};

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo(userStore.user?.id || userStore.user?.userId);
    if (res.code === 200 && res.data) {
      const d = res.data;
      Object.assign(userInfo, d);
      Object.assign(form, { username: d.username, realName: d.realName, email: d.email, phone: d.phone });
    }
  } catch {}
};

const saveProfile = async () => {
  saving.value = true;
  try {
    const res = await userApi.updateUser(userInfo.id, {
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      username: form.username,
    });
    if (res.code === 200) {
      Object.assign(userInfo, { username: form.username, realName: form.realName, email: form.email, phone: form.phone });
      editing.value = false;
    }
  } catch (e) {
    console.error('保存失败:', e);
  } finally {
    saving.value = false;
  }
};

const cancelEdit = () => {
  Object.assign(form, { username: userInfo.username, realName: userInfo.realName, email: userInfo.email, phone: userInfo.phone });
  editing.value = false;
};

const handleChangePassword = async () => {
  pwdMsg.value = '';
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdMsg.value = '两次输入的密码不一致';
    pwdMsgType.value = 'error';
    return;
  }
  if (pwdForm.newPassword.length < 6) {
    pwdMsg.value = '新密码长度不能少于6位';
    pwdMsgType.value = 'error';
    return;
  }

  changingPwd.value = true;
  try {
    const res = await userApi.changePassword(userInfo.id, pwdForm.oldPassword, pwdForm.newPassword);
    if (res.code === 200) {
      pwdMsg.value = '密码修改成功';
      pwdMsgType.value = 'success';
      pwdForm.oldPassword = '';
      pwdForm.newPassword = '';
      pwdForm.confirmPassword = '';
    } else {
      pwdMsg.value = res.message || '修改失败，请检查原密码是否正确';
      pwdMsgType.value = 'error';
    }
  } catch (e) {
    pwdMsg.value = '网络错误，请稍后重试';
    pwdMsgType.value = 'error';
  } finally {
    changingPwd.value = false;
  }
};

onMounted(() => {
  if (!userStore.isLoggedIn) {
    window.location.href = '/login';
    return;
  }
  loadUserInfo();
});
</script>
