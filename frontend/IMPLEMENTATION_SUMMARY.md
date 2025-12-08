# 前端 API 接口实现总结

## 📋 项目完成情况

本文档汇总了根据后端 API 文档和架构设计，为前端项目实现的所有 API 接口和相关工具。

---

## 📁 目录结构

```
frontend/
├── src/
│   ├── api/                          # 📌 API 接口层 (核心)
│   │   ├── index.ts                 # API 统一导出入口
│   │   ├── user.ts                  # 用户认证相关接口
│   │   ├── booking.ts               # 预订业务相关接口
│   │   ├── resource.ts              # 资源查询相关接口
│   │   ├── admin.ts                 # 管理员操作相关接口
│   │   └── README.md                # API 详细使用文档
│   ├── utils/                        # 🔧 工具函数
│   │   ├── request.ts               # HTTP 请求工具 (axios 配置)
│   │   └── api-helpers.ts           # API 辅助函数库
│   ├── types/                        # 📝 类型定义
│   │   └── index.ts                 # 共享类型定义和工具函数
│   └── composables/                  # 🎣 Vue 3 Composition API
│       └── useApiIntegration.ts      # API 集成 Hooks
├── .env.example                      # 环境配置示例
└── package.json
```

---

## 🎯 核心功能模块

### 1. 用户认证模块 (`api/user.ts`)

**✅ 已实现接口:**

| 接口方法           | 功能描述             | 返回类型        |
| ------------------ | -------------------- | --------------- |
| `register()`       | 用户注册             | `void`          |
| `login()`          | 用户登录，返回 Token | `LoginResponse` |
| `getCurrentUser()` | 获取当前用户信息     | `UserInfo`      |
| `logout()`         | 用户登出             | `void`          |
| `updateUserInfo()` | 更新用户信息         | `void`          |
| `changePassword()` | 修改密码             | `void`          |

**类型定义:**

```typescript
export interface UserRegisterParams {
  username: string;
  password: string;
  phone: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  token: string;
  role: string;
}
```

---

### 2. 资源查询模块 (`api/resource.ts`)

**✅ 已实现接口:**

| 接口方法                    | 功能描述           | 返回类型                |
| --------------------------- | ------------------ | ----------------------- |
| `getSiteTypes()`            | 获取房型列表       | `SiteType[]`            |
| `getSiteTypeDetail()`       | 获取房型详情       | `SiteType`              |
| `getCalendar()`             | 获取价格日历       | `PriceCalendarResponse` |
| `getDailyPrices()`          | 获取指定日期的价格 | `DailyPrice[]`          |
| `getEquipments()`           | 获取装备列表       | `Equipment[]`           |
| `getEquipmentDetail()`      | 获取装备详情       | `Equipment`             |
| `getEquipmentStock()`       | 获取装备库存       | `Equipment[]`           |
| `getEquipmentCategories()`  | 获取装备分类       | `string[]`              |
| `getEquipmentsByCategory()` | 按分类获取装备     | `Equipment[]`           |

**类型定义:**

```typescript
export interface SiteType {
  typeId: number;
  typeName: string;
  basePrice: number;
  maxGuests: number;
  description?: string;
  imageUrl?: string;
}

export interface Equipment {
  equipId: number;
  equipName: string;
  unitPrice: number;
  totalStock: number;
  availableStock?: number;
  description?: string;
  category?: string;
}

export interface CalendarItem {
  date: string;
  price: number;
  available: boolean;
  stock: number;
}
```

---

### 3. 预订核心模块 (`api/booking.ts`)

**✅ 已实现接口:**

| 接口方法          | 功能描述             | 返回类型                |
| ----------------- | -------------------- | ----------------------- |
| `check()`         | 预订前检查库存和价格 | `BookingCheckResponse`  |
| `create()`        | 创建订单 (事务操作)  | `BookingCreateResponse` |
| `pay()`           | 支付订单             | `void`                  |
| `getMyList()`     | 获取我的订单列表     | `BookingInfo[]`         |
| `getDetail()`     | 获取订单详情         | `BookingInfo`           |
| `cancel()`        | 取消订单             | `void`                  |
| `getEquipments()` | 获取订单的装备列表   | `Equipment[]`           |

**类型定义:**

```typescript
export interface BookingCheckParams {
  typeId: number;
  checkIn: string;
  checkOut: string;
  equipments: EquipSelection[];
}

export interface BookingCheckResponse {
  isAvailable: boolean;
  msg: string;
  totalPrice: number;
  priceDetail?: {
    basePrice: number;
    dailyPrices: Array<{ date: string; price: number }>;
    equipmentPrice: number;
    nights: number;
  };
}

export interface BookingInfo {
  bookingId: number;
  userId: number;
  typeId: number;
  siteNo: string;
  checkIn: string;
  checkOut: string;
  guestName: string;
  guestPhone: string;
  totalPrice: number;
  status: number; // 0: 待支付, 1: 已支付, 2: 已取消
  createTime: string;
  updateTime: string;
  equipments?: Array<{
    equipId: number;
    equipName: string;
    count: number;
    price: number;
  }>;
}
```

