# Spring Boot 个人博客系统（基础模板）

一个基于 Spring Boot 3 的轻量博客系统模板，内置文章发布与分类/标签/搜索、评论与审核、通知中心、邮箱验证码注册与找回密码、个人中心（账户设置）等常用能力，适合二次开发。UI 使用 Bootstrap 5，专注“开箱即用 + 易扩展”。

## 功能总览
- 文章
  - 列表页双栏布局：左侧文章卡片、右侧分类侧边栏（快速筛选，保持当前排序）
  - 首页展示最新 6 篇文章，提供醒目的“更多文章”按钮跳转列表
  - 点赞与浏览量统计（详情页点赞按钮）
  - 富文本内容渲染与图片上传（编辑页，支持插入并在正文展示）
  - 分类、标签筛选与关键字搜索（搜索支持标题/正文/标签名命中）
  - 排序：最新 | 最热（点赞）| 阅读最多（浏览量），可与分类/标签/搜索叠加，不分先后
  - 标签多选：单篇文章最多 6 个标签（前端与后端双重限制）
- 评论
  - 详情页展示评论列表与回复表单
  - 后台评论审核（通过/删除）
- 通知中心
  - 导航铃铛仅显示未读消息与数量
  - 支持“全部标记为已读”“删除已读”
  - 用户与管理员各自的通知页（未读/已读标签页）
- 用户与安全
  - 邮箱验证码注册（5 分钟有效，60 秒限频）
  - 找回密码（邮箱验证码）+ 新旧密码不可相同
  - 登录/注册页面美化与交互优化（密码可视、倒计时、输入校验）
  - 个人中心（账户设置）：左右布局，包含
    - 基本资料（昵称等）
    - 密码设置（原/新/确认，前端一致性校验）
    - 绑定信息（邮箱验证码变更、手机号绑定/解绑）
    - 登录记录（时间/IP/UA/状态）
    - 操作日志（时间/动作/详情）
  - 登录记录严格接入 Spring Security 成功/失败处理器，精准记录真实登录事件
  - 禁言标识：被禁言用户在个人信息中显示标记
  - 基于角色的权限控制：ROLE_USER / ROLE_ADMIN
- 后台管理
  - 仪表盘与基础统计（示例）
  - 用户管理（禁言/解除禁言）
  - 文章管理、评论审核

## 技术栈
- Spring Boot 3.x、Spring MVC、Thymeleaf
- Spring Data JPA、Hibernate
- Spring Security
- MySQL + HikariCP
- Bootstrap 5、Chart.js（统计图表示例）

## 快速开始（开发环境）
1) 创建数据库
```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4;
```

2) 配置数据库与基础参数  
编辑 `src/main/resources/application.yml`，设置：
- spring.datasource.url/username/password
- JPA 配置（已默认 ddl-auto: update）
- Thymeleaf 与日志级别等

3) 配置邮件（用于邮箱验证码）
- 支持环境变量（推荐）或直接在 yml 中配置：
  - MAIL_HOST（如 smtp.qq.com）
  - MAIL_PORT（465 或 587）
  - MAIL_USERNAME（发信邮箱账户）
  - MAIL_PASSWORD（邮箱授权码）
  - MAIL_PROTOCOL（smtps 或 smtp）

示例（QQ 邮箱，端口 465）：
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.qq.com}
    port: ${MAIL_PORT:465}
    username: ${MAIL_USERNAME:your@qq.com}
    password: ${MAIL_PASSWORD:your-app-code}
    protocol: ${MAIL_PROTOCOL:smtps}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
