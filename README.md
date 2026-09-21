# 爱教学 - 教学质量全链路数字化管理平台

Sprint 1 实现了课程数据底座与核心查询能力，包含：

- 学期、院系基础数据的新增、编辑和停用
- 课程与教师数据 CSV 模板下载、批量导入、逐行校验和失败原因导出
- 按学期、院系、教师、关键字筛选课程
- 教师课程简介与开课情况总览
- 课程学生人次、教学资源与督导情况详情
- 按学期、院系统计开课数、已督导数和督导覆盖率

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Data JPA、H2、Apache Commons CSV
- 前端：Vue 3、TypeScript、Vite、Vue Router、Axios、Lucide Icons
- 测试：JUnit 5、MockMvc、Vitest

## 目录结构

```text
code/
├── backend/     Spring Boot API
├── frontend/    Vue 3 单页应用
└── README.md
```

## 分支与提交策略

- `main` 保持可演示、可交付的稳定版本，不直接开发业务功能。
- 每个迭代从 `main` 创建独立功能分支，例如当前分支 `feature/sprint1-course-platform`。
- 提交信息使用 `feat`、`fix`、`docs`、`test` 等前缀，按后端、前端、文档等职责拆分提交，便于审查和回溯。
- 功能完成后通过 Pull Request 合并到 `main`，合并前必须通过后端测试、前端测试、类型检查和生产构建。

当前 Sprint 1 提交记录：

```text
e6ed4a3 feat(backend): add sprint1 course data platform api
e7f8093 feat(frontend): add course management workspace
57b8949 docs: add sprint1 setup guide
4f3cfb6 fix: address sprint1 code review findings
```

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

## 初始账号与角色

Sprint 1 聚焦数据底座与查询验收，不包含登录鉴权。首页中的角色导航用于体现教研室主任、教学督导、教师和管理员的目标工作区，后续迭代可接入统一身份认证与权限控制。
