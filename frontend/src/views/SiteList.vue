<template>
  <div class="site-list">
    <h2>房型/装备查询</h2>

    <section class="controls">
      <label for="date-select">选择日期：</label>
      <input
        id="date-select"
        type="date"
        v-model="selectedDate"
        @change="onDateChange"
      />
      <button class="btn" @click="refreshAll">刷新价格与库存</button>
      <span class="hint">周末价含1.15系数，日营下单再按6折算；特定周五当日房价*1.1，特定周末当日房价*1.2</span>
    </section>

    <section class="block">
      <header class="block__title">房型（当日价格与可用量）</header>
      <div v-if="loadingTypes" class="loading">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>房型</th>
            <th>图片</th>
            <th>当日价</th>
            <th>基础价</th>
            <th>可住人数</th>
            <th>剩余/总数</th>
            <th>描述</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="type in types" :key="type.typeId">
            <td>{{ type.typeName }}</td>
            <td>
              <img
                v-if="type.imageUrl"
                :src="type.imageUrl"
                alt="img"
                style="
                  width: 60px;
                  height: 40px;
                  object-fit: cover;
                  border-radius: 4px;
                  cursor: pointer;
                "
                @click="showImagePreview(type.imageUrl)"
              />
              <span v-else>-</span>
            </td>
            <td>￥{{ formatPrice(type.priceToday ?? type.basePrice) }}</td>
            <td>￥{{ formatPrice(type.basePrice) }}</td>
            <td>{{ type.maxGuests }}</td>
            <td>{{ type.availableSites }} / {{ type.totalSites || "-" }}</td>
            <td>{{ type.description || "-" }}</td>
          </tr>
          <tr v-if="types.length === 0">
            <td colspan="7" class="empty">暂无房型数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="block">
      <header class="block__title">装备（当日可用库存）</header>
      <div v-if="loadingEquipments" class="loading">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>装备名称</th>
            <th>分类</th>
            <th>单价</th>
            <th>剩余/总数</th>
            <th>描述</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="equip in equipments" :key="equip.equipId">
            <td>{{ equip.equipName }}</td>
            <td>{{ equip.category || "通用" }}</td>
            <td>￥{{ formatPrice(equip.unitPrice) }}</td>
            <td>{{ equip.availableStock }} / {{ equip.totalStock || "-" }}</td>
            <td>{{ equip.description || "-" }}</td>
          </tr>
          <tr v-if="equipments.length === 0">
            <td colspan="5" class="empty">暂无装备数据</td>
          </tr>
        </tbody>
      </table>
    </section>

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
import { onMounted, ref } from "vue";
import { resourceApi } from "@/api";
import type { SiteType, Equipment } from "@/api";

type SiteCard = {
  typeId: number;
  typeName: string;
  basePrice: number;
  priceToday?: number;
  maxGuests: number;
  description?: string;
  availableSites: number;
  totalSites: number;
  imageUrl?: string;
};

type EquipCard = {
  equipId: number;
  equipName: string;
  unitPrice: number;
  availableStock: number;
  totalStock?: number;
  description?: string;
  category?: string;
};

const fallbackSiteTypes: SiteCard[] = [
  {
    typeId: 1,
    typeName: "湖景標准營位",
    basePrice: 120,
    priceToday: 120,
    maxGuests: 4,
    description: "臨湖草地，含電桩與野餐桌",
    availableSites: 8,
    totalSites: 10,
  },
  {
    typeId: 2,
    typeName: "森林豪華營位",
    basePrice: 220,
    priceToday: 220,
    maxGuests: 6,
    description: "樹蔭寬闊，附遮陽與吊床",
    availableSites: 10,
    totalSites: 10,
  },
  {
    typeId: 3,
    typeName: "星空A字小屋",
    basePrice: 320,
    priceToday: 320,
    maxGuests: 4,
    description: "硬頂小屋，配備空調與獨立衛浴",
    availableSites: 10,
    totalSites: 10,
  },
  {
    typeId: 4,
    typeName: "全套接駁房車位",
    basePrice: 180,
    priceToday: 180,
    maxGuests: 8,
    description: "房車泊位，上下水與30A電源",
    availableSites: 10,
    totalSites: 10,
  },
  {
    typeId: 5,
    typeName: "輕奢鈴鐺帳",
    basePrice: 260,
    priceToday: 260,
    maxGuests: 4,
    description: "木平台 + 棉布帳，含氛圍燈",
    availableSites: 10,
    totalSites: 10,
  },
];

