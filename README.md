这是一个非常标准的**前后端分离**全栈项目。基于你提供的数据库设计和API文档，我为你梳理了项目的整体目录结构，并提供了核心后端Java代码的骨架（包含Controller和Service定义）以及前端Vue3的API层定义。

---

### 一、 项目总体架构与目录结构

建议采用 **Monorepo**（单体仓库）结构管理的两个独立工程，或者两个完全分开的仓库。

```text
camping-system/
├── sql/                        # 数据库脚本
│   ├── schema.sql              # 建表语句 (含索引)
│   ├── views_triggers.sql      # 视图与触发器 (View & Trigger)
│   └── data.sql                # 初始化测试数据 (Mock Data)
├── backend/                    # 后端工程 (Spring Boot + Maven/Gradle)
│   ├── src/main/java/com/camping
│   │   ├── common/             # 通用模块
│   │   │   └── Result.java     # 全局统一返回对象
│   │   ├── config/             # 配置类 (Cors, MybatisPlus/JPA, Swagger)
│   │   ├── controller/         # 控制层 (API 接口定义)
│   │   │   ├── UserController.java
│   │   │   ├── ResourceController.java
│   │   │   ├── BookingController.java
│   │   │   └── AdminController.java
│   │   ├── dto/                # 数据传输对象 (接收前端参数)
│   │   ├── entity/             # 数据库实体类 (对应 UserTable 等)
│   │   ├── mapper/             # 持久层接口 (MyBatis Mapper / JPA Repository)
│   │   └── service/            # 业务逻辑层 (事务控制 @Transactional 在此)
│   │       ├── impl/
│   │       └── BookingService.java
│   └── src/main/resources/
│       └── application.yml     # 数据库配置
├── frontend/                   # 前端工程 (Vue 3 + Vite + TypeScript)
│   ├── src/
│   │   ├── api/                # API 接口统一管理
│   │   │   ├── user.ts
│   │   │   ├── resource.ts
│   │   │   ├── booking.ts
│   │   │   └── admin.ts
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理 (Pinia)
│   │   ├── views/              # 页面视图
│   │   │   ├── Login.vue
│   │   │   ├── SiteList.vue
│   │   │   ├── BookingConfirm.vue
│   │   │   └── AdminDashboard.vue
│   │   ├── App.vue
│   │   └── main.ts
│   └── package.json
└── README.md
```

---
