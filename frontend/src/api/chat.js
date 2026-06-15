import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    console.log(`[API] Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('[API] Request Error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    console.log(`[API] Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('[API] Response Error:', error.response?.data || error.message);
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

  getSessionHistory: async (sessionId) => {
    const response = await api.get(`/chat/session/${sessionId}/history`);
    return response.data;
  },

  clearSession: async (sessionId) => {
    const response = await api.delete(`/chat/session/${sessionId}`);
    return response.data;
  },

  streamAsk: (question, sessionId = null, onMessage, onError) => {
    const params = new URLSearchParams({ question });
    if (sessionId) {
      params.append('sessionId', sessionId);
    }

    const eventSource = new EventSource(`${BASE_URL}/chat/stream?${params.toString()}`);

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        onMessage(data);
      } catch (error) {
        console.error('[SSE] Parse Error:', error);
      }
    };

    eventSource.onerror = (error) => {
      console.error('[SSE] Error:', error);
      if (onError) {
        onError(error);
      }
      eventSource.close();
    };

    return eventSource;
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
