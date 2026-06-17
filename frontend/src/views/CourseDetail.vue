<template>
  <div class="h-full bg-notion-canvas-soft p-6 overflow-y-auto">
    <div v-if="loading" class="flex items-center justify-center h-40">
      <div class="text-notion-ink-faint text-[14px]">加载中...</div>
    </div>
    <div v-else-if="!course" class="flex items-center justify-center h-40">
      <div class="text-notion-ink-faint text-[14px]">课程不存在</div>
    </div>
    <template v-else>
      <!-- 头部 -->
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-6 mb-4">
        <button @click="router.back()" class="text-notion-ink-faint hover:text-notion-ink-muted text-[14px] mb-4 flex items-center gap-1 transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
          返回课程列表
        </button>
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-xl font-bold text-notion-ink">{{ course.courseName }}</h1>
            <p class="text-notion-ink-muted text-[14px] mt-1">{{ course.courseCode }}</p>
          </div>
          <span class="px-3 py-1 text-[12px] rounded-notion-full" :class="courseTypeClass(course.courseType)">
            {{ courseTypeLabel(course.courseType) }}
          </span>
        </div>
      </div>

      <!-- 详情内容 -->
      <div class="grid grid-cols-2 gap-4">
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">授课教师</div>
          <div class="text-notion-ink text-[14px]">{{ course.teacherName || '未指定' }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">所属院系</div>
          <div class="text-notion-ink text-[14px]">{{ course.department || '-' }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">学分</div>
          <div class="text-notion-ink text-[14px]">{{ course.credit }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">考核方式</div>
          <div class="text-notion-ink text-[14px]">{{ examTypeLabel(course.examType) }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">总学时</div>
          <div class="text-notion-ink text-[14px]">{{ course.courseHours }} 学时</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">学时分配</div>
          <div class="text-notion-ink text-[14px]">理论 {{ course.theoryHours }} + 实践 {{ course.practiceHours }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">学期</div>
          <div class="text-notion-ink text-[14px]">{{ course.semester || '-' }}</div>
        </div>
        <div class="p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
          <div class="text-notion-ink-faint text-[12px] mb-1">课程类型</div>
          <div class="text-notion-ink text-[14px]">{{ courseTypeLabel(course.courseType) }}</div>
        </div>
      </div>

      <div v-if="course.description" class="mt-4 p-4 bg-notion-canvas rounded-notion-lg border border-notion-hairline">
        <div class="text-notion-ink-faint text-[12px] mb-2">课程简介</div>
        <p class="text-notion-ink-secondary text-[14px] leading-relaxed">{{ course.description }}</p>
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
  1: 'bg-notion-accent-orange/10 text-notion-accent-orange',
  2: 'bg-notion-accent-sky/10 text-notion-accent-sky',
  3: 'bg-notion-accent-green/10 text-notion-accent-green',
}[type] || 'bg-notion-ink-faint/10 text-notion-ink-faint');
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
