# Spring Boot 博客系统模板

一个基于 Spring Boot 3、Thymeleaf、Spring Security、JPA 和 MySQL 的单体博客系统模板，覆盖前台内容展示、后台管理、邮箱验证码认证、通知中心、个人中心和基础统计等核心能力，适合作为课程设计、二次开发底座或中小型内容站点原型。

## 项目简介

该项目采用经典的 `controller -> service -> repository -> entity` 分层结构，兼顾页面渲染与部分 AJAX 交互，当前已经具备较完整的博客业务闭环：

- 前台内容浏览：首页、文章列表、文章详情、分类/标签/关键字筛选
- 创作与管理：文章发布、编辑、删除、Markdown 写作与图片上传
- 用户中心：注册、登录、找回密码、昵称/密码/邮箱/手机号维护
- 评论与通知：评论审核、评论点赞、站内通知、后台待办通知
- 后台系统：仪表盘、用户管理、文章管理、评论审核、统计页

## 功能特性

### 内容与创作

- 支持文章发布、编辑、删除、浏览量统计与点赞
- 支持普通写作与 Markdown 双模式编辑
- 支持实时预览、语法辅助、预览折叠和拖拽分栏
- 支持本地图片上传，并按编辑模式自动插入 Markdown 或 HTML
- 支持分类、标签、关键字搜索，以及最新/点赞数/浏览量排序

### 评论与通知

- 登录用户可评论与回复，普通用户评论默认进入审核流
- 管理员可审核、删除评论
- 支持评论点赞
- 支持用户通知与管理员通知
- 支持单条已读、全部已读、删除已读
- 文章点赞、评论点赞、评论提交与回复已支持异步局部更新

### 用户与安全

- 基于 Spring Security 的表单登录
- 邮箱验证码注册与找回密码
- 个人中心支持昵称、密码、邮箱、手机号维护
- 支持登录日志与操作日志记录
- 支持用户禁言、角色切换和后台权限控制
- AJAX 接口已统一为 `ok / code / message / data` 响应结构

### 后台管理

- 仪表盘
- 用户管理
- 文章管理
- 评论审核
- 基础统计页
- 用户管理、通知中心、文章删除等操作已支持异步局部更新

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.3.2、Spring MVC、Spring Data JPA、Hibernate |
| 安全 | Spring Security |
| 视图 | Thymeleaf、Bootstrap 5 |
| 数据库 | MySQL 8 |
| 数据迁移 | Flyway |
| 邮件 | Spring Mail |
| Markdown | CommonMark |
| 测试 | JUnit 5、Spring Boot Test、H2 |
| 构建 | Maven |

## 角色与权限模型

当前权限模型已将“角色”与“后台能力”解耦：

- `ROLE_USER`：普通用户，可浏览内容、发表评论、维护个人资料
- `ROLE_ADMIN`：管理员身份，用于进入后台，具体后台能力由权限集合控制
- `superAdmin`：超级管理员，拥有完整后台权限并可管理管理员

当前内置的后台能力包括：

- `ARTICLE_WRITE`
- `ARTICLE_MANAGE`
- `COMMENT_MODERATE`
- `NOTIFICATION_MANAGE`
- `STATS_VIEW`
- `USER_MANAGE`

默认通过后台“设为管理员”提升的账号，不会自动获得全部文章权限；超级管理员保留全量能力。

## 项目结构

```text
src/
├─ main/
│  ├─ java/com/example/blog/
│  │  ├─ common/                 # 枚举、权限与通用返回结构
│  │  ├─ config/                 # 安全、初始化、全局异常、配置属性
│  │  ├─ controller/             # 前台控制器
│  │  ├─ controller/admin/       # 后台控制器
│  │  ├─ dto/                    # 表单与页面交互对象
│  │  ├─ entity/                 # JPA 实体
│  │  ├─ exception/              # 业务异常
│  │  ├─ repository/             # 数据访问层
│  │  ├─ security/               # 登录成功/失败处理器
│  │  ├─ service/                # 业务接口
│  │  └─ service/impl/           # 业务实现
│  └─ resources/
│     ├─ db/migration/           # Flyway 迁移脚本
│     ├─ static/                 # 静态资源
│     ├─ templates/              # Thymeleaf 模板
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
- `ArticleServiceImpl` 负责筛选、排序、摘要生成、浏览量、点赞等业务逻辑
- `MarkdownService` 负责 Markdown 渲染与摘要纯文本提取
- 写文章页支持普通写作 / Markdown 模式切换、实时预览与拖拽分栏

### 评论模块

- 评论支持树形回复结构
- 普通用户评论默认待审核
- 管理员可在后台通过或删除评论
- 评论点赞已改为批量查询，降低 N+1 查询倾向

### 用户与认证模块

- 注册和找回密码依赖邮箱验证码
- 登录成功与失败分别写入登录日志
- 用户资料修改会写入操作日志
- 认证相关页面的邮箱验证码发送逻辑已收敛为公共前端片段

### 通知模块

- 评论审核、角色变更、禁言等动作会触发通知
- 用户端和后台端各自有通知入口
- 通知页和导航栏通知下拉均已支持异步局部更新

### 上传模块

- 当前使用本地磁盘存储
- 已做基础大小限制、扩展名白名单、MIME 校验和路径安全处理
- 已支持通过 `APP_UPLOAD_PUBLIC_BASE_URL` 抽象上传资源访问地址

## 运行环境

- JDK 17
- Maven 3.9+
- MySQL 8.x
- 可用的 SMTP 邮件服务

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 准备配置

推荐以 `src/main/resources/application-example.yml` 为模板，结合 `application-dev.yml` 或环境变量完成本地配置。

开发环境至少需要确认以下配置：

- 数据库连接
- 邮件发送配置
- 上传目录

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

生产环境启动示例：

```bash
java -jar target/blog-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 配置说明

