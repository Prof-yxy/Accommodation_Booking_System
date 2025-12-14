<template>
  <div class="booking-detail">
    <header class="page-header">
      <button class="btn-back" @click="goBack">← 返回</button>
      <h2>订单详情</h2>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!booking" class="empty">订单不存在</div>

    <div v-else class="detail-content">
      <!-- 订单状态卡片 -->
      <section class="card status-card">
        <div class="status-header">
          <span :class="['status-badge', getStatusClass(booking.status)]">
            {{ getStatusText(booking.status) }}
          </span>
          <span class="booking-id">订单 #{{ booking.bookingId }}</span>
        </div>
        <div class="status-info">
          <div>下单时间：{{ formatDateTime(booking.createTime) }}</div>
        </div>
      </section>

      <!-- 预订信息卡片 -->
      <section class="card info-card">
        <h3>预订信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">营位号</span>
            <span class="value">{{ booking.siteNo || "-" }}</span>
          </div>
          <div class="info-item">
            <span class="label">房型</span>
            <span class="value">{{
              booking.typeName || `类型 ${booking.typeId}`
            }}</span>
          </div>
          <div class="info-item">
            <span class="label">入住日期</span>
            <span class="value">{{ booking.checkIn }}</span>
          </div>
          <div class="info-item">
            <span class="label">离店日期</span>
            <span class="value">{{ booking.checkOut }}</span>
          </div>
          <div class="info-item">
            <span class="label">预订人</span>
            <span class="value">{{ booking.guestName }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话</span>
            <span class="value">{{ booking.guestPhone }}</span>
          </div>
        </div>
      </section>

      <!-- 装备清单卡片 -->
      <section class="card equipment-card">
        <h3>装备清单</h3>
        <div v-if="loadingEquipments" class="loading-small">加载中...</div>
        <div v-else-if="equipments.length === 0" class="empty-small">
          未租用装备
        </div>
        <ul v-else class="equipment-list">
          <li
            v-for="equip in equipments"
            :key="equip.equipId"
            class="equipment-item"
          >
            <div class="equip-info">
              <span class="equip-name">{{ equip.equipName }}</span>
              <span class="equip-quantity">x {{ equip.quantity }}</span>
            </div>
            <span class="equip-price"
              >￥{{ formatPrice(equip.unitPrice) }}</span
            >
          </li>
        </ul>
      </section>

      <!-- 价格明细卡片 -->
      <section class="card price-card">
        <h3>价格明细</h3>
        <div class="price-detail">
          <div class="price-row">
            <span>总金额</span>
            <span class="total-price"
              >￥{{ formatPrice(booking.totalPrice) }}</span
            >
          </div>
        </div>
      </section>

      <!-- 操作按钮 -->
      <section class="actions">
        <button
          v-if="booking.status === 0"
          class="btn-cancel"
          @click="cancelBooking"
        >
          取消预订
        </button>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { bookingApi } from "@/api";

interface Booking {
  bookingId: number;
  typeId: number;
  typeName?: string;
  siteNo: string;
  checkIn: string;
  checkOut: string;
  guestName: string;
  guestPhone: string;
  totalPrice: number;
  status: number;
  createTime: string;
}

interface Equipment {
  equipId: number;
  equipName: string;
  unitPrice: number;
  quantity: number;
}

const route = useRoute();
const router = useRouter();

const booking = ref<Booking | null>(null);
const equipments = ref<Equipment[]>([]);
const loading = ref(false);
const loadingEquipments = ref(false);

const loadBookingDetail = async () => {
  loading.value = true;
  try {
    const bookingId = Number(route.params.id);
    const res: any = await bookingApi.getDetail(bookingId);
    booking.value = res?.data || null;

    if (booking.value) {
      loadEquipments(bookingId);
    }
  } catch (error) {
    console.error("加载订单详情失败:", error);
    booking.value = null;
  } finally {
    loading.value = false;
  }
};

const loadEquipments = async (bookingId: number) => {
  loadingEquipments.value = true;
  try {
    const res: any = await bookingApi.getEquipments(bookingId);
    equipments.value = res?.data || [];
  } catch (error) {
    console.error("加载装备列表失败:", error);
    equipments.value = [];
  } finally {
    loadingEquipments.value = false;
  }
};

const cancelBooking = async () => {
  if (!booking.value || !confirm("确定要取消此预订吗？")) return;

  try {
    await bookingApi.cancel(booking.value.bookingId);
    alert("预订已取消");
    router.push("/my-bookings");
  } catch (error: any) {
    alert("取消失败: " + (error?.message || "未知错误"));
  }
};

const goBack = () => {
  router.back();
};

const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: "待支付",
    1: "已完成",
    2: "已取消",
  };
  return statusMap[status] || "未知";
};

const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    0: "status-pending",
    1: "status-paid",
    2: "status-cancelled",
  };
  return classMap[status] || "";
};

const formatPrice = (price: number): string => {
  return Number(price || 0).toFixed(2);
};

const formatDateTime = (datetime: string): string => {
  if (!datetime) return "-";
  return new Date(datetime).toLocaleString("zh-CN");
};

onMounted(() => {
  loadBookingDetail();
});
</script>

<style scoped>
.booking-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.btn-back {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #f5f5f5;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.detail-content {
  display: grid;
  gap: 16px;
}

.card {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}

/* 状态卡片 */
.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.25);
}

.booking-id {
  font-size: 18px;
  font-weight: 600;
}

.status-info {
  font-size: 14px;
  opacity: 0.9;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 13px;
  color: #999;
}

.info-item .value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

/* 装备列表 */
.loading-small,
.empty-small {
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.equipment-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.equipment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.equip-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.equip-name {
  font-weight: 500;
}

.equip-quantity {
  color: #999;
  font-size: 14px;
}

.equip-price {
  font-weight: 600;
  color: #f56c6c;
}

/* 价格明细 */
.price-detail {
  display: grid;
  gap: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.price-row:last-child {
  border-bottom: none;
  padding-top: 16px;
  border-top: 2px solid #333;
}

.total-price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
}

/* 操作按钮 */
.actions {
  display: flex;
  gap: 12px;
}

.actions button {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f56c6c;
  color: #fff;
}

.btn-cancel:hover {
  background: #f78989;
}
</style>
