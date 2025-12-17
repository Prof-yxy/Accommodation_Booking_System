<template>
  <div class="admin-dashboard">
    <h2>管理员面板（演示）</h2>

    <div class="cards">
      <div class="card">
        <h3>预订统计</h3>
        <div v-if="loadingStats">加载中...</div>
        <div v-else>
          <div>总订单: {{ stats.totalBookings || 0 }}</div>
          <div>已支付: {{ stats.paidBookings || 0 }}</div>
          <div>待支付: {{ stats.pendingPaymentBookings || 0 }}</div>
          <div>已取消: {{ stats.canceledBookings || 0 }}</div>
          <div>总收入: {{ stats.totalRevenue || 0 }}</div>
        </div>
      </div>

      <div class="card">
        <h3>简要报表 (示例)</h3>
        <div v-if="loadingReport">加载中...</div>
        <div v-else>
          <div>
            期间: {{ report.startDate || "-" }} ~ {{ report.endDate || "-" }}
          </div>
          <div>总收入: {{ report.totalRevenue || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 房型管理 -->
    <section class="block">
      <header class="block__title">房型管理</header>

      <!-- 新增房型表单 -->
      <div class="form-grid">
        <input v-model="newType.typeName" placeholder="房型名称" />
        <input
          v-model.number="newType.basePrice"
          type="number"
          min="0"
          placeholder="基础价"
        />
        <input
          v-model.number="newType.maxGuests"
          type="number"
          min="1"
          placeholder="可住人数"
        />
        <input v-model="newType.imageUrl" placeholder="图片地址(可选)" />
        <input v-model="newType.description" placeholder="描述(可选)" />
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
        <input v-model="newEquip.equipName" placeholder="装备名称" />
        <input
          v-model.number="newEquip.unitPrice"
          type="number"
          min="0"
          placeholder="单价"
        />
        <input
          v-model.number="newEquip.totalStock"
          type="number"
          min="0"
          placeholder="总库存"
        />
        <input v-model="newEquip.category" placeholder="分类(可选)" />
        <input v-model="newEquip.description" placeholder="描述(可选)" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { adminApi, resourceApi } from "@/api";
import type { SiteType, Equipment } from "@/api";

const stats = ref<any>({});
const report = ref<any>({});
const loadingStats = ref(true);
const loadingReport = ref(true);

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

onMounted(async () => {
  try {
    const s: any = await adminApi.getBookingStats();
    stats.value = (s && s.data) || {};
  } catch (e) {
    stats.value = {};
  } finally {
    loadingStats.value = false;
  }

  try {
    const today = new Date();
    const start = new Date(today.getTime() - 7 * 24 * 3600 * 1000);
    const fmt = (d: Date) => d.toISOString().slice(0, 10);
    const r: any = await adminApi.getDailyReport(fmt(start), fmt(today));
    report.value = (r && r.data) || {};
  } catch (e) {
    report.value = {};
  } finally {
    loadingReport.value = false;
  }

  // 加载房型与装备列表
  loadTypes();
  loadEquipments();
});

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
    description: editType.value.description || "",
    imageUrl: editType.value.imageUrl || "",
  });
  cancelEditType();
  await loadTypes();
}

async function deleteSiteType(typeId: number) {
  if (!confirm("确认删除该房型？")) return;
  await adminApi.deleteSiteType(typeId);
  await loadTypes();
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
  display: grid;
  grid-template-columns: repeat(5, minmax(120px, 1fr));
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
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
</style>
