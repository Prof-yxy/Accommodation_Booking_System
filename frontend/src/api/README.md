# 前端 API 接口文档

本文档说明如何在 Vue 3 组件中使用前端 API 接口。

## 目录结构

```
src/api/
├── index.ts          # API 统一导出
├── user.ts           # 用户相关接口
├── booking.ts        # 预订相关接口
├── resource.ts       # 资源查询接口
└── admin.ts          # 管理员接口

src/utils/
└── request.ts        # HTTP 请求工具（axios 实例配置）
```

## 快速开始

### 1. 用户认证模块 (user.ts)

```typescript
import { userApi } from "@/api";

// 用户注册
const registerUser = async () => {
  try {
    const response = await userApi.register({
      username: "john_doe",
      password: "secure_password",
      phone: "13800138000",
    });
    console.log("注册成功:", response.data);
  } catch (error) {
    console.error("注册失败:", error);
  }
};

// 用户登录
const login = async () => {
  try {
    const response = await userApi.login({
      username: "john_doe",
      password: "secure_password",
    });
    const { userId, username, token, role } = response.data;
    // 保存 token 到 localStorage
    localStorage.setItem("token", token);
    localStorage.setItem("userId", userId);
  } catch (error) {
    console.error("登录失败:", error);
  }
};

// 获取当前用户信息
const getUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser();
    console.log("用户信息:", response.data);
  } catch (error) {
    console.error("获取用户信息失败:", error);
  }
};

// 修改密码
const changePassword = async () => {
  try {
    await userApi.changePassword("old_password", "new_password");
    console.log("密码修改成功");
  } catch (error) {
    console.error("密码修改失败:", error);
  }
};
```

### 2. 资源查询模块 (resource.ts)

```typescript
import { resourceApi } from "@/api";

// 获取房型列表
const getSiteTypes = async () => {
  try {
    const response = await resourceApi.getSiteTypes();
    const types = response.data; // SiteType[]
    console.log("房型列表:", types);
  } catch (error) {
    console.error("获取房型列表失败:", error);
  }
};

// 获取价格日历
const getPriceCalendar = async () => {
  try {
    const response = await resourceApi.getCalendar(
      1, // typeId
      "2024-12-01",
      "2024-12-31"
    );
    const calendarData = response.data; // PriceCalendarResponse
    console.log("价格日历:", calendarData);
  } catch (error) {
    console.error("获取价格日历失败:", error);
  }
};

// 获取所有装备
const getEquipments = async () => {
  try {
    const response = await resourceApi.getEquipments();
    const equipments = response.data; // Equipment[]
    console.log("装备列表:", equipments);
  } catch (error) {
    console.error("获取装备列表失败:", error);
  }
};
```

### 3. 预订核心模块 (booking.ts)

