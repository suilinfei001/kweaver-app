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

## gstack 开发工作流

### 新功能开发流程

```
/office-hours → /plan-ceo-review → /plan-eng-review → 编码 → /review → /qa → /ship
```

1. 用 `/office-hours` 澄清需求
2. 用 `/plan-ceo-review` 评审方案
3. 用 `/plan-eng-review` 确认架构
4. 编码实现（UI 部分先调用 `/frontend-design`）
5. 用 `/review` 做代码审查
6. 用 `/qa` 做测试验证
7. 用 `/ship` 部署

### Bug 修复流程

```
/investigate → 修复根因 → /qa → /review
```

1. 用 `/investigate` 系统化排查，**不要跳过调查直接改代码**
2. 修复根因
3. 用 `/qa` 验证修复
4. 用 `/review` 确认无回归

### 安全防护

在执行危险操作前，考虑使用安全 skill：
- `/careful` — 危险命令前警告（rm -rf、DROP TABLE 等）
- `/freeze` — 限制编辑范围到一个目录
- `/guard` — 完整安全模式

**必须要遵循Agent.md**