### Profile 说明

- `application.yml`：公共配置，默认激活 `dev`
- `application-dev.yml`：开发环境配置
- `application-prod.yml`：生产环境配置
- `application-example.yml`：示例模板配置

### 关键环境变量

#### 数据库

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

#### 邮件

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_PROTOCOL`

#### 上传与资源访问

- `APP_UPLOAD_DIR`
- `APP_UPLOAD_PUBLIC_BASE_URL`

#### 其他

- `SPRING_CACHE_TYPE`
- `LOGGING_FILE_NAME`
- `LOGGING_LEVEL_ROOT`

### 邮件配置说明

邮箱验证码功能依赖 `spring.mail.username` 等邮件配置。若邮件服务未配置或授权信息错误，验证码发送将失败。

### 本地配置建议

- 不要在版本库中长期保留真实数据库密码、邮箱授权码等敏感信息
- 开发环境可临时写入 `application-dev.yml` 进行联调，但建议最终改回环境变量
- 生产环境务必使用环境变量或密钥管理系统注入配置

## 默认初始化数据

项目启动时会自动检查并初始化以下内容：

- 管理员账号：`admin`
- 默认密码：`admin123456`
- 默认分类：`默认分类`

该初始化逻辑仅适用于本地开发与演示环境，生产环境应替换为更安全的初始化方案。

## 测试与质量保障

### 本地测试

```bash
mvn test
```

当前已包含的测试：

- `SmokeTest`：验证 Spring 上下文能正常启动
- `LocalUploadServiceTest`：验证本地上传服务的基本行为
- `MarkdownServiceTest`：验证 Markdown 渲染与摘要提取
- `UserServiceImplTest`：验证权限校验、用户状态切换和个人资料更新逻辑

### CI

GitHub Actions 已配置基础流水线：

- `mvn -B -ntp test`
- `mvn -B -ntp -DskipTests package`

## 文档索引

- 部署文档：`docs/DEPLOYMENT.md`
- 接口说明：`docs/API.md`

## 当前现状与限制

- 验证码仍保存在应用内存中，重启后失效，不适合多实例部署
- 默认管理员账号由启动初始化写入，固定密码不适合生产环境
- 上传文件默认保存在本地磁盘，适合开发和轻量部署
- 测试覆盖仍偏少，尚未覆盖所有核心服务与控制器场景
- 后台部分交互仍可继续异步化

## 后续优化建议

### 高优先级

1. 安全配置外置化
   - 避免在仓库中保留真实数据库账号、密码和邮箱授权码
   - 默认管理员密码不要写死在初始化逻辑中

2. 验证码能力升级
   - 将验证码存储迁移到 Redis
   - 增加过期清理、限流和失败次数控制

3. 增加核心业务测试
   - 补充用户注册、评论审核、文章发布、权限控制等服务层测试
   - 为重要控制器补充集成测试

4. 完善权限边界
   - 继续细化文章写作与管理权限
   - 若后续支持作者体系，可在管理员之外引入作者角色

### 中优先级

1. 优化查询性能
   - 已完成评论点赞批量查询与后台统计按日聚合查询
   - 已为统计页 Top/Recent 查询改用投影查询
   - 已为分类、标签列表增加缓存

2. 提升部署能力
   - 已支持通过 `APP_UPLOAD_PUBLIC_BASE_URL` 对接 CDN 或对象存储域名
   - 邮件、上传、缓存、日志已支持环境变量外部化

3. 提升可维护性
   - 已统一 AJAX 响应结构和错误码
   - 已将部分 controller 权限判断和分支逻辑下沉到 service
   - 已补充部署文档与接口说明，后续可继续补 OpenAPI

## 适用场景

- 课程作业或毕业设计
- 博客/资讯类站点原型
- Spring Boot + Thymeleaf 教学示例
- 单体后台管理系统的扩展示例

## 许可证

当前仓库未显式声明许可证。若计划公开分发或团队协作，建议补充 `LICENSE` 文件。
