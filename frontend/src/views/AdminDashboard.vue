<template>
  <div class="admin-dashboard">
    <h2>管理员面板</h2>

    <div class="cards">
      <div class="card">
        <h3>预订统计</h3>
        <div v-if="loadingStats">加载中...</div>
        <div v-else>
          <div>总订单: {{ stats.totalBookings || 0 }}</div>
          <div>已支付: {{ stats.paidBookings || 0 }}</div>
          <div>
            待支付:
            {{ stats.pendingBookings || stats.pendingPaymentBookings || 0 }}
          </div>
          <div>
            已取消: {{ stats.cancelledBookings || stats.canceledBookings || 0 }}
          </div>
          <div>总收入: {{ stats.totalRevenue || 0 }}</div>
        </div>
      </div>

      <div class="card">
        <h3>简要报表 (示例)</h3>
        <div class="date-range-picker">
          <input type="date" v-model="reportStartDate" />
          <span>至</span>
          <input type="date" v-model="reportEndDate" />
          <button class="btn" @click="loadReport">更新</button>
        </div>
        <div v-if="loadingReport">加载中...</div>
        <div v-else>
          <div>总收入: {{ report.totalRevenue.toFixed(2) || 0 }}</div>
          <div>总订单数: {{ report.totalBookings || 0 }}</div>
          <div>日均收入: {{ report.averageDailyRevenue.toFixed(2) || 0 }}</div>
        </div>
      </div>

      <div class="card">
        <h3>数据维护</h3>
        <button class="btn" :disabled="resetLoading" @click="onResetResources">
          {{ resetLoading ? "重置中..." : "一键重置房型/营位/装备" }}
        </button>
        <div class="hint">重置后会写入默认房型与装备并清空定价</div>
      </div>
    </div>

    <!-- 全量订单列表 -->
    <section class="block">
      <header class="block__title bookings-header">
        <span>订单列表</span>
        <div class="filter-tabs">
          <button
            :class="{ active: statusFilter === null }"
            @click="statusFilter = null"
          >
            全部
          </button>
          <button
            :class="{ active: statusFilter === 0 }"
            @click="statusFilter = 0"
          >
            待支付
          </button>
          <button
            :class="{ active: statusFilter === 1 }"
            @click="statusFilter = 1"
          >
            已完成
          </button>
          <button
            :class="{ active: statusFilter === 2 }"
            @click="statusFilter = 2"
          >
            已取消
          </button>
        </div>
      </header>

      <div v-if="loadingBookings" class="empty">加载中...</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>房型</th>
              <th>营位号</th>
              <th>日期</th>
              <th>联系人</th>
              <th>电话</th>
              <th>装备</th>
              <th>总价</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in filteredBookings" :key="b.bookingId">
              <td>{{ b.bookingId }}</td>
              <td>{{ b.userName || b.userId }}</td>
              <td>{{ b.typeName || "-" }}</td>
              <td>{{ b.siteNo || "-" }}</td>
              <td>{{ b.checkIn }} ~ {{ b.checkOut }}</td>
              <td>{{ b.guestName }}</td>
              <td>{{ b.guestPhone }}</td>
              <td>{{ b.equipments || "-" }}</td>
              <td>￥{{ formatPrice(b.totalPrice) }}</td>
              <td>{{ getStatusText(b.status) }}</td>
              <td>
                <button
                  class="btn btn-danger"
                  :disabled="b.status === 2 || endLoading"
                  @click="endBooking(b.bookingId)"
                >
                  结束订单
                </button>
              </td>
            </tr>
            <tr v-if="!filteredBookings.length">
              <td colspan="10" class="empty">暂无订单</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 房型管理 -->
    <section class="block">
      <header class="block__title">房型管理</header>

      <!-- 新增房型表单 -->
      <div class="form-grid">
        <div class="form-item">
          <span class="label">房型名称</span>
          <input v-model="newType.typeName" placeholder="房型名称" />
        </div>
        <div class="form-item">
          <span class="label">基础价</span>
          <input
            v-model.number="newType.basePrice"
            type="number"
            min="0"
            placeholder="基础价"
          />
        </div>
        <div class="form-item">
          <span class="label">可住人数</span>
          <input
            v-model.number="newType.maxGuests"
            type="number"
            min="1"
            placeholder="可住人数"
          />
        </div>
        <div class="form-item">
          <span class="label">图片地址</span>
          <input v-model="newType.imageUrl" placeholder="图片地址(可选)" />
        </div>
        <div class="form-item">
          <span class="label">描述</span>
          <input v-model="newType.description" placeholder="描述(可选)" />
        </div>
        <button class="btn" @click="createSiteType">新增房型</button>
      </div>

      <!-- 房型列表/编辑 -->
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名称</th>
              <th>基础价</th>
              <th>可住人数</th>
              <th>总数</th>
              <th>图片</th>
              <th>描述</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in types" :key="t.typeId">
              <td>{{ t.typeId }}</td>
              <template v-if="editingTypeId === t.typeId">
                <td><input v-model="editType.typeName" /></td>
                <td>
                  <input
                    v-model.number="editType.basePrice"
                    type="number"
                    min="0"
                  />
                </td>
                <td>
                  <input
                    v-model.number="editType.maxGuests"
                    type="number"
                    min="1"
                  />
                </td>
                <td>
                  <button class="btn" @click="openSiteManager(t)">
                    管理营位
                  </button>
                </td>
                <td><input v-model="editType.imageUrl" /></td>
                <td><input v-model="editType.description" /></td>
                <td>
                  <button class="btn" @click="saveEditType">保存</button>
                  <button class="btn btn-ghost" @click="cancelEditType">
                    取消
                  </button>
                </td>
              </template>
              <template v-else>
                <td>{{ t.typeName }}</td>
                <td>￥{{ formatPrice(t.basePrice) }}</td>
                <td>{{ t.maxGuests }}</td>
                <td>{{ t.totalSites || "-" }}</td>
                <td>
                  <img
                    v-if="t.imageUrl"
                    :src="t.imageUrl"
                    alt="img"
                    style="
                      width: 50px;
                      height: 50px;
                      object-fit: cover;
                      cursor: pointer;
                    "
                    @click="showImagePreview(t.imageUrl)"
                  />
                  <span v-else>-</span>
                </td>
                <td>{{ t.description || "-" }}</td>
                <td>
                  <button class="btn" @click="startEditType(t)">编辑</button>
                  <button
                    class="btn btn-danger"
                    @click="deleteSiteType(t.typeId)"
                  >
                    删除
                  </button>
                </td>
              </template>
            </tr>
            <tr v-if="!types.length">
              <td colspan="5" class="empty">暂无房型</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 装备管理 -->
    <section class="block">
      <header class="block__title">装备管理</header>

      <!-- 新增装备表单 -->
      <div class="form-grid">
        <div class="form-item">
          <span class="label">装备名称</span>
          <input v-model="newEquip.equipName" placeholder="装备名称" />
        </div>
        <div class="form-item">
          <span class="label">单价</span>
          <input
            v-model.number="newEquip.unitPrice"
            type="number"
            min="0"
            placeholder="单价"
          />
        </div>
        <div class="form-item">
          <span class="label">总库存</span>
          <input
            v-model.number="newEquip.totalStock"
            type="number"
            min="0"
            placeholder="总库存"
          />
        </div>
        <div class="form-item">
          <span class="label">分类</span>
          <input v-model="newEquip.category" placeholder="分类(可选)" />
        </div>
        <div class="form-item">
          <span class="label">描述</span>
          <input v-model="newEquip.description" placeholder="描述(可选)" />
        </div>
        <button class="btn" @click="createEquipment">新增装备</button>
      </div>

      <!-- 装备列表/编辑 -->
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名称</th>
              <th>单价</th>
              <th>总库存</th>
              <th>分类</th>
              <th>描述</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in equipments" :key="e.equipId">
              <td>{{ e.equipId }}</td>
              <template v-if="editingEquipId === e.equipId">
                <td><input v-model="editEquip.equipName" /></td>
                <td>
                  <input
                    v-model.number="editEquip.unitPrice"
                    type="number"
                    min="0"
                  />
                </td>
                <td>
                  <input
                    v-model.number="editEquip.totalStock"
                    type="number"
                    min="0"
                  />
                </td>
                <td><input v-model="editEquip.category" /></td>
                <td><input v-model="editEquip.description" /></td>
                <td>
                  <button class="btn" @click="saveEditEquip">保存</button>
                  <button class="btn btn-ghost" @click="cancelEditEquip">
                    取消
                  </button>
                </td>
              </template>
              <template v-else>
                <td>{{ e.equipName }}</td>
                <td>￥{{ formatPrice(e.unitPrice) }}</td>
                <td>{{ e.totalStock ?? "-" }}</td>
                <td>{{ e.category || "-" }}</td>
                <td>{{ e.description || "-" }}</td>
                <td>
                  <button class="btn" @click="startEditEquip(e)">编辑</button>
                  <button
                    class="btn btn-danger"
                    @click="deleteEquipment(e.equipId)"
                  >
                    删除
                  </button>
                </td>
              </template>
            </tr>
            <tr v-if="!equipments.length">
              <td colspan="6" class="empty">暂无装备</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 营位管理弹窗 -->
    <div v-if="showSiteManager" class="modal-mask">
      <div class="modal-container">
        <div class="modal-header">
          <h3>管理营位 (房型ID: {{ currentManageTypeId }})</h3>
          <button class="btn-close" @click="closeSiteManager">×</button>
        </div>
        <div class="modal-body">
          <div class="site-form">
            <input v-model="newSiteNo" placeholder="输入新营位编号" />
            <button class="btn" @click="addSite">添加营位</button>
          </div>
          <div class="site-list-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>编号</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in siteList" :key="s.siteId">
                  <td>{{ s.siteId }}</td>
                  <td>{{ s.siteNo }}</td>
                  <td>{{ getSiteStatusText(s.status) }}</td>
                  <td>
                    <button class="btn btn-danger" @click="removeSite(s.siteId)">
                      删除
                    </button>
                  </td>
                </tr>
                <tr v-if="!siteList.length">
                  <td colspan="4" class="empty">暂无营位数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <div v-if="previewImage" class="image-modal" @click="closeImagePreview">
      <div class="image-modal-content" @click.stop>
        <img :src="previewImage" alt="Preview" />
        <button class="close-btn" @click="closeImagePreview">×</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { adminApi, resourceApi, bookingApi } from "@/api";
