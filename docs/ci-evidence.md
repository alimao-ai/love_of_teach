# GitHub Actions CI 通过记录

## Sprint 3 v1.0.3 最终发布运行

| 项目 | 内容 |
|---|---|
| 工作流名称 | `CI` |
| 最终标签 | `v1.0.3-sprint3` |
| 发布提交 | `339c0cf4d8db60e867b082182fbaea93aec615d8` |
| 提交说明 | `fix(frontend): remove internal iteration labels from release UI` |
| 运行结论 | **Success** |

| 触发对象 | 运行编号 | Run ID | 结果 | 运行链接 |
|---|---|---|---|---|
| 功能分支 | `#18` | `40162626193` | **Success** | https://github.com/alimao-ai/love_of_teach/actions/runs/40162626193 |
| 发布标签 | `#19` | `40162699365` | **Success** | https://github.com/alimao-ai/love_of_teach/actions/runs/40162699365 |
| `main` | `#20` | `40162702126` | **Success** | https://github.com/alimao-ai/love_of_teach/actions/runs/40162702126 |

### 任务结果

| 任务 | 检查内容 | 结果 |
|---|---|---|
| Backend tests | Java 17、Maven Wrapper、后端全量 JUnit/MockMvc `verify` | **Success** |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 | **Success** |

### 对应测试结果

- 后端：61 个测试通过，包含 Sprint 1/2/3、全量端到端旅程和安全回归。
- 前端：11 个测试文件、15 个 Vitest 用例通过。
- TypeScript 类型检查通过。
- Vite 生产构建通过。
- 本地 `npm audit --omit=dev`：0 个已知漏洞。
- 技术债登记表、Release Notes、用户手册、发布检查单和发布归档清单均已纳入 `code/docs`。

## Sprint 3 发布期间安全与债务收口记录

| 项目 | 内容 |
|---|---|
| 工作流名称 | `CI` |
| 运行编号 | `#8` |
| Run ID | `40085356558` |
| 分支 | `fix/sprint3-security-regression` |
| 提交 | `c73279a25e5839b7e4fabdb0b8b13cb21ba9cffd` |
| 提交说明 | `docs: document sprint3 security hardening` |
| 运行结论 | **Success** |
| 运行链接 | https://github.com/alimao-ai/love_of_teach/actions/runs/40085356558 |

### 任务结果

| 任务 | 检查内容 | 结果 |
|---|---|---|
| Backend tests | Java 17、Maven Wrapper、后端全量 JUnit/MockMvc `verify` | **Success** |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 | **Success** |

### 对应测试结果

- 后端：61 个测试通过，包含 Sprint 1/2/3、全量端到端旅程和安全回归。
- 前端：11 个测试文件、15 个 Vitest 用例通过。
- TypeScript 类型检查通过。
- Vite 生产构建通过。
- 本地 `npm audit --omit=dev`：0 个已知漏洞。

## Sprint 3 安全整改前历史运行

| 项目 | 内容 |
|---|---|
| 工作流名称 | `CI` |
| 运行编号 | `#3` |
| Run ID | `40081830683` |
| 分支 | `feature/sprint3-evaluation-closure-release` |
| 提交 | `cc3572f31a9ac16d46687fa8eeed162a2c5670fe` |
| 提交说明 | `docs: add sprint3 release guide and acceptance checklist` |
| 运行结论 | **Success** |
| 运行链接 | https://github.com/alimao-ai/love_of_teach/actions/runs/40081830683 |

### 任务结果

| 任务 | 检查内容 | 结果 |
|---|---|---|
| Backend tests | Java 17、Maven Wrapper、后端全量 JUnit/MockMvc `verify` | **Success** |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 | **Success** |

### 对应测试结果

- 后端：40 个测试通过，包含 Sprint 1、Sprint 2、Sprint 3、角色权限和技术债回归。
- 前端：11 个测试文件、15 个 Vitest 用例通过。
- TypeScript 类型检查通过。
- Vite 生产构建通过。
- 本地 `npm audit --omit=dev`：0 个已知漏洞。

## Sprint 2 历史记录

### 运行信息

| 项目 | 内容 |
|---|---|
| 工作流名称 | `CI` |
| 运行编号 | `#1` |
| Run ID | `39120087569` |
| 分支 | `feature/sprint2-resource-evaluation-reporting` |
| 提交 | `c77eac2ed6569054ce5c934d92f4eaf9f765f19f` |
| 提交说明 | `ci: add github actions test and build pipeline` |
| 运行结论 | **Success** |
| 运行链接 | https://github.com/alimao-ai/love_of_teach/actions/runs/39120087569 |

### 任务结果

| 任务 | 检查内容 | 结果 |
|---|---|---|
| Backend tests | Java 17、Maven Wrapper、后端全部 JUnit/MockMvc 测试 | **Success** |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 | **Success** |

### 对应测试结果

- 后端：33 个测试通过，包含 Sprint 1、鉴权回归和 Sprint 2 集成测试。
- 前端：11 个测试通过。
- TypeScript 类型检查通过。
- Vite 生产构建通过。

## 配置文件

流水线配置保存在仓库根目录：

```text
.github/workflows/ci.yml
```

## 说明

本文件根据 GitHub Actions 运行页面整理，用于在离线提交材料中保留 CI 通过证据。原始运行记录仍可通过上方 GitHub Actions 链接核验。