```typescript
import { bookingApi } from "@/api";
import type { BookingCheckParams, BookingCreateParams } from "@/api";

// 预订前检查 - 验证库存和计算价格
const checkAvailability = async () => {
  try {
    const checkParams: BookingCheckParams = {
      typeId: 1,
      checkIn: "2024-12-10",
      checkOut: "2024-12-15",
      equipments: [
        { equipId: 1, count: 2 }, // 租赁装备1，数量2
        { equipId: 2, count: 1 }, // 租赁装备2，数量1
      ],
    };

    const response = await bookingApi.check(checkParams);
    const { isAvailable, totalPrice, priceDetail } = response.data;

    if (isAvailable) {
      console.log("可以预订，总价:", totalPrice);
      console.log("价格明细:", priceDetail);
    } else {
      console.log("无法预订:", response.data.msg);
    }
  } catch (error) {
    console.error("检查预订失败:", error);
  }
};

// 创建订单 - 核心事务操作
const createBooking = async () => {
  try {
    const createParams: BookingCreateParams = {
      userId: 123,
      typeId: 1,
      checkIn: "2024-12-10",
      checkOut: "2024-12-15",
      guestName: "张三",
      guestPhone: "13900139000",
      equipments: [
        { equipId: 1, count: 2 },
        { equipId: 2, count: 1 },
      ],
    };

    const response = await bookingApi.create(createParams);
    const { bookingId, siteNo, totalPrice } = response.data;

    console.log("订单创建成功");
    console.log("订单ID:", bookingId);
    console.log("营位号:", siteNo);
    console.log("总价:", totalPrice);
  } catch (error) {
    console.error("创建订单失败:", error);
  }
};

// 获取我的订单列表
const getMyBookings = async (userId: number) => {
  try {
    // 获取所有订单
    const allBookings = await bookingApi.getMyList(userId);

    // 获取待支付订单
    const pendingBookings = await bookingApi.getMyList(userId, 0);

    // 获取已支付订单
    const paidBookings = await bookingApi.getMyList(userId, 1);

    console.log("所有订单:", allBookings.data);
  } catch (error) {
    console.error("获取订单列表失败:", error);
  }
};

// 支付订单
const payBooking = async (bookingId: number) => {
  try {
    const response = await bookingApi.pay(bookingId);
    console.log("支付成功:", response.data);
  } catch (error) {
    console.error("支付失败:", error);
  }
};

// 取消订单
const cancelBooking = async (bookingId: number) => {
  try {
    const response = await bookingApi.cancel(bookingId);
    console.log("订单已取消:", response.data);
  } catch (error) {
    console.error("取消订单失败:", error);
  }
};
```

### 4. 管理员模块 (admin.ts)

```typescript
import { adminApi } from "@/api";
import type { PriceSetParams } from "@/api";

// 设置日价格
const setDailyPrice = async () => {
  try {
    const priceData: PriceSetParams = {
      typeId: 1,
      dates: ["2024-12-25", "2024-12-26", "2024-12-27"], // 圣诞节期间涨价
      price: 599.99,
    };

    const response = await adminApi.setDailyPrice(priceData);
    console.log("价格设置成功:", response.data);
  } catch (error) {
    console.error("价格设置失败:", error);
  }
};

// 获取日收入报表
const getDailyRevenue = async () => {
  try {
    const response = await adminApi.getDailyReport("2024-12-01", "2024-12-31");
    const report = response.data;
    console.log("日收入报表:", report);
    // 可以在图表中展示 report.dailyData
  } catch (error) {
    console.error("获取报表失败:", error);
  }
};

// 获取房型统计
const getTypeStats = async () => {
  try {
    const response = await adminApi.getTypeStats();
    const stats = response.data; // TypeStats[]
    console.log("房型统计:", stats);
  } catch (error) {
    console.error("获取房型统计失败:", error);
  }
};

// 获取操作日志
const getOperationLogs = async () => {
  try {
    const response = await adminApi.getOperationLogs(1, 20, "NEW_ORDER");
    const logs = response.data; // OperationLog[]
    console.log("操作日志:", logs);
  } catch (error) {
    console.error("获取操作日志失败:", error);
  }
};
```

## 在 Vue 组件中使用

### 示例：预订流程组件