import type { SiteType, Equipment } from "@/api";

const stats = ref<any>({});
const report = ref<any>({});
const loadingStats = ref(true);
const loadingReport = ref(true);

// 报表日期范围
const today = new Date();
const start = new Date(today.getTime() - 7 * 24 * 3600 * 1000);
const fmt = (d: Date) => d.toISOString().slice(0, 10);
const reportStartDate = ref(fmt(start));
const reportEndDate = ref(fmt(today));

// 全部订单
const bookings = ref<any[]>([]);
const loadingBookings = ref(true);
const statusFilter = ref<number | null>(null);
const endLoading = ref(false);
const resetLoading = ref(false);
const filteredBookings = computed(() => {
  if (statusFilter.value === null) return bookings.value;
  return bookings.value.filter((b) => b.status === statusFilter.value);
});

// 房型与装备列表
const types = ref<SiteType[]>([]);
const equipments = ref<Equipment[]>([]);
const loadingTypes = ref<boolean>(true);
const loadingEquips = ref<boolean>(true);

// 新增房型/装备表单模型
const newType = ref({
  typeName: "",
  basePrice: 0,
  maxGuests: 1,
  description: "",
  imageUrl: "",
});
const newEquip = ref({
  equipName: "",
  unitPrice: 0,
  totalStock: 0,
  category: "",
  description: "",
});

