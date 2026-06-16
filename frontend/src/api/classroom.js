import api from './chat';

export const classroomApi = {
  list: async (params = {}) => {
    const response = await api.get('/classroom/list', { params });
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/classroom/${id}`);
    return response.data;
  },

  findEmpty: async (weekday, startSection, endSection, semester = '2025-2026-1') => {
    const response = await api.get('/classroom/empty', {
      params: { weekday, startSection, endSection, semester },
    });
    return response.data;
  },
};
