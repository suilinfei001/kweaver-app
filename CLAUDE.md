## gstack (REQUIRED — global install)

**Before doing ANY work, verify gstack is installed:**

```bash
test -d ~/.claude/skills/gstack/bin && echo "GSTACK_OK" || echo "GSTACK_MISSING"
```

If GSTACK_MISSING: STOP. Do not proceed. Tell the user:

> gstack is required for all AI-assisted work in this repo.
> Install it:
> ```bash
> git clone --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
> cd ~/.claude/skills/gstack && ./setup --team
> ```
> Then restart your AI coding tool.

Do not skip skills, ignore gstack errors, or work around missing gstack.

Using gstack skills: After install, skills like /qa, /ship, /review, /investigate,
and /browse are available. Use /browse for all web browsing.
Use ~/.claude/skills/gstack/... for gstack file paths (the global path).

## gstack Skill 路由规则

当用户的请求匹配以下场景时，**主动调用**对应的 skill，不要直接回答：

| 用户意图 | 调用的 Skill | 说明 |
|---|---|---|
| 产品想法、头脑风暴、"我有个想法" | `/office-hours` | 产品思考，找到真正要解决的问题 |
| 战略规划、方案评审 | `/plan-ceo-review` | 战略级方案评审 |
| 架构设计、技术方案 | `/plan-eng-review` | 技术架构评审 |
| Bug、错误、异常排查 | `/investigate` | 系统化排查根因，不要直接改代码 |
| UI/布局设计、界面实现 | `/frontend-design` | 先生成设计方案再编码 |
| 测试验证、功能验证 | `/qa` | 用真实浏览器测试 |
| 代码审查、上线前检查 | `/review` | 捕获 CI 发现不了的 bug |
| 部署、发布 | `/ship` | 自动化部署 + 测试覆盖率审计 |

## gstack + Superpowers 最佳实践

gstack 负责「做什么」(产品方向、质量验证)，Superpowers 负责「怎么做」(工程执行、编码纪律)。
两者配合形成完整的开发生命周期。

### 职责划分

| 阶段 | gstack (方向) | Superpowers (执行) |
|---|---|---|
| 需求澄清 | `/office-hours` — 产品思考、需求重构 | `/brainstorming` — 技术探索、设计文档 |
| 方案评审 | `/plan-ceo-review` → `/plan-eng-review` | `/writing-plans` — 编写实现计划 |
| 编码实现 | `/frontend-design` — UI 设计方案 | `/subagent-driven-development` — 分任务并行实现 |
| 测试驱动 | — | `/test-driven-development` — 红-绿-重构循环 |
| 调试排查 | `/investigate` — 系统化根因排查 | `/systematic-debugging` — 四阶段调试法 |
| 质量验证 | `/review` → `/qa` — 代码审查 + 浏览器测试 | `/finishing-a-development-branch` — 收尾分支 |
| 部署发布 | `/ship` → `/land-and-deploy` | — |

### 新功能开发完整流程

```
/office-hours → /brainstorming → /plan-ceo-review → /plan-eng-review →
/writing-plans → /subagent-driven-development → /review → /qa → /ship
```

1. `/office-hours` — 产品思考，找到真正要解决的问题
2. `/brainstorming` — 技术探索，生成设计文档和 spec
3. `/plan-ceo-review` — 战略级方案评审
4. `/plan-eng-review` — 技术架构评审，锁定数据流和边界
5. `/writing-plans` — 将设计转化为可执行的实施计划
6. `/subagent-driven-development` — 按计划分任务实现（每个任务遵循 TDD）
7. `/review` — 代码审查，捕获 CI 发现不了的 bug
8. `/qa` — 用真实浏览器测试验证
9. `/ship` — 自动化部署 + 测试覆盖率审计

### Bug 修复流程

```
/investigate + /systematic-debugging → TDD 修复 → /qa → /review
```

1. 用 `/investigate` 系统化排查（gstack 铁律：先调查再修）
2. 用 `/systematic-debugging` 四阶段定位根因（superpowers 铁律：不找到根因不动手）
3. 用 TDD 方式编写修复代码（先写失败测试 → 最小修复 → 重构）
4. 用 `/qa` 验证修复
5. 用 `/review` 确认无回归

### 安全防护

在执行危险操作前，考虑使用安全 skill：
- `/careful` — 危险命令前警告（rm -rf、DROP TABLE 等）
- `/freeze` — 限制编辑范围到一个目录
- `/guard` — 完整安全模式

### Superpowers 工程纪律（所有编码工作必须遵循）

- **TDD 铁律**：先写失败测试，再看它失败，再写最小代码通过。没有失败测试不写生产代码
- **调试铁律**：不找到根因不修。Phase 1 调查不完不进 Phase 2 修复
- **Subagent 分工**：实现 → Spec 审查 → 代码质量审查，三阶段保证质量
- **收尾铁律**：所有任务完成后必须 `/finishing-a-development-branch` 验证测试通过再合入

**必须要遵循Agent.md**