// 编辑状态
const editingTypeId = ref<number | null>(null);
const editType = ref<any>({});
const editingEquipId = ref<number | null>(null);
const editEquip = ref<any>({});

// 房屋管理
const showSiteManager = ref(false);
const currentManageTypeId = ref<number | null>(null);
const siteList = ref<any[]>([]);
const newSiteNo = ref("");

// 图片预览
const previewImage = ref<string | null>(null);

function showImagePreview(url: string) {
  if (url) previewImage.value = url;
}

function closeImagePreview() {
  previewImage.value = null;
}

onMounted(async () => {
  await Promise.all([loadStats(), loadReport()]);
  loadTypes();
  loadEquipments();
  loadBookings();
});

async function onResetResources() {
  if (!confirm("确认重置房型、营位与装备到默认数据？")) return;
  resetLoading.value = true;
  try {
    await adminApi.resetResources();
    await Promise.all([
      loadStats(),
      loadReport(),
      loadTypes(),
      loadEquipments(),
      loadBookings(),
    ]);
  } catch (e: any) {
    alert(e?.message || "重置失败");
  } finally {
    resetLoading.value = false;
  }
}

async function loadStats() {
  loadingStats.value = true;
  try {
    const s: any = await adminApi.getBookingStats();
    stats.value = (s && s.data) || {};
  } catch (e) {
    stats.value = {};
  } finally {
    loadingStats.value = false;
  }
}

