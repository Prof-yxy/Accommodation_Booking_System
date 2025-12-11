# 环境配置指南 - Camping Booking System

## 📋 系统要求

### 最低配置

- **OS**: Windows 7+ / macOS 10.12+ / Linux (Ubuntu 18.04+)
- **磁盘**: 2GB+ 空闲空间
- **内存**: 4GB+ RAM

---

## 🔧 必需工具版本

| 工具       | 最低版本 | 推荐版本  | 用途            |
| ---------- | -------- | --------- | --------------- |
| JDK        | 11       | 11/17/21  | Java 编译与运行 |
| Maven      | 3.6.0    | 3.8.1+    | Java 项目构建   |
| PostgreSQL | 12       | 12/13/14+ | 数据库服务      |
| Node.js    | 16.0.0   | 18+/20+   | 前端构建        |
| npm        | 8.0.0    | 9+        | 包管理          |
| Git        | 2.20+    | 2.40+     | 版本控制        |

---

## ⚙️ Windows 环境配置

### 方式 1: 自动配置（推荐）

```powershell
# 1. 以管理员身份打开 PowerShell
# 2. 运行一键配置脚本
.\setup-env-windows.ps1
```

**脚本会自动：**

- ✅ 检测已安装工具版本
- ✅ 下载缺失的工具
- ✅ 配置环境变量
- ✅ 验证安装成功

---

### 方式 2: 手动配置

#### 1️⃣ 安装 JDK 11

**选项 A: 使用 Chocolatey（推荐）**

```powershell
# 安装 Chocolatey (如果未安装)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
iex (New-Object Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1')

# 安装 JDK 11
choco install openjdk11
```

**选项 B: 官方下载**