---

### 4. 管理员模块 (`api/admin.ts`)

**✅ 已实现接口:**

| 接口方法                | 功能描述         | 返回类型         |
| ----------------------- | ---------------- | ---------------- |
| `setDailyPrice()`       | 设置日价格       | `void`           |
| `setDailyPricesBatch()` | 批量设置价格     | `void`           |
| `getDailyReport()`      | 获取日收入报表   | `RevenueReport`  |
| `getTypeReport()`       | 获取房型收入报表 | `TypeReport`     |
| `getBookingStats()`     | 获取预订统计     | `BookingStats`   |
| `getTypeStats()`        | 获取房型统计     | `TypeStats[]`    |
| `getOperationLogs()`    | 获取操作日志     | `OperationLog[]` |
| `getAllSites()`         | 获取所有营位     | `SiteInfo[]`     |
| `getSiteDetail()`       | 获取营位详情     | `SiteInfo`       |
| `updateSiteStatus()`    | 更新营位状态     | `void`           |
| `getOccupancyByDate()`  | 获取占用情况     | `any[]`          |
| `getRevenueTrend()`     | 获取收益趋势     | `any[]`          |
| `adjustBookingPrice()`  | 调整订单价格     | `void`           |
| `getUserBehaviorLog()`  | 查询用户行为记录 | `OperationLog[]` |

**类型定义:**

```typescript
export interface PriceSetParams {
  typeId: number;
  dates: string[];
  price: number;
}

export interface BookingStats {
  totalBookings: number;
  paidBookings: number;
  pendingPaymentBookings: number;
  canceledBookings: number;
  totalRevenue: number;
}

export interface TypeStats {
  typeId: number;
  typeName: string;
  basePrice: number;
  totalSites: number;
  availableSites: number;
  occupiedSites: number;
  occupancyRate: number;
  revenue: number;
}
```

---

## 🔧 工具函数

### HTTP 请求工具 (`utils/request.ts`)

```typescript
// Axios 实例，带有:
// - 自动 Token 注入 (从 localStorage)
// - 统一错误处理
// - 业务状态码处理 (code == 1 表示成功)
// - ElMessage 提示消息
// - 自动处理 401 过期重定向

import request from "@/utils/request";
```

### API 辅助函数 (`utils/api-helpers.ts`)

**预订相关:**

```typescript
buildBookingCheckParams(); // 构建预订检查参数
isValidDateRange(); // 验证日期范围
isValidBookingAdvance(); // 验证预订提前天数
calculateNights(); // 计算夜数
hasDateConflict(); // 检查日期冲突
```

**装备相关:**

```typescript
isValidEquipmentSelection(); // 验证装备选择
getTotalEquipmentCount(); // 获取装备总数量
isEquipmentCountExceeded(); // 检查装备数量是否超限
```

**价格相关:**

```typescript
calculateBasePrice(); // 计算基础房价
calculateDynamicPrice(); // 计算动态价格
calculateEquipmentPrice(); // 计算装备费用
calculateTotalPrice(); // 计算总价
```

**日期相关:**

```typescript
getDatesBetween(); // 获取日期范围内所有日期
isWeekend(); // 检查是否周末
getSeason(); // 获取季节
isChineseHoliday(); // 检查是否节假日
```

**验证相关:**

```typescript
isValidPhone(); // 验证电话号码
isValidIdCard(); // 验证身份证号
isValidUsername(); // 验证用户名
validatePasswordStrength(); // 验证密码强度
```

**缓存管理:**

```typescript
cacheManager.set(); // 设置缓存
cacheManager.get(); // 获取缓存
cacheManager.delete(); // 删除缓存
cacheManager.clear(); // 清空缓存
```

---

## 🎣 Vue 3 Composition Hooks

### useApiIntegration.ts

提供了以下可复用 Hooks:

```typescript
// 用户模块
const { currentUser, isLoggedIn, login, register, logout, initializeUser }
  = useUserModule()

// 资源模块
const { siteTypes, equipments, loading, loadSiteTypes, loadEquipments }
  = useResourceModule()

// 预订模块
const { myBookings, selectedBooking, bookingLoading, loadMyBookings, ... }
  = useBookingModule()

// 管理员模块
const { revenueReport, typeStats, adminLoading, setDailyPrice, ... }
  = useAdminModule()

// 完整的预订流程
const { step1_login, step2_loadResources, step3_checkAvailability, ... }
  = useCompleteBookingFlow()
```

---

## 📝 类型定义

### 共享类型 (`types/index.ts`)

```typescript
// 统一响应格式
export interface ApiResponse<T>

// 分页相关
export interface PaginationParams
export interface PaginationResponse<T>

// 时间范围
export interface DateRangeParams

// 枚举类型
export enum BookingStatus
export enum SiteStatus
export enum OperationType
export enum UserRole

// 工具函数
formatDate()                       // 格式化日期
getDateRange()                     // 获取日期范围
daysBetween()                      // 计算天数差
formatCurrency()                   // 格式化金额
getBookingStatusText()             // 获取订单状态文本
getBookingStatusClass()            // 获取订单状态样式类
```

