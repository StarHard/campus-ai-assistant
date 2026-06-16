import api from './chat';

export const courseApi = {
  list: async (params = {}) => {
    const response = await api.get('/course/list', { params });
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/course/${id}`);
    return response.data;
  },

  add: async (course) => {
    const response = await api.post('/course', course);
    return response.data;
  },

  update: async (id, course) => {
    const response = await api.put(`/course/${id}`, course);
    return response.data;
  },

  remove: async (id) => {
    const response = await api.delete(`/course/${id}`);
    return response.data;
  },
};
