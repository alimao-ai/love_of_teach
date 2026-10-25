# 爱教学 v1.0.3-sprint3 发布归档说明

## 发布身份

| 项目 | 内容 |
|---|---|
| 产品版本 | v1.0.3-sprint3 |
| 发布标签 | `v1.0.3-sprint3` |
| 标签目标提交 | `339c0cf4d8db60e867b082182fbaea93aec615d8` |
| 发布分支 | `main`、`fix/sprint3-security-regression` |
| 发布时 main | `339c0cf4d8db60e867b082182fbaea93aec615d8` |
| 发布提交说明 | `fix(frontend): remove internal iteration labels from release UI` |
| 发布性质 | 单机课程演示版 |

## 归档内容

| 路径 | 内容 |
|---|---|
| `backend/` | Spring Boot 后端源码、Maven Wrapper、测试与配置 |
| `frontend/` | Vue 3 前端源码、测试、TypeScript 与 Vite 配置 |
| `.github/workflows/ci.yml` | GitHub Actions 流水线 |
| `README.md` | 启动、测试、角色、安全和排障入口 |
| `docs/release-notes.md` | 18 个 PBI、缺陷、兼容性、升级和回滚 |
| `docs/user-manual.md` | 分角色用户手册初稿 |
| `docs/release-checklist.md` | 发布就绪检查单 |
| `docs/ci-evidence.md` | 最终分支、标签和 main 的 CI 记录 |
| `docs/security-hardening.md` | 安全整改范围 |
| `docs/technical-debt-register.md` | TD-01 至 TD-05 结转债务 |
| `docs/release-manifest.json` | 机器可读发布信息 |

## 验证结果

| 检查项 | 结果 |
|---|---|
| 后端测试 | 61 通过，0 失败，0 错误 |
| 全量回归与端到端 | 12 个跨 Sprint 回归 + 2 个 E2E，全部通过 |
| 前端测试 | 11 个测试文件、15 个用例通过 |
| TypeScript | 类型检查通过 |
| Vite | 生产构建通过 |
| 依赖审计 | 0 个已知漏洞 |
| CI | 分支 `#18`、标签 `#19`、main `#20` 全部成功 |

## 离线核验

如果归档保留隐藏的 `.git` 目录，可直接执行：

```powershell
git status --short --branch
git rev-parse main
git rev-list -n 1 refs/tags/v1.0.3-sprint3
git show --stat --oneline v1.0.3-sprint3
```

预期标签目标为 `339c0cf4d8db60e867b082182fbaea93aec615d8`，且该提交是当前 `main` 的祖先。发布后可能追加 CI 证据、归档说明等文档提交，因此 `main` 可以比标签多出文档提交；产品发布身份以标签目标为准。

如果打包时排除了 `.git`，`docs/release-manifest.json` 仍可用于核对版本、提交、测试和 CI。此时无法在本地重建完整提交图和标签对象。

## 打包边界

应包含：

- `backend/src/`
- `frontend/src/`
- `backend/pom.xml`、`backend/mvnw`、`backend/mvnw.cmd`
- `frontend/package.json`、`frontend/package-lock.json`
- `.github/`、`README.md`、`docs/`

可以排除并重新生成：

- `backend/target/`
- `frontend/node_modules/`
- `frontend/dist/`
- `backend/data/`
- `backend/uploads/`

`dev` 配置会幂等生成演示数据。数据库和上传目录只在运行后出现，不属于源码交付。

## 尚需人工完成

- 产品负责人对用户故事验收和演示链路签字。
- 正式部署前完成 TD-01 至 TD-05 的偿还与验收。
