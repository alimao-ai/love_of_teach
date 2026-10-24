# Sprint 3 安全收口说明

## 默认安全配置

- H2 控制台默认关闭，仅 `dev` 配置开启。
- 演示数据默认关闭，仅 `dev` 配置初始化。
- 生产环境通过 `TEACHER_REGISTRATION_CODE` 提供教师注册邀请码；未配置时公开注册关闭。
- 登录失败按“账号 + 来源地址”在进程内限流，默认 5 次失败后阻断 15 分钟。

## 身份与数据完整性

- 公开注册仍只允许教师角色。
- `user_accounts.teacher_id` 增加唯一约束，同一教师档案只能绑定一个账号。
- 用户名与教师绑定并发冲突统一返回 409，不再泄漏数据库异常。

## 文件与 CSV

- 文件上传同时校验扩展名和文件头，支持带 BOM 的 UTF-16/UTF-32 文本。
- 文件落盘后若事务回滚，会注册补偿逻辑删除孤儿文件。
- 外部资源链接只允许绝对 HTTP/HTTPS 地址。
- 下载时对非法或活动型媒体类型回退为 `application/octet-stream`，并设置 `nosniff`。
- 开课统计与导入失败 CSV 共用公式前缀转义。

## 配置示例

```powershell
$env:TEACHER_REGISTRATION_CODE = "replace-with-a-random-secret"
.\mvnw.cmd spring-boot:run
```

```text
TEACHER_REGISTRATION_CODE=replace-with-a-random-secret
LOGIN_MAX_FAILURES=5
LOGIN_BLOCK_MINUTES=15
H2_CONSOLE_ENABLED=false
APP_SEED_ENABLED=false
```

## 验证范围

- 贡献者提供的 `FullRegressionAndEndToEndJourneyTest` 14 个用例。
- 原有 Sprint 1、Sprint 2、Sprint 3 与权限回归。
- 邀请码、教师唯一绑定、登录限流、失败 CSV 注入、UTF-16 文本、危险链接和媒体类型下载回归。
