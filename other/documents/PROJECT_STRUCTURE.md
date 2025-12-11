# 项目结构总览

## 文件树结构

```
Accommodation_Booking_System/
│
├── README.md                          # 项目主文档
├── DEPLOYMENT.md                      # 部署指南
├── IMPLEMENTATION_REPORT.md           # 实现完成报告
├── start.sh                           # Linux/Mac 启动脚本
├── start.bat                          # Windows 启动脚本
│
├── backend/                           # 后端 Spring Boot 项目
│   ├── pom.xml                        # Maven 配置
│   ├── .gitignore                     # Git 忽略文件
│   ├── README.md                      # 后端说明文档
│   ├── .mvn/                          # Maven Wrapper (可选)
│   │   └── wrapper/
│   │       ├── maven-wrapper.jar
│   │       └── maven-wrapper.properties
│   │
│   └── src/
│       ├── main/
│       │   ├── java/com/camping/
│       │   │   ├── CampingApplication.java          # 应用入口点
│       │   │   │
│       │   │   ├── common/
│       │   │   │   └── Result.java                  # 统一响应包装类
│       │   │   │
│       │   │   ├── config/                          # 配置类
│       │   │   │   ├── CorsConfig.java              # CORS 跨域配置
│       │   │   │   └── ApplicationConfig.java       # 应用配置
│       │   │   │
│       │   │   ├── controller/                      # REST 控制器 (4 个)
│       │   │   │   ├── UserController.java          # 用户 API (6 端点)
│       │   │   │   ├── BookingController.java       # 预订 API (7 端点)
│       │   │   │   ├── ResourceController.java      # 资源 API (8 端点)
│       │   │   │   └── AdminController.java         # 管理员 API (12 端点)
│       │   │   │
│       │   │   ├── dto/                             # 数据传输对象 (7 个)
│       │   │   │   ├── UserRegisterDTO.java
│       │   │   │   ├── UserLoginDTO.java
│       │   │   │   ├── BookingCheckDTO.java
│       │   │   │   ├── BookingCreateDTO.java
│       │   │   │   ├── EquipSelectDTO.java
│       │   │   │   ├── PayDTO.java
│       │   │   │   └── PriceSetDTO.java
│       │   │   │
│       │   │   ├── entity/                          # JPA 实体类 (8 个)
│       │   │   │   ├── User.java
│       │   │   │   ├── SiteType.java
│       │   │   │   ├── Site.java
│       │   │   │   ├── Booking.java
│       │   │   │   ├── Equipment.java
│       │   │   │   ├── DailyPrice.java
│       │   │   │   ├── BookingEquip.java
│       │   │   │   └── OperationLog.java
│       │   │   │
│       │   │   ├── mapper/                          # MyBatis 数据访问层 (8 个接口)
│       │   │   │   ├── UserMapper.java
│       │   │   │   ├── SiteTypeMapper.java
│       │   │   │   ├── SiteMapper.java
│       │   │   │   ├── EquipmentMapper.java
│       │   │   │   ├── BookingMapper.java
│       │   │   │   ├── DailyPriceMapper.java
│       │   │   │   ├── BookingEquipMapper.java
│       │   │   │   └── OperationLogMapper.java
│       │   │   │
│       │   │   ├── service/                         # 业务逻辑层
│       │   │   │   ├── UserService.java             # 用户服务接口
│       │   │   │   ├── BookingService.java          # 预订服务接口
│       │   │   │   ├── ResourceService.java         # 资源服务接口
│       │   │   │   ├── AdminService.java            # 管理员服务接口
│       │   │   │   │
│       │   │   │   └── impl/                        # 服务实现 (4 个)
│       │   │   │       ├── UserServiceImpl.java
│       │   │   │       ├── BookingServiceImpl.java   # 含事务管理
│       │   │   │       ├── ResourceServiceImpl.java
│       │   │   │       └── AdminServiceImpl.java
│       │   │   │
│       │   │   └── util/                            # 工具类 (3 个)
│       │   │       ├── PasswordUtil.java            # 密码加密和验证
│       │   │       ├── JwtUtil.java                 # JWT 令牌处理
│       │   │       └── DateUtil.java                # 日期处理
│       │   │
│       │   └── resources/
│       │       ├── application.yml                  # Spring Boot 配置
│       │       │
│       │       └── mapper/                          # MyBatis XML 映射 (8 个)
│       │           ├── UserMapper.xml
│       │           ├── SiteTypeMapper.xml
│       │           ├── SiteMapper.xml
│       │           ├── EquipmentMapper.xml
│       │           ├── BookingMapper.xml
│       │           ├── DailyPriceMapper.xml
│       │           ├── BookingEquipMapper.xml
│       │           └── OperationLogMapper.xml
│       │
│       └── test/                                     # 测试代码 (可选)
│           └── java/com/camping/
│               ├── BookingServiceImplTest.java
│               └── UserServiceImplTest.java
│
├── frontend/                          # 前端 Vue 3 项目
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   │
│   └── src/
│       ├── main.ts                    # 应用入口
│       ├── App.vue                    # 根组件
│       │
│       ├── api/                       # API 客户端
│       │   ├── request.ts             # 请求工具
│       │   ├── user.ts                # 用户 API
│       │   ├── booking.ts             # 预订 API
│       │   ├── resource.ts            # 资源 API
│       │   ├── admin.ts               # 管理员 API
│       │   └── types/
│       │       └── index.ts           # TypeScript 类型定义
│       │
│       ├── router/                    # 路由配置
│       │   └── index.ts
│       │
│       ├── stores/                    # Pinia 状态管理
│       │   ├── auth.ts
│       │   └── booking.ts
│       │
│       ├── composables/               # 组合函数 (5 个)
│       │   ├── useUserModule.ts
│       │   ├── useResourceModule.ts
│       │   ├── useBookingModule.ts
│       │   ├── useAdminModule.ts
│       │   └── useCompleteBookingFlow.ts
│       │
│       ├── components/                # 可复用组件
│       │   ├── Header.vue
│       │   ├── Navigation.vue
│       │   ├── Footer.vue
│       │   └── ...
│       │
│       ├── views/                     # 页面组件
│       │   ├── AdminDashboard.vue
│       │   ├── BookingConfirm.vue
│       │   └── SiteList.vue
│       │
│       ├── assets/                    # 静态资源
│       │   └── ...
│       │
│       └── utils/                     # 工具函数 (30+)
│           ├── request.ts
│           ├── storage.ts
│           ├── formatter.ts
│           └── ...
│
├── sql/                               # 数据库脚本
│   ├── schema.sql                     # 数据库架构 (表、索引、视图、触发器)
│   ├── views_triggers.sql             # 高级视图和存储过程
│   └── data.sql                       # 初始测试数据
│
└── docker/                            # Docker 配置 (可选)
    ├── Dockerfile                     # 后端镜像定义
    ├── docker-compose.yml             # 编排配置
    └── nginx.conf                     # Nginx 配置
```

