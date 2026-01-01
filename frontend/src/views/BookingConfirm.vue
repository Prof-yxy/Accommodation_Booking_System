<template>
  <div class="booking-confirm">
    <h2>预订（房型 + 可选装备）</h2>

    <form @submit.prevent="onSubmit">
      <!-- 1. 选择房型 -->
      <div class="row">
        <label>房型：</label>
        <select v-model.number="selectedTypeId" required>
          <option
            v-for="opt in typeOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
      </div>

      <!-- 2. 入住与离店日期 -->
      <div class="row">
        <label>入住日期：</label>
        <input v-model="checkIn" type="date" required />
      </div>
      <div class="row">
        <label>离店日期：</label>
        <input v-model="checkOut" type="date" required />
      </div>

      <!-- 查询剩余量按钮 -->
      <div class="row actions">
        <button type="button" @click="onQuery">查询剩余量</button>
        <span v-if="remainingInfo" class="hint">
          剩余：{{ remainingInfo.remaining }} / 总数：{{ remainingInfo.total }}
        </span>
        <span v-else class="hint">请先查询</span>
      </div>

      <!-- 设备选择（可选） -->
      <div class="equipment-block">
        <div class="equipment-header">
          <span>可选装备（可多选，0 表示不租）</span>
          <button type="button" class="btn-ghost" @click="resetEquipments">
            全部清零
          </button>
        </div>
        <div class="equipment-list">
          <div
            class="equip-item"
            v-for="equip in equipments"
            :key="equip.equipId"
          >
            <div class="equip-main">
              <div class="equip-name">{{ equip.equipName }}</div>
              <div class="equip-meta">
                ￥{{ formatPrice(equip.unitPrice) }} /天 · 库存:
                {{ equip.availableStock ?? equip.totalStock ?? "?" }}
              </div>
              <div class="equip-desc">{{ equip.description || "" }}</div>
            </div>
            <div class="equip-actions">
              <input
                type="number"
                min="0"
                :max="equip.availableStock ?? equip.totalStock ?? 99"
                v-model.number="equip.count"
              />
              <span
                class="equip-subtotal"
                v-if="equip.count && equip.count > 0"
              >
                小计：￥{{
                  formatPrice(equip.unitPrice * equip.count * nightsEstimate)
                }}
              </span>
            </div>
          </div>
          <div v-if="!equipments.length" class="hint">无装备数据</div>
        </div>
      </div>

      <!-- 3. 选择预订数量 -->
      <div class="row">
        <label>预订数量：</label>
        <select
          v-model.number="quantity"
          :disabled="!remainingInfo || remainingInfo.remaining === 0"
        >
          <option v-for="n in quantityOptions" :key="n" :value="n">
            {{ n }}
          </option>
        </select>
        <span
          v-if="remainingInfo && remainingInfo.remaining === 0"
          class="warn"
        >
          当前资源无剩余
        </span>
      </div>

      <!-- 4. 联系人信息 -->
      <div class="row">
        <label>联系人姓名：</label>
        <input v-model="guestName" type="text" required />
      </div>
      <div class="row">
        <label>联系电话：</label>
        <input v-model="guestPhone" type="text" required />
      </div>

      <!-- 5. 提交 -->
      <div class="form-actions">
        <button type="submit" :disabled="!canSubmit">提交预订单</button>
      </div>
    </form>

    <div v-if="result" class="result">
      <h3>提交结果</h3>
      <p v-if="result.bookingIds || result.bookingId">
        订单编号：{{ result.bookingIds?.join(", ") || result.bookingId }}
      </p>
      <p v-if="result.siteNos || result.siteNo">
        场地编号：{{ result.siteNos?.join(", ") || result.siteNo }}
      </p>
      <p v-if="result.quantity">数量：{{ result.quantity }}</p>
      <p v-if="result.totalPrice">总价：{{ result.totalPrice }}</p>
      <p v-if="result.priceDetail?.formula" class="formula">
        价格计算：{{ result.priceDetail.formula }}
      </p>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { bookingApi, resourceApi } from "@/api";

const selectedTypeId = ref<number | null>(null);
const checkIn = ref("");
const checkOut = ref("");
const guestName = ref("");
const guestPhone = ref("");
const result = ref<any>(null);
const remainingInfo = ref<{ remaining: number; total: number } | null>(null);
const quantity = ref<number>(1);
const quantityOptions = ref<number[]>([]);
const typeOptions = ref<Array<{ value: number; label: string }>>([]);
const equipments = ref<Array<any>>([]);
const nightsEstimate = computed(() => {
  if (!checkIn.value || !checkOut.value) return 1;
  const start = new Date(checkIn.value);
  const end = new Date(checkOut.value);
  const diff = Math.max(
    1,
    Math.ceil((end.getTime() - start.getTime()) / (1000 * 3600 * 24))
  );
  return diff;
});

async function onSubmit() {
  try {
    if (!remainingInfo.value) {
      await onQuery();
    }
    if (!canSubmit.value) {
      result.value = { error: "请先查询并选择有效数量" };
      return;
    }
    const equipsPayload = equipments.value
      .filter((e) => e.count && e.count > 0)
      .map((e) => ({ equipId: e.equipId, count: e.count }));
    const payload = {
      typeId: selectedTypeId.value,
      checkIn: checkIn.value,
      checkOut: checkOut.value,
      equipments: equipsPayload,
      quantity: quantity.value,
      userId: (() => {
        const u = localStorage.getItem("user");
        return u ? JSON.parse(u).userId : 1;
      })(),
      guestName: guestName.value,
      guestPhone: guestPhone.value,
    };
    const res: any = await bookingApi.create(payload);
    result.value = res && res.data ? res.data : res;
  } catch (e: any) {
    result.value = {
      error: e?.message || String(e),
      status: e?.response?.status,
      response: e?.response?.data,
    };
  }
}

