# 🚀 前端 API 快速开始指南

## 5 分钟快速上手

### Step 1: 环境配置

复制环境配置文件:

```bash
cp .env.example .env
```

编辑 `.env` 文件，配置后端 API 地址:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Step 2: 安装依赖

```bash
npm install
```

### Step 3: 启动开发服务器

```bash
npm run dev
```

### Step 4: 开始使用 API

在任何 Vue 组件中导入并使用 API:

```typescript
import { bookingApi, resourceApi } from "@/api";

// 获取房型列表
const types = await resourceApi.getSiteTypes();

// 创建订单
const booking = await bookingApi.create({
  userId: 1,
  typeId: 1,
  checkIn: "2024-12-10",
  checkOut: "2024-12-15",
  guestName: "张三",
  guestPhone: "13800138000",
  equipments: [],
});
```

---

## 📚 常见场景速查表

### 场景 1: 用户登录

```typescript
import { userApi } from "@/api";

const response = await userApi.login({
  username: "john_doe",
  password: "password123",
});

localStorage.setItem("token", response.data.token);
```

### 场景 2: 获取房型和装备列表

```typescript
import { resourceApi } from "@/api";

const [typesRes, equipsRes] = await Promise.all([
  resourceApi.getSiteTypes(),
  resourceApi.getEquipments(),
]);

const types = typesRes.data;
const equipments = equipsRes.data;
```

### 场景 3: 预订前检查可用性

```typescript
import { bookingApi } from "@/api";

const checkResult = await bookingApi.check({
  typeId: 1,
  checkIn: "2024-12-10",
  checkOut: "2024-12-15",
  equipments: [
    { equipId: 1, count: 2 },
    { equipId: 2, count: 1 },
  ],
});

if (checkResult.data.isAvailable) {
  console.log("可以预订，总价:", checkResult.data.totalPrice);
}
```

### 场景 4: 创建订单和支付

```typescript
import { bookingApi } from "@/api";

// 创建订单
const bookingRes = await bookingApi.create({
  userId: 123,
  typeId: 1,
  checkIn: "2024-12-10",
  checkOut: "2024-12-15",
  guestName: "李四",
  guestPhone: "13900139000",
  equipments: [{ equipId: 1, count: 2 }],
});

const bookingId = bookingRes.data.bookingId;

// 支付订单
await bookingApi.pay(bookingId);
```

### 场景 5: 获取我的订单

```typescript
import { bookingApi } from "@/api";

// 获取所有订单
const allBookings = await bookingApi.getMyList(userId);

// 获取待支付订单
const pendingBookings = await bookingApi.getMyList(userId, 0);

// 获取已支付订单
const paidBookings = await bookingApi.getMyList(userId, 1);
```

### 场景 6: 取消订单

```typescript
import { bookingApi } from "@/api";

await bookingApi.cancel(bookingId);
```

### 场景 7: 获取价格日历

```typescript
import { resourceApi } from "@/api";

const calendar = await resourceApi.getCalendar(
  1, // 房型 ID
  "2024-12-01", // 开始日期
  "2024-12-31" // 结束日期
);

console.log(calendar.data.calendarData);
```

### 场景 8: 管理员设置日价格

```typescript
import { adminApi } from "@/api";

await adminApi.setDailyPrice({
  typeId: 1,
  dates: ["2024-12-25", "2024-12-26", "2024-12-27"],
  price: 599.99,
});
```

### 场景 9: 获取收入报表

```typescript
import { adminApi } from "@/api";

const report = await adminApi.getDailyReport("2024-12-01", "2024-12-31");

console.log("总收入:", report.data.totalRevenue);
console.log("日数据:", report.data.dailyData);
```

---

## 🛠️ 常用工具函数

### 日期处理

```typescript
import { formatDate, getDateRange, daysBetween } from "@/types";

// 格式化日期
const dateStr = formatDate(new Date()); // '2024-12-08'

// 获取日期范围
const dates = getDateRange("2024-12-01", "2024-12-05");
// ['2024-12-01', '2024-12-02', '2024-12-03', '2024-12-04', '2024-12-05']

// 计算天数
const nights = daysBetween("2024-12-10", "2024-12-15"); // 5
```

### 预订验证

```typescript
import {
  isValidDateRange,
  calculateNights,
  isValidEquipmentSelection,
} from "@/utils/api-helpers";

// 验证日期范围
const valid = isValidDateRange("2024-12-10", "2024-12-15"); // true

// 计算夜数
const nights = calculateNights("2024-12-10", "2024-12-15"); // 5

// 验证装备选择
const equipValid = isValidEquipmentSelection([
  { equipId: 1, count: 2 },
  { equipId: 2, count: 1 },
]); // true
```

### 价格计算

```typescript
import {
  calculateBasePrice,
  calculateEquipmentPrice,
  calculateTotalPrice,
} from "@/utils/api-helpers";

// 计算基础房价
const basePrice = calculateBasePrice(300, 5); // 300 * 5 = 1500

// 计算装备费用
const equipPrice = calculateEquipmentPrice(100, 2, 5); // 100 * 2 * 5 = 1000

// 计算总价
const total = calculateTotalPrice(1500, 1000, 0.1); // 2750
```

### 验证函数