```vue
<template>
  <div class="booking-process">
    <!-- 第一步：选择房型和日期 -->
    <div class="step-1">
      <select v-model="selectedTypeId">
        <option
          v-for="type in siteTypes"
          :key="type.typeId"
          :value="type.typeId"
        >
          {{ type.typeName }} - ¥{{ type.basePrice }}
        </option>
      </select>
      <input v-model="checkIn" type="date" />
      <input v-model="checkOut" type="date" />
    </div>

    <!-- 第二步：选择装备 -->
    <div class="step-2">
      <div v-for="equip in equipments" :key="equip.equipId" class="equip-item">
        <label>{{ equip.equipName }}</label>
        <input
          v-model.number="equipmentSelections[equip.equipId]"
          type="number"
          min="0"
        />
        <span>¥{{ equip.unitPrice }}/件</span>
      </div>
    </div>

    <!-- 第三步：检查可用性 -->
    <button @click="checkBooking">检查可用性</button>
    <div v-if="checkResult" class="check-result">
      <p>可用性: {{ checkResult.isAvailable ? "可以预订" : "无法预订" }}</p>
      <p v-if="checkResult.isAvailable">总价: ¥{{ checkResult.totalPrice }}</p>
    </div>

    <!-- 第四步：提交订单 -->
    <button v-if="checkResult?.isAvailable" @click="createBooking">
      提交订单
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { resourceApi, bookingApi } from "@/api";
import type {
  SiteType,
  Equipment,
  BookingCheckParams,
  BookingCreateParams,
} from "@/api";

const siteTypes = ref<SiteType[]>([]);
const equipments = ref<Equipment[]>([]);
const selectedTypeId = ref<number>(0);
const checkIn = ref<string>("");
const checkOut = ref<string>("");
const equipmentSelections = ref<Record<number, number>>({});
const checkResult = ref<any>(null);

// 初始化数据
onMounted(async () => {
  try {
    const typesRes = await resourceApi.getSiteTypes();
    siteTypes.value = typesRes.data;

    const equipRes = await resourceApi.getEquipments();
    equipments.value = equipRes.data;

    // 初始化装备选择
    equipments.value.forEach((e) => {
      equipmentSelections.value[e.equipId] = 0;
    });
  } catch (error) {
    console.error("初始化数据失败:", error);
  }
});

// 检查可用性
const checkBooking = async () => {
  try {
    const params: BookingCheckParams = {
      typeId: selectedTypeId.value,
      checkIn: checkIn.value,
      checkOut: checkOut.value,
      equipments: Object.entries(equipmentSelections.value)
        .filter(([_, count]) => count > 0)
        .map(([equipId, count]) => ({
          equipId: parseInt(equipId),
          count,
        })),
    };

    const response = await bookingApi.check(params);
    checkResult.value = response.data;
  } catch (error) {
    console.error("检查可用性失败:", error);
  }
};

// 创建订单
const createBooking = async () => {
  try {
    const userId = parseInt(localStorage.getItem("userId") || "0");

    const params: BookingCreateParams = {
      userId,
      typeId: selectedTypeId.value,
      checkIn: checkIn.value,
      checkOut: checkOut.value,
      guestName: "李四", // 应该从表单获取
      guestPhone: "13900139000", // 应该从表单获取
      equipments: Object.entries(equipmentSelections.value)
        .filter(([_, count]) => count > 0)
        .map(([equipId, count]) => ({
          equipId: parseInt(equipId),
          count,
        })),
    };

    const response = await bookingApi.create(params);
    const { bookingId, siteNo, totalPrice } = response.data;

    alert(
      `订单创建成功！订单号: ${bookingId}, 营位: ${siteNo}, 总价: ¥${totalPrice}`
    );
  } catch (error) {
    console.error("创建订单失败:", error);
  }
};
</script>
```

## 错误处理最佳实践

```typescript
import { userApi } from "@/api";

const handleLogin = async () => {
  try {
    const response = await userApi.login({
      username: "user",
      password: "pass",
    });
    // 业务成功处理
    const { token } = response.data;
    localStorage.setItem("token", token);
  } catch (error) {
    // 错误处理已在 request.ts 中通过拦截器处理
    // 这里的 catch 主要捕获网络错误
    if (error instanceof Error) {
      console.error("错误信息:", error.message);
    }
  }
};
```

## Token 管理

Token 在请求拦截器中自动从 localStorage 中读取并添加到请求头：

```
Authorization: Bearer <token>
```

登录成功后，需要手动保存 token：

```typescript
const token = response.data.token;
localStorage.setItem("token", token);
```

登出时清除 token：

```typescript
localStorage.removeItem("token");
window.location.href = "/login";
```

## 环境配置

在 `.env` 文件中配置 API 基础 URL：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 总结

- 所有 API 都通过 `request.ts` 中的 axios 实例进行
- 统一的错误处理和响应格式处理
- 完整的 TypeScript 类型支持
- 自动的 Token 管理和刷新
- 在 Vue 组件中通过 `await` 语法简洁地调用
