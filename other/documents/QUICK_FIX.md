# 快速修复指南 - 解决错误后重新启动

## 📋 问题总结

您的系统遇到了两个错误：

1. **数据库编码错误** ❌

   - 原始 SQL 文件包含 GBK 字符，无法在 UTF-8 数据库中执行

2. **Maven 依赖错误** ❌
   - jjwt 0.11.5 版本在 Maven 仓库中不可用

## ✅ 已完成的修复

我已为您创建和修改了以下文件：

### 新创建的文件：

- ✅ `sql/schema_utf8.sql` - UTF-8 编码的数据库架构（只包含 ASCII 字符）
- ✅ `sql/init-db.bat` - Windows 数据库初始化脚本
- ✅ `sql/init-db.sh` - Linux/macOS 数据库初始化脚本
- ✅ `setup-full-fix.bat` - Windows 一键修复脚本
- ✅ `setup-full-fix.ps1` - PowerShell 一键修复脚本
- ✅ `DEPENDENCY_FIX_GUIDE.md` - 详细修复指南

### 已修改的文件：

- ✅ `backend/pom.xml` - 更新 JJWT 依赖到版本 0.12.3（模块化）

---

## 🚀 立即修复 - 三种方法

### 方法 1：Windows Batch（最简单）⭐ 推荐

```batch
双击 setup-full-fix.bat
```

**优点**：最简单，自动执行所有步骤

---

### 方法 2：Windows PowerShell

```powershell
# 以管理员身份打开PowerShell

# 方法A: 执行完整修复
.\setup-full-fix.ps1

# 方法B: 分步执行
.\setup-env-windows.ps1        # 安装/更新工具
.\setup-full-fix.ps1           # 修复数据库和依赖
```

---

### 方法 3：手动执行（完全控制）

```powershell
# 第1步：初始化数据库
cd sql
.\init-db.bat

# 或手动执行SQL命令
$env:PGPASSWORD = "postgres"
psql -h localhost -U postgres -d postgres -c "DROP DATABASE IF EXISTS camping_db;"
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE camping_db ENCODING 'UTF8';"
psql -h localhost -U postgres -d camping_db -f schema_utf8.sql
psql -h localhost -U postgres -d camping_db -f views_triggers.sql
psql -h localhost -U postgres -d camping_db -f data.sql

# 第2步：构建后端
cd ..\backend
mvn clean install -DskipTests -U

# 第3步：启动系统
cd ..
.\start.bat
```

---

## ✨ 修复后的步骤

### 1️⃣ 验证数据库

```bash
# 连接到数据库
$env:PGPASSWORD = "postgres"
psql -h localhost -U postgres -d camping_db

# 查看所有表
\dt

# 应该看到7个表：
# - users
# - site_types
# - sites
# - bookings
# - daily_prices
# - equipment
# - booking_equips
```

### 2️⃣ 启动系统

```batch
.\start.bat
```

### 3️⃣ 访问应用

- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080/api

### 4️⃣ 测试登录

使用任何有效的用户名和密码登录

---

## 🔧 如果仍然出现问题

### 问题 1：数据库连接失败

```powershell
# 检查PostgreSQL是否运行
psql --version

# 检查连接
$env:PGPASSWORD = "postgres"
psql -h localhost -U postgres -d postgres -c "SELECT 1"

# 如果需要，手动启动PostgreSQL服务
# Windows: services.msc -> PostgreSQL -> Start
```

### 问题 2：Maven 构建仍然失败

```bash
# 清除Maven缓存
mvn clean -U
rm -rf ~/.m2/repository/io/jsonwebtoken/

# 重新构建
mvn install -DskipTests -U
```

### 问题 3：某些 SQL 脚本仍有错误

```bash
# 确认使用的是新的UTF-8文件
# 检查文件是否存在
ls -la sql/schema_utf8.sql

# 如果不存在，从sql文件夹重新复制
```

---

## 📊 文件对照表

| 文件                  | 状态        | 说明                    |
| --------------------- | ----------- | ----------------------- |
| `sql/schema.sql`      | ⚠️ 不要使用 | 包含 GBK 字符，编码错误 |
| `sql/schema_utf8.sql` | ✅ 使用这个 | 纯 ASCII，UTF-8 安全    |
| `backend/pom.xml`     | ✅ 已更新   | jjwt 0.12.3（模块化）   |
| `sql/init-db.bat`     | ✅ 新建     | Windows 数据库初始化    |
| `sql/init-db.sh`      | ✅ 新建     | Linux/macOS 初始化      |
| `setup-full-fix.bat`  | ✅ 新建     | 完整修复脚本            |
| `setup-full-fix.ps1`  | ✅ 新建     | PowerShell 修复脚本     |

---

## 🎯 预期结果

修复完成后，您应该看到：

```
[1/4] Initializing database...
OK: Database initialized

[2/4] Building backend...
[INFO] BUILD SUCCESS
OK: Backend built

[3/4] Building frontend...
OK: Frontend built

[4/4] Starting services...
Backend: http://localhost:8080
Frontend: http://localhost:5173
```

---

## 📞 需要帮助？

如果按照上述步骤操作仍有问题，请检查：

1. PostgreSQL 是否正确安装和运行
2. Maven 是否正确安装
3. Node.js 和 npm 是否正确安装
4. 网络连接是否正常（下载依赖时需要）
5. 防火墙是否阻止了 localhost 连接

详细信息请参考 `DEPENDENCY_FIX_GUIDE.md`

---

**最后更新**：2024 年 12 月
**系统**：Camping Booking System v1.0.0
