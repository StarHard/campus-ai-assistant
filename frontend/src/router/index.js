import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'Chat',
    component: () => import('../components/ChatWindow.vue'),
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('../views/CourseList.vue'),
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('../views/CourseDetail.vue'),
  },
  {
    path: '/classrooms',
    name: 'Classrooms',
    component: () => import('../views/ClassroomList.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
