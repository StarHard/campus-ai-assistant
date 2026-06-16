<template>
  <div class="h-full flex flex-col bg-white/5 backdrop-blur-sm rounded-2xl m-4 overflow-hidden">
    <!-- 顶部搜索栏 -->
    <div class="p-4 border-b border-white/10">
      <div class="flex items-center gap-3 flex-wrap">
        <input
          v-model="filters.buildingName"
          type="text"
          placeholder="楼栋名称"
          class="px-3 py-2 rounded-lg bg-white/10 text-white placeholder-white/40 text-sm outline-none focus:ring-2 focus:ring-blue-500/40 w-32"
          @keyup.enter="loadClassrooms"
        />
        <select
          v-model="filters.roomType"
          class="px-3 py-2 rounded-lg bg-white/10 text-white text-sm outline-none focus:ring-2 focus:ring-blue-500/40"
        >
          <option :value="null" class="text-gray-800">全部类型</option>
          <option :value="1" class="text-gray-800">普通教室</option>
          <option :value="2" class="text-gray-800">多媒体教室</option>
          <option :value="3" class="text-gray-800">实验室</option>
          <option :value="4" class="text-gray-800">机房</option>
        </select>
        <button @click="loadClassrooms" class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm rounded-lg transition-colors">查询</button>
        <button @click="resetFilters" class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white/80 text-sm rounded-lg transition-colors">重置</button>
        <button @click="showEmptySearch = true" class="px-4 py-2 bg-green-500/80 hover:bg-green-500 text-white text-sm rounded-lg transition-colors">查询空教室</button>
      </div>
    </div>

    <!-- 教室列表 -->
    <div class="flex-1 overflow-y-auto p-4">
      <div v-if="loading" class="flex items-center justify-center h-40">
        <div class="text-white/50">加载中...</div>
      </div>
      <div v-else-if="classrooms.length === 0" class="flex items-center justify-center h-40">
        <div class="text-white/50">暂无教室数据</div>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <div
          v-for="room in classrooms"
          :key="room.id"
          @click="showDetail(room)"
          class="p-4 bg-white/8 hover:bg-white/12 rounded-xl border border-white/10 cursor-pointer transition-all hover:shadow-lg"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <h3 class="text-white font-medium text-sm">{{ room.buildingName }} {{ room.roomName || room.roomNumber }}</h3>
            </div>
            <span class="ml-2 px-2 py-0.5 text-xs rounded-full shrink-0" :class="roomTypeClass(room.roomType)">
              {{ roomTypeLabel(room.roomType) }}
            </span>
          </div>
          <div class="mt-3 flex items-center gap-3 text-xs text-white/60">
            <span>{{ room.capacity }}人</span>
            <span>{{ room.floor }}F</span>
            <span v-if="room.hasProjector === 1" class="text-blue-300">投影</span>
            <span v-if="room.hasAirCond === 1" class="text-cyan-300">空调</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="p-4 border-t border-white/10 flex items-center justify-between">
      <span class="text-white/50 text-xs">共 {{ total }} 条</span>
      <div class="flex items-center gap-2">
        <button @click="goPage(pageNum - 1)" :disabled="pageNum <= 1" class="px-3 py-1.5 text-xs rounded-lg bg-white/10 text-white/70 hover:bg-white/20 disabled:opacity-30 transition-colors">上一页</button>
        <span class="text-white/60 text-xs">{{ pageNum }} / {{ totalPages }}</span>
        <button @click="goPage(pageNum + 1)" :disabled="pageNum >= totalPages" class="px-3 py-1.5 text-xs rounded-lg bg-white/10 text-white/70 hover:bg-white/20 disabled:opacity-30 transition-colors">下一页</button>
      </div>
    </div>

    <!-- 教室详情弹窗 -->
    <div v-if="selectedRoom" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="selectedRoom = null">
      <div class="w-full max-w-md mx-4 bg-gray-900 border border-white/10 rounded-2xl p-6 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-white font-bold text-lg">{{ selectedRoom.buildingName }} {{ selectedRoom.roomName || selectedRoom.roomNumber }}</h2>
          <button @click="selectedRoom = null" class="text-white/50 hover:text-white text-xl">&times;</button>
        </div>
        <div class="space-y-3 text-sm">
          <div class="flex"><span class="text-white/50 w-20">教室编号</span><span class="text-white">{{ selectedRoom.roomNumber }}</span></div>
          <div class="flex"><span class="text-white/50 w-20">所在楼栋</span><span class="text-white">{{ selectedRoom.buildingName }}</span></div>
          <div class="flex"><span class="text-white/50 w-20">楼层</span><span class="text-white">{{ selectedRoom.floor }}F</span></div>
          <div class="flex"><span class="text-white/50 w-20">容纳人数</span><span class="text-white">{{ selectedRoom.capacity }}人</span></div>
          <div class="flex"><span class="text-white/50 w-20">教室类型</span><span class="text-white">{{ roomTypeLabel(selectedRoom.roomType) }}</span></div>
          <div class="flex"><span class="text-white/50 w-20">投影设备</span><span class="text-white">{{ selectedRoom.hasProjector === 1 ? '有' : '无' }}</span></div>
          <div class="flex"><span class="text-white/50 w-20">空调</span><span class="text-white">{{ selectedRoom.hasAirCond === 1 ? '有' : '无' }}</span></div>
          <div v-if="selectedRoom.description" class="flex flex-col gap-1">
            <span class="text-white/50">备注</span>
            <p class="text-white/80 leading-relaxed">{{ selectedRoom.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 空教室查询弹窗 -->
    <div v-if="showEmptySearch" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showEmptySearch = false">
      <div class="w-full max-w-lg mx-4 bg-gray-900 border border-white/10 rounded-2xl p-6 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-white font-bold text-lg">查询空教室</h2>
          <button @click="showEmptySearch = false" class="text-white/50 hover:text-white text-xl">&times;</button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm text-white/60 mb-1">星期</label>
            <select v-model="emptyQuery.weekday" class="w-full px-3 py-2.5 rounded-xl bg-white/10 text-white text-sm outline-none border border-white/10">
              <option :value="1" class="text-gray-800">周一</option>
              <option :value="2" class="text-gray-800">周二</option>
              <option :value="3" class="text-gray-800">周三</option>
              <option :value="4" class="text-gray-800">周四</option>
              <option :value="5" class="text-gray-800">周五</option>
              <option :value="6" class="text-gray-800">周六</option>
              <option :value="7" class="text-gray-800">周日</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm text-white/60 mb-1">开始节次</label>
              <select v-model="emptyQuery.startSection" class="w-full px-3 py-2.5 rounded-xl bg-white/10 text-white text-sm outline-none border border-white/10">
                <option v-for="n in 12" :key="n" :value="n" class="text-gray-800">第{{ n }}节</option>
              </select>
            </div>
            <div>
              <label class="block text-sm text-white/60 mb-1">结束节次</label>
              <select v-model="emptyQuery.endSection" class="w-full px-3 py-2.5 rounded-xl bg-white/10 text-white text-sm outline-none border border-white/10">
                <option v-for="n in 12" :key="n" :value="n" class="text-gray-800">第{{ n }}节</option>
              </select>
            </div>
          </div>
          <button @click="searchEmpty" :disabled="searchingEmpty" class="w-full py-2.5 bg-green-500 hover:bg-green-600 text-white text-sm rounded-xl transition-colors disabled:opacity-50">
            {{ searchingEmpty ? '查询中...' : '查询空教室' }}
          </button>
        </div>

        <!-- 空教室结果 -->
        <div v-if="emptyResults.length > 0" class="mt-4 max-h-60 overflow-y-auto space-y-2">
          <div class="text-xs text-white/40 mb-2">找到 {{ emptyResults.length }} 间空教室</div>
          <div
            v-for="room in emptyResults"
            :key="room.id"
            @click="showDetail(room)"
            class="p-3 bg-white/8 hover:bg-white/12 rounded-lg border border-white/10 cursor-pointer transition-all"
          >
            <div class="flex items-center justify-between">
              <span class="text-white text-sm">{{ room.buildingName }} {{ room.roomName || room.roomNumber }}</span>
              <span class="text-xs text-white/50">{{ room.capacity }}人</span>
            </div>
            <div class="mt-1 flex items-center gap-2 text-xs text-white/40">
              <span>{{ roomTypeLabel(room.roomType) }}</span>
              <span v-if="room.hasProjector === 1">投影</span>
              <span v-if="room.hasAirCond === 1">空调</span>
            </div>
          </div>
        </div>
        <div v-else-if="emptySearched" class="mt-4 text-center text-white/40 text-sm">该时段没有空教室</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { classroomApi } from '../api/classroom';
import { useUserStore } from '../stores/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();
const classrooms = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(12);
const loading = ref(false);
const selectedRoom = ref(null);
const showEmptySearch = ref(false);
const searchingEmpty = ref(false);
const emptySearched = ref(false);
const emptyResults = ref([]);

const filters = reactive({ buildingName: '', roomType: null });
const emptyQuery = reactive({ weekday: 1, startSection: 1, endSection: 2 });

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));

