#!/bin/bash
# 快速启动脚本 (Quick Start Script)

set -e

echo "=========================================="
echo "露营营地预订系统 - 快速启动"
echo "=========================================="
echo ""

# 检查前置工具
echo "[1/6] 检查前置工具..."

if ! command -v java &> /dev/null; then
    echo "❌ 未找到 Java，请先安装 JDK 11+"
    exit 1
fi

if ! command -v psql &> /dev/null; then
    echo "❌ 未找到 PostgreSQL 客户端，请先安装 PostgreSQL"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ 未找到 Maven，请先安装 Maven 3.6+"
    exit 1
fi

echo "✓ 所有必要工具已安装"
echo ""

# 初始化数据库
echo "[2/6] 初始化数据库..."

PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -U ${DB_USER:-postgres} -d postgres -f sql/schema.sql > /dev/null 2>&1 || {
    echo "❌ 数据库初始化失败"
    echo "请检查 PostgreSQL 是否运行，以及连接参数是否正确"
    exit 1
}

PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -U ${DB_USER:-postgres} -d camping_db -f sql/views_triggers.sql > /dev/null 2>&1 || {
    echo "❌ 视图和触发器创建失败"
    exit 1
}

PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -U ${DB_USER:-postgres} -d camping_db -f sql/data.sql > /dev/null 2>&1 || {
    echo "❌ 初始数据插入失败"
    exit 1
}

echo "✓ 数据库初始化完成"
echo ""

# 构建后端
echo "[3/6] 构建后端项目..."

cd backend

mvn clean package -DskipTests > /dev/null 2>&1 || {
    echo "❌ 后端构建失败"
    exit 1
}

echo "✓ 后端构建完成"
echo ""

cd ..

# 启动后端
echo "[4/6] 启动后端服务..."

java -jar backend/target/camping-booking-system-1.0.0.jar \
    --spring.datasource.url=jdbc:postgresql://${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_NAME:-camping_db} \
    --spring.datasource.username=${DB_USER:-postgres} \
    --spring.datasource.password=${DB_PASSWORD:-postgres} \
    &

BACKEND_PID=$!

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 10

if ! kill -0 $BACKEND_PID 2>/dev/null; then
    echo "❌ 后端启动失败"
    exit 1
fi

echo "✓ 后端服务已启动 (PID: $BACKEND_PID)"
echo "  API 地址: http://localhost:8080/api"
echo ""

# 构建前端
echo "[5/6] 构建前端项目..."

cd frontend

npm install > /dev/null 2>&1 || {
    echo "❌ 前端依赖安装失败"
    exit 1
}

echo "✓ 前端依赖安装完成"
echo ""

# 启动前端
echo "[6/6] 启动前端开发服务器..."

npm run dev &

FRONTEND_PID=$!

sleep 5

echo "✓ 前端服务已启动 (PID: $FRONTEND_PID)"
echo "  前端地址: http://localhost:5173"
echo ""

echo "=========================================="
echo "✓ 应用启动完成！"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  - 前端应用: http://localhost:5173"
echo "  - 后端 API: http://localhost:8080/api"
echo "  - 数据库: localhost:5432/camping_db"
echo ""
echo "测试账号:"
echo "  - 用户名: user1"
echo "  - 密码: (空字符串)"
echo ""
echo "停止应用: 按 Ctrl+C"
echo ""

# 等待
wait