```typescript
import {
  isValidPhone,
  isValidUsername,
  validatePasswordStrength,
} from "@/utils/api-helpers";

// 验证电话
isValidPhone("13800138000"); // true

// 验证用户名
isValidUsername("john_doe"); // true

// 验证密码强度
const strength = validatePasswordStrength("Abc@1234");
// 'strong' | 'medium' | 'weak'
```

---

## 🎣 使用 Vue Hooks

### 完整的预订流程

```typescript
import { useCompleteBookingFlow } from "@/composables/useApiIntegration";

const {
  step1_login,
  step2_loadResources,
  step3_checkAvailability,
  step4_createOrder,
  step5_pay,
} = useCompleteBookingFlow();

// 第一步：登录
await step1_login("username", "password");

// 第二步：加载资源
await step2_loadResources();

// 第三步：检查可用性
const checkResult = await step3_checkAvailability(
  1,
  "2024-12-10",
  "2024-12-15",
  {}
);

// 第四步：创建订单
const booking = await step4_createOrder(
  1,
  "2024-12-10",
  "2024-12-15",
  "李四",
  "13900139000",
  {}
);

// 第五步：支付
await step5_pay(booking.bookingId);
```

### 用户模块

```typescript
import { useUserModule } from "@/composables/useApiIntegration";

const { login, register, logout, currentUser } = useUserModule();

// 注册
await register("newuser", "password123", "13800138000");

// 登录
await login("newuser", "password123");

// 查看当前用户
console.log(currentUser.value); // { userId, username, token, role }

// 登出
await logout();
```

### 资源模块

```typescript
import { useResourceModule } from "@/composables/useApiIntegration";

const { siteTypes, equipments, loadSiteTypes, loadEquipments } =
  useResourceModule();

// 组件挂载时自动加载
// 可以直接使用 siteTypes.value 和 equipments.value
```

### 预订模块

```typescript
import { useBookingModule } from "@/composables/useApiIntegration";

const {
  myBookings,
  loadMyBookings,
  checkBookingAvailability,
  createBooking,
  payBooking,
  cancelBooking,
} = useBookingModule();

// 加载我的订单
await loadMyBookings(userId, 0); // 获取待支付订单

// 检查可用性
const result = await checkBookingAvailability(
  1,
  "2024-12-10",
  "2024-12-15",
  {}
);

// 创建订单
const booking = await createBooking(
  userId,
  1,
  "2024-12-10",
  "2024-12-15",
  "李四",
  "13900139000",
  {}
);

// 支付
await payBooking(booking.bookingId);

// 取消
await cancelBooking(booking.bookingId);
```

---

## 🐛 常见问题

### Q: API 请求失败怎么办？

**A:** 检查以下几点:

1. 确保后端服务运行在 `http://localhost:8080`
2. 检查 `.env` 中的 `VITE_API_BASE_URL` 配置
3. 打开浏览器 DevTools Network 标签查看请求
4. 查看浏览器 Console 是否有错误信息

### Q: Token 过期怎么办？

**A:** 自动处理，当收到 401 响应时:

- Token 会被自动清除
- 用户会被重定向到登录页
- 需要重新登录

### Q: 如何处理 API 响应错误？

**A:** 所有错误都通过 `ElMessage.error()` 显示，你也可以捕获异常:

```typescript
try {
  await bookingApi.create(params);
} catch (error) {
  console.error("创建订单失败:", error);
}
```

### Q: 如何自定义请求拦截器？

**A:** 编辑 `src/utils/request.ts`，修改 `instance.interceptors`

### Q: 如何禁用 ElMessage 提示？

**A:** 编辑 `src/utils/request.ts`，注释掉 `ElMessage` 相关代码

---

## 📖 详细文档

- **API 完整文档:** `src/api/README.md`
- **实现总结:** `IMPLEMENTATION_SUMMARY.md`
- **类型定义:** `src/types/index.ts`
- **工具函数:** `src/utils/api-helpers.ts`
- **Hooks 示例:** `src/composables/useApiIntegration.ts`

---

## 💡 最佳实践

1. **总是验证用户输入**

```typescript
import { isValidPhone, isValidDateRange } from "@/utils/api-helpers";

if (!isValidDateRange(checkIn, checkOut)) {
  console.error("日期范围无效");
  return;
}
```

2. **使用 Hooks 管理状态**

```typescript
const { myBookings, loadMyBookings } = useBookingModule();

onMounted(() => {
  loadMyBookings(userId);
});
```

3. **缓存常用数据**

```typescript
import { cacheManager } from "@/utils/api-helpers";

const types = cacheManager.get("siteTypes");
if (!types) {
  const res = await resourceApi.getSiteTypes();
  cacheManager.set("siteTypes", res.data, 3600); // 缓存 1 小时
}
```

4. **处理加载状态**

```typescript
const { bookingLoading, loadMyBookings } = useBookingModule()

<button :disabled="bookingLoading">加载订单</button>
```

5. **优雅的错误处理**

```typescript
try {
  await bookingApi.create(params);
} catch (error) {
  // 错误已通过 ElMessage 显示
  // 这里可以记录额外的日志或分析
  console.error("订单创建失败", error);
}
```

---

## 🔗 相关链接

- [Vue 3 官方文档](https://vuejs.org/)
- [Axios 官方文档](https://axios-http.com/)
- [Element Plus 官方文档](https://element-plus.org/)
- [Pinia 官方文档](https://pinia.vuejs.org/)

---

**祝你使用愉快！** 🎉
