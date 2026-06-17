<template>
  <div class="px-6 py-8">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="text-notion-ink-muted text-[14px] flex items-center gap-2">
        <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="opacity-25"/><path d="M4 12a8 8 0 018-8" stroke="currentColor" stroke-width="3" class="opacity-75"/></svg>
        正在加载数据...
      </div>
    </div>

    <template v-else>
      <!-- 顶部统计卡片 -->
      <div class="grid grid-cols-4 gap-4 mb-6">
        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-5">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">课程总数</div>
              <div class="text-[28px] font-bold text-notion-ink">{{ overview.courses }}</div>
              <div class="text-[13px] text-notion-accent-green mt-1">{{ overview.coursesByType[1] || 0 }} 必修 / {{ overview.coursesByType[2] || 0 }} 选修</div>
            </div>
            <div class="w-12 h-12 rounded-notion-md bg-notion-primary/10 flex items-center justify-center">
              <svg class="w-6 h-6 text-notion-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
            </div>
          </div>
        </div>

        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-5">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">教室总数</div>
              <div class="text-[28px] font-bold text-notion-ink">{{ overview.classrooms }}</div>
              <div class="text-[13px] text-notion-ink-muted mt-1">{{ Object.keys(overview.deptMap || {}).length }} 个院系</div>
            </div>
            <div class="w-12 h-12 rounded-notion-md bg-notion-accent-purple/15 flex items-center justify-center">
              <svg class="w-6 h-6 text-notion-accent-purple-deep" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
            </div>
          </div>
        </div>

        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-5">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">知识库文档</div>
              <div class="text-[28px] font-bold text-notion-ink">{{ overview.kbDocs }}</div>
              <div class="text-[13px] text-notion-ink-muted mt-1">{{ overview.kbChunks }} 个文本块</div>
            </div>
            <div class="w-12 h-12 rounded-notion-md bg-notion-accent-orange/10 flex items-center justify-center">
              <svg class="w-6 h-6 text-notion-accent-orange" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            </div>
          </div>
        </div>

        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-5">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-[12px] text-notion-ink-faint uppercase tracking-wide mb-1">总学分</div>
              <div class="text-[28px] font-bold text-notion-ink">{{ overview.totalCredits }}</div>
              <div class="text-[13px] text-notion-ink-muted mt-1">{{ overview.totalHours }} 总学时</div>
            </div>
            <div class="w-12 h-12 rounded-notion-md bg-notion-accent-teal/10 flex items-center justify-center">
              <svg class="w-6 h-6 text-notion-accent-teal" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 2v9a2 2 0 01-2 2H7a2 2 0 01-2-2V9a2 2 0 012-2h6zm-3 2v10h4V9H8z"/></svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-2 gap-6 mb-6">
        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
          <h3 class="text-[16px] font-semibold text-notion-ink mb-4">课程类型分布</h3>
          <div ref="courseTypeChartRef" style="width: 100%; height: 280px;"></div>
        </div>

        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
          <h3 class="text-[16px] font-semibold text-notion-ink mb-4">各院系课程数</h3>
          <div ref="deptChartRef" style="width: 100%; height: 280px;"></div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-6 mb-6">
        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
          <h3 class="text-[16px] font-semibold text-notion-ink mb-4">每周课程分布（基于排课时间）</h3>
          <div ref="weekChartRef" style="width: 100%; height: 280px;"></div>
        </div>

        <div class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
          <h3 class="text-[16px] font-semibold text-notion-ink mb-4">考试类型分布</h3>
          <div ref="examTypeChartRef" style="width: 100%; height: 280px;"></div>
        </div>
      </div>

      <!-- 底部：知识库信息 -->
      <div v-if="overview.kbStatus" class="bg-notion-canvas rounded-notion-lg border border-notion-hairline shadow-notion-1 p-6">
        <h3 class="text-[16px] font-semibold text-notion-ink mb-4">知识库详情</h3>
        <div class="grid grid-cols-3 gap-4">
          <div class="p-4 bg-notion-canvas-soft rounded-notion-md">
            <div class="text-[12px] text-notion-ink-faint mb-1">索引路径</div>
            <div class="text-[13px] text-notion-ink-secondary break-all">{{ overview.kbStatus.indexPath || '-' }}</div>
          </div>
          <div class="p-4 bg-notion-canvas-soft rounded-notion-md">
            <div class="text-[12px] text-notion-ink-faint mb-1">文档目录</div>
            <div class="text-[13px] text-notion-ink-secondary break-all">{{ overview.kbStatus.kbPath || '-' }}</div>
          </div>
          <div class="p-4 bg-notion-canvas-soft rounded-notion-md">
            <div class="text-[12px] text-notion-ink-faint mb-1">检索模式</div>
            <div class="text-[13px] text-notion-ink-secondary">{{ overview.kbStatus.retrievalMode || 'Lucene全文检索' }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import api from '../api/chat';

const courseTypeChartRef = ref(null);
const deptChartRef = ref(null);
const weekChartRef = ref(null);
const examTypeChartRef = ref(null);

let chartInstances = [];
const loading = ref(true);

// 概览数据 - 全部从API获取
const overview = ref({
  courses: 0,
  classrooms: 0,
  kbDocs: 0,
  kbChunks: 0,
  totalCredits: 0,
  totalHours: 0,
  coursesByType: {},   // { 1: 必修数, 2: 选修数, 3: 实践数 }
  deptMap: {},         // { '计算机学院': 15, ... }
  kbStatus: null,
});

// 原始课程列表，用于图表渲染
const courseList = ref([]);

// Notion风格配色
const notionColors = {
  primary: '#0075de',
  sky: '#62aef0',
  purple: '#d6b6f6',
  pink: '#ff64c8',
  orange: '#dd5b00',
  teal: '#2a9d99',
  green: '#1aae39',
};

// 课程类型映射
const courseTypeNames = { 1: '必修课', 2: '选修课', 3: '实践课' };
// 考试类型映射
const examTypeNames = { 1: '闭卷', 2: '开卷', 3: '论文', 4: '实操' };

// ==================== 数据加载 ====================

const loadAllData = async () => {
  loading.value = true;
  try {
    // 并行请求：课程、教室、知识库统计
    const [courseRes, classroomRes, ragRes] = await Promise.allSettled([
      api.get('/course/list', { params: { pageNum: 1, pageSize: 500 } }),
      api.get('/classroom/list', { params: { pageNum: 1, pageSize: 200 } }),
      api.get('/rag/stats'),
    ]);

    // 处理课程数据
    if (courseRes.status === 'fulfilled' && courseRes.value?.data?.code === 200) {
      const pageData = courseRes.value.data.data;
      const courses = pageData?.records || pageData || [];
      courseList.value = Array.isArray(courses) ? courses : [];

      overview.value.courses = pageData?.total ?? courseList.value.length;

      // 统计课程类型分布
      const typeCount = {};
      let totalCredits = 0;
      let totalHours = 0;
      const deptMap = {};

      courseList.value.forEach(c => {
        const t = c.courseType;
        if (t != null) typeCount[t] = (typeCount[t] || 0) + 1;
        totalCredits += parseFloat(c.credit) || 0;
        totalHours += c.courseHours || 0;
        if (c.department) {
          deptMap[c.department] = (deptMap[c.department] || 0) + 1;
        }
      });

      overview.value.coursesByType = typeCount;
      overview.value.totalCredits = totalCredits.toFixed(1);
      overview.value.totalHours = totalHours;
      overview.value.deptMap = deptMap;
    }

    // 处理教室数据
    if (classroomRes.status === 'fulfilled' && classroomRes.value?.data?.code === 200) {
      const pageData = classroomRes.value.data.data;
      overview.value.classrooms = pageData?.total ?? (Array.isArray(pageData) ? pageData.length : 0);
    }

    // 处理知识库统计数据
    if (ragRes.status === 'fulfilled' && ragRes.value?.data?.code === 200) {
      const data = ragRes.value.data.data || {};
      overview.value.kbDocs = data.documentCount ?? 0;
      overview.value.kbChunks = data.totalChunks ?? 0;
      overview.value.kbStatus = data;
    }
  } catch (e) {
    console.error('[Dashboard] 数据加载失败:', e);
  } finally {
    loading.value = false;
    // 数据加载完成后初始化图表
    setTimeout(() => initAllCharts(), 50);
  }
};

// ==================== 图表初始化 ====================

const initAllCharts = () => {
  initCourseTypeChart();
  initDeptChart();
  initWeekChart();
  initExamTypeChart();
};

// 课程类型饼图
const initCourseTypeChart = () => {
  if (!courseTypeChartRef.value) return;
  const chart = echarts.init(courseTypeChartRef.value);
  const typeData = overview.value.coursesByType || {};
  const data = Object.entries(typeData).map(([k, v]) => ({
    value: v,
    name: courseTypeNames[k] || `类型${k}`,
  }));
  if (data.length === 0) data.push({ value: 0, name: '暂无数据' });

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}门 ({d}%)' },
    legend: { bottom: '0', textStyle: { fontSize: 12, color: '#615d59' }, itemWidth: 12, itemHeight: 12 },
    color: [notionColors.primary, notionColors.orange, notionColors.teal],
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data,
    }],
  });
  chartInstances.push(chart);
};

