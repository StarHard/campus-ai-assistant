import axios from 'axios';

const BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080/api`;

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['X-Token'] = token;
    }
    return config;
  },
  (error) => {
    console.error('[API] Request Error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const chatApi = {
  ask: async (question, sessionId = null, history = [], enableRag = true) => {
    const response = await api.post('/chat/ask', {
      question,
      sessionId,
      history,
      enableRag,
    });
    return response.data;
  },

  simpleAsk: async (question) => {
    const response = await api.get('/chat/simple', {
      params: { question },
    });
    return response.data;
  },

  // 获取当前用户所有会话列表
  getSessions: async () => {
    const response = await api.get('/chat/sessions');
    return response.data;
  },

  // 创建新会话，返回 sessionId
  createSession: async (title = '新会话') => {
    const response = await api.post('/chat/session', null, {
      params: { title },
    });
    return response.data;
  },

  getSessionHistory: async (sessionId) => {
    const response = await api.get(`/chat/session/${sessionId}/history`);
    return response.data;
  },

  clearSession: async (sessionId) => {
    const response = await api.delete(`/chat/session/${sessionId}`);
    return response.data;
  },

  streamAsk: async (question, sessionId = null, onMessage, onDone, onError, signal) => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`${BASE_URL}/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'X-Token': token } : {}),
        },
        body: JSON.stringify({ question, sessionId, enableRag: true }),
        signal,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let currentEvent = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop();

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim();
            if (currentEvent === 'done') {
              try {
                const data = JSON.parse(dataStr);
                onMessage({ type: 'done', ...data });
              } catch {
                if (onDone) onDone();
                return;
              }
              if (onDone) onDone();
              return;
            } else if (currentEvent === 'message') {
              onMessage({ type: 'message', content: dataStr });
            }
          }
        }
      }
      if (onDone) onDone();
    } catch (error) {
      if (error.name === 'AbortError') {
        // 用户取消了生成，正常结束
        if (onDone) onDone();
      } else {
        if (onError) onError(error);
      }
    }
  },
};

export const ragApi = {
  getStats: async () => {
    const response = await api.get('/rag/stats');
    return response.data;
  },

  uploadDocument: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post('/rag/document/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  batchLoadDocuments: async (directoryPath = './data/knowledge-base') => {
    const response = await api.post('/rag/documents/batch-load', {
      directoryPath,
    });
    return response.data;
  },

  clearKnowledgeBase: async () => {
    const response = await api.delete('/rag/clear');
    return response.data;
  },

  testRetrieval: async (query) => {
    const response = await api.get('/rag/test-retrieval', {
      params: { query },
    });
    return response.data;
  },
};

export default api;