async function onQuery() {
  result.value = null;
  remainingInfo.value = null;
  quantity.value = 1;
  quantityOptions.value = [];

  if (checkIn.value && checkOut.value && checkIn.value > checkOut.value) {
    result.value = { error: "入住日期不能晚于离店日期" };
    return;
  }

  fetchEquipments();
  if (!selectedTypeId.value || !checkIn.value || !checkOut.value) {
    result.value = { error: "请先选择类型并填写日期" };
    return;
  }
  try {
    const res: any = await resourceApi.queryAvailability(
      "site",
      selectedTypeId.value,
      checkIn.value,
      checkOut.value
    );
    const data = (res && res.data) || {};
    const remaining = Number(data.remaining || 0);
    const total = Number(data.total || 0);
    remainingInfo.value = { remaining, total };
    quantityOptions.value = Array.from(
      { length: Math.max(remaining, 0) },
      (_, i) => i + 1
    );
  } catch (e: any) {
    result.value = { error: e?.message || String(e) };
  }
}

watch([selectedTypeId, checkIn, checkOut], () => {
  if (selectedTypeId.value && checkIn.value && checkOut.value) {
    onQuery();
  }
});

const canSubmit = computed(() => {
  const basicValid =
    !!selectedTypeId.value &&
    !!checkIn.value &&
    !!checkOut.value &&
    !!guestName.value &&
    !!guestPhone.value;

  if (!basicValid) return false;
  if (checkIn.value > checkOut.value) return false;

  if (remainingInfo.value) {
    return (
      remainingInfo.value.remaining > 0 &&
      quantity.value >= 1 &&
      quantity.value <= remainingInfo.value.remaining
    );
  }
  return true;
});

async function fetchEquipments() {
  try {
    const equipsRes: any = await resourceApi.getEquipments();
    let rawList = equipsRes?.data || [];

    // 如果已选择日期，则查询每个装备在该日期的可用量
    if (checkIn.value && checkOut.value && rawList.length > 0) {
      try {
        const promises = rawList.map((e: any) => {
          const eId = Number(e.equipId || e.id);
          return resourceApi
            .queryAvailability("equip", eId, checkIn.value, checkOut.value)
            .then((res: any) => ({
              id: eId,
              remaining: res?.data?.remaining,
            }))
            .catch(() => ({ id: eId, remaining: null }));
        });
        const results = await Promise.all(promises);
        const resultMap = new Map(results.map((r) => [r.id, r.remaining]));

        rawList = rawList.map((e: any) => {
          const eId = Number(e.equipId || e.id);
          const rem = resultMap.get(eId);
          if (rem !== null && rem !== undefined) {
            return { ...e, availableStock: rem };
          }
          return e;
        });
      } catch (queryErr) {
        console.error("Failed to query equipment availability", queryErr);
      }
    }

    const currentCounts = new Map(
      equipments.value.map((e) => [e.equipId, e.count])
    );
    equipments.value = rawList.map((e: any) => ({
      equipId: Number(e.equipId || e.id),
      equipName: e.equipName || e.name,
      unitPrice: Number(e.unitPrice || e.price || 0),
      availableStock:
        e.availableStock != null && e.totalStock != null
          ? Math.min(e.availableStock, e.totalStock)
          : e.availableStock,
      totalStock: e.totalStock,
      description: e.description,
      count: currentCounts.get(Number(e.equipId || e.id)) || 0,
    }));
  } catch (err) {
    console.error(err);
  }
}

onMounted(async () => {
  const fallbackTypes = [
    { value: 1, label: "湖景標准營位" },
    { value: 2, label: "森林豪華營位" },
    { value: 3, label: "星空A字小屋" },
    { value: 4, label: "全套接駁房車位" },
    { value: 5, label: "輕奢鈴鐺帳" },
  ];
  try {
    const typesRes: any = await resourceApi.getSiteTypes();
    const typeList = (typesRes?.data || []).map((t: any) => ({
      value: Number(t.typeId || t.id),
      label: String(t.typeName || t.name || "房型"),
    }));
    typeOptions.value = typeList.length ? typeList : fallbackTypes;
  } catch (err) {
    typeOptions.value = fallbackTypes;
  }
  await fetchEquipments();
});

function resetEquipments() {
  equipments.value = equipments.value.map((e) => ({ ...e, count: 0 }));
}

function formatPrice(val: any) {
  if (val === undefined || val === null || Number.isNaN(val)) return "-";
  return Number(val).toFixed(2);
}
</script>

<style scoped>
.booking-confirm {
  padding: 16px;
}
.booking-confirm form > .row {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.actions {
  gap: 12px;
}
.hint {
  color: #4b5563;
}
.warn {
  color: #f43f5e;
  margin-left: 8px;
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
.formula {
  color: #2563eb;
  margin: 6px 0;
}
.equipment-block {
  margin-top: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}
.equipment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.equip-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}
.equip-main {
  flex: 1;
}
.equip-name {
  font-weight: 600;
}
.equip-meta {
  color: #6b7280;
  font-size: 13px;
}
.equip-desc {
  color: #475569;
  font-size: 13px;
}
.equip-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
  min-width: 160px;
}
.equip-actions input {
  width: 80px;
}
.equip-subtotal {
  color: #ea580c;
  font-weight: 600;
}
.btn-ghost {
  padding: 4px 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}
</style>
