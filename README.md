# Blog System

基于 `Spring Boot 3`、`Spring Security`、`Spring Data JPA`、`Thymeleaf` 和 `MySQL` 的个人博客系统。

项目定位为个人独立博客，采用单体应用架构，覆盖文章发布、评论互动、通知中心、后台管理、权限控制、系统监控、文件上传和数据库迁移等核心能力，适合作为毕业设计、课程设计或二次开发基础项目。

## 项目概览

- 前台支持首页展示、文章列表、文章详情、分类与标签筛选、关键字搜索
- 后台支持文章管理、评论审核、用户管理、通知管理、统计监控
- 文章采用三态模型：`DRAFT`、`PUBLISHED`、`OFFLINE`
- 登录注册支持邮箱验证码与找回密码
- 系统集成 `Flyway`，支持空库初始化与版本化迁移
- 系统提供操作日志、登录记录、数据库与 JVM 运行状态展示

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 3.3.2 |
| Web 层 | Spring MVC |
| 安全框架 | Spring Security 6 |
| 持久层 | Spring Data JPA、Hibernate |
| 模板引擎 | Thymeleaf、thymeleaf-extras-springsecurity6 |
| 数据库 | MySQL 8、H2（测试） |
| 数据迁移 | Flyway |
| 邮件服务 | Spring Mail |
| Markdown | CommonMark |
| 测试 | JUnit 5、Spring Boot Test、Spring Security Test |
| 构建工具 | Maven |

## 核心功能

### 文章与内容管理

- 支持文章创建、编辑、删除、浏览量统计、点赞
- 支持草稿、发布、下架三种文章状态
- 支持 Markdown 写作、预览与本地图片上传
- 支持分类、标签和关键字搜索
- 前台首页、列表、详情和搜索仅展示 `PUBLISHED` 状态文章

### 评论与通知

- 登录用户可发表评论和点赞评论
- 普通用户评论默认进入审核流
- 管理员可审核、删除评论
- 支持站内通知、已读管理和通知列表展示

### 用户与权限

- 支持注册、登录、找回密码
- 支持昵称、邮箱、手机号、密码维护
- 记录登录成功、登录失败和后台操作日志
- 管理员权限按能力项细分，不依赖单一角色粗粒度控制

### 后台与监控

- 提供后台仪表盘与统计页面
- 展示文章、评论、用户等业务统计
- 展示 Java 版本、操作系统、JVM 内存、数据库状态、上传目录空间
- 支持查看最近登录失败记录和最近操作日志

## 角色与权限模型

### 角色说明

- `ROLE_USER`：普通用户，可浏览、评论、点赞、维护个人资料
- `ROLE_ADMIN`：管理员，可进入后台，具体能力由后台权限集合决定
- `superAdmin`：超级管理员，拥有全部后台权限

### 后台权限

系统当前使用的后台能力包括：

- `ARTICLE_WRITE`
- `ARTICLE_MANAGE`
- `COMMENT_MODERATE`
- `NOTIFICATION_MANAGE`
- `STATS_VIEW`
- `USER_MANAGE`

### 文章状态

- `DRAFT`：草稿，前台不可见
- `PUBLISHED`：已发布，前台可见
- `OFFLINE`：已下架，前台不可见，可在后台重新发布

## 项目结构

```text
src/
├─ main/
│  ├─ java/com/example/blog/
│  │  ├─ common/               # 枚举、权限、统一响应结构
│  │  ├─ config/               # 安全配置、初始化配置、全局异常、属性配置
│  │  ├─ controller/           # 前台控制器
│  │  ├─ controller/admin/     # 后台控制器
│  │  ├─ controller/dev/       # 开发环境兼容控制器
│  │  ├─ dto/                  # 表单对象、请求对象
│  │  ├─ entity/               # JPA 实体
│  │  ├─ exception/            # 业务异常
│  │  ├─ repository/           # 数据访问层
│  │  ├─ security/             # 登录成功/失败处理
│  │  ├─ service/              # 业务接口
│  │  └─ service/impl/         # 业务实现
│  └─ resources/
│     ├─ db/migration/         # Flyway 迁移脚本
│     ├─ static/               # 静态资源
│     ├─ templates/            # Thymeleaf 模板
│     ├─ application.yml
│     ├─ application-dev.yml
│     └─ application-prod.yml
└─ test/
   ├─ java/
   └─ resources/
```

## 运行环境

- JDK 17 或 21
- Maven 3.8+
- MySQL 8.x
- 可用的 SMTP 邮件服务

## 快速开始

### 1. 创建数据库

创建一个空的 MySQL 数据库即可，建表由 Flyway 自动完成。

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 准备本地配置

开发环境配置位于 [application-dev.yml](file:///c:/Users/Administrator/Desktop/blog-system-template/blog-system-template/src/main/resources/application-dev.yml)，其中敏感信息通过环境变量或本地私有配置文件注入。

推荐在项目根目录创建 `application-dev.local.yml`，该文件已被忽略，不会提交到仓库。

示例：

```yaml
spring:
  datasource:
    username: root
    password: your-mysql-password
  mail:
    username: your-mail-account
    password: your-mail-password
```

也可以直接使用环境变量：

```bash
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
MAIL_USERNAME
MAIL_PASSWORD
APP_FLYWAY_REPAIR_ON_STARTUP
```

### 3. 启动项目

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

默认访问地址：

- 首页：`http://localhost:8080/`
- 登录：`http://localhost:8080/auth/login`
- 注册：`http://localhost:8080/auth/register`
- 后台：`http://localhost:8080/admin`

### 4. Flyway 说明

- 空库首次启动时，系统会自动执行 `db/migration` 下的迁移脚本
- 如果本地开发过程中修改过已执行的迁移脚本，可能触发 checksum 校验失败
- 开发环境可通过 `APP_FLYWAY_REPAIR_ON_STARTUP=true` 临时执行 repair 后再启动

### 5. 默认管理员说明

系统启动时会初始化默认管理员账号，默认密码来源于配置项：

```text
app.admin.default-password
```

该配置在代码中定义于 [DataInitializer.java](file:///c:/Users/Administrator/Desktop/blog-system-template/blog-system-template/src/main/java/com/example/blog/config/DataInitializer.java)，默认值为 `admin123456`。建议通过环境变量或生产配置覆盖。

## 测试

执行全部测试：

```bash
mvn test
```

项目测试环境使用 `H2` 数据库，包含文章状态流、权限控制和后台统计页面等集成测试。

## 打包与部署

打包：

```bash
mvn clean package -DskipTests
```

运行：

```bash
java -jar target/blog-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境建议：

- 使用独立 MySQL 实例
- 使用环境变量注入数据库和邮箱敏感信息
- 将上传目录放置在非临时路径
- 配置日志输出文件与反向代理

## 相关文档

- 接口文档：`docs/API.md`
- 数据库迁移脚本：`src/main/resources/db/migration/`

## 安全说明

- 不要把数据库密码、邮箱授权码写入仓库
- 开发环境建议使用 `application-dev.local.yml` 保存本地私有配置
- 生产环境建议使用环境变量、配置中心或容器密钥管理方案

## 许可证

本项目仅用于学习、课程设计和二次开发参考。若需对外发布，请根据你的实际情况补充许可证文件与版权说明。