---

## 🚀 使用示例

### 1. 用户注册登录

```typescript
import { useUserModule } from "@/composables/useApiIntegration";

const { login, register } = useUserModule();

// 注册
await register("username", "password123", "13800138000");

// 登录
await login("username", "password123");
```

### 2. 完整预订流程

```typescript
import { useCompleteBookingFlow } from "@/composables/useApiIntegration";

const { step1_login, step3_checkAvailability, step4_createOrder, step5_pay } =
  useCompleteBookingFlow();

// 第一步：登录
await step1_login("user", "pass");

// 第二步：检查可用性
const checkResult = await step3_checkAvailability(
  1,
  "2024-12-10",
  "2024-12-15",
  {}
);

// 第三步：创建订单
const booking = await step4_createOrder(
  1,
  "2024-12-10",
  "2024-12-15",
  "李四",
  "13900139000",
  {}
);

// 第四步：支付
await step5_pay(booking.bookingId);
```

### 3. 在 Vue 组件中使用

```vue
<script setup lang="ts">
import { ref } from "vue";
import { bookingApi, resourceApi } from "@/api";

const siteTypes = ref([]);

onMounted(async () => {
  const response = await resourceApi.getSiteTypes();
  siteTypes.value = response.data;
});

const handleBooking = async () => {
  const result = await bookingApi.check({
    typeId: 1,
    checkIn: "2024-12-10",
    checkOut: "2024-12-15",
    equipments: [],
  });

  if (result.isAvailable) {
    console.log("总价:", result.totalPrice);
  }
};
</script>
```

---

## ⚙️ 环境配置

### .env 配置

```env
# 开发环境
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Camping Booking System

# 生产环境 (.env.production)
VITE_API_BASE_URL=https://api.example.com/api
```

---

## 🔒 Token 管理

Token 在以下情况自动管理:

1. **登录后自动保存:** `localStorage.setItem('token', token)`
2. **请求中自动注入:** `Authorization: Bearer <token>`
3. **过期时自动清理:** 301/401 状态码时清除 token 并重定向登录

---

## 📊 错误处理机制

所有 API 请求都通过统一的 `request.ts` 处理:

- ✅ **成功:** code == 1，返回 `response.data`
- ❌ **失败:** code != 1，通过 `ElMessage.error()` 显示错误
- 🌐 **网络错误:** 自动提示并处理
- 🔐 **认证失败:** 自动清除 token 并重定向

---

## 📦 依赖项

确保在 `package.json` 中安装以下依赖:

```json
{
  "dependencies": {
    "vue": "^3.x.x",
    "axios": "^1.x.x",
    "element-plus": "^2.x.x",
    "pinia": "^2.x.x"
  }
}
```

---

## 📚 文件清单

| 文件路径                               | 描述                    |
| -------------------------------------- | ----------------------- |
| `src/api/index.ts`                     | API 统一导出            |
| `src/api/user.ts`                      | 用户认证接口 (6 个方法) |
| `src/api/booking.ts`                   | 预订业务接口 (7 个方法) |
| `src/api/resource.ts`                  | 资源查询接口 (9 个方法) |
| `src/api/admin.ts`                     | 管理员接口 (14 个方法)  |
| `src/api/README.md`                    | API 详细文档            |
| `src/utils/request.ts`                 | HTTP 请求工具           |
| `src/utils/api-helpers.ts`             | 辅助函数库 (30+ 函数)   |
| `src/types/index.ts`                   | 通用类型和常量          |
| `src/composables/useApiIntegration.ts` | Vue Hooks               |
| `.env.example`                         | 环境配置示例            |

---

## ✨ 总计实现

- **API 接口:** 36 个
- **类型定义:** 15+ 个接口
- **工具函数:** 30+ 个
- **Vue Hooks:** 5 个可复用组合
- **文档:** 完整的使用文档和示例

---

## 🎓 学习资源

- 详细的 API 使用文档: `src/api/README.md`
- 完整的集成示例: `src/composables/useApiIntegration.ts`
- TypeScript 类型参考: `src/types/index.ts`
- 工具函数库: `src/utils/api-helpers.ts`

---

## 🔄 后续建议

1. **前端状态管理 (Pinia):** 创建 Store 来管理用户、预订等全局状态
2. **路由守卫:** 在 `router/index.ts` 中添加需要认证的路由保护
3. **表单验证:** 集成 VeeValidate 进行表单验证
4. **国际化 (i18n):** 支持多语言
5. **单元测试:** 为 API 层和工具函数编写测试用例
6. **错误上报:** 集成 Sentry 上报生产环境错误

---

**项目完成日期:** 2025-12-08

**版本:** 1.0.0

**状态:** ✅ 完整实现并可用
