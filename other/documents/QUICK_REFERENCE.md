# 📋 快速参考卡 - Quick Reference Card

## 🚀 一行启动

### Windows

```powershell
.\setup-env-windows.ps1; .\start.bat
```

### Linux/macOS

```bash
bash setup-env-linux.sh; bash start.sh
```

---

## 🔍 环境诊断

### Windows

```powershell
.\diagnose-env.ps1
```

### Linux/macOS

```bash
java -version && mvn -version && psql --version && node --version
```

---

## 🛠️ 常用命令

### 数据库操作

```bash
# 连接数据库
psql -U postgres -d camping_db

# 备份数据
pg_dump -U postgres camping_db > backup.sql

# 恢复数据
psql -U postgres camping_db < backup.sql

# 查看所有表
\dt
```

### 后端命令

```bash
cd backend

# 编译
mvn clean compile

# 构建 JAR
mvn clean package -DskipTests

# 运行测试
mvn test

# 启动应用
java -jar target/camping-booking-system-1.0.0.jar
```

### 前端命令

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build

# 清除缓存
npm cache clean --force
```

---

## 📍 服务地址

| 服务     | 地址                                  |
| -------- | ------------------------------------- |
| 前端     | http://localhost:5173                 |
| 后端 API | http://localhost:8080/api             |
| API 文档 | http://localhost:8080/swagger-ui.html |
| 数据库   | localhost:5432                        |

---

## 👤 测试账户

```
用户名: admin1 / user1
密码: (空)
```

---

## 📂 文件清单

| 文件                    | 说明                          |
| ----------------------- | ----------------------------- |
| `README.md`             | 项目主文档                    |
| `QUICK_START.md`        | 快速开始指南                  |
| `ENVIRONMENT_SETUP.md`  | 环境配置详细指南              |
| `PROJECT_STRUCTURE.md`  | 项目结构说明                  |
| `DEPLOYMENT.md`         | 部署指南                      |
| `start.bat`             | Windows 启动脚本              |
| `start.sh`              | Linux/macOS 启动脚本          |
| `setup-env-windows.ps1` | Windows 环境配置 (PowerShell) |
| `setup-env-windows.bat` | Windows 环境配置 (Batch)      |
| `setup-env-linux.sh`    | Linux 环境配置                |
| `setup-env-mac.sh`      | macOS 环境配置                |
| `diagnose-env.ps1`      | Windows 诊断工具              |

---

## ⚡ 快速问题解决

### Maven not found

```
→ 重启 PowerShell
→ 运行: .\setup-env-windows.ps1
```

### PostgreSQL connection failed

```
→ 检查: pg_isready
→ 启动: net start postgresql-x64-14
→ 初始化数据库脚本
```

### Port already in use

```powershell
# Windows
netstat -ano | findstr :5173
taskkill /PID <PID> /F

# Linux
lsof -i :5173
kill -9 <PID>
```

### npm install fails

```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

---

## 🔄 工作流程

### 日常开发

1. **启动后端**

   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端**（新终端）

   ```bash
   cd frontend
   npm run dev
   ```

3. **打开浏览器**
   ```
   http://localhost:5173
   ```

### 提交代码前

```bash
# 1. 后端测试
cd backend
mvn test

# 2. 前端构建
cd frontend
npm run build

# 3. 提交代码
git add .
git commit -m "Your message"
git push origin main
```

---

## 🐛 日志查看

### 后端日志

```bash
# 直接输出
java -jar target/camping-booking-system-1.0.0.jar

# 日志文件（配置后）
tail -f logs/application.log
```

### 前端日志

```bash
# 浏览器控制台
F12 → Console 标签

# 构建日志
npm run build 2>&1 | tee build.log
```

### 数据库日志

```bash
# PostgreSQL 日志
tail -f /var/log/postgresql/postgresql.log  # Linux
```

---

## 🔐 修改配置

### 修改数据库连接

```yaml
# backend/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/camping_db
    username: postgres
    password: postgres # 改这里
```

### 修改服务端口

```yaml
# backend
server:
  port: 8080  # 改这里

# frontend (vite.config.ts)
server: {
  port: 5173  # 改这里
}
```

### 修改数据库密码

```sql
ALTER USER postgres WITH PASSWORD 'new_password';
```

---

## 📊 性能检查

```bash
# CPU 占用
top                    # Linux
Get-Process          # Windows PowerShell

# 内存占用
free -h              # Linux
Get-Process java     # Windows

# 磁盘占用
df -h                # Linux
Get-Volume           # Windows PowerShell

# 数据库连接数
SELECT count(*) FROM pg_stat_activity;
```

---

## 🔄 更新依赖

### Maven

```bash
cd backend
mvn dependency:update-properties
mvn clean package -DskipTests
```

### npm

```bash
cd frontend
npm outdated          # 查看过期包
npm update            # 更新包
npm audit fix         # 修复安全问题
```

---

## 🌐 跨域配置（生产环境）

```yaml
# application.yml
cors:
  allowed-origins: https://yourdomain.com
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  exposed-headers: Authorization
  allow-credentials: true
  max-age: 3600
```

---

## 📱 测试 API

### 使用 curl

```bash
# 登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":""}'

# 获取营地列表
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/booking/sites?pageNum=1&pageSize=10
```

### 使用 Postman

1. 导入 API 集合
2. 设置环境变量
3. 运行测试

---

## 🚀 Docker 快速启动

```bash
# 构建镜像
docker build -t camping-booking-system .

# 启动容器
docker run -p 8080:8080 -p 5173:5173 \
  -e DB_HOST=localhost \
  -e DB_PORT=5432 \
  -e DB_NAME=camping_db \
  camping-booking-system
```

---

## 📞 快速获帮助

| 问题         | 查看文件                          |
| ------------ | --------------------------------- |
| 环境安装问题 | ENVIRONMENT_SETUP.md              |
| 项目结构     | PROJECT_STRUCTURE.md              |
| 部署问题     | DEPLOYMENT.md                     |
| 快速开始     | QUICK_START.md                    |
| API 文档     | Swagger UI (8080/swagger-ui.html) |

---

## ✅ 检查清单

启动前检查：

- [ ] Java 已安装 (`java -version`)
- [ ] Maven 已安装 (`mvn -version`)
- [ ] PostgreSQL 已运行 (`pg_isready`)
- [ ] Node.js 已安装 (`node -version`)
- [ ] 数据库已初始化 (`psql -l`)
- [ ] 端口 5173, 8080, 5432 未被占用

---

## 💾 备份与恢复

```bash
# 完整备份
pg_dump -U postgres camping_db > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复备份
psql -U postgres -d camping_db < backup_20241208_120000.sql

# 只备份数据
pg_dump -U postgres --data-only camping_db > data_backup.sql
```

---

**更新日期**: 2024 年 12 月 8 日

**快速帮助**: 遇到问题？运行 `.\diagnose-env.ps1` (Windows) 诊断环境！
