# 📚 前端 API 实现 - 文档索引

> 快速导航到项目中的各个文档和文件

---

## 🚀 快速开始

### 我是第一次使用这个项目

👉 **[QUICK_START.md](./QUICK_START.md)** - 5 分钟快速上手指南

### 我想立即看到代码示例

👉 **[src/api/README.md](./src/api/README.md#示例vue-组件中使用)** - API 使用示例

---

## 📖 详细文档

### 一般信息

| 文档                                                           | 描述         | 适合场景           |
| -------------------------------------------------------------- | ------------ | ------------------ |
| [DELIVERY_SUMMARY.md](./DELIVERY_SUMMARY.md)                   | 项目交付总结 | 了解项目整体情况   |
| [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)       | 完整实现总结 | 查看所有实现的功能 |
| [PROJECT_COMPLETION_REPORT.md](./PROJECT_COMPLETION_REPORT.md) | 项目完成报告 | 了解项目完成度     |
| [FILE_MANIFEST.md](./FILE_MANIFEST.md)                         | 文件清单详解 | 查看文件结构和统计 |

### API 和代码

| 文档                                     | 描述         | 适合场景           |
| ---------------------------------------- | ------------ | ------------------ |
| [src/api/README.md](./src/api/README.md) | API 详细文档 | 学习如何使用 API   |
| [src/api/index.ts](./src/api/index.ts)   | API 统一导出 | 查看所有导出的 API |

---

## 🔧 API 模块速查

### 用户认证 (`src/api/user.ts`)

| 方法               | 功能         | 返回类型        |
| ------------------ | ------------ | --------------- |
| `register()`       | 用户注册     | `void`          |
| `login()`          | 用户登录     | `LoginResponse` |
| `getCurrentUser()` | 获取用户信息 | `UserInfo`      |
| `logout()`         | 用户登出     | `void`          |
| `updateUserInfo()` | 更新用户信息 | `void`          |
| `changePassword()` | 修改密码     | `void`          |

👉 **[使用示例](./src/api/README.md#用户认证模块userts)**

### 资源查询 (`src/api/resource.ts`)

| 方法                        | 功能           | 返回类型                |
| --------------------------- | -------------- | ----------------------- |
| `getSiteTypes()`            | 获取房型列表   | `SiteType[]`            |
| `getSiteTypeDetail()`       | 获取房型详情   | `SiteType`              |
| `getCalendar()`             | 获取价格日历   | `PriceCalendarResponse` |
| `getDailyPrices()`          | 获取日价       | `DailyPrice[]`          |
| `getEquipments()`           | 获取装备列表   | `Equipment[]`           |
| `getEquipmentDetail()`      | 获取装备详情   | `Equipment`             |
| `getEquipmentStock()`       | 获取装备库存   | `Equipment[]`           |
| `getEquipmentCategories()`  | 获取装备分类   | `string[]`              |
| `getEquipmentsByCategory()` | 按分类获取装备 | `Equipment[]`           |

👉 **[使用示例](./src/api/README.md#资源查询模块resourcets)**

### 预订业务 (`src/api/booking.ts`)

| 方法              | 功能         | 返回类型                |
| ----------------- | ------------ | ----------------------- |
| `check()`         | 预订前检查   | `BookingCheckResponse`  |
| `create()`        | 创建订单     | `BookingCreateResponse` |
| `pay()`           | 支付订单     | `void`                  |
| `getMyList()`     | 获取订单列表 | `BookingInfo[]`         |
| `getDetail()`     | 获取订单详情 | `BookingInfo`           |
| `cancel()`        | 取消订单     | `void`                  |
| `getEquipments()` | 获取订单装备 | `Equipment[]`           |

👉 **[使用示例](./src/api/README.md#预订核心模块bookingts)**

### 管理员操作 (`src/api/admin.ts`)

| 方法                    | 功能             | 返回类型         |
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
| `getUserBehaviorLog()`  | 查询用户行为     | `OperationLog[]` |

👉 **[使用示例](./src/api/README.md#管理员模块admintsadminti)**

---

## 🎣 Vue Hooks 速查

### `useUserModule()` - 用户认证

```typescript
const { login, register, logout, currentUser } = useUserModule();
```

👉 **[详细说明](./src/composables/useApiIntegration.ts#用户模块示例)**

### `useResourceModule()` - 资源管理

```typescript
const { siteTypes, equipments, loadSiteTypes, loadEquipments } =
  useResourceModule();
```

👉 **[详细说明](./src/composables/useApiIntegration.ts#资源模块示例)**

### `useBookingModule()` - 预订业务

```typescript
const { myBookings, createBooking, cancelBooking } = useBookingModule();
```

👉 **[详细说明](./src/composables/useApiIntegration.ts#预订模块示例)**

### `useAdminModule()` - 管理员操作

```typescript
const { setDailyPrice, loadRevenueReport } = useAdminModule();
```

👉 **[详细说明](./src/composables/useApiIntegration.ts#管理员模块示例)**

### `useCompleteBookingFlow()` - 完整预订流程

```typescript
const { step1_login, step4_createOrder, step5_pay } = useCompleteBookingFlow();
```

👉 **[详细说明](./src/composables/useApiIntegration.ts#组合使用示例)**

---

## 🔧 工具函数速查

### 日期处理

| 函数                | 功能                       |
| ------------------- | -------------------------- |
| `formatDate()`      | 格式化日期为 yyyy-MM-dd    |
| `getDateRange()`    | 获取日期范围内的所有日期   |
| `daysBetween()`     | 计算两个日期之间的天数     |
| `getDatesBetween()` | 获取两个日期之间的所有日期 |

### 预订验证

| 函数                        | 功能                       |
| --------------------------- | -------------------------- |
| `isValidDateRange()`        | 验证日期范围是否有效       |
| `isValidBookingAdvance()`   | 验证是否至少提前多少天预订 |
| `calculateNights()`         | 计算预订的总天数           |
| `hasDateConflict()`         | 检查日期是否冲突           |
| `buildBookingCheckParams()` | 构建预订检查参数           |

### 装备管理

| 函数                          | 功能                 |
| ----------------------------- | -------------------- |
| `isValidEquipmentSelection()` | 验证装备选择是否有效 |
| `getTotalEquipmentCount()`    | 获取选中装备的总数量 |
| `isEquipmentCountExceeded()`  | 检查装备数量是否超限 |

### 价格计算

| 函数                        | 功能             |
| --------------------------- | ---------------- |
| `calculateBasePrice()`      | 计算基础房价     |
| `calculateDynamicPrice()`   | 计算动态价格总额 |
| `calculateEquipmentPrice()` | 计算装备费用     |
| `calculateTotalPrice()`     | 计算总价         |

### 数据验证

| 函数                         | 功能         |
| ---------------------------- | ------------ |
| `isValidPhone()`             | 验证电话号码 |
| `isValidIdCard()`            | 验证身份证号 |
| `isValidUsername()`          | 验证用户名   |
| `validatePasswordStrength()` | 验证密码强度 |

👉 **[所有工具函数](./src/utils/api-helpers.ts)**

---

## 📝 类型定义速查

### API 响应类型

| 类型                    | 描述              |
| ----------------------- | ----------------- |
| `ApiResponse<T>`        | 统一 API 响应格式 |
| `PaginationParams`      | 分页请求参数      |
| `PaginationResponse<T>` | 分页响应数据      |
| `DateRangeParams`       | 时间范围查询参数  |

### 枚举类型

| 枚举            | 值                                    |
| --------------- | ------------------------------------- |
| `BookingStatus` | PENDING, PAID, CANCELED               |
| `SiteStatus`    | NORMAL, MAINTENANCE                   |
| `OperationType` | NEW_ORDER, CANCEL_ORDER, PAY_ORDER... |
| `UserRole`      | USER, ADMIN                           |

👉 **[所有类型定义](./src/types/index.ts)**

---

## ⚙️ 环境配置

### 配置文件位置

- 📄 **[.env.example](./.env.example)** - 环境配置示例

### 快速配置

```bash
# 1. 复制配置文件
copy .env.example .env

# 2. 编辑 .env
VITE_API_BASE_URL=http://localhost:8080/api

# 3. 安装依赖
npm install

# 4. 启动开发
npm run dev
```

---

## 🔍 常见任务

### 我想用 API 获取房型列表

1. 导入：`import { resourceApi } from '@/api'`
2. 使用：`await resourceApi.getSiteTypes()`
3. 详见：[src/api/README.md - 场景 2](./src/api/README.md#场景-2-获取房型和装备列表)

### 我想创建订单

1. 导入：`import { bookingApi } from '@/api'`
2. 使用：`await bookingApi.create({...})`
3. 详见：[src/api/README.md - 场景 4](./src/api/README.md#场景-4-创建订单和支付)

### 我想使用 Hooks

1. 导入：`import { useBookingModule } from '@/composables'`
2. 使用：`const { createBooking } = useBookingModule()`
3. 详见：[src/composables/useApiIntegration.ts](./src/composables/useApiIntegration.ts)

### 我想获取工具函数

1. 导入：`import { calculateNights } from '@/utils/api-helpers'`
2. 使用：`const nights = calculateNights('2024-12-10', '2024-12-15')`
3. 详见：[src/utils/api-helpers.ts](./src/utils/api-helpers.ts)

---

## 📊 项目统计

| 指标      | 数量       |
| --------- | ---------- |
| API 接口  | 36 个      |
| 工具函数  | 30+ 个     |
| 类型定义  | 15+ 个     |
| Vue Hooks | 5 个       |
| 文档页面  | 7 个       |
| **总计**  | **93+ 个** |

---

## 🎓 学习路径

### 初级 (1-2 小时)

- [ ] 阅读 `QUICK_START.md`
- [ ] 理解项目结构
- [ ] 运行示例代码

### 中级 (2-4 小时)

- [ ] 深入学习 API 文档
- [ ] 理解工具函数用法
- [ ] 学会使用 Hooks

### 高级 (4+ 小时)

- [ ] 自定义和扩展 API
- [ ] 集成到实际项目
- [ ] 优化性能

---

## 🆘 需要帮助？

### 快速问题

→ 查看 [QUICK_START.md 中的常见问题](./QUICK_START.md#常见问题)

### API 相关

→ 查看 [src/api/README.md](./src/api/README.md)

### 工具函数相关

→ 查看 [src/utils/api-helpers.ts](./src/utils/api-helpers.ts) 中的代码注释

### 使用 Hooks 相关

→ 查看 [src/composables/useApiIntegration.ts](./src/composables/useApiIntegration.ts)

---

## 📞 相关资源

- [Vue 3 官方文档](https://vuejs.org/)
- [TypeScript 官方文档](https://www.typescriptlang.org/)
- [Axios 官方文档](https://axios-http.com/)
- [Element Plus 官方文档](https://element-plus.org/)

---

## ✅ 验证安装

运行验证脚本检查所有文件是否正确创建：

```powershell
# Windows
powershell -ExecutionPolicy Bypass -File verify.ps1

# Linux/Mac
bash verify.sh
```

---

**上次更新:** 2025-12-08

**项目版本:** 1.0.0

**状态:** ✅ 完整可用