async function loadReport() {
  loadingReport.value = true;
  try {
    const r: any = await adminApi.getDailyReport(
      reportStartDate.value,
      reportEndDate.value
    );
    
    const list = r.data || [];
    let totalRevenue = 0;
    let totalBookings = 0;

    for (const d of list) {
      totalRevenue += Number(d.revenue || 0);
      totalBookings += Number(d.bookingCount || 0);
    }

    report.value = {
      totalRevenue,
      totalBookings,
      averageDailyRevenue: list.length > 0 ? totalRevenue / list.length : 0
    };
    
  } catch (e) {
    report.value = {};
  } finally {
    loadingReport.value = false;
  }
}

async function loadBookings() {
  loadingBookings.value = true;
  try {
    const res: any = await bookingApi.getAll();
    bookings.value = res?.data || [];
  } catch (e) {
    bookings.value = [];
  } finally {
    loadingBookings.value = false;
  }
}

async function endBooking(bookingId: number) {
  if (!confirm("确定结束该订单并释放资源？")) return;
  endLoading.value = true;
  try {
    await bookingApi.end(bookingId);
    await loadBookings();
  } catch (e: any) {
    alert(e?.message || "结束失败");
  } finally {
    endLoading.value = false;
  }
}

// ---------- 加载列表 ----------
async function loadTypes() {
  loadingTypes.value = true;
  try {
    const res: any = await resourceApi.getSiteTypes();
    types.value = res?.data || [];
  } catch {
    types.value = [];
  } finally {
    loadingTypes.value = false;
  }
}

async function loadEquipments() {
  loadingEquips.value = true;
  try {
    const res: any = await resourceApi.getEquipments();
    equipments.value = res?.data || [];
    // Sort by ID ascending
    equipments.value.sort((a, b) => (a.equipId || 0) - (b.equipId || 0));
  } catch {
    equipments.value = [];
  } finally {
    loadingEquips.value = false;
  }
}

// ---------- 房型增删改 ----------
async function createSiteType() {
  if (!newType.value.typeName) return;
  await adminApi.createSiteType({
    typeName: newType.value.typeName,
    basePrice: Number(newType.value.basePrice) || 0,
    maxGuests: Number(newType.value.maxGuests) || 1,
    description: newType.value.description || "",
    imageUrl: newType.value.imageUrl || "",
  });
  newType.value = {
    typeName: "",
    basePrice: 0,
    maxGuests: 1,
    description: "",
    imageUrl: "",
  };
  await loadTypes();
}

function startEditType(t: SiteType) {
  editingTypeId.value = Number(t.typeId);
  editType.value = {
    typeName: t.typeName,
    basePrice: t.basePrice,
    maxGuests: t.maxGuests,
    totalSites: t.totalSites,
    description: (t as any).description || "",
    imageUrl: (t as any).imageUrl || "",
  };
}

function cancelEditType() {
  editingTypeId.value = null;
  editType.value = {};
}

async function saveEditType() {
  if (editingTypeId.value == null) return;
  await adminApi.updateSiteType(editingTypeId.value, {
    typeName: editType.value.typeName,
    basePrice: Number(editType.value.basePrice) || 0,
    maxGuests: Number(editType.value.maxGuests) || 1,
    totalSites: Number(editType.value.totalSites) || 0,
    description: editType.value.description || "",
    imageUrl: editType.value.imageUrl || "",
  });
  cancelEditType();
  await loadTypes();
}

async function deleteSiteType(typeId: number) {
  if (!confirm("确认删除该房型？这将同时删除该房型下的所有营位！")) return;
  try {
    // 1. 获取该房型下的所有营位
    const res: any = await adminApi.getAllSites(typeId);
    const sites = res?.data || [];

    // 2. 逐个删除营位
    for (const site of sites) {
      await adminApi.deleteSite(site.siteId);
    }

    // 3. 删除房型
    await adminApi.deleteSiteType(typeId);
    await loadTypes();
  } catch (e: any) {
    alert("删除失败: " + (e?.message || "未知错误"));
  }
}

// ---------- 装备增删改 ----------
async function createEquipment() {
  if (!newEquip.value.equipName) return;
  await adminApi.createEquipment({
    equipName: newEquip.value.equipName,
    unitPrice: Number(newEquip.value.unitPrice) || 0,
    totalStock: Number(newEquip.value.totalStock) || 0,
    category: newEquip.value.category || "",
    description: newEquip.value.description || "",
  });
  newEquip.value = {
    equipName: "",
    unitPrice: 0,
    totalStock: 0,
    category: "",
    description: "",
  };
  await loadEquipments();
}

