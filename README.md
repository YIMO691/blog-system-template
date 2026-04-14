# Spring Boot 博客系统模板

一个基于 Spring Boot 3、Thymeleaf、Spring Security 和 MySQL 的博客系统模板，面向“单体应用 + 后台管理 + 二次开发”场景。项目已经具备文章管理、评论审核、通知中心、邮箱验证码注册/找回密码、个人中心、基础统计等能力，适合作为课程设计、毕业设计或中小型内容站点原型。

## 项目概览

### 已实现能力

- 文章系统
  - 首页展示最新文章
  - 文章列表支持分类、标签、关键字搜索
  - 支持按最新、点赞数、浏览量排序
  - 文章详情支持浏览量与点赞
  - 管理员可创建、编辑、删除文章
  - 编辑页支持本地图片上传并插入正文

- 评论与通知
  - 登录用户可发表评论与回复
  - 普通用户评论默认进入审核流，管理员评论可直接通过
  - 支持评论点赞
  - 支持用户通知、管理员待办通知
  - 支持全部已读、删除已读
  - 文章点赞、评论点赞、评论提交与回复已支持异步局部更新
  - 后台评论审核、删除已支持异步局部更新

- 用户与安全
  - Spring Security 表单登录
  - 邮箱验证码注册
  - 邮箱验证码找回密码
  - 个人中心支持昵称、密码、邮箱、手机号维护
  - 记录登录日志和操作日志
  - 支持用户禁言、角色切换
  - 个人中心资料保存已支持异步提交与局部反馈

- 后台管理
  - 仪表盘
  - 用户管理
  - 文章管理
  - 评论审核
  - 基础统计页
  - 用户管理、通知中心、文章删除已支持异步局部更新

### 当前角色模型

- `ROLE_USER`：普通用户，可浏览、评论、管理个人资料
- `ROLE_ADMIN`：管理员身份，用于进入后台；具体后台能力由 `AdminPermission` 决定
- `superAdmin`：超级管理员，拥有完整后台能力并可管理管理员

当前实现中，管理员能力已拆分为独立权限集合，主要包括：

- `ARTICLE_WRITE`
- `ARTICLE_MANAGE`
- `COMMENT_MODERATE`
- `NOTIFICATION_MANAGE`
- `STATS_VIEW`
- `USER_MANAGE`

默认通过后台“设为管理员”提升的账号，不再自动获得“文章写作”和“文章管理”权限；超级管理员仍保留全量权限。

## 技术栈

- 后端：Spring Boot 3.3.2、Spring MVC、Spring Data JPA、Hibernate
- 安全：Spring Security
- 视图：Thymeleaf、Bootstrap 5
- 数据库：MySQL
- 迁移：Flyway
- 邮件：Spring Mail
- 测试：JUnit 5、Spring Boot Test、H2
- 构建：Maven

## 架构分析

### 优点

- 分层比较清晰：`controller -> service -> repository -> entity`
- 依赖选择稳妥，适合 Spring Boot 单体项目起步
- 关键功能较完整，覆盖“内容站点 + 用户中心 + 后台管理”核心闭环
- 已引入 Flyway、测试环境配置、CI 工作流，具备继续工程化的基础
- 上传服务、登录日志、通知、评论审核等模块已经形成可复用模板

### 适合的使用场景

- 课程作业或毕业设计
- 博客/资讯类站点原型
- Spring Boot + Thymeleaf 教学示例
- 单体后台管理系统的扩展示例

### 当前主要限制

- 验证码存储在应用内存中，重启后失效，也不适合多实例部署
- 默认管理员账号由启动初始化写入，密码是固定值，生产环境不安全
- 本地上传文件保存在服务器磁盘，适合开发和轻量部署，不适合大规模生产
- 文档与实现曾出现过信息漂移，后续需要保持 README 与代码同步维护
- 测试覆盖仍偏少，目前主要集中在启动验证和上传服务
- 后台文章状态切换、通知下拉单条操作等交互仍可继续异步化

## 项目结构

```text
src/
├─ main/
│  ├─ java/com/example/blog/
│  │  ├─ common/        # 枚举与通用常量
│  │  ├─ config/        # 安全、初始化、全局异常、配置属性
│  │  ├─ controller/    # 前台控制器
│  │  ├─ controller/admin/
│  │  ├─ dto/           # 表单与页面交互对象
│  │  ├─ entity/        # JPA 实体
│  │  ├─ exception/     # 业务异常
│  │  ├─ repository/    # 数据访问层
│  │  ├─ security/      # 登录成功/失败处理器
│  │  ├─ service/       # 业务接口
│  │  └─ service/impl/  # 业务实现
│  └─ resources/
│     ├─ db/migration/  # Flyway 迁移脚本
│     ├─ static/        # 静态资源
│     ├─ templates/     # Thymeleaf 页面模板
│     ├─ application.yml
│     ├─ application-dev.yml
│     ├─ application-prod.yml
│     └─ application-example.yml
└─ test/
   ├─ java/
   └─ resources/
```

## 核心模块说明

### 文章模块

- `ArticleController` 负责首页、列表、详情、编辑、图片上传等入口
- `ArticleServiceImpl` 负责筛选、排序、摘要生成、点赞、浏览量等业务逻辑
- 文章支持分类和标签，多条件组合查询主要通过 JPA Specification 实现
- 后台文章删除已支持异步局部更新

