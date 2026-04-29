# KWeaver DIP Android App

一个基于 AI 的数字人对话平台 Android 客户端，采用现代 Android 开发技术栈构建，适合作为 **MVVM + Domain Layer 架构 + AI 辅助开发 + TDD 测试驱动** 的学习示例。

## 功能模块

| 模块 | 说明 |
|---|---|
| AI Chat | 与数字人进行实时 SSE 流式对话 |
| Digital Human | 数字人（AI Agent）管理，支持创建/编辑/删除 |
| AI Store | 应用商店，浏览和安装 AI 应用 |
| Session History | 对话历史记录 |
| Skills | AI 技能管理 |
| Plans | 计划管理 |

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Domain Layer (UseCase) |
| 依赖注入 | Hilt |
| 网络 | Retrofit + OkHttp |
| 异步 | Kotlin Coroutines + StateFlow |
| 本地存储 | DataStore |
| 导航 | Navigation Compose |

## 项目结构

```
kweaver-app/
├── app/                          # Android 应用模块
│   └── src/main/java/com/kweaver/dip/
│       ├── data/                 # 数据层
│       │   ├── api/              # Retrofit API 接口
│       │   ├── model/            # 数据模型
│       │   ├── repository/       # Repository 实现
│       │   └── local/datastore/  # 本地存储
│       ├── di/                   # Hilt 依赖注入模块
│       ├── domain/usecase/       # 业务逻辑 (UseCase)
│       ├── ui/screens/           # Compose UI 页面
│       └── ui/navigation/       # 导航路由
├── e2e-tests/                    # Appium E2E 测试 (Python)
├── docs/                         # 开发文档
├── .claude/                      # AI 工具配置
│   ├── skills/                   # Claude Code Skills
│   └── plans/                    # 架构计划文档
├── CLAUDE.md                     # AI 工具项目指令
├── Agent.md                      # 代码变更工作流规范
└── reference/                    # Web 端参考代码
```

## 测试覆盖

| 测试层级 | 数量 | 框架 |
|---|---|---|
| 单元测试 | 23 个文件 | JUnit + Mockito + Coroutines Test |
| 集成测试 | 4 个文件 | Hilt Android Test + AndroidX Test |
| E2E 测试 | 16 个文件，17 个用例 | Appium + pytest + Page Object Model |

## 快速开始

### 环境要求

- Windows 10+ / macOS / Linux
- JDK 17（**必须**，高版本会导致编译失败）
- Android Studio（最新稳定版）
- Android SDK API 35
- 16 GB+ 内存

### 构建与运行

```bash
# 编译
./gradlew :app:compileDebugKotlin

# 运行单元测试
./gradlew :app:testDebugUnitTest

# 构建 APK
./gradlew :app:assembleDebug
```

### 运行 E2E 测试

```bash
cd e2e-tests
python -m venv .venv && source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -e .
appium --address 127.0.0.1 --port 4723 &
python -m pytest tests/ -v
```

## AI 辅助开发

本项目集成了 AI 辅助开发工具链，详细配置和使用方法见开发手册。

```bash
# 安装 Claude Code
npm install -g @anthropic-ai/claude-code

# 安装 gstack skills
git clone --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup --team

# 在项目中启动
claude
```

每次代码变更后执行 `/verify` 运行 7 步验证流水线（编译 → 单元测试 → 集成测试 → 构建 → 安装 → Appium → E2E）。

## 文档

- [Android 开发手册](docs/android-development-handbook.md) — 环境搭建、AI 工具配置、架构说明、测试体系完整指南

## License

Private