function startEditEquip(e: Equipment) {
  editingEquipId.value = Number(e.equipId);
  editEquip.value = {
    equipName: e.equipName,
    unitPrice: e.unitPrice,
    totalStock: (e as any).totalStock ?? 0,
    category: (e as any).category || "",
    description: (e as any).description || "",
  };
}

function cancelEditEquip() {
  editingEquipId.value = null;
  editEquip.value = {};
}

async function saveEditEquip() {
  if (editingEquipId.value == null) return;
  await adminApi.updateEquipment(editingEquipId.value, {
    equipName: editEquip.value.equipName,
    unitPrice: Number(editEquip.value.unitPrice) || 0,
    totalStock: Number(editEquip.value.totalStock) || 0,
    category: editEquip.value.category || "",
    description: editEquip.value.description || "",
  });
  cancelEditEquip();
  await loadEquipments();
}

async function deleteEquipment(equipId: number) {
  if (!confirm("确认删除该装备？")) return;
  await adminApi.deleteEquipment(equipId);
  await loadEquipments();
}

function formatPrice(val: any) {
  if (val === undefined || val === null) return "-";
  return Number(val).toFixed(2);
}

function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: "待支付",
    1: "已完成",
    2: "已取消",
  };
  return map[status] || "-";
}

// ---------- 房屋管理逻辑 ----------
async function openSiteManager(t: SiteType) {
  currentManageTypeId.value = t.typeId;
  showSiteManager.value = true;
  await loadSites(t.typeId);
}

function closeSiteManager() {
  showSiteManager.value = false;
  currentManageTypeId.value = null;
  siteList.value = [];
  newSiteNo.value = "";
}

async function loadSites(typeId: number) {
  try {
    const res: any = await adminApi.getAllSites(typeId);
    siteList.value = res?.data || [];
  } catch (e) {
    siteList.value = [];
  }
}

async function addSite() {
  if (!currentManageTypeId.value || !newSiteNo.value) return;
  try {
    await adminApi.createSite(currentManageTypeId.value, newSiteNo.value);
    newSiteNo.value = "";
    await loadSites(currentManageTypeId.value);
    // 刷新房型列表以更新总数
    await loadTypes();
  } catch (e: any) {
    alert("添加失败: " + (e?.message || "未知错误"));
  }
}

async function removeSite(siteId: number) {
  if (!confirm("确认删除该营位？")) return;
  if (!currentManageTypeId.value) return;
  try {
    await adminApi.deleteSite(siteId);
    await loadSites(currentManageTypeId.value);
    // 刷新房型列表以更新总数
    await loadTypes();
  } catch (e: any) {
    alert("删除失败: " + (e?.message || "未知错误"));
  }
}

function getSiteStatusText(status: number) {
  return status === 1 ? "正常" : "维护中";
}
</script>

<style scoped>
.block {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}
.block__title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}
.bookings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cards {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.card {
  border: 1px solid #e6e6e6;
  padding: 12px;
  border-radius: 6px;
  min-width: 200px;
}
.form-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-item .label {
  font-size: 12px;
  color: #666;
}

.form-item input {
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.table-wrap {
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  border: 1px solid #e6e6e6;
  padding: 8px;
  text-align: left;
}
.data-table th {
  background: #f7f7f7;
}
.filter-tabs {
  display: flex;
  gap: 8px;
}
.filter-tabs button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}
.filter-tabs button.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
.btn {
  padding: 6px 10px;
  border: 1px solid #ccc;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}
.btn-ghost {
  background: #fafafa;
}
.btn-danger {
  background: #fee2e2;
  border-color: #fecaca;
}
.empty {
  text-align: center;
  color: #888;
}

/* 模态框样式 */
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-container {
  width: 600px;
  max-width: 90%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 16px;
  overflow-y: auto;
}

.site-form {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.site-list-container {
  border: 1px solid #eee;
  border-radius: 4px;
}

.date-range-picker {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.date-range-picker input {
  padding: 4px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

/* 图片预览弹窗 */
.image-modal {
  position: fixed;
  z-index: 9999;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.image-modal-content img {
  max-width: 100%;
  max-height: 90vh;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.image-modal-content .close-btn {
  position: absolute;
  top: -40px;
  right: -40px;
  background: none;
  border: none;
  color: #fff;
  font-size: 32px;
  cursor: pointer;
  padding: 10px;
}

.image-modal-content .close-btn:hover {
  color: #ddd;
}
</style>
