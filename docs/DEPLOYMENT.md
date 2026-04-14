# 部署说明

本文档给出当前单体博客系统在开发环境与生产环境下的最小部署流程。

## 1. 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.x
- 可用的 SMTP 邮件服务

## 2. 数据库准备

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

建议为应用单独创建数据库账号，并授予该库的最小必要权限。

## 3. 配置方式

项目默认读取 `application.yml`，并激活 `dev` profile。
生产环境请显式指定 `prod`，并通过环境变量覆盖敏感配置。

推荐准备以下环境变量：

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/blog_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8`
- `SPRING_DATASOURCE_USERNAME=blog_user`
- `SPRING_DATASOURCE_PASSWORD=your-password`
- `MAIL_HOST=smtp.example.com`
- `MAIL_PORT=587`
- `MAIL_USERNAME=noreply@example.com`
- `MAIL_PASSWORD=your-mail-password`
- `MAIL_PROTOCOL=smtp`
- `APP_UPLOAD_DIR=/data/blog/uploads`
- `APP_UPLOAD_PUBLIC_BASE_URL=https://cdn.example.com/blog-images/`
- `SPRING_CACHE_TYPE=simple`
- `LOGGING_FILE_NAME=/data/blog/logs/app.log`

## 4. 构建与启动

### 开发环境

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 生产构建

```bash
mvn clean package
```

### 生产启动

```bash
java -jar target/blog-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 5. 首次启动检查项

- 确认 Flyway 已自动执行迁移脚本
- 确认上传目录存在且有读写权限
- 如果使用 CDN、Nginx 静态目录或对象存储回源，确认 `APP_UPLOAD_PUBLIC_BASE_URL` 已正确配置
- 确认邮件配置可正常发送验证码
- 首次登录后立即修改默认管理员密码

## 6. 反向代理建议

如果使用 Nginx，可至少保证以下能力：

- 将 `80/443` 转发到应用端口
- 为上传接口放宽请求体大小限制
- 增加 HTTPS
- 为静态资源开启缓存

示例要点：

- `client_max_body_size 10m;`
- `proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;`
- `proxy_set_header X-Forwarded-Proto $scheme;`

## 7. 生产注意事项

- 默认管理员账号与密码仅适用于本地演示，生产环境应改为安全初始化方案
- 当前验证码仍保存在应用内存中，不适合多实例部署
- 上传文件默认保存在本地磁盘，生产环境可进一步迁移到对象存储
- 当前上传服务已将访问 URL 配置抽象为环境变量，便于后续切换到 MinIO、S3 或 OSS
- 建议将日志、上传目录和数据库做独立备份
