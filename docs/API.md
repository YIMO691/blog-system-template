# 接口说明

本文档聚焦当前项目中前端 AJAX 调用较多的接口，以及统一响应结构。

## 统一响应结构

AJAX 接口统一返回以下结构：

```json
{
  "ok": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {}
}
```

失败响应同样保持一致：

```json
{
  "ok": false,
  "code": "NO_PERMISSION",
  "message": "无权管理用户",
  "data": null
}
```

## 错误码

当前主要错误码如下：

- `SUCCESS`
- `BAD_REQUEST`
- `VALIDATION_ERROR`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `NO_PERMISSION`
- `NOT_FOUND`
- `CANNOT_MODIFY_SUPER_ADMIN`
- `CANNOT_MUTE_SUPER_ADMIN`
- `INTERNAL_ERROR`

## 认证相关

### 发送邮箱验证码

- `POST /auth/email-code`
- 表单参数：`email`

成功示例：

```json
{
  "ok": true,
  "code": "SUCCESS",
  "message": "验证码发送成功",
  "data": null
}
```

## 文章相关

### 点赞文章

- `POST /articles/{id}/like`

成功示例：

```json
{
  "ok": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "liked": true,
    "likes": 12
  }
}
```

### 提交评论

- `POST /articles/{slug}/comments`

成功示例：

```json
{
  "ok": true,
  "code": "SUCCESS",
  "message": "评论发布成功",
  "data": {
    "approved": true
  }
}
```

### 上传文章图片

- `POST /articles/upload-image`
- 表单参数：`file`
- 返回的 `data.url` 可能是站内路径，也可能是通过 `APP_UPLOAD_PUBLIC_BASE_URL` 生成的完整外部地址

成功示例：

```json
{
  "ok": true,
  "code": "SUCCESS",
  "message": "图片上传成功",
  "data": {
    "url": "/articles/image/xxx.png"
  }
}
```

## 个人中心

### 修改昵称

- `POST /profile/update-nickname`
- 表单参数：`nickname`

### 修改密码

- `POST /profile/update-password`
- 表单参数：`oldPassword`、`newPassword`

### 修改邮箱

- `POST /profile/update-email`
- 表单参数：`newEmail`、`code`

### 修改手机号

- `POST /profile/update-phone`
- 表单参数：`phone`

成功响应中的 `data` 会根据场景返回更新后的 `nickname`、`email`、`emailVerified` 或 `phone`。

## 后台管理

### 切换管理员身份

- `POST /admin/users/{id}/toggle-admin`

### 切换禁言状态

- `POST /admin/users/{id}/toggle-mute`

成功响应中的 `data` 包含：

- `id`
- `isAdmin`
- `muted`
- `superAdmin`
- `permissionLabels`

### 删除文章

- `POST /admin/articles/{id}/delete`

### 审核评论

- `POST /admin/comments/{id}/approve`

### 删除评论

- `POST /admin/comments/{id}/delete`

### 标记通知已读

- `POST /admin/notifications/{id}/read`
- `POST /notifications/read-all`
- `POST /notifications/delete-read`

通知相关成功响应中的 `data` 包含最新的 `unreadCount`。
