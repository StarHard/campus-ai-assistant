import { defineStore } from 'pinia';
import { ref } from 'vue';
import { userApi } from '../api/user';

export const useUserStore = defineStore('user', () => {
  const user = ref(null);
  const isLoggedIn = ref(!!localStorage.getItem('token'));

  const init = () => {
    const saved = localStorage.getItem('user');
    if (saved) {
      try { user.value = JSON.parse(saved); } catch {}
    }
  };
  init();

  const login = async (username, password) => {
    const res = await userApi.login(username, password);
    if (res.code === 200 && res.data) {
      const data = res.data;
      localStorage.setItem('token', data.token);
      user.value = data;
      isLoggedIn.value = true;
      localStorage.setItem('user', JSON.stringify(data));
      return { success: true };
    }
    return { success: false, message: res.message || '登录失败' };
  };

  const register = async (userInfo) => {
    const res = await userApi.register(userInfo);
    if (res.code === 200 && res.data) {
      user.value = res.data;
      isLoggedIn.value = true;
      return { success: true };
    }
    return { success: false, message: res.message || '注册失败' };
  };

  const logout = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) await userApi.logout(token);
    } catch {}
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    user.value = null;
    isLoggedIn.value = false;
  };

  return { user, isLoggedIn, login, register, logout };
});
