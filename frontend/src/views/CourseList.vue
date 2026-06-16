<template>
  <div class="h-full flex flex-col bg-white/5 backdrop-blur-sm rounded-2xl m-4 overflow-hidden">
    <!-- 顶部搜索栏 -->
    <div class="p-4 border-b border-white/10">
      <div class="flex items-center gap-3 flex-wrap">
        <input
          v-model="filters.department"
          type="text"
          placeholder="院系筛选"
          class="px-3 py-2 rounded-lg bg-white/10 text-white placeholder-white/40 text-sm outline-none focus:ring-2 focus:ring-blue-500/40 w-36"
          @keyup.enter="loadCourses"
        />
        <input
          v-model="filters.semester"
          type="text"
          placeholder="学期 (如 2025-2026-1)"
          class="px-3 py-2 rounded-lg bg-white/10 text-white placeholder-white/40 text-sm outline-none focus:ring-2 focus:ring-blue-500/40 w-44"
          @keyup.enter="loadCourses"
        />
        <select
          v-model="filters.courseType"
          class="px-3 py-2 rounded-lg bg-white/10 text-white text-sm outline-none focus:ring-2 focus:ring-blue-500/40"
        >
          <option :value="null" class="text-gray-800">全部类型</option>
          <option :value="1" class="text-gray-800">必修</option>
          <option :value="2" class="text-gray-800">选修</option>
          <option :value="3" class="text-gray-800">公选</option>
        </select>
        <button
          @click="loadCourses"
          class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm rounded-lg transition-colors"
        >
          查询
        </button>
        <button
          @click="resetFilters"
          class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white/80 text-sm rounded-lg transition-colors"
        >
          重置
        </button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="flex-1 overflow-y-auto p-4">
      <div v-if="loading" class="flex items-center justify-center h-40">
        <div class="text-white/50">加载中...</div>
      </div>

      <div v-else-if="courses.length === 0" class="flex items-center justify-center h-40">
        <div class="text-white/50">暂无课程数据</div>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div
          v-for="course in courses"
          :key="course.id"
          @click="goDetail(course)"
          class="p-4 bg-white/8 hover:bg-white/12 rounded-xl border border-white/10 cursor-pointer transition-all hover:shadow-lg"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <h3 class="text-white font-medium text-sm truncate">{{ course.courseName }}</h3>
              <p class="text-white/50 text-xs mt-1">{{ course.courseCode }}</p>
            </div>
            <span
              class="ml-2 px-2 py-0.5 text-xs rounded-full shrink-0"
              :class="courseTypeClass(course.courseType)"
            >
              {{ courseTypeLabel(course.courseType) }}
            </span>
          </div>
          <div class="mt-3 flex items-center gap-4 text-xs text-white/60">
            <span>{{ course.teacherName || '未指定' }}</span>
            <span>{{ course.department || '-' }}</span>
            <span>{{ course.credit }} 学分</span>
            <span>{{ course.courseHours }} 学时</span>
          </div>
          <div class="mt-2 text-xs text-white/40">
            {{ course.semester || '-' }} · {{ examTypeLabel(course.examType) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="p-4 border-t border-white/10 flex items-center justify-between">
      <span class="text-white/50 text-xs">共 {{ total }} 条</span>
      <div class="flex items-center gap-2">
        <button
          @click="goPage(pageNum - 1)"
          :disabled="pageNum <= 1"
          class="px-3 py-1.5 text-xs rounded-lg bg-white/10 text-white/70 hover:bg-white/20 disabled:opacity-30 transition-colors"
        >
          上一页
        </button>
        <span class="text-white/60 text-xs">{{ pageNum }} / {{ totalPages }}</span>
        <button
          @click="goPage(pageNum + 1)"
          :disabled="pageNum >= totalPages"
          class="px-3 py-1.5 text-xs rounded-lg bg-white/10 text-white/70 hover:bg-white/20 disabled:opacity-30 transition-colors"
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
    1: 'bg-red-500/20 text-red-300',
    2: 'bg-blue-500/20 text-blue-300',
    3: 'bg-green-500/20 text-green-300',
  };
  return map[type] || 'bg-gray-500/20 text-gray-300';
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