1. 访问 [Oracle JDK 11](https://www.oracle.com/cn/java/technologies/javase/jdk11-archive-downloads.html)
2. 下载 Windows x64 安装包
3. 运行安装程序，选择默认路径

**选项 C: OpenJDK**

1. 访问 [Adoptium](https://adoptium.net/)
2. 下载 OpenJDK 11 (LTS) for Windows
3. 安装到 `C:\Program Files\jdk-11.0.x`

**验证安装：**

```powershell
java -version
javac -version
```

---

#### 2️⃣ 安装 Maven 3.8.1+

**选项 A: Chocolatey**

```powershell
choco install maven
```

**选项 B: 手动安装**

1. 下载 Maven 3.8.1+ 二进制文件

   - 访问 [Maven 官方下载](https://maven.apache.org/download.cgi)
   - 下载 `apache-maven-3.8.1-bin.zip`

2. 解压到路径（如 `C:\Tools\maven`）

   ```powershell
   Expand-Archive apache-maven-3.8.1-bin.zip -DestinationPath C:\Tools
   Rename-Item C:\Tools\apache-maven-3.8.1 maven
   ```

3. 配置环境变量

   ```powershell
   # 以管理员身份打开 PowerShell
   [Environment]::SetEnvironmentVariable('MAVEN_HOME', 'C:\Tools\maven', 'Machine')
   [Environment]::SetEnvironmentVariable('PATH', "$env:PATH;C:\Tools\maven\bin", 'Machine')
   ```

4. 验证安装
   ```powershell
   mvn -version
   ```

---

#### 3️⃣ 安装 PostgreSQL 12+

**选项 A: Chocolatey**

```powershell
choco install postgresql12
```

**选项 B: 官方安装程序**

1. 访问 [PostgreSQL 官方下载](https://www.postgresql.org/download/windows/)
2. 下载安装程序
3. 运行安装，记下密码（默认用户：postgres）
4. 安装路径建议：`C:\Program Files\PostgreSQL\12`

**配置 PostgreSQL：**

```powershell
# 验证安装
psql --version

# 启动 PostgreSQL 服务
net start postgresql-x64-12
```

---

#### 4️⃣ 安装 Node.js + npm

**选项 A: Chocolatey**

```powershell
choco install nodejs
```

**选项 B: 官方安装程序**

1. 访问 [Node.js 官方网站](https://nodejs.org/)
2. 下载 LTS 版本 (18+)
3. 运行安装程序
4. npm 会自动安装

**验证安装：**

```powershell
node --version
npm --version
```

---

#### 5️⃣ 配置 Git（可选）

```powershell
choco install git
# 或访问 https://git-scm.com/download/win
```

---

## 🐧 Linux 环境配置

### Ubuntu/Debian

```bash
# 1. 一键配置脚本（推荐）
chmod +x setup-env-linux.sh
./setup-env-linux.sh

# 2. 或手动配置
sudo apt update
sudo apt install -y openjdk-11-jdk maven postgresql postgresql-contrib nodejs npm git
```

### CentOS/RHEL

```bash
# 一键配置脚本（推荐）
chmod +x setup-env-centos.sh
./setup-env-centos.sh

# 或手动配置
sudo yum install -y java-11-openjdk java-11-openjdk-devel maven postgresql-server postgresql-contrib nodejs npm git
```

---

## 🍎 macOS 环境配置

### 使用 Homebrew（推荐）

```bash
# 1. 安装 Homebrew (如果未安装)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 2. 一键配置脚本
chmod +x setup-env-mac.sh
./setup-env-mac.sh

# 3. 或手动安装
brew install openjdk@11 maven postgresql node git
```

---

## 🗄️ 数据库配置

### PostgreSQL 初始化

```bash
# Windows
psql -U postgres -f sql/schema.sql
psql -U postgres -d camping_db -f sql/views_triggers.sql
psql -U postgres -d camping_db -f sql/data.sql

# Linux/macOS
sudo -u postgres psql -f sql/schema.sql
sudo -u postgres psql -d camping_db -f sql/views_triggers.sql
sudo -u postgres psql -d camping_db -f sql/data.sql
```

### 验证数据库

```bash
# 连接数据库
psql -U postgres -d camping_db

# 列出所有表
\dt

# 显示表结构
\d booking_info

# 退出
\q
```

---

## 📦 Maven 配置（可选）

### 配置 settings.xml（加速下载）

```xml
<!-- 文件位置: ~/.m2/settings.xml (Linux/macOS) 或 C:\Users\[用户名]\.m2\settings.xml (Windows) -->

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <!-- 阿里云镜像 -->
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven Mirror</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
```

---

## 🚀 快速启动

### 方式 1: 自动启动脚本（推荐）

```powershell
# Windows
.\start.bat

# Linux/macOS
bash start.sh
```

### 方式 2: 手动启动

#### 1. 初始化数据库

```bash
psql -U postgres -f sql/schema.sql
psql -U postgres -d camping_db -f sql/views_triggers.sql
psql -U postgres -d camping_db -f sql/data.sql
```

#### 2. 构建后端

```bash
cd backend
mvn clean package -DskipTests
cd ..
```

#### 3. 启动后端服务

```bash
cd backend
java -jar target/camping-booking-system-1.0.0.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/camping_db \
  --spring.datasource.username=postgres \
  --spring.datasource.password=postgres
cd ..
```

#### 4. 启动前端开发服务器

```bash
cd frontend
npm install
npm run dev
cd ..
```

---

## ✅ 环境验证清单

运行以下命令验证环境配置：

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

# 检查 Git
git --version
```

所有命令都应该显示版本号，不应该出现 "命令不找到" 错误。

---

## 🛠️ 故障排查

### 错误: "Maven not found"

**解决方案:**

1. 确认 Maven 已安装：`mvn -version`
2. 如果未安装，按上文安装 Maven
3. 重启 PowerShell/terminal
4. 重新运行启动脚本

### 错误: "PostgreSQL connection failed"

**解决方案:**

1. 检查 PostgreSQL 是否运行：`pg_isready`
2. Windows: 在服务管理器中启动 PostgreSQL
3. Linux: `sudo systemctl start postgresql`
4. 验证用户密码正确（默认：postgres/postgres）

### 错误: "npm not found"

**解决方案:**

1. 检查 Node.js 是否安装：`node -version`
2. npm 应该随 Node.js 自动安装
3. 如果缺失，重新安装 Node.js
4. 清除 npm 缓存：`npm cache clean --force`

### 错误: "Cannot connect to database"

**解决方案:**

1. 确认 PostgreSQL 正在运行
2. 检查数据库名称：`camping_db`
3. 检查用户名密码：`postgres / postgres`
4. 检查连接字符串中的主机和端口：`localhost:5432`

---

## 📝 配置文件位置

| 文件       | 位置                                    | 说明             |
| ---------- | --------------------------------------- | ---------------- |
| 后端配置   | `backend/src/resources/application.yml` | Spring Boot 配置 |
| 前端配置   | `frontend/package.json`                 | Node.js 依赖     |
| 数据库脚本 | `sql/`                                  | SQL 初始化脚本   |

---

## 🔒 安全建议

1. **修改数据库密码**

   ```bash
   ALTER USER postgres WITH PASSWORD 'your_secure_password';
   ```

2. **配置后端环境变量**

   ```bash
   export DB_PASSWORD=your_secure_password
   ```

3. **生产环境配置**
   - 修改 `application.yml` 中的数据库连接
   - 配置 JWT secret key
   - 启用 HTTPS

---

## 📚 参考资源

- [JDK 官方文档](https://docs.oracle.com/en/java/javase/11/)
- [Maven 官方文档](https://maven.apache.org/guides/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [Node.js 官方文档](https://nodejs.org/docs/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

---

## 📞 获取帮助

如遇到问题，请：

1. 查看本文档的 **故障排查** 部分
2. 查看各工具的官方文档
3. 运行诊断脚本：`.\diagnose-env.ps1` (Windows)
