<template>
  <div class="booking-confirm">
    <h2>预订（演示）</h2>

    <form @submit.prevent="onSubmit">
      <!-- 1. 选择资源种类 -->
      <div class="row">
        <label>资源种类：</label>
        <select v-model="kind" required>
          <option value="site">房型</option>
          <option value="equip">装备</option>
        </select>
      </div>

      <!-- 2. 选择具体类型（房型/装备） -->
      <div class="row">
        <label>资源类型：</label>
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

      <!-- 3/4. 入住与离店日期 -->
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

      <!-- 5. 选择预订数量 -->
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
        <span v-if="remainingInfo && remainingInfo.remaining === 0" class="warn"
          >当前资源无剩余</span
        >
      </div>

      <!-- 联系人信息（简化） -->
      <div class="row">
        <label>联系人姓名：</label>
        <input v-model="guestName" type="text" required />
      </div>
      <div class="row">
        <label>联系电话：</label>
        <input v-model="guestPhone" type="text" required />
      </div>

      <!-- 6. 提交 -->
      <div class="form-actions">
        <button type="submit" :disabled="!canSubmit">提交预订单</button>
      </div>
    </form>

    <div v-if="result" class="result">
      <h3>提交结果</h3>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { bookingApi, resourceApi } from "@/api";
import { computed, watch } from "vue";

const kind = ref<"site" | "equip">("site");
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

async function onSubmit() {
  try {
    if (!canSubmit.value) {
      result.value = { error: "请先查询并选择有效数量" };
      return;
    }
    const payload = {
      typeId: selectedTypeId.value,
      checkIn: checkIn.value,
      checkOut: checkOut.value,
      equipments:
        kind.value === "equip"
          ? [{ equipId: selectedTypeId.value, count: quantity.value }]
          : [],
      userId: parseInt(localStorage.getItem("userId") || "1"),
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
  if (!selectedTypeId.value || !checkIn.value || !checkOut.value) {
    result.value = { error: "请先选择类型并填写日期" };
    return;
  }
  try {
    const res: any = await resourceApi.queryAvailability(
      kind.value,
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

const canSubmit = computed(() => {
  return (
    !!selectedTypeId.value &&
    !!checkIn.value &&
    !!checkOut.value &&
    !!guestName.value &&
    !!guestPhone.value &&
    !!remainingInfo.value &&
    (remainingInfo.value?.remaining || 0) > 0 &&
    quantity.value >= 1 &&
    quantity.value <= (remainingInfo.value?.remaining || 0)
  );
});

onMounted(async () => {
  // 载入房型与装备作为选项（在后端不可用时也提供回退）
  const fallbackTypes = [
    { value: 1, label: "湖景標准營位" },
    { value: 2, label: "森林豪華營位" },
    { value: 3, label: "星空A字小屋" },
    { value: 4, label: "全套接駁房車位" },
    { value: 5, label: "輕奢鈴鐺帳" },
  ];
  const fallbackEquips = [
    { value: 1, label: "羽絨睡袋" },
    { value: 2, label: "自充氣防潮墊" },
    { value: 3, label: "鈦合金炊煮套裝" },
    { value: 4, label: "雙口瓦斯爐" },
    { value: 5, label: "可折疊桌椅組" },
    { value: 6, label: "LED氛圍燈串" },
    { value: 7, label: "便攜保溫冰箱" },
    { value: 8, label: "戶外咖啡組" },
  ];

  try {
    const results = await Promise.allSettled([
      resourceApi.getSiteTypes(),
      resourceApi.getEquipments(),
    ]);
    const typesRes =
      results[0].status === "fulfilled"
        ? (results[0] as PromiseFulfilledResult<any>).value
        : null;
    const equipsRes =
      results[1].status === "fulfilled"
        ? (results[1] as PromiseFulfilledResult<any>).value
        : null;

    const typeList = (typesRes?.data || []).map((t: any) => ({
      value: Number(t.typeId || t.id),
      label: String(t.typeName || t.name || "房型"),
    }));
    const equipList = (equipsRes?.data || []).map((e: any) => ({
      value: Number(e.equipId || e.id),
      label: String(e.equipName || e.name || "装备"),
    }));

    const finalTypes = typeList.length ? typeList : fallbackTypes;
    const finalEquips = equipList.length ? equipList : fallbackEquips;

    typeOptions.value = kind.value === "site" ? finalTypes : finalEquips;

    watch(kind, (k) => {
      typeOptions.value = k === "site" ? finalTypes : finalEquips;
      selectedTypeId.value = null;
      remainingInfo.value = null;
      quantityOptions.value = [];
      quantity.value = 1;
    });
  } catch {
    // 任一异常时直接使用回退选项
    typeOptions.value = kind.value === "site" ? fallbackTypes : fallbackEquips;
    watch(kind, (k) => {
      typeOptions.value = k === "site" ? fallbackTypes : fallbackEquips;
      selectedTypeId.value = null;
      remainingInfo.value = null;
      quantityOptions.value = [];
      quantity.value = 1;
    });
  }
});
</script>

<style scoped>
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
</style>