const roomTypeLabel = (type) => ({ 1: '普通教室', 2: '多媒体', 3: '实验室', 4: '机房' }[type] || '其他');
const roomTypeClass = (type) => ({
  1: 'bg-gray-500/20 text-gray-300',
  2: 'bg-blue-500/20 text-blue-300',
  3: 'bg-purple-500/20 text-purple-300',
  4: 'bg-green-500/20 text-green-300',
}[type] || 'bg-gray-500/20 text-gray-300');

const loadClassrooms = async () => {
  if (!userStore.isLoggedIn) { router.push('/login'); return; }
  loading.value = true;
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value };
    if (filters.buildingName) params.buildingName = filters.buildingName;
    if (filters.roomType !== null) params.roomType = filters.roomType;
    const res = await classroomApi.list(params);
    if (res.code === 200 && res.data) {
      classrooms.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (e) { console.error('加载教室失败:', e); }
  finally { loading.value = false; }
};

const searchEmpty = async () => {
  searchingEmpty.value = true;
  emptySearched.value = false;
  try {
    const res = await classroomApi.findEmpty(emptyQuery.weekday, emptyQuery.startSection, emptyQuery.endSection);
    if (res.code === 200 && res.data) {
      emptyResults.value = res.data;
      emptySearched.value = true;
    }
  } catch (e) { console.error('查询空教室失败:', e); }
  finally { searchingEmpty.value = false; }
};

const goPage = (p) => { if (p < 1 || p > totalPages.value) return; pageNum.value = p; loadClassrooms(); };
const resetFilters = () => { filters.buildingName = ''; filters.roomType = null; pageNum.value = 1; loadClassrooms(); };
const showDetail = (room) => { selectedRoom.value = room; };

onMounted(() => { loadClassrooms(); });
</script>
