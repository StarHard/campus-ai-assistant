<template>
  <div class="h-full bg-notion-canvas-soft p-6 overflow-y-auto">
    <!-- 筛选栏 -->
    <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline p-4 mb-4">
      <div class="flex items-center gap-3 flex-wrap">
        <input
          v-model="filters.department"
          type="text"
          placeholder="院系筛选"
          class="px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[14px] text-notion-ink outline-none focus:border-notion-primary transition-colors placeholder:text-notion-ink-faint w-36"
          @keyup.enter="loadCourses"
        />
        <input
          v-model="filters.semester"
          type="text"
          placeholder="学期 (如 2025-2026-1)"
          class="px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[14px] text-notion-ink outline-none focus:border-notion-primary transition-colors placeholder:text-notion-ink-faint w-44"
          @keyup.enter="loadCourses"
        />
        <select
          v-model="filters.courseType"
          class="px-3 py-2 bg-notion-canvas border border-notion-hairline rounded-notion-xs text-[14px] text-notion-ink outline-none focus:border-notion-primary transition-colors"
        >
          <option :value="null">全部类型</option>
          <option :value="1">必修</option>
          <option :value="2">选修</option>
          <option :value="3">公选</option>
        </select>
        <button
          @click="loadCourses"
          class="px-4 py-2 bg-notion-primary text-white text-[14px] rounded-notion-md hover:bg-notion-primary-active transition-colors"
        >
          查询
        </button>
        <button
          @click="resetFilters"
          class="px-4 py-2 bg-notion-canvas text-notion-ink-muted text-[14px] rounded-notion-md border border-notion-hairline hover:bg-notion-canvas-soft transition-colors"
        >
          重置
        </button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div v-if="loading" class="flex items-center justify-center h-40">
      <div class="text-notion-ink-faint text-[14px]">加载中...</div>
    </div>

    <div v-else-if="courses.length === 0" class="flex items-center justify-center h-40">
      <div class="text-notion-ink-faint text-[14px]">暂无课程数据</div>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
      <div
        v-for="course in courses"
        :key="course.id"
        @click="goDetail(course)"
        class="bg-notion-canvas border border-notion-hairline rounded-notion-lg p-4 hover:shadow-notion-1 cursor-pointer transition-shadow"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1 min-w-0">
            <h3 class="text-notion-ink font-medium text-[14px] truncate">{{ course.courseName }}</h3>
            <p class="text-notion-ink-faint text-[13px] mt-1">{{ course.courseCode }}</p>
          </div>
          <span
            class="ml-2 px-2 py-0.5 text-[12px] rounded-notion-full shrink-0"
            :class="courseTypeClass(course.courseType)"
          >
            {{ courseTypeLabel(course.courseType) }}
          </span>
        </div>
        <div class="mt-3 flex items-center gap-4 text-[13px] text-notion-ink-muted">
          <span>{{ course.teacherName || '未指定' }}</span>
          <span>{{ course.department || '-' }}</span>
          <span>{{ course.credit }} 学分</span>
          <span>{{ course.courseHours }} 学时</span>
        </div>
        <div class="mt-2 text-[12px] text-notion-ink-faint">
          {{ course.semester || '-' }} · {{ examTypeLabel(course.examType) }}
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="mt-4 flex items-center justify-between">
      <span class="text-notion-ink-faint text-[13px]">共 {{ total }} 条</span>
      <div class="flex items-center gap-2">
        <button
          @click="goPage(pageNum - 1)"
          :disabled="pageNum <= 1"
          class="px-3 py-1.5 text-[13px] border border-notion-hairline rounded-notion-md hover:bg-notion-canvas-soft text-notion-ink-muted disabled:opacity-30 transition-colors"
        >
          上一页
        </button>
        <span class="text-notion-ink-muted text-[13px]">{{ pageNum }} / {{ totalPages }}</span>
        <button
          @click="goPage(pageNum + 1)"
          :disabled="pageNum >= totalPages"
          class="px-3 py-1.5 text-[13px] border border-notion-hairline rounded-notion-md hover:bg-notion-canvas-soft text-notion-ink-muted disabled:opacity-30 transition-colors"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { courseApi } from '../api/course';
import { useUserStore } from '../stores/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();
const courses = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(12);
const loading = ref(false);

const filters = reactive({
  department: '',
  semester: '',
  courseType: null,
});

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));

const courseTypeLabel = (type) => {
  const map = { 1: '必修', 2: '选修', 3: '公选' };
  return map[type] || '其他';
};

const courseTypeClass = (type) => {
  const map = {
    1: 'bg-notion-accent-orange/10 text-notion-accent-orange',
    2: 'bg-notion-accent-sky/10 text-notion-accent-sky',
    3: 'bg-notion-accent-green/10 text-notion-accent-green',
  };
  return map[type] || 'bg-notion-ink-faint/10 text-notion-ink-faint';
};

const examTypeLabel = (type) => {
  const map = { 1: '考试', 2: '考查', 3: '其他' };
  return map[type] || '-';
};

const loadCourses = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login');
    return;
  }
  loading.value = true;
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    };
    if (filters.department) params.department = filters.department;
    if (filters.semester) params.semester = filters.semester;
    if (filters.courseType !== null) params.courseType = filters.courseType;

    const res = await courseApi.list(params);
    if (res.code === 200 && res.data) {
      courses.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (e) {
    console.error('加载课程失败:', e);
  } finally {
    loading.value = false;
  }
};

const goPage = (p) => {
  if (p < 1 || p > totalPages.value) return;
  pageNum.value = p;
  loadCourses();
};

const resetFilters = () => {
  filters.department = '';
  filters.semester = '';
  filters.courseType = null;
  pageNum.value = 1;
  loadCourses();
};

const goDetail = (course) => {
  router.push(`/courses/${course.id}`);
};

onMounted(() => {
  loadCourses();
});
</script>
