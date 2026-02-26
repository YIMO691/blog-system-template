# Spring Boot 个人博客系统（基础模板）

## 技术栈
- Spring Boot 3.x
- Spring MVC + Thymeleaf
- Spring Data JPA
- Spring Security
- MySQL
- Bootstrap 5

## 运行前准备
1. 本地安装 MySQL，并创建数据库：

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4;
```

2. 修改 `src/main/resources/application.yml` 中的 MySQL 账号密码。

## 启动
```bash
mvn spring-boot:run
```

访问：
- 首页：http://localhost:8080/
- 登录：http://localhost:8080/auth/login
- 注册：http://localhost:8080/auth/register
- 后台：http://localhost:8080/admin （需要管理员）

## 默认管理员账号
- username: admin
- password: admin123456

> DataInitializer 会在首次启动时自动创建。

## 你可以用 Cursor 继续扩展
- 文章编辑：支持富文本（Quill/TinyMCE）
- 标签输入：自动补全、多选
- 文章编辑/删除：补齐 CRUD + 权限校验（作者/管理员）
- 搜索升级：正文搜索、全文检索（Elasticsearch）
- 分页/统计/仪表盘
"# blog" 
