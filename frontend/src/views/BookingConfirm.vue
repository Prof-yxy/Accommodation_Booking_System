<template>
  <div class="booking-confirm">
    <h2>创建预订（演示）</h2>

    <form @submit.prevent="onCheck">
      <div>
        <label
          >房型 ID：<input v-model.number="form.typeId" type="number" required
        /></label>
      </div>
      <div>
        <label
          >入住日期：<input v-model="form.checkIn" type="date" required
        /></label>
      </div>
      <div>
        <label
          >离店日期：<input v-model="form.checkOut" type="date" required
        /></label>
      </div>
      <div>
        <label
          >联系人姓名：<input v-model="form.guestName" type="text" required
        /></label>
      </div>
      <div>
        <label
          >联系电话：<input v-model="form.guestPhone" type="text" required
        /></label>
      </div>
      <div>
        <label>装备 (JSON 数组)：</label>
        <textarea
          v-model="equipJson"
          rows="4"
          placeholder='例如: [{"equipId":1,"count":2}]'
        ></textarea>
      </div>

      <div class="form-actions">
        <button type="submit">检查价格 & 库存</button>
        <button type="button" @click="onCreate">创建订单</button>
      </div>
    </form>

    <div v-if="result" class="result">
      <h3>检查结果</h3>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { bookingApi } from "@/api";

const form = ref({
  typeId: null as number | null,
  checkIn: "",
  checkOut: "",
  guestName: "",
  guestPhone: "",
});
const equipJson = ref("[]");
const result = ref<any>(null);

async function onCheck() {
  try {
    const equipments = JSON.parse(equipJson.value || "[]");
    const payload = {
      typeId: form.value.typeId,
      checkIn: form.value.checkIn,
      checkOut: form.value.checkOut,
      equipments,
    };
    const res: any = await bookingApi.check(payload);
    result.value = res && res.data ? res.data : res;
  } catch (e: any) {
    result.value = { error: e?.message || String(e) };
  }
}

async function onCreate() {
  try {
    const equipments = JSON.parse(equipJson.value || "[]");
    const payload = {
      typeId: form.value.typeId,
      checkIn: form.value.checkIn,
      checkOut: form.value.checkOut,
      equipments,
      userId: parseInt(localStorage.getItem("userId") || "1"), // 从 localStorage 获取用户 ID
      guestName: form.value.guestName,
      guestPhone: form.value.guestPhone,
    };
    const res: any = await bookingApi.create(payload);

    if (res && res.data && res.data.bookingId) {
      // 课程设计演示：创建订单后直接视为完成，跳过支付流程
      result.value = {
        success: true,
        message: "预订成功！",
        ...res.data,
      };

      // 可选：自动跳转到订单详情页
      // setTimeout(() => {
      //   window.location.href = `/#/booking/${res.data.bookingId}`;
      // }, 1500);
    } else {
      result.value = res && res.data ? res.data : res;
    }
  } catch (e: any) {
    result.value = { error: e?.message || String(e) };
  }
}
</script>

<style scoped>
.booking-confirm form > div {
  margin: 8px 0;
}
.form-actions {
  margin-top: 12px;
}
.result {
  margin-top: 16px;
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
}
</style>
