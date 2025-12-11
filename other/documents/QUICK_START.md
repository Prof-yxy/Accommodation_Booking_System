# 快速开始指南 - Quick Start Guide

## 📌 首次使用 (First Time Setup)

### Windows 用户 (Windows Users)

#### 推荐方式：自动配置（管理员打开 PowerShell）

```powershell
# 一键配置环境
.\setup-env-windows.ps1

# 启动应用
.\start.bat
```

#### 或使用批处理脚本

```cmd
REM 以管理员身份运行
setup-env-windows.bat
start.bat
```

#### 诊断环境问题

```powershell
.\diagnose-env.ps1
```

---

### Linux 用户 (Linux Users)

#### Ubuntu/Debian

```bash
# 一键配置
chmod +x setup-env-linux.sh
sudo bash setup-env-linux.sh

# 启动应用
bash start.sh
```

#### CentOS/RHEL

```bash
chmod +x setup-env-linux.sh
sudo bash setup-env-linux.sh

bash start.sh
```

---

### macOS 用户 (macOS Users)

```bash
# 一键配置
chmod +x setup-env-mac.sh
bash setup-env-mac.sh

# 启动应用
bash start.sh
```

---

## ⚡ 快速命令参考

### 环境检查

```bash
# 检查 Java
java -version
javac -version

# 检查 Maven
mvn -version

# 检查 PostgreSQL
psql --version

# 检查 Node.js
node --version
npm --version
```

### 数据库操作

```bash
# 连接数据库
psql -U postgres -d camping_db

# 初始化数据库（Windows）
psql -U postgres -f sql\schema.sql
psql -U postgres -d camping_db -f sql\views_triggers.sql
psql -U postgres -d camping_db -f sql\data.sql

# 初始化数据库（Linux/Mac）
psql -f sql/schema.sql
psql -d camping_db -f sql/views_triggers.sql
psql -d camping_db -f sql/data.sql
```

### 后端开发

```bash
# 进入后端目录
cd backend

# 编译
mvn clean compile

# 构建 JAR
mvn clean package -DskipTests

# 运行所有测试
mvn test

# 启动应用
java -jar target/camping-booking-system-1.0.0.jar

# 跳过测试快速构建
mvn clean package -DskipTests -q
```

### 前端开发

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 开发模式（热更新）
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 清除缓存
npm cache clean --force
```

---

## 🔌 访问地址

| 服务       | 地址                                  | 说明             |
| ---------- | ------------------------------------- | ---------------- |
| 前端       | http://localhost:5173                 | Vue 3 开发服务器 |
| 后端 API   | http://localhost:8080/api             | Spring Boot API  |
| 数据库     | localhost:5432                        | PostgreSQL       |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档         |

---

## 👤 测试账户

| 用户名 | 密码    | 角色     |
| ------ | ------- | -------- |
| admin1 | (empty) | 管理员   |
| user1  | (empty) | 普通用户 |

---

## 🛠️ 常见问题解决

### 问题 1: "Maven not found"

**解决方案:**

1. 检查 Maven 是否安装：`mvn -version`
2. 如果未安装，运行：`setup-env-windows.ps1`
3. **重启 PowerShell/CMD 后重试**

### 问题 2: "Cannot connect to database"

**解决方案:**

1. 检查 PostgreSQL 是否运行
   - Windows: 在任务管理器查看 `postgres` 进程
   - Linux: `sudo systemctl status postgresql`
   - Mac: `brew services list | grep postgresql`
2. 检查数据库是否初始化：`psql -U postgres -l`
3. 默认连接：`psql -U postgres -d camping_db`

### 问题 3: "Port 5173/8080 already in use"

**解决方案:**

```bash
# Windows: 查找占用端口的进程
netstat -ano | findstr :5173

# Kill 进程 (替换 PID)
taskkill /PID <PID> /F

# Linux/Mac: 查找占用端口的进程
lsof -i :5173

# Kill 进程
kill -9 <PID>
```

### 问题 4: "npm install 失败"

**解决方案:**

```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 问题 5: "后端启动失败"

**解决方案:**

1. 检查 Java 版本：`java -version` (需要 11+)
2. 检查数据库连接：`psql -U postgres -d camping_db`
3. 查看日志：`java -jar target/camping-booking-system-1.0.0.jar 2>&1 | tail -50`

---