### 评论模块

- 评论支持树形回复结构
- 普通用户评论默认待审核
- 管理员可在后台通过或删除评论
- 评论点赞状态已改为批量查询，避免逐条 `exists` 带来的 N+1 查询倾向
- 评论提交、回复、管理员删除评论均已支持异步局部刷新

### 用户与认证模块

- 认证基于 Spring Security 表单登录
- 注册和找回密码依赖邮箱验证码
- 登录成功和失败分别写入登录日志
- 用户资料修改会写入操作日志
- 个人中心中的昵称、密码、邮箱、手机号修改已支持异步提交与页内提示

### 通知模块

- 评论审核、角色变更、禁言等动作会触发通知
- 前台用户和后台管理员各自有通知入口
- 通知页“单条已读 / 全部已读 / 删除已读”已支持异步局部更新
- 导航栏铃铛下拉中的“全部标记为已读”已支持异步更新角标与未读列表

### 上传模块

- 当前使用本地磁盘存储
- 已做基础大小限制、扩展名白名单、MIME 校验和路径安全处理

## 运行要求

- JDK 17
- Maven 3.9+
- MySQL 8.x

## 配置说明

### Profile

- `application.yml`：公共配置，默认激活 `dev`
- `application-dev.yml`：开发环境配置
- `application-prod.yml`：生产环境配置
- `application-example.yml`：示例配置模板

### 关键配置项

- 数据源
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

- 邮件
  - `MAIL_HOST`
  - `MAIL_PORT`
  - `MAIL_USERNAME`
  - `MAIL_PASSWORD`
  - `MAIL_PROTOCOL`

- 上传目录
  - `APP_UPLOAD_DIR`

说明：开发环境数据源建议通过环境变量注入，避免将本地真实数据库凭据直接提交到版本库。

### 邮件说明

邮箱验证码功能依赖 `spring.mail.username` 等邮件配置。如果未正确配置，验证码发送会失败。

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4;
```

### 2. 配置应用

建议以 `src/main/resources/application-example.yml` 为参考，补齐数据库和邮件配置。

### 3. 启动项目

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

默认访问地址：

- 首页：`http://localhost:8080/`
- 登录：`http://localhost:8080/auth/login`
- 注册：`http://localhost:8080/auth/register`
- 后台：`http://localhost:8080/admin`

### 4. 构建打包

```bash
mvn clean package
```

生产环境可通过以下方式启动：

```bash
java -jar target/blog-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 默认初始化数据

项目启动时会自动检查并初始化：

- 管理员账号：`admin`
- 默认密码：`admin123456`
- 默认分类：`默认分类`

仅建议用于本地开发和演示环境，生产环境请改为安全的初始化方案。

## 测试与 CI

### 本地测试

```bash
mvn test
```

当前已包含：

- `SmokeTest`：验证 Spring 上下文能正常启动
- `LocalUploadServiceTest`：验证本地上传服务的基本行为

### CI

GitHub Actions 已配置基础流水线：

- `mvn -B -ntp test`
- `mvn -B -ntp -DskipTests package`

## 整体建议

### 优先级高

1. 安全配置外置化
   - 避免在本地配置中保留真实数据库账号、密码
   - 管理员默认密码不要写死在初始化逻辑中

2. 验证码能力升级
   - 当前验证码存在内存中，建议迁移到 Redis
   - 同时补充过期清理、限流、失败次数控制

3. 增加核心业务测试
   - 补充用户注册、评论审核、文章发布、权限控制等服务层测试
   - 为重要控制器补充集成测试

4. 完善权限边界
   - 当前已完成“角色”和“后台能力”解耦，但文章权限仍可继续细分授权方式
   - 若未来支持作者体系，建议在管理员之外单独引入作者角色

### 优先级中

1. 优化查询性能
   - 已完成评论点赞批量查询与后台统计按日聚合查询
   - 列表、统计类接口仍可继续引入更明确的投影查询和聚合优化

2. 提升部署能力
   - 上传文件可迁移到 OSS、S3 或 MinIO
   - 邮件、上传、缓存、日志等都建议外部化配置

3. 提升可维护性
   - 抽离统一响应和错误码
   - 对 controller 中的部分权限判断和分支逻辑继续下沉到 service
   - 补充更明确的 README、部署文档和接口说明

4. 完成剩余异步交互
   - 后台文章状态切换可继续扩展为异步按钮
   - 通知下拉中的单条通知操作可继续做局部更新

### 可扩展方向

- 引入 Redis 做验证码、限流、热点缓存
- 引入 WebSocket 或 SSE 做实时通知
- 增加审计、封禁策略、异常登录提醒
- 增加对象存储和 CDN
- 增加 OpenAPI 文档或前后端分离接口层

## 已知现状说明

- 默认激活 `dev` 配置，部署生产环境时应显式指定 `prod`
- 评论系统支持回复，但更深层级的复杂运营规则仍可继续完善
- 当前项目更适合作为模板和二开基础，而不是直接无改造上线

## 许可证

当前仓库未显式声明许可证。若计划公开分发或团队协作，建议补充 `LICENSE` 文件。