// 院系柱状图
const initDeptChart = () => {
  if (!deptChartRef.value) return;
  const chart = echarts.init(deptChartRef.value);
  const deptMap = overview.value.deptMap || {};
  const entries = Object.entries(deptMap).sort((a, b) => b[1] - a[1]);
  const names = entries.map(([k]) => k);
  const values = entries.map(([, v]) => v);

  if (names.length === 0) { names.push('暂无'); values.push(0); }

  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 门课程' },
    grid: { left: '3%', right: '4%', bottom: '10%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { fontSize: 11, color: '#a39e98', rotate: names.length > 5 ? 20 : 0 },
      axisLine: { lineStyle: { color: '#e6e6e6' } },
    },
    yAxis: { type: 'value', axisLabel: { fontSize: 11, color: '#a39e98' }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    color: [notionColors.primary],
    series: [{
      type: 'bar',
      barWidth: names.length > 6 ? '55%' : '40%',
      itemStyle: { borderRadius: [4, 4, 0, 0] },
      data: values,
    }],
  });
  chartInstances.push(chart);
};

// 周课表热力图 - 从课程的scheduleTime字段解析
const initWeekChart = () => {
  if (!weekChartRef.value) return;
  const chart = echarts.init(weekChartRef.value);
  const days = ['周一', '周二', '周三', '周四', '周五'];
  const sections = ['第1-2节', '第3-4节', '第5-6节', '第7-8节', '第9-10节'];
  const dayMap = { '一': 0, '二': 1, '三': 2, '四': 3, '五': 4 };

  // 从真实课程数据中解析scheduleTime
  const heatMatrix = Array.from({ length: 5 }, () => Array(5).fill(0));
  let maxVal = 1;

  courseList.value.forEach(c => {
    const st = c.scheduleTime;
    if (!st) return;
    const dayMatch = st.match(/周([一二三四五六日])|星期([一二三四五六日])/);
    const secMatch = st.match(/(\d+)-(\d+)/);
    if (dayMatch && secMatch) {
      const dayStr = dayMatch[1] || dayMatch[2];
      const dayIdx = dayMap[dayStr];
      if (dayIdx != null) {
        const startSec = parseInt(secMatch[1]);
        // 映射到5大节：1-2→0, 3-4→1, 5-6→2, 7-8→3, 9-10→4
        const secIdx = Math.min(4, Math.floor((startSec - 1) / 2));
        heatMatrix[dayIdx][secIdx]++;
        maxVal = Math.max(maxVal, heatMatrix[dayIdx][secIdx]);
      }
    }
  });

  // 转换为Echarts热力图格式
  const data = [];
  for (let d = 0; d < 5; d++) {
    for (let s = 0; s < 5; s++) {
      data.push([d, s, heatMatrix[d][s]]);
    }
  }

  chart.setOption({
    tooltip: { position: 'top', formatter: (p) => `${days[p.data[0]]} ${sections[p.data[1]]}: ${p.data[2]}门课` },
    grid: { left: '12%', right: '8%', bottom: '15%', top: '5%' },
    xAxis: { type: 'category', data: days, splitArea: { show: true }, axisLabel: { fontSize: 12, color: '#615d59' }, axisLine: { lineStyle: { color: '#e6e6e6' } } },
    yAxis: { type: 'category', data: sections, splitArea: { show: true }, axisLabel: { fontSize: 11, color: '#615d59' }, axisLine: { lineStyle: { color: '#e6e6e6' } } },
    visualMap: {
      min: 0,
      max: maxVal,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      inRange: { color: ['#f6f5f4', '#d6b6f6', '#62aef0', '#0075de'] },
      textStyle: { fontSize: 11, color: '#a39e98' },
    },
    series: [{
      type: 'heatmap',
      data,
      label: { show: true, fontSize: 11, color: '#000' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,117,222,0.3)' } },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 1 },
    }],
  });
  chartInstances.push(chart);
};

// 考试类型饼图
const initExamTypeChart = () => {
  if (!examTypeChartRef.value) return;
  const chart = echarts.init(examTypeChartRef.value);

  const examCount = {};
  courseList.value.forEach(c => {
    if (c.examType != null) {
      examCount[c.examType] = (examCount[c.examType] || 0) + 1;
    }
  });

  const data = Object.entries(examCount).map(([k, v]) => ({
    value: v,
    name: examTypeNames[k] || `类型${k}`,
  }));
  if (data.length === 0) data.push({ value: 0, name: '暂无数据' });

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}门 ({d}%)' },
    legend: { bottom: '0', textStyle: { fontSize: 12, color: '#615d59' } },
    color: [notionColors.purple, notionColors.sky, notionColors.green, notionColors.orange],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%', fontSize: 12, color: '#31302e' },
      data,
    }],
  });
  chartInstances.push(chart);
};

const handleResize = () => {
  chartInstances.forEach(c => c.resize());
};

onMounted(() => {
  loadAllData();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstances.forEach(c => c.dispose());
});
</script>