const fallbackEquipments: EquipCard[] = [
  {
    equipId: 1,
    equipName: "羽絨睡袋",
    unitPrice: 28,
    availableStock: 120,
    totalStock: 120,
    description: "舒適溫標5C，可壓縮",
    category: "睡眠",
  },
  {
    equipId: 2,
    equipName: "自充氣防潮墊",
    unitPrice: 16,
    availableStock: 160,
    totalStock: 160,
    description: "5cm 厚度，R值 3.5",
    category: "睡眠",
  },
  {
    equipId: 3,
    equipName: "鈦合金炊煮套裝",
    unitPrice: 45,
    availableStock: 90,
    totalStock: 90,
    description: "含鍋碗與酒精爐架",
    category: "烹飪",
  },
  {
    equipId: 4,
    equipName: "雙口瓦斯爐",
    unitPrice: 55,
    availableStock: 70,
    totalStock: 70,
    description: "含兩罐230g氣罐",
    category: "烹飪",
  },
  {
    equipId: 5,
    equipName: "可折疊桌椅組",
    unitPrice: 32,
    availableStock: 140,
    totalStock: 140,
    description: "四人桌 + 四折疊椅",
    category: "營地",
  },
  {
    equipId: 6,
    equipName: "LED氛圍燈串",
    unitPrice: 12,
    availableStock: 180,
    totalStock: 180,
    description: "USB 供電，10m 長",
    category: "照明",
  },
  {
    equipId: 7,
    equipName: "便攜保溫冰箱",
    unitPrice: 48,
    availableStock: 80,
    totalStock: 80,
    description: "42L，附車載電源線",
    category: "存儲",
  },
  {
    equipId: 8,
    equipName: "戶外咖啡組",
    unitPrice: 26,
    availableStock: 110,
    totalStock: 110,
    description: "手沖壺 + 濾杯 + 豆",
    category: "烹飪",
  },
];

const types = ref<SiteCard[]>([]);
const equipments = ref<EquipCard[]>([]);
const loadingTypes = ref(true);
const loadingEquipments = ref(true);
const selectedDate = ref<string>(new Date().toISOString().slice(0, 10));
const previewImage = ref<string | null>(null);

const showImagePreview = (url: string) => {
  if (url) previewImage.value = url;
};

const closeImagePreview = () => {
  previewImage.value = null;
};

const normalizeSiteType = (
  raw: Partial<SiteType> & Record<string, any>
): SiteCard => {
  const total = Number(raw.totalSites ?? raw.total_sites ?? raw.siteCount ?? 0);
  const occupied = Number(
    raw.occupiedSites ?? raw.occupied_sites ?? raw.busyCount ?? 0
  );
  const availableCandidate =
    raw.availableSites ?? raw.available_sites ?? undefined;
  const available =
    availableCandidate !== undefined
      ? Number(availableCandidate)
      : total
      ? Math.max(total - occupied, 0)
      : 0;

  return {
    typeId: Number(raw.typeId ?? raw.type_id ?? raw.id ?? 0),
    typeName: String(raw.typeName ?? raw.type_name ?? "未命名房型"),
    basePrice: Number(raw.basePrice ?? raw.base_price ?? 0),
    priceToday:
      raw.priceToday !== undefined
        ? Number(raw.priceToday)
        : raw.price_today !== undefined
        ? Number(raw.price_today)
        : undefined,
    maxGuests: Number(raw.maxGuests ?? raw.max_guests ?? 0),
    description: raw.description ?? raw.remark ?? "",
    availableSites: available,
    totalSites: total || available,
    imageUrl: raw.imageUrl ?? raw.image_url ?? "",
  };
};

