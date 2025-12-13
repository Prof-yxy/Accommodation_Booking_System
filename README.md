# Accommodation Booking System

露营住宿预订系统（开发中）

## 一、 项目总体架构与目录结构

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
│       ├── mapper/             # 数据库映射相关
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

## 二、部署

### 1. Windows系统

首先修改后端资源文件`backend/src/main/resources/application.yml`，将数据库密码字段改为自己的数据库密码。

```yaml
...
  datasource:
    url: jdbc:postgresql://localhost:5432/camping_db
    username: postgres
    password: 5723 # 将这个密码改为自己的PostgreSQL超级用户密码
    driver-class-name: org.postgresql.Driver
...
```

以管理员模式打开 Powershell，进入当前目录，运行以下脚本（基础环境完整性检查与后端配置）：

```powershell
./setup-env-windows.ps1

./setup-full-fix.ps1
```

其中遇到询问是否继续时请全部按回车继续。

随后执行运行脚本：

```powershell
./start.bat
```

随后前端和后端分别在两个终端运行。

后端测试时，使用`localhost:8080/api/...`，省略号部分参考后端源码中的Controller部分。

前端访问URL：`localhost:5173`

默认的两个登录账户：

```
username	password	  role
 admin		 admin		 admin
 user1		 user		 user
```

## 三、TODO与建议

### TODO（按顺序）：

1. 后端源码中的TODO部分
2. 修正前端错误
3. 前端api与后端对接
4. 检查并修复后端与数据库可能出现逻辑错误的地方
5. 优化前端样式

### 建议：

1. 整理项目文件，文档有点多，比较杂乱。一些看上去冗余的文档放在了other文件夹，到后面看情况将内容合并进README.md。保证最后文档最好只留一个README.md.
2. 目前只有windows的脚本有效，其他系统的脚本可以删除或若有时间可以修正
3. ./setup-full-fix.ps1 用于配置数据库、后端和前端，可以把这三个部分分开成三个文件，这样在一个部分配置失败后无需从头开始。
4. ./setup-env-windows.ps1和./setup-full-fix.ps1两个部署文件一个是用于环境完整性检查，另一个是进行后端配置部署，但两者有功能重复的部分，可以简化。同时修改名字使其更加贴切他们各自的功能。
