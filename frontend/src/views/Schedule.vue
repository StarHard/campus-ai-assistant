<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-[28px] font-bold text-notion-ink">我的课表</h1>
      <div class="flex items-center gap-3">
        <select
          v-model="selectedSemester"
          class="px-3 py-1.5 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[14px] text-notion-ink outline-none focus:border-notion-primary"
        >
          <option value="2024-2025-1">2024-2025 第一学期</option>
          <option value="2024-2025-2">2024-2025 第二学期</option>
          <option value="2025-2026-1">2025-2026 第一学期</option>
        </select>
        <button
          @click="loadSchedule"
          :disabled="loading"
          class="px-4 py-1.5 bg-notion-primary text-white text-[14px] font-medium rounded-notion-full hover:bg-notion-primary-active transition-colors disabled:opacity-60"
        >
          {{ loading ? '加载中...' : '刷新' }}
        </button>
      </div>
    </div>

    <!-- 课表网格 -->
    <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 overflow-hidden">
      <!-- 表头 -->
      <div class="grid grid-cols-8 border-b border-notion-hairline">
        <div class="p-3 text-center text-[13px] font-semibold text-notion-ink-muted bg-notion-canvas-soft/50 border-r border-notion-hairline">
          节次
        </div>
        <div v-for="day in weekdays" :key="day.value" class="p-3 text-center bg-notion-canvas-soft/50" :class="{ 'border-r border-notion-hairline': day.value < 5 }">
          <div class="text-[13px] font-semibold text-notion-ink">{{ day.label }}</div>
          <div class="text-[12px] text-notion-ink-faint mt-0.5">{{ day.sub }}</div>
        </div>
      </div>

      <!-- 课程格子 -->
      <div v-for="(section, sIdx) in sections" :key="sIdx" class="grid grid-cols-8 border-b border-notion-hairline last:border-b-0">
        <!-- 节次标签 -->
        <div class="p-2 text-center flex items-center justify-center border-r border-notion-hairline bg-notion-canvas-soft/30">
          <span class="text-[12px] font-medium text-notion-ink-muted">{{ section.label }}</span>
          <span class="block text-[11px] text-notion-ink-faint">{{ section.time }}</span>
        </div>

        <!-- 每天的课程 -->
        <template v-for="day in weekdays" :key="day.value">
          <div
            class="p-1.5 min-h-[72px] relative border-r border-notion-hairline last:border-r-0 hover:bg-notion-canvas-soft/30 transition-colors"
            :class="{ 'border-r-0': day.value === 5 }"
          >
            <template v-if="getCourse(day.value, section.start)">
              <div
                class="absolute inset-1 p-2 rounded-notion-sm overflow-hidden cursor-pointer hover:shadow-notion-1 transition-shadow"
                :style="{ backgroundColor: getCourseColor(getCourse(day.value, section.start)) }"
                @click="showCourseDetail(getCourse(day.value, section.start))"
              >
                <div class="text-[12px] font-semibold text-white truncate">{{ getCourse(day.value, section.start).courseName }}</div>
                <div class="text-[11px] text-white/80 truncate mt-0.5">{{ getCourse(day.value, section.start).teacherName }}</div>
                <div class="text-[11px] text-white/70 truncate mt-0.5">{{ getCourse(day.value, section.start).classroomName || getCourse(day.value, section.start).courseCode }}</div>
              </div>
            </template>
          </div>
        </template>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="mt-6 grid grid-cols-4 gap-4">
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">本周课程</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ courseList.length }}</div>
        <div class="text-[13px] text-notion-ink-muted">门课程</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">总学分</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ totalCredits }}</div>
        <div class="text-[13px] text-notion-ink-muted">学分</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">总学时</div>
        <div class="text-[24px] font-bold text-notion-ink">{{ totalHours }}</div>
        <div class="text-[13px] text-notion-ink-muted">学时</div>
      </div>
      <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-4">
        <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">当前学期</div>
        <div class="text-[16px] font-semibold text-notion-ink leading-tight">{{ selectedSemesterLabel }}</div>
      </div>
    </div>

    <!-- 课程详情弹窗 -->
    <Teleport to="body">
      <div
        v-if="selectedCourse"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/20 backdrop-blur-sm"
        @click.self="selectedCourse = null"
      >
        <div class="bg-notion-canvas rounded-notion-xl shadow-notion-1 w-full max-w-md mx-4 p-6">
          <div class="flex items-start justify-between mb-4">
            <h3 class="text-[18px] font-bold text-notion-ink">{{ selectedCourse.courseName }}</h3>
            <button @click="selectedCourse = null" class="text-notion-ink-faint hover:text-notion-ink transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="space-y-3">
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">课程代码</span>
              <span class="text-[14px] text-notion-ink font-medium">{{ selectedCourse.courseCode || '-' }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">授课教师</span>
              <span class="text-[14px] text-notion-ink font-medium">{{ selectedCourse.teacherName || '-' }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">所属院系</span>
              <span class="text-[14px] text-notion-ink font-medium">{{ selectedCourse.department || '-' }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">上课地点</span>
              <span class="text-[14px] text-notion-ink font-medium">{{ selectedCourse.classroomName || '-' }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">学分 / 学时</span>
              <span class="text-[14px] text-notion-ink font-medium">{{ selectedCourse.credit }} / {{ selectedCourse.courseHours }}学时</span>
            </div>
            <div class="flex justify-between py-2 border-b border-notion-hairline">
              <span class="text-[14px] text-notion-ink-muted">课程类型</span>
              <span class="inline-block px-2 py-0.5 text-[12px] rounded-notion-full" :class="typeClass(selectedCourse.courseType)">
                {{ courseTypeLabel(selectedCourse.courseType) }}
              </span>
            </div>
            <div class="py-2">
              <span class="text-[14px] text-notion-ink-muted block mb-1">课程简介</span>
              <p class="text-[14px] text-notion-ink-secondary leading-relaxed">{{ selectedCourse.description || '暂无简介' }}</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '../stores/user';
import api from '../api/chat';

const userStore = useUserStore();
const loading = ref(false);
const selectedSemester = ref('2024-2025-1');
const courseList = ref([]);
const selectedCourse = ref(null);

const weekdays = [
  { label: '周一', sub: 'Mon', value: 1 },
  { label: '周二', sub: 'Tue', value: 2 },
  { label: '周三', sub: 'Wed', value: 3 },
  { label: '周四', sub: 'Thu', value: 4 },
  { label: '周五', sub: 'Fri', value: 5 },
];

const sections = [
  { label: '第1-2节', time: '08:00-09:40', start: 1, end: 2 },
  { label: '第3-4节', time: '10:00-11:40', start: 3, end: 4 },
  { label: '第5-6节', time: '14:00-15:40', start: 5, end: 6 },
  { label: '第7-8节', time: '16:00-17:40', start: 7, end: 8 },
  { label: '第9-10节', time: '19:00-20:40', start: 9, end: 10 },
];

// 课程颜色池
const colorPool = [
  '#0075de', '#d6b6f6', '#62aef0', '#dd5b00',
  '#2a9d99', '#ff64c8', '#1aae39', '#391c57',
];

let colorIndex = 0;
const courseColorMap = new Map();

const getCourseColor = (course) => {
  if (!course) return '';
  const key = course.courseId || course.courseName;
  if (!courseColorMap.has(key)) {
    courseColorMap.set(key, colorPool[colorIndex % colorPool.length]);
    colorIndex++;
  }
  return courseColorMap.get(key);
};

// 解析 scheduleTime 字段，构建课表矩阵
// 后端返回的 scheduleTime 可能是 "周一1-2节" 或 JSON 格式，这里做兼容处理
const scheduleMatrix = computed(() => {
  const matrix = {};
  for (const course of courseList.value) {
    const st = course.scheduleTime;
    if (!st) continue;

    // 尝试解析 scheduleTime 字符串（如 "周一1-2节" 或 "星期一 第1-2节" 等）
    const dayMatch = st.match(/周([一二三四五六日])|星期([一二三四五六日])/);
    const secMatch = st.match(/(\d+)-(\d+)/);

    if (dayMatch && secMatch) {
      const dayStr = dayMatch[1] || dayMatch[2];
      const dayMap = { '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '日': 7 };
      const weekday = dayMap[dayStr];
      if (weekday) {
        const key = `${weekday}-${parseInt(secMatch[1])}`;
        if (!matrix[key]) matrix[key] = course;
      }
    }

    // 如果有独立的 weekday/startSection/endSection 字段（Schedule实体字段）
    if (course.weekday && course.startSection) {
      const key = `${course.weekday}-${course.startSection}`;
      if (!matrix[key]) matrix[key] = course;
    }
  }
  return matrix;
});

const getCourse = (weekday, sectionStart) => {
  return scheduleMatrix.value[`${weekday}-${sectionStart}`] || null;
};

const totalCredits = computed(() => {
  return courseList.value.reduce((sum, c) => sum + (parseFloat(c.credit) || 0), 0).toFixed(1);
});

const totalHours = computed(() => {
  return courseList.value.reduce((sum, c) => sum + (c.courseHours || 0), 0);
});

const selectedSemesterLabel = computed(() => {
  const map = {
    '2024-2025-1': '2024-2025 第一学期',
    '2024-2025-2': '2024-2025 第二学期',
    '2025-2026-1': '2025-2026 第一学期',
  };
  return map[selectedSemester.value] || selectedSemester.value;
});

const courseTypeLabel = (type) => {
  const map = { 1: '必修课', 2: '选修课', 3: '实践课' };
  return map[type] || '未知';
};

const typeClass = (type) => {
  const classes = {
    1: 'bg-notion-accent-orange/10 text-notion-accent-orange-deep',
    2: 'bg-notion-accent-sky/10 text-notion-accent-sky',
    3: 'bg-notion-accent-teal/10 text-notion-accent-teal',
  };
  return classes[type] || 'bg-notion-ink-faint/10 text-notion-ink-muted';
};

const loadSchedule = async () => {
  loading.value = true;
  try {
    const res = await api.get('/schedule/my', {
      params: { semester: selectedSemester.value },
    });
    if (res.data.code === 200 && res.data.data) {
      courseList.value = res.data.data;
      // 重置颜色映射
      colorIndex = 0;
      courseColorMap.clear();
    }
  } catch (e) {
    console.error('[Schedule] 加载课表失败:', e);
  } finally {
    loading.value = false;
  }
};

const showCourseDetail = (course) => {
  selectedCourse.value = course;
};

onMounted(() => {
  if (!userStore.isLoggedIn) {
    window.location.href = '/login';
    return;
  }
  loadSchedule();
});
</script>
