# Spring Boot 个人博客系统（基础模板）

一个基于 Spring Boot 3 的轻量博客系统模板，内置文章发布与分类、评论与审核、通知中心、邮箱验证码注册与找回密码、个人中心信息维护等常用能力，适合二次开发。

## 功能总览
- 文章
  - 列表页双栏布局：左侧文章卡片、右侧分类侧边栏（支持快速筛选）
  - 首页仅展示最新 5 篇文章，提供“更多文章”跳转列表
  - 点赞与浏览量统计（详情页点赞按钮）
  - 富文本内容渲染与图片上传（编辑页）
  - 按分类、标签筛选与关键字搜索
- 评论
  - 详情页展示评论列表与回复表单
  - 后台评论审核（通过/删除）
- 通知中心
  - 导航铃铛仅显示未读消息数量与列表
  - 支持“全部标记为已读”“删除已读”
  - 用户与管理员各自的通知页（未读/已读标签页）
- 用户与安全
  - 邮箱验证码注册（5 分钟有效，60 秒限频）
  - 找回密码（邮箱验证码）+ 新旧密码不可相同
  - 登录/注册页面美化与交互优化（密码可视、倒计时、输入校验）
  - 个人中心：昵称、密码修改（确认新密码）、邮箱变更（验证码）、手机号绑定/解绑
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

## 快速开始
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

## 主要页面与交互亮点
- 导航栏
  - 铃铛仅显示未读通知；无未读时显示“暂无未读消息”
  - 一键“全部标记为已读”，入口“查看所有通知”
- 首页
  - 最新 5 篇文章展示，更多跳文章列表
- 文章列表
  - 分类分组展示（默认），支持分组标题滚动定位优化（避免被固定页眉遮挡）
  - 侧边栏快速切换分类，含“全部文章”“未分类”
- 文章详情
  - 返回列表按钮、点赞、浏览量、标签展示
  - 评论列表与发表表单
- 注册与登录
  - 邮箱验证码注册（倒计时与错误提示）
  - 登录/注册/找回密码均支持密码可视切换
- 找回密码
  - 邮箱验证码 + 新密码确认；新旧密码不能相同
- 个人中心
  - 基本信息、禁言标识
  - 修改昵称、修改密码（确认新密码）
  - 修改邮箱（验证码验证，同邮箱前置拦截）
  - 修改手机号（可留空解绑，前置相同性拦截）
- 通知页面
  - 未读/已读分栏，支持查看、标记已读、批量操作

## 常见问题
- 邮件发送失败：`mail_unconfigured`
  - 未配置 `spring.mail.username` 或邮件参数，请按上文“配置邮件”完成配置
- 535 Authentication failed
  - 授权码/账户错误，或未开启 SMTP 服务
- 端口/协议不匹配
  - 465 使用 smtps + ssl.enable=true；587 使用 smtp + starttls.enable=true

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
- 标签自动补全与多选
- 搜索扩展至全文检索（Elasticsearch）
- 通知实时推送（WebSocket）
- Redis 存储验证码与限流
- 更完整的仪表盘与统计图表

## 许可证
本模板主要用于学习与二次开发，按需添加许可证。