## 分层架构

```
表示层 (Presentation Layer)
    ├── Controller (4 个)
    │   ├── UserController
    │   ├── BookingController
    │   ├── ResourceController
    │   └── AdminController
    └── 统一响应 (Result)

业务逻辑层 (Business Logic Layer)
    ├── Service 接口 (4 个)
    │   ├── UserService
    │   ├── BookingService
    │   ├── ResourceService
    │   └── AdminService
    └── Service 实现 (4 个)
        └── @Transactional 事务管理

数据访问层 (Data Access Layer)
    ├── Mapper 接口 (8 个)
    └── MyBatis XML 映射 (8 个)

工具层 (Utility Layer)
    ├── PasswordUtil - 密码处理
    ├── JwtUtil - 令牌处理
    └── DateUtil - 日期处理
```

## 数据库架构

```
核心表
├── users (用户)
├── site_types (营地类型)
├── sites (营地)
├── bookings (预订) [主表]
├── equipments (设备)
├── daily_prices (日价格)
├── booking_equips (预订-设备关系)
└── operation_logs (操作日志)

视图层
├── view_daily_revenue (每日收入)
├── view_site_occupancy (营地利用率)
├── view_user_booking_stats (用户统计)
└── view_equipment_inventory (设备库存)

触发器层
├── trigger_booking_creation (创建审计)
├── trigger_booking_status_change (状态变更审计)
├── trigger_update_booking_time (时间戳更新)
├── trigger_update_user_time (用户时间戳)
└── trigger_update_site_time (营地时间戳)

存储过程
├── cancel_booking_and_release_equipment (取消预订)
├── get_site_price (获取价格)
└── get_available_sites_count (获取可用营地数)
```

