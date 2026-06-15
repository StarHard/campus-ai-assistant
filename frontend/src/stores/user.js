import { defineStore } from 'pinia';
import { ref } from 'vue';
import { userApi } from '../api/user';

export const useUserStore = defineStore('user', () => {
  const user = ref(null);
  const isLoggedIn = ref(false);

  const login = async (username, password) => {
    const res = await userApi.login(username, password);
    if (res.code === 200 && res.data) {
      user.value = res.data;
      isLoggedIn.value = true;
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

  const logout = () => {
    user.value = null;
    isLoggedIn.value = false;
  };

  return { user, isLoggedIn, login, register, logout };
});
