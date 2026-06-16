<template>
  <div class="h-full flex flex-col bg-white/5 backdrop-blur-sm rounded-2xl m-4 overflow-hidden">
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="text-white/50">加载中...</div>
    </div>
    <div v-else-if="!course" class="flex-1 flex items-center justify-center">
      <div class="text-white/50">课程不存在</div>
    </div>
    <template v-else>
      <!-- 头部 -->
      <div class="p-6 border-b border-white/10">
        <button @click="router.back()" class="text-white/50 hover:text-white text-sm mb-4 flex items-center gap-1">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
          返回课程列表
        </button>
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-xl font-bold text-white">{{ course.courseName }}</h1>
            <p class="text-white/50 text-sm mt-1">{{ course.courseCode }}</p>
          </div>
          <span class="px-3 py-1 text-xs rounded-full" :class="courseTypeClass(course.courseType)">
            {{ courseTypeLabel(course.courseType) }}
          </span>
        </div>
      </div>

      <!-- 详情内容 -->
      <div class="flex-1 overflow-y-auto p-6">
        <div class="grid grid-cols-2 gap-4">
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">授课教师</div>
            <div class="text-white text-sm">{{ course.teacherName || '未指定' }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">所属院系</div>
            <div class="text-white text-sm">{{ course.department || '-' }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">学分</div>
            <div class="text-white text-sm">{{ course.credit }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">考核方式</div>
            <div class="text-white text-sm">{{ examTypeLabel(course.examType) }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">总学时</div>
            <div class="text-white text-sm">{{ course.courseHours }} 学时</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">学时分配</div>
            <div class="text-white text-sm">理论 {{ course.theoryHours }} + 实践 {{ course.practiceHours }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">学期</div>
            <div class="text-white text-sm">{{ course.semester || '-' }}</div>
          </div>
          <div class="p-4 bg-white/5 rounded-xl border border-white/10">
            <div class="text-white/40 text-xs mb-1">课程类型</div>
            <div class="text-white text-sm">{{ courseTypeLabel(course.courseType) }}</div>
          </div>
        </div>

        <div v-if="course.description" class="mt-6 p-4 bg-white/5 rounded-xl border border-white/10">
          <div class="text-white/40 text-xs mb-2">课程简介</div>
          <p class="text-white/80 text-sm leading-relaxed">{{ course.description }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { courseApi } from '../api/course';

const route = useRoute();
const router = useRouter();
const course = ref(null);
const loading = ref(true);

const courseTypeLabel = (type) => ({ 1: '必修', 2: '选修', 3: '公选' }[type] || '其他');
const courseTypeClass = (type) => ({
  1: 'bg-red-500/20 text-red-300',
  2: 'bg-blue-500/20 text-blue-300',
  3: 'bg-green-500/20 text-green-300',
}[type] || 'bg-gray-500/20 text-gray-300');
const examTypeLabel = (type) => ({ 1: '考试', 2: '考查', 3: '其他' }[type] || '-');

onMounted(async () => {
  try {
    const id = route.params.id;
    const res = await courseApi.getById(id);
    if (res.code === 200 && res.data) {
      course.value = res.data;
    }
  } catch (e) {
    console.error('加载课程详情失败:', e);
  } finally {
    loading.value = false;
  }
});
</script>
