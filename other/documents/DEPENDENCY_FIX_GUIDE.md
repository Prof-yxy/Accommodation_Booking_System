# 解决依赖和编码问题 - 完整指南

## 问题分析

您遇到了两个关键问题：

### 1. 数据库编码错误

```
psql:sql/schema.sql:21: 错误:  编码"GBK"的字符0x0x80 0xe6在编码"UTF8"没有相对应值
```

**原因**：SQL 文件包含 GBK 编码的字符，但 PostgreSQL 数据库使用 UTF-8 编码

**解决方案**：使用新的`schema_utf8.sql`文件（仅包含 ASCII 字符）

---

### 2. Maven 依赖解析失败

```
[ERROR] Could not find artifact io.jsonwebtoken:jjwt:jar:0.11.5 in central
```

**原因**：版本`0.11.5`在 Maven 中央仓库中不存在或不可用

**解决方案**：已将 pom.xml 更新为使用`0.12.3`版本的模块化 JJWT 依赖

---

## 修复步骤

### 步骤 1：重新初始化数据库

#### Windows 用户：

```powershell
# 使用新的batch脚本
cd sql
init-db.bat

# 或手动执行每个SQL文件
$env:PGPASSWORD = "postgres"
psql -h localhost -U postgres -d postgres -c "DROP DATABASE IF EXISTS camping_db;"
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE camping_db ENCODING 'UTF8';"
psql -h localhost -U postgres -d camping_db -f schema_utf8.sql
psql -h localhost -U postgres -d camping_db -f views_triggers.sql
psql -h localhost -U postgres -d camping_db -f data.sql
```

#### Linux/macOS 用户：

```bash
# 使用新的shell脚本
cd sql
chmod +x init-db.sh
./init-db.sh

# 或手动执行
export PGPASSWORD="postgres"
psql -h localhost -U postgres -d postgres -c "DROP DATABASE IF EXISTS camping_db;"
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE camping_db ENCODING 'UTF8';"
psql -h localhost -U postgres -d camping_db -f schema_utf8.sql
psql -h localhost -U postgres -d camping_db -f views_triggers.sql
psql -h localhost -U postgres -d camping_db -f data.sql
```

### 步骤 2：验证 pom.xml 已更新

检查后端项目的`pom.xml`文件，确保 JWT 依赖已更新：

```xml
<!-- Should contain these properties -->
<jjwt-api.version>0.12.3</jjwt-api.version>

<!-- Should contain these dependencies instead of single jjwt -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt-api.version}</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt-api.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt-api.version}</version>
    <scope>runtime</scope>
</dependency>
```

### 步骤 3：清理并重新构建后端

```bash
# 进入后端目录
cd backend

# 清理上次的构建
mvn clean

# 重新下载依赖并构建
mvn install -U
```

### 步骤 4：修复 start.bat 脚本（可选）

如果需要更新`start.bat`以使用新的数据库脚本：

```batch
@echo off
title Camping Booking System

echo ==========================================
echo Camping Booking System - Quick Start
echo ==========================================
echo.

REM [1/4] Initialize database
echo [1/4] Initializing database...
cd sql
call init-db.bat
cd ..
if errorlevel 1 goto error
echo OK: Database initialized
echo.

REM [2/4] Build backend
echo [2/4] Building backend...
cd backend
call mvn clean install -DskipTests
if errorlevel 1 goto error
echo OK: Backend built
echo.

REM [3/4] Build frontend
echo [3/4] Building frontend...
cd ..\frontend
call npm install
call npm run build
if errorlevel 1 goto error
echo OK: Frontend built
echo.

REM [4/4] Start services
echo [4/4] Starting services...
cd ..
cd backend
start "Backend" java -jar target\camping-booking-system-1.0.0.jar
echo OK: Services started
echo.

echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo.
timeout /t 30
goto end

:error
echo.
echo ERROR: Setup failed. Please check the output above.
pause
exit /b 1

:end
pause
```

---

## 文件清单

以下文件已创建/更新：

| 文件                  | 描述                                    |
| --------------------- | --------------------------------------- |
| `sql/schema_utf8.sql` | ✅ UTF-8 编码的数据库 schema（新建）    |
| `sql/init-db.bat`     | ✅ Windows 数据库初始化脚本（新建）     |
| `sql/init-db.sh`      | ✅ Linux/macOS 数据库初始化脚本（新建） |
| `backend/pom.xml`     | ✅ 更新了 JJWT 依赖版本（已修改）       |

---

## 验证步骤

### 1. 验证数据库

```sql
-- 连接到数据库
psql -h localhost -U postgres -d camping_db

-- 查询表列表
\dt

-- 应该看到以下表：
-- - users
-- - site_types
-- - sites
-- - bookings
-- - daily_prices
-- - equipment
-- - booking_equips
```

### 2. 验证 Maven 依赖

```bash
cd backend
mvn dependency:tree | grep jjwt

# 应该看到：
# - io.jsonwebtoken:jjwt-api:jar:0.12.3
# - io.jsonwebtoken:jjwt-impl:jar:0.12.3
# - io.jsonwebtoken:jjwt-jackson:jar:0.12.3
```

### 3. 运行应用

```bash
# Windows
.\start.bat

# Linux/macOS
./start.sh
```

---

## 常见问题排查

### Q1: 仍然收到"Can't find artifact"错误

**A**: 清除 Maven 缓存并重新下载

```bash
mvn clean -U
rm -rf ~/.m2/repository/io/jsonwebtoken/
mvn install
```

### Q2: PostgreSQL 连接失败

**A**: 确保：

1. PostgreSQL 正在运行
2. 用户名和密码正确（默认: postgres/postgres）
3. 数据库使用 UTF-8 编码创建

### Q3: 某些 SQL 文件仍有编码问题

**A**: 确保使用新的`schema_utf8.sql`文件，不要使用原始的`schema.sql`

---

## 下一步

完成上述步骤后：

1. ✅ 运行 `.\start.bat` (Windows) 或 `./start.sh` (Linux/macOS)
2. ✅ 访问 http://localhost:5173 查看前端
3. ✅ 测试登录（使用测试账号）
4. ✅ 验证所有功能是否正常

---

**需要帮助？** 联系技术支持或查看 `README.md` 获取更多信息