const normalizeEquipment = (
  raw: Partial<Equipment> & Record<string, any>
): EquipCard => {
  const total = Number(raw.totalStock ?? raw.total_stock ?? raw.stock ?? 0);
  const reserved = Number(raw.reserved ?? raw.reserved_stock ?? 0);
  const availableCandidate =
    raw.availableStock ?? raw.available_stock ?? undefined;
  const available =
    availableCandidate !== undefined
      ? Number(availableCandidate)
      : total
      ? Math.max(total - reserved, 0)
      : 0;

  return {
    equipId: Number(raw.equipId ?? raw.equip_id ?? raw.id ?? 0),
    equipName: String(raw.equipName ?? raw.equip_name ?? "未命名装备"),
    unitPrice: Number(raw.unitPrice ?? raw.unit_price ?? raw.price ?? 0),
    availableStock: available,
    totalStock: total,
    description: raw.description ?? raw.remark ?? "",
    category: raw.category ?? raw.type ?? "",
  };
};

const loadSiteTypes = async (dateStr: string) => {
  loadingTypes.value = true;
  try {
    // 先拿基础房型，再基于所选日期查询日历获取当日价格与库存
    const baseRes = await resourceApi.getSiteTypes();
    const baseList: SiteCard[] = (baseRes?.data ?? []).map(normalizeSiteType);

    const calendarResults = await Promise.all(
      baseList.map(async (t) => {
        try {
          const cRes: any = await resourceApi.getCalendar(
            t.typeId,
            dateStr,
            dateStr
          );
          const day = cRes?.data?.calendarData?.[0];
          return { id: t.typeId, day };
        } catch (err) {
          return { id: t.typeId, day: null };
        }
      })
    );

    const calendarMap = new Map<number, any>();
    calendarResults.forEach((item) => calendarMap.set(item.id, item.day));

    const merged = baseList.map((t) => {
      const day = calendarMap.get(t.typeId);
      return {
        ...t,
        priceToday: day?.price ?? t.priceToday ?? t.basePrice,
        availableSites: day?.stock ?? t.availableSites,
      };
    });

    types.value = merged.length ? merged : fallbackSiteTypes;
  } catch (error) {
    types.value = fallbackSiteTypes;
  } finally {
    loadingTypes.value = false;
  }
};

const loadEquipments = async (dateStr: string) => {
  loadingEquipments.value = true;
  try {
    const res = await resourceApi.getEquipments();
    const data = (res?.data ?? []).map(normalizeEquipment);

    const withAvailability = await Promise.all(
      data.map(async (equip) => {
        try {
          const avail: any = await resourceApi.queryAvailability(
            "equip",
            equip.equipId,
            dateStr,
            dateStr
          );
          const remaining = Number(
            avail?.data?.remaining ?? equip.availableStock
          );
          return { ...equip, availableStock: remaining };
        } catch (err) {
          return equip;
        }
      })
    );

    equipments.value = withAvailability.length
      ? withAvailability
      : fallbackEquipments;
  } catch (error) {
    equipments.value = fallbackEquipments;
  } finally {
    loadingEquipments.value = false;
  }
};

function formatPrice(val: any) {
  if (val === undefined || val === null) return "-";
  return Number(val).toFixed(2);
}

const refreshAll = () => {
  const date = selectedDate.value || new Date().toISOString().slice(0, 10);
  loadSiteTypes(date);
  loadEquipments(date);
};

const onDateChange = () => {
  refreshAll();
};

onMounted(() => {
  refreshAll();
});
</script>

<style scoped>
.site-list {
  display: grid;
  gap: 24px;
  padding: 16px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.controls .hint {
  color: #475569;
  font-size: 13px;
}

.btn {
  padding: 6px 12px;
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}

.loading {
  padding: 12px;
  color: #555;
}

.block {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.04);
}

.block__title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.card-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th,
.data-table td {
  border: 1px solid #e6e6e6;
  padding: 10px;
  text-align: left;
}

.data-table th {
  background: #f7f7f7;
  font-weight: 600;
}

.card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #fafafa, #ffffff);
}

.card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.card__name {
  font-weight: 600;
  font-size: 15px;
}

.card__price {
  color: #f97316;
  font-weight: 600;
}

.card__meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #606266;
  flex-wrap: wrap;
}

.card__desc {
  margin: 6px 0 0;
  color: #4b5563;
  font-size: 13px;
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

.loading,
.empty {
  color: #6b7280;
  font-size: 14px;
}
</style>