## 📚 详细文档

更详细的配置说明请参考：

- **ENVIRONMENT_SETUP.md** - 完整环境配置指南
- **PROJECT_STRUCTURE.md** - 项目结构说明
- **README.md** - 项目概述

---

## 🚀 项目启动流程

### 自动启动（推荐）

```bash
# Windows
.\start.bat

# Linux/Mac
bash start.sh
```

### 手动启动

#### 步骤 1: 初始化数据库

```bash
# Windows
psql -U postgres -f sql\schema.sql
psql -U postgres -d camping_db -f sql\views_triggers.sql
psql -U postgres -d camping_db -f sql\data.sql

# Linux/Mac
psql -f sql/schema.sql
psql -d camping_db -f sql/views_triggers.sql
psql -d camping_db -f sql/data.sql
```

#### 步骤 2: 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/camping-booking-system-1.0.0.jar
```

#### 步骤 3: 启动前端（新终端窗口）

```bash
cd frontend
npm install
npm run dev
```

#### 步骤 4: 打开浏览器

访问 http://localhost:5173

---

## 🔍 诊断工具

### Windows

```powershell
# 检查环境配置
.\diagnose-env.ps1

# 详细输出
.\diagnose-env.ps1 -Verbose
```

### Linux/Mac

```bash
# 检查 Java
java -version && echo "✓ Java OK"

# 检查 Maven
mvn -version && echo "✓ Maven OK"

# 检查 PostgreSQL
psql --version && echo "✓ PostgreSQL OK"

# 检查 Node.js
node --version && npm --version && echo "✓ Node.js OK"
```

---

## 💾 数据库备份与恢复

### 备份数据库

```bash
# Windows/Linux/Mac
pg_dump -U postgres camping_db > backup.sql

# 只备份数据（不备份结构）
pg_dump -U postgres --data-only camping_db > data_backup.sql
```

### 恢复数据库

```bash
# 创建新数据库
createdb camping_db_restore

# 恢复数据
psql -U postgres camping_db_restore < backup.sql
```

---

## 📦 项目结构速查

```
Accommodation_Booking_System/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/       # Java 源码
│   ├── src/main/resources/  # 配置文件
│   └── pom.xml              # Maven 配置
├── frontend/                # Vue 3 前端
│   ├── src/                 # 源码
│   ├── package.json         # npm 配置
│   └── vite.config.ts       # Vite 配置
├── sql/                     # 数据库脚本
│   ├── schema.sql           # 表结构
│   ├── views_triggers.sql   # 视图和触发器
│   └── data.sql             # 初始数据
├── start.bat                # Windows 启动脚本
├── start.sh                 # Linux/Mac 启动脚本
├── setup-env-windows.ps1    # Windows 环境配置
├── setup-env-linux.sh       # Linux 环境配置
├── setup-env-mac.sh         # macOS 环境配置
└── ENVIRONMENT_SETUP.md     # 详细配置指南
```

---

## 🔒 安全提示

1. **生产环境**：修改数据库密码

   ```sql
   ALTER USER postgres WITH PASSWORD 'strong_password';
   ```

2. **修改 JWT 密钥** (在 `application.yml` 中)

   ```yaml
   jwt:
     secret: your-secret-key-min-32-chars
   ```

3. **CORS 配置** (生产环境中限制域名)

4. **SQL 注入防护**：所有 API 都使用参数化查询

---

## 📞 获取帮助

如遇到问题，按以下步骤排查：

1. 运行诊断工具：

   ```powershell
   # Windows
   .\diagnose-env.ps1

   # Linux/Mac
   bash setup-env-linux.sh
   ```

2. 查看详细文档：`ENVIRONMENT_SETUP.md`

3. 检查日志文件

4. 查看各工具官方文档

---

## 📝 版本信息

| 组件        | 版本  | 说明          |
| ----------- | ----- | ------------- |
| JDK         | 11+   | Java 运行环境 |
| Maven       | 3.6+  | 构建工具      |
| Spring Boot | 2.7.8 | 后端框架      |
| PostgreSQL  | 12+   | 数据库        |
| Node.js     | 16+   | 前端运行环境  |
| Vue         | 3     | 前端框架      |
| TypeScript  | 5.x   | 类型系统      |

---

**祝您使用愉快！** 🎉

如有问题，请参考详细文档或运行诊断工具。
