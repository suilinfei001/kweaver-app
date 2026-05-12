# Trae 项目规则

**你好，我是 Trae AI。每次对话开始时，我会确保本规则文件中的所有规则被正确调用。**

**重要**：本项目还有 `Agent.md` 文件，这是项目的**核心开发规范文档**，包含构建命令、验证流水线、工程纪律等关键内容。每次对话中也必须加载并遵循 `Agent.md` 中的所有规定。

本项目使用 Trae AI 编码助手，遵循以下规则。

## gstack 技能路由规则

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

## 开发流程

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

1. 用 `/investigate` 系统化排查（铁律：先调查再修）
2. 用 `/systematic-debugging` 四阶段定位根因（铁律：不找到根因不动手）
3. 用 TDD 方式编写修复代码（先写失败测试 → 最小修复 → 重构）
4. 用 `/qa` 验证修复
5. 用 `/review` 确认无回归

## 工程纪律

- **TDD 铁律**：先写失败测试，再写最小代码通过。没有失败测试不写生产代码
- **调试铁律**：不找到根因不修。Phase 1 调查不完不进 Phase 2 修复
- **收尾铁律**：所有任务完成后必须 `/finishing-a-development-branch` 验证测试通过再合入

## 安全防护

在执行危险操作前，考虑使用安全 skill：
- `/careful` — 危险命令前警告（rm -rf、DROP TABLE 等）
- `/freeze` — 限制编辑范围到一个目录
- `/guard` — 完整安全模式

## Build & Environment

### JDK 版本要求

本项目必须使用 **JDK 17** 编译。

编译命令：
```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:assembleDebug
```

### 验证流水线

| 步骤 | 命令 | 说明 |
|---|---|---|
| 1. 编译 | `JAVA_HOME="..." ./gradlew :app:compileDebugKotlin` | 确保 Kotlin 代码无语法错误 |
| 2. 单元测试 | `JAVA_HOME="..." ./gradlew :app:testDebugUnitTest` | 验证 Repository / ViewModel 逻辑正确 |
| 3. 集成测试 | `JAVA_HOME="..." ./gradlew :app:connectedDebugAndroidTest` | 验证 Hilt DI 和 Android 组件集成 |
| 4. 构建 APK | `JAVA_HOME="..." ./gradlew :app:assembleDebug` | 生成可安装的 debug APK |
| 5. 安装 APK | `adb.exe install -r app/build/outputs/apk/debug/app-debug.apk` | 安装到 Android 设备 |

### ADB 路径

```
C:\Users\Yabo.sui\AppData\Local\Android\Sdk\platform-tools
```

## Logcat 日志排查

```powershell
# 获取 APP PID
adb.exe shell pidof com.kweaver.dip

# 抓取指定 PID 日志
adb.exe logcat -d --pid=<PID>

# 过滤关键字
adb.exe logcat -d --pid=<PID> | Select-String -Pattern "SseChatService|ChatViewModel|SSE"

# 清除日志缓冲区
adb.exe logcat -c
```

### 常用排查场景

| 场景 | 命令 |
|---|---|
| 查看 OkHttp SSE 响应数据 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "okhttp.OkHttpClient"` |
| 查看应用 Debug 日志 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "D\/com\.kweaver"` |
| 查找应用崩溃异常 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "FATAL\|AndroidRuntime\|Exception"` |