## API 路由映射

```
/api
├── /user (用户模块 - 6 个端点)
│   ├── POST /register
│   ├── POST /login
│   ├── GET /info
│   ├── POST /logout
│   ├── PUT /info
│   └── POST /password
│
├── /booking (预订模块 - 7 个端点)
│   ├── POST /check
│   ├── POST /create
│   ├── POST /pay
│   ├── GET /my
│   ├── GET /{id}
│   └── POST /cancel
│
├── /type (资源-营地类型 - 4 个端点)
│   ├── GET /list
│   ├── GET /{id}
│   ├── GET /calendar
│   └── POST /prices
│
├── /equip (资源-设备 - 4 个端点)
│   ├── GET /list
│   ├── GET /{id}
│   ├── POST /stock
│   └── GET /categories
│
└── /admin (管理员 - 12 个端点)
    ├── POST /price/set
    ├── GET /report/daily
    ├── GET /stats/booking
    ├── GET /logs/operation
    ├── GET /sites
    ├── GET /site/{id}
    └── ...
```

## 部署拓扑

```
最终用户
    ↓
浏览器 (http://localhost:5173)
    ↓
    └─→ Nginx (reverse proxy)
           ↓
        Vite Dev Server / 静态服务

API 请求
    ↓
后端 API (http://localhost:8080)
    ↓
Spring Boot Application (8080)
    ├── Controller 层
    ├── Service 层 (@Transactional)
    ├── Mapper 层 (MyBatis)
    └── Database 层
           ↓
    PostgreSQL (localhost:5432)
        ├── 8 个表
        ├── 4 个视图
        ├── 5 个触发器
        ├── 3 个存储过程
        └── 10 个索引
```

## 核心业务流程

```
用户预订流程:
1. 用户注册 → 密码加盐加密 → 存储
2. 用户登录 → JWT 令牌生成 → 返回令牌
3. 查询营地类型 → 获取日历数据
4. 选择日期和营地 → /booking/check
   - 验证设备库存
   - 计算价格分解
   - 返回可用性
5. 确认预订 → /booking/create
   - @Transactional 事务开始
   - 验证设备库存
   - 计算总价
   - 自动分配营地
   - 创建 Booking 记录
   - 创建 BookingEquip 记录
   - @Transactional 事务提交
6. 支付 → /booking/pay
   - 更新预订状态
   - 记录操作日志
7. 取消 → /booking/cancel
   - @Transactional 事务
   - 更新预订状态
   - 删除设备预订
   - 触发器自动审计

价格计算:
总价 = (基础价格 + 动态价格差异) × 夜数 + 设备费用
其中:
- 基础价格: site_types.base_price
- 动态价格: daily_prices.price
- 夜数: DATEDIFF(checkOut, checkIn)
- 设备费用: SUM(equipment.unit_price × quantity)
```

## 关键技术点

1. **事务管理**: @Transactional 在 createOrder() 和 cancelBooking()
2. **并发控制**: 数据库级别的唯一约束和外键
3. **性能优化**: 覆盖性索引和查询优化
4. **安全认证**: JWT 令牌和 SHA-256 加密
5. **日志审计**: 触发器自动记录操作
6. **错误处理**: 统一的 Result 包装类
7. **跨域支持**: CORS 配置允许前端请求
8. **时间管理**: 统一使用 LocalDateTime 和 LocalDate

## 文件数量统计

- **Java 文件**: 50+
- **XML 映射**: 8
- **Vue 组件**: 15+
- **SQL 脚本**: 3
- **配置文件**: 10+
- **文档**: 5
- **总计**: 100+ 个文件

---

**这个项目结构完全遵循 Spring Boot 最佳实践和 Vue 3 项目规范。**
