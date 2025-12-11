# 部署指南 (Deployment Guide)

## 系统要求

### 硬件要求

- CPU: 2 核及以上
- 内存: 4GB 及以上
- 磁盘: 10GB 及以上

### 软件要求

- JDK 11+
- PostgreSQL 12+
- Maven 3.6+ (开发环境)
- Docker (容器部署)

## 开发环境部署

### 步骤 1: 数据库初始化

```bash
# 使用 psql 连接到 PostgreSQL
psql -U postgres

# 在 PostgreSQL 提示符下执行以下命令
CREATE DATABASE camping_db ENCODING 'UTF8';
CREATE USER camping_user WITH PASSWORD 'camping_password';
ALTER ROLE camping_user SET client_encoding TO 'utf8';
ALTER ROLE camping_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE camping_user SET default_transaction_deferrable TO on;
ALTER ROLE camping_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE camping_db TO camping_user;

# 切换到新建的数据库
\c camping_db

# 执行 SQL 脚本
\i /path/to/sql/schema.sql
\i /path/to/sql/views_triggers.sql
\i /path/to/sql/data.sql
```

### 步骤 2: 后端构建

```bash
cd backend

# 清理并编译
mvn clean compile

# 运行测试 (可选)
mvn test

# 打包
mvn package

# 启动应用
mvn spring-boot:run
# 或
java -jar target/camping-booking-system-1.0.0.jar
```

### 步骤 3: 前端构建

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

## 测试

### 登录测试

```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password"}'
```

### 预订测试

```bash
# 1. 检查可用性
curl -X POST http://localhost:8080/api/booking/check \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "typeId": 1,
    "checkIn": "2024-01-15",
    "checkOut": "2024-01-17",
    "equipments": [{"equipId": 1, "count": 1}]
  }'

# 2. 创建预订
curl -X POST http://localhost:8080/api/booking/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "typeId": 1,
    "checkIn": "2024-01-15",
    "checkOut": "2024-01-17",
    "userId": 1,
    "guestName": "John Doe",
    "guestPhone": "13800000001",
    "equipments": [{"equipId": 1, "count": 1}]
  }'
```

## 生产环境部署

### 使用 Docker 部署

#### Dockerfile

```dockerfile
# Build stage
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline

COPY backend/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/camping-booking-system-1.0.0.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-Xmx2G -Xms1G"
CMD ["java", "$JAVA_OPTS", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: camping-postgres
    environment:
      POSTGRES_USER: camping_user
      POSTGRES_PASSWORD: camping_password
      POSTGRES_DB: camping_db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql:/docker-entrypoint-initdb.d
    networks:
      - camping-network

  api:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: camping-api
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: camping_db
      DB_USER: camping_user
      DB_PASSWORD: camping_password
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - camping-network
    restart: unless-stopped

  frontend:
    image: node:16-alpine AS build
    working_dir: /app
    copy:
      - ./frontend:/app
    run: npm install && npm run build

    image: nginx:alpine
    container_name: camping-frontend
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    ports:
      - "80:80"
    depends_on:
      - api
    networks:
      - camping-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  camping-network:
    driver: bridge
```

#### 部署命令

```bash
# 构建镜像
docker build -t camping-booking-system:1.0.0 .

# 使用 docker-compose 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f api

# 停止服务
docker-compose down
```

### 使用 Kubernetes 部署 (可选)

#### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: camping-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: camping-api
  template:
    metadata:
      labels:
        app: camping-api
    spec:
      containers:
        - name: api
          image: camping-booking-system:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: DB_HOST
              value: postgres-service
            - name: DB_PORT
              value: "5432"
            - name: DB_NAME
              value: camping_db
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
          livenessProbe:
            httpGet:
              path: /api/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /api/health
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: camping-api-service
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: camping-api
```

```bash
# 创建 secret
kubectl create secret generic db-credentials \
  --from-literal=username=camping_user \
  --from-literal=password=camping_password

# 部署
kubectl apply -f deployment.yaml

# 查看部署
kubectl get pods
kubectl get svc
```

## 性能优化

### 数据库优化

```sql
-- 创建必要的索引
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_check_dates ON bookings(check_in, check_out);
CREATE INDEX idx_daily_prices_type_date ON daily_prices(type_id, specific_date);

-- 定期维护
VACUUM ANALYZE;
REINDEX DATABASE camping_db;
```

### 应用优化

编辑 `application-prod.yml`:

```yaml
spring:
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000

server:
  tomcat:
    max-threads: 200
    min-spare-threads: 10

logging:
  level:
    root: WARN
    com.camping: INFO
```

## 监控和日志

### 配置日志

创建 `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <springProperty name="LOG_FILE" source="logging.file.name"
                  defaultValue="logs/camping.log"/>

  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_FILE}</file>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
      <timeBasedFileNamingAndTriggeringPolicy
        class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>10MB</maxFileSize>
      </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
  </appender>

  <root level="INFO">
    <appender-ref ref="FILE"/>
  </root>
</configuration>
```

### 监控指标

使用 Spring Boot Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

访问 `http://localhost:8080/actuator/health` 检查应用健康状态。

## 故障排除

### 常见错误

| 错误                         | 原因                       | 解决方案                   |
| ---------------------------- | -------------------------- | -------------------------- |
| `Cannot connect to database` | 数据库未运行或连接参数错误 | 检查 PostgreSQL 状态和配置 |
| `JWT token expired`          | Token 过期                 | 重新登录获取新 Token       |
| `Booking conflict`           | 时间段冲突                 | 选择其他日期               |
| `Out of stock`               | 设备库存不足               | 减少数量或选择其他设备     |

### 日志分析

```bash
# 查看最近的错误
tail -100 logs/camping.log | grep ERROR

# 统计错误数量
grep ERROR logs/camping.log | wc -l

# 查看特定时间段的日志
grep "2024-01-15" logs/camping.log
```

## 备份和恢复

### 数据库备份

```bash
# 完整备份
pg_dump -U postgres camping_db > camping_db_backup.sql

# 压缩备份
pg_dump -U postgres camping_db | gzip > camping_db_backup.sql.gz

# 定时备份 (cron job)
0 2 * * * pg_dump -U postgres camping_db | gzip > /backup/camping_db_$(date +\%Y\%m\%d).sql.gz
```

### 恢复数据库

```bash
# 从 SQL 文件恢复
psql -U postgres camping_db < camping_db_backup.sql

# 从压缩文件恢复
gunzip -c camping_db_backup.sql.gz | psql -U postgres camping_db
```

## 维护清单

- [ ] 定期检查数据库备份
- [ ] 监控磁盘空间使用
- [ ] 检查应用日志和错误
- [ ] 更新依赖库
- [ ] 性能基准测试
- [ ] 安全扫描 (OWASP)
- [ ] 用户反馈收集
- [ ] 版本更新计划
