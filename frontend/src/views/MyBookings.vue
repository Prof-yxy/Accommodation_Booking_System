<template>
  <div class="my-bookings">
    <header class="page-header">
      <h2>我的预订</h2>
      <div class="filter-tabs">
        <button
          :class="{ active: currentStatus === null }"
          @click="loadBookings(null)"
        >
          全部
        </button>
        <button
          :class="{ active: currentStatus === 1 }"
          @click="loadBookings(1)"
        >
          已完成
        </button>
        <button
          :class="{ active: currentStatus === 0 }"
          @click="loadBookings(0)"
        >
          待支付
        </button>
        <button
          :class="{ active: currentStatus === 2 }"
          @click="loadBookings(2)"
        >
          已取消
        </button>
      </div>
    </header>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="bookings.length === 0" class="empty">暂无预订记录</div>

    <ul v-else class="booking-list">
      <li
        v-for="booking in bookings"
        :key="booking.bookingId"
        class="booking-card"
      >
        <div class="card-header">
          <span class="booking-id">订单 #{{ booking.bookingId }}</span>
          <span :class="['status-badge', getStatusClass(booking.status)]">
            {{ getStatusText(booking.status) }}
          </span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">营位号：</span>
            <span class="value">{{ booking.siteNo || "-" }}</span>
          </div>
          <div class="info-row">
            <span class="label">入住时间：</span>
            <span class="value"
              >{{ booking.checkIn }} 至 {{ booking.checkOut }}</span
            >
          </div>
          <div class="info-row">
            <span class="label">预订人：</span>
            <span class="value"
              >{{ booking.guestName }} ({{ booking.guestPhone }})</span
            >
          </div>
          <div class="info-row">
            <span class="label">总价：</span>
            <span class="value price"
              >￥{{ formatPrice(booking.totalPrice) }}</span
            >
          </div>
        </div>

        <div class="card-actions">
          <button class="btn-detail" @click="viewDetail(booking.bookingId)">
            查看详情
          </button>
          <button
            v-if="booking.status === 0"
            class="btn-cancel"
            @click="cancelBooking(booking.bookingId)"
          >
            取消预订
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { bookingApi } from "@/api";

interface Booking {
  bookingId: number;
  siteNo: string;
  checkIn: string;
  checkOut: string;
  guestName: string;
  guestPhone: string;
  totalPrice: number;
  status: number;
  createTime: string;
}

const router = useRouter();
const bookings = ref<Booking[]>([]);
const loading = ref(false);
const currentStatus = ref<number | null>(null);

const loadBookings = async (status: number | null = null) => {
  loading.value = true;
  currentStatus.value = status;

  try {
    // 从 localStorage 获取用户 ID（演示用）
    const userId = parseInt(localStorage.getItem("userId") || "1");
    const res: any = await bookingApi.getMyList(userId, status ?? undefined);
    bookings.value = res?.data || [];
  } catch (error) {
    console.error("加载订单失败:", error);
    bookings.value = [];
  } finally {
    loading.value = false;
  }
};

const viewDetail = (bookingId: number) => {
  router.push(`/booking/${bookingId}`);
};

const cancelBooking = async (bookingId: number) => {
  if (!confirm("确定要取消此预订吗？")) return;

  try {
    await bookingApi.cancel(bookingId);
    alert("预订已取消");
    loadBookings(currentStatus.value);
  } catch (error: any) {
    alert("取消失败: " + (error?.message || "未知错误"));
  }
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

onMounted(() => {
  loadBookings();
});
</script>

<style scoped>
.my-bookings {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tabs button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tabs button:hover {
  border-color: #409eff;
}

.filter-tabs button.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.booking-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 16px;
}

.booking-card {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.booking-id {
  font-weight: 600;
  font-size: 16px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
}

.status-paid {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-cancelled {
  background: #f5f5f5;
  color: #757575;
}

.card-body {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row .label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.info-row .value {
  color: #333;
}

.info-row .price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-actions button {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-detail {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.btn-detail:hover {
  background: #66b1ff;
}

.btn-cancel {
  background: #fff;
  color: #f56c6c;
  border-color: #f56c6c;
}

.btn-cancel:hover {
  background: #fef0f0;
}
</style>
