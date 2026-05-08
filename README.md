# Android App Demo

Android 客户端学习项目，演示现代 Android 开发的完整工程实践：**MVVM + Domain Layer 架构、TDD 测试驱动、AI 辅助开发**。

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
| E2E 测试 | Appium + pytest + Page Object Model |

## 项目结构

```
kweaver-app/
├── app/                          # Android 应用模块
│   └── src/main/java/com/kweaver/dip/
│       ├── data/                 # 数据层（API、模型、仓库、本地存储）
│       ├── di/                   # Hilt 依赖注入模块
│       ├── domain/usecase/       # 业务逻辑 UseCase
│       ├── ui/screens/           # Compose UI 页面
│       └── ui/navigation/        # 导航路由
├── e2e-tests/                    # Appium E2E 测试 (Python)
├── docs/                         # 开发文档
├── .claude/                      # AI 工具配置
│   ├── skills/                   # Claude Code Skills
│   └── plans/                    # 架构计划文档
├── CLAUDE.md                     # AI 工具项目指令
└── Agent.md                      # 代码变更工作流规范
```

## 测试体系

| 测试层级 | 框架 | 说明 |
|---|---|---|
| 单元测试 | JUnit + Mockito + Coroutines Test | Repository / ViewModel / UseCase 逻辑验证 |
| 集成测试 | Hilt Android Test + AndroidX Test | DI 图和 Android 组件集成验证 |
| E2E 测试 | Appium + pytest + Page Object Model | 端到端 UI 自动化验证 |

## 快速开始

### 环境要求

- JDK 17（**必须**，高版本会导致编译失败）
- Android Studio（最新稳定版）
- Android SDK API 35
- Python 3.10+（E2E 测试）

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
