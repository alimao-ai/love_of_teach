# 爱教学 - 教学质量全链路数字化管理平台

Sprint 1 实现了课程数据底座与核心查询能力，Sprint 2 在此基础上完成资源管理、课程评价和统计报表增量。当前包含：

- 登录、注册、角色识别和基于令牌的接口鉴权
- 学期、院系基础数据的新增、编辑和停用
- 课程与教师数据 CSV 模板下载、批量导入、逐行校验和失败原因导出
- 按学期、院系、教师、关键字筛选课程
- 教师课程简介与开课情况总览
- 课程学生人次、教学资源与督导情况详情
- 按学期、院系统计开课数、已督导数和督导覆盖率
- 课件、教案和视频上传，章节创建与教学资源检索
- 督导/主任课程打分、评语填写与课程督导状态联动
- 课程评价汇总、薄弱课程识别和教师本人反馈查看
- 按学期、院系、教师导出开课情况统计 CSV

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Data JPA、H2、Apache Commons CSV、BCrypt、Bearer Token
- 前端：Vue 3、TypeScript、Vite、Vue Router、Axios、Lucide Icons
- 测试：JUnit 5、MockMvc、Vitest

## 目录结构

```text
code/
├── backend/     Spring Boot API
├── frontend/    Vue 3 单页应用
├── uploads/     教学资源上传目录（运行时生成，不提交 Git）
└── README.md
```

## 分支与提交策略

- `main` 保持可演示、可交付的稳定版本，不直接开发业务功能。
- 每个迭代从上一迭代稳定代码创建独立功能分支。当前 Sprint 2 分支为 `feature/sprint2-resource-evaluation-reporting`，基线为包含 Sprint 1 全部成果的 `13f3171`。
- 提交信息使用 `feat`、`fix`、`docs`、`test` 等前缀，按后端、前端、文档等职责拆分提交，便于审查和回溯。
- 功能完成后通过 Pull Request 合并到 `main`，合并前必须通过后端测试、前端测试、类型检查和生产构建。

Sprint 1 主要提交记录：

```text
e6ed4a3 feat(backend): add sprint1 course data platform api
e7f8093 feat(frontend): add course management workspace
57b8949 docs: add sprint1 setup guide
4f3cfb6 fix: address sprint1 code review findings
```

Sprint 2 功能、测试和文档使用独立提交继续记录，避免将多个职责混在同一个提交中。

## 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8080`，接口前缀为 `/api`。开发环境使用文件型 H2 数据库，数据保存在 `backend/data/`。

H2 控制台地址：`http://localhost:8080/h2-console`

- JDBC URL：`jdbc:h2:file:./data/love_of_teach`
- 用户名：`sa`
- 密码：留空

教学资源文件默认保存在 `backend/uploads/`。该目录、H2 数据文件和日志都是运行时内容，不提交到 Git。

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，Vite 会将 `/api` 请求代理到后端。

## 运行测试

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm test
```

前端还需要执行类型检查和生产构建：

```powershell
npm run typecheck
npm run build
```

## 持续集成

仓库使用 GitHub Actions 执行 CI。工作流配置位于 `.github/workflows/ci.yml`，会在以下场景自动运行：

- 向任意分支推送代码
- 创建或更新 Pull Request
- 在 GitHub Actions 页面手动触发

流水线包含两个并行任务：

| 任务 | 检查内容 |
|---|---|
| Backend tests | Java 17、Maven Wrapper、后端 JUnit/MockMvc 全量测试 |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 |

同一分支的新提交会自动取消旧流水线，避免重复占用执行资源。合并代码前应确认对应提交的 CI 全部通过。

离线提交材料中的 CI 通过记录见 `docs/ci-evidence.md`。

## 登录与身份

系统启动后首先进入登录页，也可以从登录页进入注册页。登录成功后，前端将令牌放入 `Authorization: Bearer <token>` 请求头，后端按角色开放对应接口。

演示账号：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 教研室主任 | `director` | `director123` |
| 教学督导 | `supervisor` | `supervisor123` |
| 教师（张明） | `teacher` | `teacher123` |
| 教师（李华） | `teacher2` | `teacher123` |
| 管理员 | `admin` | `admin123` |

公开注册仅允许教师角色，且需要填写已存在的教师工号。督导、主任和管理员账号由系统初始化或后续管理端创建。

演示账号仅记录在本 README 中，登录页和生产构建不展示账号密码。

密码使用 BCrypt 哈希存储。令牌默认有效期为 24 小时，可在 `application.yml` 的 `app.auth.token-ttl-hours` 中修改。

## Sprint 2 功能入口

- 教学资源：所有登录用户可检索章节化资源；任课教师可在课程详情上传文件和创建章节。
- 课程评价：督导和主任可打分并填写评语，首次评价会自动生成督导记录。
- 评价汇总：按学期、院系汇总平均分、评价数和薄弱课程。
- 我的反馈：教师只能查看本人所授课程的评价。
- 统计导出：主任和管理员可导出开课统计 CSV。

| 用户故事 | 实现结果 |
|---|---|
| US12 上传课件与教案 | 教师课程详情页上传文件，校验扩展名、大小和课程归属，数据库记录文件元数据 |
| US13 按章节组织资源 | 创建课程章节、资源挂载章节、课程详情分组展示、资源库按关键字/章节检索 |
| US10 给课程打分并填写评语 | 督导/主任提交 0-100 分评价，评价持久化并联动督导状态 |
| US05 课程评价汇总 | 按学期/院系计算平均分、评价数、薄弱课程和最新评语 |
| US08 教师查看课程反馈 | 教师只能查询本人关联课程的评价，接口同时校验角色和教师档案 |
| US03 导出开课统计表 | 主任/管理员按学期、院系、教师导出 UTF-8 CSV |

## CSV 导入字段

登录系统后进入“批量导入”页面下载模板。导入列定义如下：

```text
courseCode,courseName,departmentCode,credits,description,teacherNo,teacherName,semesterCode,className,studentCount,status
```

`status` 支持 `PLANNED`、`OPEN`、`CLOSED`，也兼容 `待开课`、`开课`、`停课`。

导入时每一行使用独立事务。某一行校验或保存失败时，该行已执行的教师、课程和开课写入会一起回滚，不影响其他成功行。

## 数据一致性说明

- 院系和学期采用停用而不是删除。停用后保留已有课程引用，但不再作为新增业务数据的可用选项。
- CSV 导入会拒绝停用院系和停用学期，避免继续产生新的业务引用。
- 开发环境使用 H2 与 `ddl-auto: update` 便于快速启动。正式部署建议改用 MySQL/PostgreSQL，并将 JPA 配置调整为 `validate`，由 Flyway 管理数据库迁移。
- 教学资源上传采用本地文件存储，正式部署时应替换为对象存储，并增加病毒扫描、文件大小配额和访问审计。
- 上传文件除扩展名白名单外还会校验文件头；CSV 导出会转义电子表格公式前缀。
- 当前令牌保存在浏览器 localStorage，适合原型验收。正式系统建议使用 HttpOnly Cookie、刷新令牌和 HTTPS。
- 登录接口尚未实现失败次数限流，正式部署应在网关或认证服务中补充。

## 初始账号与角色

Sprint 2 已实现本地账号登录注册和基础角色权限。正式接入学校环境时，可替换为统一身份认证，并补充菜单权限、接口权限矩阵、账号审批和审计日志。
