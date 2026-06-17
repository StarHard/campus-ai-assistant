import api from './chat';

export const userApi = {
  login: async (username, password) => {
    const response = await api.post('/user/login', { username, password });
    return response.data;
  },

  register: async (userInfo) => {
    const response = await api.post('/user/register', userInfo);
    return response.data;
  },

  getUserInfo: async (id) => {
    const response = await api.get(`/user/${id}`);
    return response.data;
  },

  changePassword: async (id, oldPassword, newPassword) => {
    const response = await api.put(`/user/${id}/password`, null, {
      params: { oldPassword, newPassword },
    });
    return response.data;
  },

  logout: async (token) => {
    const response = await api.post('/user/logout', null, {
      headers: { 'X-Token': token },
    });
    return response.data;
  },

  updateUser: async (id, data) => {
    const response = await api.put(`/user/${id}`, data);
    return response.data;
  },
};
