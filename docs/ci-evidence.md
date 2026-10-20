# GitHub Actions CI 通过记录

## 运行信息

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

## 任务结果

| 任务 | 检查内容 | 结果 |
|---|---|---|
| Backend tests | Java 17、Maven Wrapper、后端全部 JUnit/MockMvc 测试 | **Success** |
| Frontend tests and build | Node.js 22、`npm ci`、Vitest、TypeScript 类型检查、Vite 生产构建 | **Success** |

## 对应测试结果

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