```

4) 启动
```bash
mvn spring-boot:run
```

访问地址：
- 首页：http://localhost:8080/
- 登录：http://localhost:8080/auth/login
- 注册：http://localhost:8080/auth/register
- 通知中心：导航栏铃铛下拉 / 用户页 / 管理员页
- 后台：http://localhost:8080/admin （需管理员）

> 关于管理员账号  
> 若项目内置 DataInitializer 会自动创建 admin 账号；若未创建，请在数据库中将目标用户的角色改为 `ROLE_ADMIN`，或按需补充初始化脚本。

## 前端交互与样式亮点
- 全站卡片与背景：更现代的卡片圆角与阴影，列表页单独覆盖为柔和背景
- 首页右侧模块：分类以“标签样式”展示，悬停特效；最新评论昵称首字母头像 + 小号排版
- 首页“更多文章”按钮：大号胶囊按钮，居中自适应，带阴影与悬停上浮
- 文章列表
  - 标题更突出（20px/600），悬停变蓝
  - 标签展示区（点击跳转标签筛选，保持当前排序）
  - 分组标题强化（左侧色条，滚动定位）
- 页脚 Footer：统一片段，粘底布局（无内容时仍贴底）

## 主要页面与交互亮点
- 导航栏
  - 铃铛仅显示未读通知；无未读时显示“暂无未读消息”
  - 一键“全部标记为已读”，入口“查看所有通知”
- 首页
  - 最新 6 篇文章展示，更多跳文章列表
- 文章列表
  - 分类分组展示（默认），滚动定位优化（避免被固定页眉遮挡）
  - 排序选择：最新 / 最热 / 阅读最多（与分类/标签/搜索自由叠加）
  - 侧边栏快速切换分类（保持当前排序），含“全部文章”“未分类”
- 文章详情
  - 返回列表按钮、点赞、浏览量、标签展示
  - 评论列表与发表表单
- 注册与登录
  - 邮箱验证码注册（倒计时与错误提示）
  - 登录/注册/找回密码均支持密码可视切换
- 找回密码
  - 邮箱验证码 + 新密码确认；新旧密码不能相同
- 个人中心
  - 左右分栏账户设置：基本资料 / 密码设置 / 绑定信息 / 登录记录 / 操作日志 / 危险区域
  - 登录记录：时间、IP、UA、状态（由认证成功/失败处理器写入）
  - 操作日志：记录账户关键操作（昵称/密码/邮箱/手机号/注销）
- 通知页面
  - 未读/已读分栏，支持查看、标记已读、批量操作

## 路由与能力速览
- 公开路由：`/`、`/articles`、`/articles/search`、`/articles/{slug}`、`/auth/**`
- 需要登录：`/articles/editor/**`、`/profile/**`
- 管理员：`/admin/**`
- 文章列表参数：
  - `category`（Long，可为 -1 表示未分类）、`tag`（String）、`sort`（latest/hot/views）、`page`
- 搜索：`/articles/search?keyword=...&sort=latest|hot|views&page=...`

## 数据模型（节选）
- 文章 Article：标题、内容、分类（可空）、标签集合、浏览量、点赞、发布时间等
- 分类 Category：name、description
- 标签 Tag：name（与文章多对多）
- 评论 Comment：content、article、user（可空）、创建时间、审核状态等
- 通知 Notification：类型、目标用户、内容、链接、已读状态
- 登录记录 LoginRecord：user（可空）、time、ip、ua、success
- 操作日志 ActionLog：user、time、action、detail

> JPA 配置为 `ddl-auto: update`，开发期会自动迁移建表，生产环境请改为 `validate` 并显式维护迁移脚本（Flyway/Liquibase）。

## 登录记录与操作日志实现说明
- Spring Security
  - `LoginSuccessHandler`：在认证成功时写入 LoginRecord(success=true) 与 ActionLog("登录成功")
  - `LoginFailureHandler`：在认证失败时写入 LoginRecord(success=false)，若能定位用户则写入 ActionLog("登录失败")
- 其他账户操作在 ProfileController 中写入对应 ActionLog
- 个人中心页面仅展示最近 20 条（可按需分页拓展）

## 图片上传与正文渲染
- 编辑页支持本地图片上传后自动在正文插入 `<img>`，详情页按 HTML 渲染
- 请确保存储位置与访问接口已在控制器中放行或加鉴权

## 搜索与筛选/排序组合
- 搜索支持标题/正文/标签名模糊匹配
- 排序（最新/最热/阅读最多）可与分类、标签、搜索自由组合，所有链接均保留当前组合条件

## 常见问题
- 邮件发送失败：`mail_unconfigured`
  - 未配置 `spring.mail.username` 或邮件参数，请按上文“配置邮件”完成配置
- 535 Authentication failed
  - 授权码/账户错误，或未开启 SMTP 服务
- 端口/协议不匹配
  - 465 使用 smtps + ssl.enable=true；587 使用 smtp + starttls.enable=true
- 无法找到相关文章（筛选 + 排序）
  - 若显示“分类ID: x”被当作搜索词，请确保模板已使用 `filterLabel` 作为展示文案，排序按钮区分搜索/非搜索模式链接（本模板已修复）

## 项目结构
```
src/
 ├─ main/
 │   ├─ java/com/example/blog/
 │   │   ├─ config/        # 安全、MVC等配置
 │   │   ├─ controller/    # 前台与后台控制器
 │   │   │   └─ admin/     # 后台控制器
 │   │   ├─ entity/        # JPA 实体（Article/Tag/Comment/...）
 │   │   ├─ repository/    # Spring Data JPA 仓储
 │   │   ├─ service/       # 业务服务与实现
 │   │   └─ security/      # 登录成功/失败处理器
 │   └─ resources/
 │       ├─ templates/     # Thymeleaf 模板（auth/article/admin/...）
 │       ├─ static/        # CSS/JS/图片
 │       └─ application.yml
```

## 配置与环境变量
- 基础数据库：`spring.datasource.url` / `username` / `password`
- JPA：`spring.jpa.hibernate.ddl-auto`（开发期 `update`，生产建议 `validate`）
- 邮件：`spring.mail.*`（见上文“配置邮件”）
- 可选环境变量（示例）：
  - `MAIL_HOST`、`MAIL_PORT`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`MAIL_PROTOCOL`
  - `APP_BASE_URL`（用于生成邮件中的链接，可按需扩展）

## 构建与运行
- 开发运行：
  ```
  mvn spring-boot:run
  ```
- 打包可执行 Jar：
  ```
  mvn -DskipTests package
  java -jar target/blog-*.jar
  ```
- 常见 JVM 参数（按需选择）：
  - `-Xms512m -Xmx512m` 固定堆内存
  - `-Dserver.port=8080` 修改端口

## 生产部署示例
- 反向代理（Nginx）：
  ```
  server {
    listen 80;
    server_name your.domain.com;
    location / {
      proxy_pass http://127.0.0.1:8080;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
  }
  ```
- SSL：申请证书后将 `listen 443 ssl` 与 `ssl_certificate`/`ssl_certificate_key` 加入上面配置。
- 数据库迁移：生产环境建议使用 Flyway/Liquibase 管理 SQL 迁移，避免 `ddl-auto` 修改结构。

## 接口速查（选摘）
- 认证与账户
  - `POST /auth/login` 登录
  - `POST /auth/register` 注册（邮箱验证码）
  - `POST /auth/forgot` 找回密码（验证码）
  - `POST /profile/update-nickname|password|email|phone` 账户设置
- 文章与列表
  - `GET /articles` 列表：参数 `category`（Long，-1=未分类）、`tag`（String）、`sort=latest|hot|views`、`page`
  - `GET /articles/search?keyword=...&sort=...` 关键字搜索（标题/正文/标签名）
  - `GET /articles/{slug}` 详情
- 上传与图片
  - `POST /upload/image` 图片上传（编辑页使用）

## 安全与日志
- 登录记录
  - 成功：由 `LoginSuccessHandler` 写入 `LoginRecord(success=true)`，并记录 `ActionLog("登录成功")`
  - 失败：由 `LoginFailureHandler` 写入 `LoginRecord(success=false)`；若用户可识别，记录 `ActionLog("登录失败")`
- 操作日志
  - 在个人中心对昵称/密码/邮箱/手机号/注销等操作写入 `ActionLog`
- 风控建议（可选扩展）
  - 登录失败次数限制、IP/地区告警、短信/邮箱二次验证、验证码/限流（Redis）

## 自定义与主题
- 样式集中于 `static/css/app.css` 与页面内联 `<style>` 块
- 调整列表标题大小/颜色、按钮圆角与投影、卡片阴影等可直接在 CSS 中覆盖
- 后台 Dashboard
  - 当前仅保留 KPI 概览与系统信息；统计图表已移除
  - 如需恢复图表：在 `admin/stats.html` 中添加对应 canvas 与脚本，并在 `AdminController` 注入数据

## 性能优化建议
- HikariCP 连接池参数按生产负载酌情调整
- 页面长列表可做分页/懒加载
- 统计类数据可加入本地缓存/Redis
- 图片上传建议接入对象存储（S3、OSS 等），正文中使用外链

## 代码风格与贡献
- 命名简洁、方法职责单一
- 控制器仅做路由与组装，业务下沉到 Service
- PR 建议：附上变更说明、运行截图与影响范围

## Commit Convention

- init: project initialization
- feat: new feature
- fix: bug fix
- refactor: code improvement
- style: UI adjustment
- docs: documentation update
- chore: config/dependency update

## 开发建议与扩展方向
- 富文本编辑器替换（Quill/TinyMCE/Editor.js）
- 通知实时推送（WebSocket）
- Redis 存储验证码与限流
- 审计与风控：登录失败次数限制、账号冻结、异地登录提醒等
- CI/CD：添加单元测试、GitHub Actions 或 Jenkins Pipeline

## 许可证
本模板主要用于学习与二次开发，按需添加许可证。
