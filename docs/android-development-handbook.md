# Android Development Handbook

本手册面向新加入团队的 Android 开发者，涵盖从环境搭建到 AI 辅助开发的完整工作流。不涉及具体业务逻辑，聚焦于工具链、工程规范和质量保障体系。

---

## 目录

- [1. 开发环境搭建](#1-开发环境搭建)
  - [1.0 硬件与系统要求](#10-硬件与系统要求)
  - [1.1 Android Studio](#11-android-studio)
  - [1.2 JDK 配置](#12-jdk-配置)
  - [1.3 Android SDK 与模拟器](#13-android-sdk-与模拟器)
  - [1.4 构建工具](#14-构建工具)
- [2. AI 工具配置](#2-ai-工具配置)
  - [2.1 Claude Code](#21-claude-code)
- [3. AI Skills](#3-ai-skills)
  - [3.1 gstack](#31-gstack)
  - [3.2 常用 Skills 速查](#32-常用-skills-速查)
  - [3.3 自定义 Skill 开发](#33-自定义-skill-开发)
- [4. Android 应用架构](#4-android-应用架构)
  - [4.1 整体架构](#41-整体架构)
  - [4.2 分层职责](#42-分层职责)
  - [4.3 依赖注入 (Hilt)](#43-依赖注入-hilt)
  - [4.4 UI 层 (Jetpack Compose)](#44-ui-层-jetpack-compose)
  - [4.5 网络层 (Retrofit + OkHttp)](#45-网络层-retrofit--okhttp)
- [5. Harness Engineering](#5-harness-engineering)
  - [5.1 项目配置文件](#51-项目配置文件)
  - [5.2 单元测试](#52-单元测试)
  - [5.3 集成测试](#53-集成测试)
  - [5.4 E2E 测试](#54-e2e-测试)
  - [5.5 验证流水线 (/verify)](#55-验证流水线-verify)
  - [5.6 CI 持续集成](#56-ci-持续集成)
  - [5.7 测试策略总结](#57-测试策略总结)

---

## 1. 开发环境搭建

### 1.0 硬件与系统要求

| 项目 | 最低要求 |
|---|---|
| 操作系统 | Windows 10 及以上（推荐）；macOS、Linux 亦可 |
| CPU | 4 核及以上 |
| 内存 | 16 GB 及以上 |
| 磁盘 | 20 GB 可用空间（SDK + 模拟器镜像） |

> Android 模拟器和 Gradle 构建对内存和 CPU 消耗较大，低于上述配置会导致编译缓慢、模拟器卡顿。建议 8 核 CPU + 32 GB 内存以获得流畅体验。

### 1.1 Android Studio

下载并安装最新稳定版 [Android Studio](https://developer.android.com/studio)。

> **Windows 用户强烈建议：** 将 Android Studio 安装到 D 盘（如 `D:\Android Studio`），避免占用 C 盘空间。默认安装路径在 C 盘，加上后续插件和缓存，可达数 GB。

安装完成后，打开 **Settings → Languages & Frameworks → Kotlin**，确认 Kotlin 版本与项目一致。

### 1.2 JDK 配置

本项目强制使用 **JDK 17**。更高版本（如 JDK 25）会导致 Kotlin 编译器崩溃。

**步骤：**

1. 下载 [JDK 17](https://adoptium.net/)（推荐 Eclipse Temurin 发行版）
2. 在 Android Studio 中配置：**File → Project Structure → SDK Location → Gradle Settings → Gradle JDK** 选择 JDK 17
3. 设置环境变量：

```bash
# Linux/macOS
export JAVA_HOME=/path/to/jdk-17

# Windows（系统环境变量）
JAVA_HOME=C:\Users\<YourUser>\.jdks\jdk-17.0.18+8
```

4. 验证安装：

```bash
java -version
# 应输出 openjdk version "17.x.x"
```

### 1.3 Android SDK 与模拟器

**SDK 安装：**

打开 Android Studio → **Tools → SDK Manager**，安装以下组件：

> **Windows 用户强烈建议：** 将 Android SDK 路径改到 D 盘（如 `D:\Android\Sdk`）。在 SDK Manager 中修改 **Android SDK Location** 即可。SDK 组件和模拟器镜像加起来可达 10+ GB，放在 C 盘会迅速耗尽空间。

| 组件 | 说明 |
|---|---|
| Android 15 (API 35) | compileSdk 和 targetSdk |
| Android 8.0 (API 26) | minSdk |
| Android SDK Platform-Tools | 包含 adb |
| Android SDK Build-Tools | 构建工具链 |
| Intel x86 Emulator Accelerator (HAXM) | 模拟器加速（Windows） |

**模拟器创建：**

> **Windows 用户强烈建议：** AVD 模拟器镜像默认存放在 `C:\Users\<用户名>\.android\avd\`，每个镜像约 2-4 GB。通过设置环境变量将 AVD 目录指向 D 盘：
>
> ```bash
> # 设置系统环境变量
> ANDROID_AVD_HOME=D:\Android\avd
> ```
>
> 如果已有模拟器，将 `.android\avd\` 下的文件迁移到新路径后，修改对应的 `.ini` 文件中的路径即可。

1. 打开 **Tools → Device Manager**
2. 点击 **Create Device**，选择一个手机型号（如 Pixel 7）
3. 选择系统镜像：推荐 **API 35, x86_64, with Google APIs**
4. 完成创建并启动模拟器

**ADB 常用命令：**

```bash
# 查看已连接设备
adb devices

# 安装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 清除应用数据
adb shell pm clear com.kweaver.dip

# 查看应用日志
adb logcat --pid=$(adb shell pidof com.kweaver.dip)
```

### 1.4 构建工具

项目使用 **Gradle 8.11.1** + **Android Gradle Plugin (AGP) 8.7.3**，版本由 Version Catalog 统一管理。

**关键配置文件：**

| 文件 | 作用 |
|---|---|
| `settings.gradle.kts` | 项目模块声明、插件仓库 |
| `build.gradle.kts`（根目录） | 全局插件声明 |
| `app/build.gradle.kts` | 应用模块配置（SDK 版本、依赖） |
| `gradle/libs.versions.toml` | 版本目录，统一管理所有依赖版本 |

**常用构建命令：**

```bash
# 编译 Kotlin 代码
./gradlew :app:compileDebugKotlin

# 运行单元测试
./gradlew :app:testDebugUnitTest

# 运行集成测试（需要设备/模拟器）
./gradlew :app:connectedDebugAndroidTest

# 构建 Debug APK
./gradlew :app:assembleDebug

# 构建Release APK
./gradlew :app:assembleRelease

# 清理构建产物
./gradlew clean
```

> **Windows 用户注意：** 使用 `gradlew.bat` 或 `./gradlew`（Git Bash 中）。所有 Gradle 命令需要设置正确的 `JAVA_HOME`。

> **Gradle 缓存管理：** Gradle 编译过程会在 `~/.gradle/` 下生成大量缓存（可达 5-10 GB），默认位于 C 盘。建议将其迁移到 D 盘：
>
> ```bash
> # 设置系统环境变量
> GRADLE_USER_HOME=D:\Gradle\.gradle
> ```
>
> 定期清理缓存释放空间：
>
> ```bash
> ./gradlew clean                          # 清理项目构建产物
> ./gradlew cleanBuildCache                # 清理构建缓存
> # 如需彻底清理全局缓存（谨慎操作）
> rm -rf $GRADLE_USER_HOME/caches/
> rm -rf $GRADLE_USER_HOME/wrapper/dists/
> ```

---

## 2. AI 工具配置

### 2.1 Claude Code

[Claude Code](https://claude.ai/code) 是 Anthropic 官方的 CLI 编码助手，支持在终端和 IDE 中使用。

**安装（Windows）：**

在 Git Bash 中执行：

```bash
npm install -g @anthropic-ai/claude-code
```

其他安装方式：

- **VS Code 扩展**：在扩展市场搜索 "Claude Code"
- **JetBrains 扩展**：在插件市场搜索 "Claude Code"

**配置大模型：**

Claude Code 支持接入 Anthropic 官方模型或国内大模型（如智谱 GLM）。

**方案 A：Anthropic 官方 Key**

如果你拥有 Anthropic 官方 API Key，按 Claude Code 首次启动提示登录 Anthropic 账号或输入 Key 即可，无需额外配置。

**方案 B：国内大模型（智谱 GLM）**

1. 创建配置文件 `~/.claude/config`，写入以下内容：

```bash
export ANTHROPIC_BASE_URL="https://open.bigmodel.cn/api/anthropic"
export ANTHROPIC_AUTH_TOKEN="your model key"
export ANTHROPIC_MODEL=GLM-5.1
export ANTHROPIC_SMALL_FAST_MODEL=GLM-4-5-Air
```

2. 在 `~/.bashrc` 中加入自动加载：

```bash
source ~/.claude/config
```

3. 使配置立即生效：

```bash
source ~/.bashrc
```

> 将 `your model key` 替换为你在智谱开放平台申请的 API Key。

**启动与使用：**

```bash
cd /path/to/project
claude
```

Claude Code 会自动读取项目中的 `CLAUDE.md`、`Agent.md` 等配置文件作为上下文。

**核心交互方式：**

| 方式 | 说明 |
|---|---|
| 直接对话 | 在 Claude Code 中描述需求 |
| `/skill-name` | 调用已安装的 Skill |
| `@file` | 引用特定文件作为上下文 |

---

## 3. AI Skills

Skills 是 Claude Code 的扩展机制，提供专业化的工作流和自动化能力。

### 3.1 gstack

[gstack](https://github.com/garrytan/gstack) 是本项目的 **必装** Skill 集合，提供代码审查、质量保证、发布管理等核心工作流。

**安装（全局）：**

```bash
git clone --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup --team
```

安装后重启 Claude Code。

**验证安装：**

```bash
test -d ~/.claude/skills/gstack/bin && echo "GSTACK_OK" || echo "GSTACK_MISSING"
```

> **重要：** gstack 安装是项目的前置条件。如果 `GSTACK_MISSING`，所有 AI 辅助工作必须暂停，先完成安装。

### 3.2 常用 Skills 速查

以下 Skills 在安装 gstack 后可用，通过 `/skill-name` 调用：

| Skill | 用途 | 典型场景 |
|---|---|---|
| `/verify` | 运行完整验证流水线 | 每次代码变更后 |
| `/qa` | 质量检查 + 测试 | "这段代码能正常工作吗？" |
| `/qa-only` | 仅运行测试 | 快速验证测试通过 |
| `/review` | 代码审查 | PR 提交前 |
| `/investigate` | 问题排查 | Bug 调查、错误分析 |
| `/ship` | 发布流程 | 准备合并/部署 |
| `/plan-eng-review` | 架构评审 | "这个设计方案可行吗？" |
| `/autoplan` | 自动规划 | 复杂任务的计划制定 |
| `/design-review` | UI/设计审查 | 界面变更 |
| `/office-hours` | 产品讨论 | 头脑风暴、想法讨论 |
| `/context-save` | 保存工作上下文 | 需要暂停当前任务 |
| `/context-restore` | 恢复工作上下文 | 继续之前暂停的任务 |
| `/learn` | 记录项目经验 | 发现值得记录的知识 |

### 3.3 自定义 Skill 开发

Skills 是存放在 `~/.claude/skills/` 目录下的结构化指令文件。

**目录结构：**

```
~/.claude/skills/
└── my-skill/
    └── SKILL.md      # Skill 定义文件
```

**SKILL.md 格式：**

```markdown
---
name: my-skill
version: 1.0.0
description: 简要描述这个 Skill 做什么
triggers:
  - my-skill
  - trigger phrase
allowed-tools:
  - Bash
  - Read
  - Edit
---

## /my-skill — 做某事

### 步骤 1: 检查前置条件
...
### 步骤 2: 执行操作
...
### 步骤 3: 报告结果
...
```

**项目级 Skill：**

也可以在项目目录下创建 Skill：

```
<project-root>/.claude/skills/
└── verify/
    └── SKILL.md
```

项目级 Skill 优先级高于全局 Skill，适合放置项目特有的工作流（如 `/verify` 流水线）。

---

## 4. Android 应用架构

### 4.1 整体架构

本项目采用 **MVVM + Domain Layer** 架构，使用 Hilt 进行依赖注入：

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│  (Compose Screens + ViewModels)                  │
├─────────────────────────────────────────────────┤
│                  Domain Layer                    │
│  (UseCases — 业务逻辑封装)                        │
├─────────────────────────────────────────────────┤
│                  Data Layer                      │
│  (Repositories + API + Local Storage)            │
└─────────────────────────────────────────────────┘
```

### 4.2 分层职责

**目录结构：**

```
app/src/main/java/com/kweaver/dip/
├── data/
│   ├── api/            # Retrofit API 接口定义
│   ├── model/          # 数据模型（data class）
│   ├── repository/     # Repository 实现
│   └── local/
│       └── datastore/  # DataStore 本地存储
├── di/                 # Hilt 依赖注入模块
│   ├── NetworkModule.kt    # 网络层 DI
│   └── RepositoryModule.kt # Repository DI
├── domain/
│   └── usecase/        # UseCase 类（业务逻辑）
├── ui/
│   ├── screens/        # Compose 页面
│   └── navigation/     # 导航路由
└── runner/             # 测试 Runner
```

**各层职责：**

| 层 | 职责 | 关键技术 |
|---|---|---|
| **UI Layer** | 展示数据、处理用户交互 | Jetpack Compose, ViewModel, StateFlow |
| **Domain Layer** | 封装业务逻辑、协调多个 Repository | UseCase 类, Kotlin Coroutines + Flow |
| **Data Layer** | 数据获取（远程/本地）、数据转换 | Retrofit, OkHttp, DataStore |

**数据流向：**

```
UI (Compose) → ViewModel → UseCase → Repository → API (Retrofit)
                  ↑           ↑
                  └── StateFlow ← Flow ← Response
```

### 4.3 依赖注入 (Hilt)

使用 [Hilt](https://dagger.dev/hilt/) 管理依赖注入。

**Application 类** 需添加 `@HiltAndroidApp` 注解。

**关键 DI 模块：**

- **NetworkModule**：提供 OkHttpClient、Retrofit 实例、API 接口
- **RepositoryModule**：提供 Repository 实例
- **UseCaseModule**：提供 UseCase 实例

**使用方式：**

```kotlin
// ViewModel 中注入
@HiltViewModel
class MyViewModel @Inject constructor(
    private val myUseCase: MyUseCase
) : ViewModel() { ... }

// UseCase 中注入
class MyUseCase @Inject constructor(
    private val myRepository: MyRepository
) { ... }
```

**测试中替换依赖：**

```kotlin
@HiltAndroidTest
class MyIntegrationTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue @Mock
    lateinit var mockRepository: MyRepository
}
```

### 4.4 UI 层 (Jetpack Compose)

UI 全部使用 [Jetpack Compose](https://developer.android.com/compose) 构建，不使用传统 View 系统。

**核心模式：**

- **单向数据流**：ViewModel 通过 `StateFlow<UiState>` 暴露状态，Composable 订阅并渲染
- **Navigation Compose**：管理页面导航，路由定义在 `ui/navigation/Routes.kt`
- **Hilt Navigation Compose**：通过 `hiltViewModel()` 获取 ViewModel

**UiState 模式：**

```kotlin
data class MyScreenUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MyViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow(MyScreenUiState())
    val uiState: StateFlow<MyScreenUiState> = _uiState.asStateFlow()
}
```

**Compose 页面结构：**

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(...) }) { padding ->
        when {
            uiState.isLoading -> { CircularProgressIndicator() }
            uiState.error != null -> { ErrorView(uiState.error) }
            else -> { ContentList(uiState.items) }
        }
    }
}
```

**无障碍设计（影响 E2E 测试）：**

Compose 元素通过 `contentDescription` 暴露给无障碍服务和 Appium：

```kotlin
IconButton(onClick = onNavigateToSettings) {
    Icon(Icons.Default.Settings, contentDescription = "Settings")
}
```

> E2E 测试通过 `contentDescription` 查找元素。修改 UI 时务必保持 `contentDescription` 的一致性。

### 4.5 网络层 (Retrofit + OkHttp)

**API 接口定义：**

```kotlin
interface MyApi {
    @GET("api/path")
    suspend fun getItems(): List<Item>
}
```

**拦截器链：**

| 拦截器 | 职责 |
|---|---|
| `BaseUrlInterceptor` | 动态切换 Base URL（从 DataStore 读取） |
| `AuthInterceptor` | 自动附加 `Authorization: Bearer <token>` |
| `TokenAuthenticator` | 处理 401，自动刷新 Token |
| `HttpLoggingInterceptor` | 请求/响应日志（Debug 模式） |

**SSE 流式通信：**

使用 OkHttp 的 `callbackFlow` 实现 Server-Sent Events，用于 AI 对话的流式响应。

---

## 5. Harness Engineering

Harness Engineering 指的是确保代码质量的工程基础设施，包括配置文件、测试体系和自动化流水线。

### 5.1 项目配置文件

#### CLAUDE.md

位于项目根目录，是 Claude Code 的项目级指令文件。Claude Code 每次启动时自动读取。

**作用：**
- 声明项目对 AI 工具的硬性要求（如必须安装 gstack）
- 定义全局行为约束
- 提供项目级 Skill 路由规则

**示例结构：**

```markdown
## gstack (REQUIRED)
安装验证命令、安装步骤...

## Skill routing
When the user's request matches an available skill, invoke it...
```

#### Agent.md

位于项目根目录，定义代码变更的强制工作流。

**核心内容：**
- JDK 版本要求及原因
- 项目结构说明
- **强制验证流水线**：每次代码变更后必须执行 `/verify`

#### .claude/settings.local.json

Claude Code 的本地权限配置，定义哪些工具调用允许自动执行（无需手动确认）。

**典型配置：**

- 允许 Gradle 构建命令（需指定 `JAVA_HOME`）
- 允许 ADB 操作
- 允许 pytest 执行
- 允许向测试服务器发起网络请求

> 此文件包含本地路径和测试凭据，不应提交到版本控制。

### 5.2 单元测试

**位置：** `app/src/test/java/com/kweaver/dip/`

**框架与依赖：**

| 依赖 | 版本 | 用途 |
|---|---|---|
| JUnit 4 | (AndroidX 内置) | 测试框架 |
| Mockito | 5.14.2 | Mock 对象 |
| Mockito-Kotlin | 5.4.0 | Kotlin 友好的 Mock 扩展 |
| Kotlin Coroutines Test | 1.9.0 | 测试协程（`runTest`） |
| Arch Core Testing | 2.2.0 | 测试 ViewModel（`InstantTaskExecutorRule`） |

**测试分层：**

```
app/src/test/java/com/kweaver/dip/
├── data/
│   ├── api/            # API 拦截器测试
│   └── repository/     # Repository 单元测试
├── domain/
│   └── usecase/        # UseCase 单元测试
└── ui/
    └── screens/        # ViewModel 单元测试
```

**编写规范：**

```kotlin
class MyUseCaseTest {
    @Mock lateinit var repository: MyRepository
    private lateinit var useCase: MyUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = MyUseCase(repository)
    }

    @Test
    fun `should return data when repository succeeds`() = runTest {
        // Given
        val expected = listOf(Item(id = "1", name = "Test"))
        whenever(repository.getItems()).thenReturn(expected)

        // When
        val result = useCase()

        // Then
        assertEquals(expected, result)
    }
}
```

**运行：**

```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:testDebugUnitTest
```

### 5.3 集成测试

**位置：** `app/src/androidTest/java/com/kweaver/dip/`

**框架与依赖：**

| 依赖 | 用途 |
|---|---|
| Hilt Android Testing | `@HiltAndroidTest`, `HiltAndroidRule` |
| AndroidX Test Runner | `AndroidJUnit4` |
| AndroidX Test Ext JUnit | `assertThat` 等 |

**关键配置：**

`app/build.gradle.kts` 中指定自定义 TestRunner：

```kotlin
testInstrumentationRunner = "com.kweaver.dip.runner.HiltTestRunner"
```

**编写规范：**

```kotlin
@HiltAndroidTest
class MyIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var myRepository: MyRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun repositoryReturnsData() = runBlocking {
        val result = myRepository.getItems()
        assertThat(result).isNotEmpty()
    }
}
```

**运行（需要设备/模拟器）：**

```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:connectedDebugAndroidTest
```

### 5.4 E2E 测试

**位置：** `e2e-tests/`

**技术栈：**

| 组件 | 版本/说明 |
|---|---|
| Python | >= 3.10 |
| Appium Python Client | >= 4.0.0 |
| pytest | >= 8.0 |
| UiAutomator2 | Appium Driver |
| Appium Server | 运行在 localhost:4723 |

**目录结构：**

```
e2e-tests/
├── pages/              # Page Object Model
│   ├── base_page.py    # 基类（通用查找、等待方法）
│   ├── login_page.py   # 登录页面对象
│   ├── home_page.py    # 首页对象
│   └── chat_page.py    # 聊天页面对象
├── tests/              # 测试用例
│   ├── test_login.py
│   ├── test_navigation.py
│   └── ...
├── conftest.py         # pytest 配置（Appium 驱动初始化）
└── pyproject.toml      # 项目依赖
```

**Page Object 模式：**

每个页面封装为一个类，提供元素查找和操作方法：

```python
class ChatPage(BasePage):
    def is_loaded(self, timeout=10):
        # 检查页面是否加载完成
        ...

    def send_chat_message(self, text):
        self.type_message(text)
        self.send_message()
```

**conftest.py 核心 Fixture：**

| Fixture | 作用域 | 说明 |
|---|---|---|
| `appium_driver` | session | 创建 Appium WebDriver，测试结束后销毁 |
| `reset_app` | function（autouse） | 每个测试前清除应用数据 |
| `logged_in_driver` | function | 确保应用已登录并返回 driver |

**运行 E2E 测试：**

```bash
cd e2e-tests

# 创建虚拟环境
python -m venv .venv
.venv/Scripts/activate  # Windows
# source .venv/bin/activate  # Linux/macOS

# 安装依赖
pip install -e .

# 确保 Appium Server 运行
appium --address 127.0.0.1 --port 4723 &

# 运行测试
python -m pytest tests/ -v
```

**元素定位策略（按优先级）：**

1. **contentDescription（无障碍 ID）**：`find_by_accessibility_id("Settings")`
2. **UiSelector 文本匹配**：`new UiSelector().textContains("Start a conversation")`
3. **XPath 文本匹配**：`//*[@text='Agents']`

### 5.5 验证流水线 (/verify)

`/verify` 是项目自定义的 Skill，定义了代码变更后的 **强制** 验证流程。

**7 步流水线：**

```
┌──────────┐    ┌──────────┐    ┌──────────────┐    ┌──────────┐
│ 1. 编译   │───▶│ 2. 单元   │───▶│ 3. 集成测试   │───▶│ 4. 构建   │
│           │    │    测试   │    │  (需设备)     │    │   APK    │
└──────────┘    └──────────┘    └──────────────┘    └──────────┘
                                                            │
┌──────────┐    ┌──────────┐    ┌──────────────┐           │
│ 7. E2E   │◀───│ 6. Appium│◀───│ 5. 安装 APK  │◀──────────┘
│    测试   │    │  Server  │    │   到设备      │
└──────────┘    └──────────┘    └──────────────┘
```

| 步骤 | 命令 | 失败处理 |
|---|---|---|
| 1. 编译 | `./gradlew :app:compileDebugKotlin` | 检查语法错误 |
| 2. 单元测试 | `./gradlew :app:testDebugUnitTest` | 查看失败测试名和错误 |
| 3. 集成测试 | `./gradlew :app:connectedDebugAndroidTest` | 需设备；无设备则跳过 |
| 4. 构建 APK | `./gradlew :app:assembleDebug` | 检查构建配置 |
| 5. 安装 APK | `adb install -r` | 需设备；无设备则跳过 |
| 6. Appium | 检查/启动 4723 端口 | 启动失败则跳过 E2E |
| 7. E2E 测试 | `pytest tests/ -v` | 查看失败测试名 |

**规则：**
- 任何步骤失败，立即停止，不继续后续步骤
- 修复后必须从步骤 1 重新开始
- 仅修改 `.md` 或 `.gitignore` 时可跳过

### 5.6 CI 持续集成

项目使用 **GitHub Actions** 实现持续集成，配置文件位于 `.github/workflows/android-ci.yml`。

**触发条件：**

- `push` 到 `main` 或 `develop` 分支
- 针对 `main` 或 `develop` 分支的 Pull Request

同一分支的重复运行会自动取消（`concurrency` 配置）。

**流水线结构：**

```
┌──────────────┐     ┌──────────────┐
│  Lint 检查    │     │  单元测试     │     （并行执行）
│ lintDebug    │     │ testDebug    │
└──────┬───────┘     └──────┬───────┘
       │                    │
       └────────┬───────────┘
                ▼
       ┌──────────────┐
       │  构建 APK    │     （Lint + 测试通过后执行）
       │ Debug+Release│
       └──────────────┘
```

**Job 详情：**

| Job | 命令 | 产物 | 保留天数 |
|---|---|---|---|
| Lint & Code Check | `./gradlew lintDebug` | `lint-results-debug.html` | 7 |
| Unit Tests | `./gradlew testDebugUnitTest` | 测试报告 HTML | 7 |
| Build APK | `./gradlew assembleDebug` + `assembleRelease` | Debug APK + Release APK（已签名） | 30 |

**运行环境：**

- Ubuntu Latest
- JDK 17 (Temurin)
- Gradle 缓存（通过 `gradle/actions/setup-gradle@v4`）

**Release APK 签名：**

CI 构建 Release APK 时使用 keystore 签名。签名配置通过 GitHub Secrets 注入，`app/build.gradle.kts` 中的 `signingConfigs` 从环境变量读取密码：

```kotlin
signingConfigs {
    create("release") {
        val ksFile = file("kweaver-release.jks")
        if (ksFile.exists()) {
            storeFile = ksFile
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "kweaver"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
}
```

CI 中通过 base64 解码还原 keystore 文件：

```yaml
- name: Decode Keystore
  run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/kweaver-release.jks
```

**所需 GitHub Secrets：**

| Secret 名 | 说明 |
|---|---|
| `KEYSTORE_BASE64` | keystore 文件的 base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 密钥别名（`kweaver`） |
| `KEY_PASSWORD` | 密钥密码 |

**下载构建产物：**

CI 运行完成后，在 GitHub 仓库 → **Actions** → 选择对应运行 → **Artifacts** 区域下载：
- `app-debug` — Debug APK
- `app-release` — 已签名的 Release APK
- `lint-report` — Lint 检查报告
- `unit-test-report` — 单元测试报告

### 5.7 测试策略总结

```
                    ┌─────────────────────┐
                    │    E2E 测试          │  ← Appium + Python
                    │  覆盖关键用户流程     │     少量，高价值
                    ├─────────────────────┤
                    │    集成测试          │  ← Hilt + AndroidX Test
                    │  验证 DI 和组件集成   │     按需，核心模块
                    ├─────────────────────┤
                    │    单元测试          │  ← JUnit + Mockito
                    │  覆盖所有业务逻辑     │     大量，快速执行
                    └─────────────────────┘
                         测试金字塔
```

**TDD 工作流：**

1. **先写测试**：定义期望行为
2. **运行测试**：看到测试失败（红灯）
3. **实现功能**：最小代码使测试通过（绿灯）
4. **重构**：优化代码，确保测试仍通过
5. **执行 `/verify`**：运行完整流水线验证

---

## 附录：学习 Demo 仓库

本手册对应的完整示例项目托管在 GitHub：

**https://github.com/suilinfei001/kweaver-app**

该仓库包含本文档描述的所有内容：MVVM + Domain Layer 架构、Hilt 依赖注入、Jetpack Compose UI、三层测试体系（单元/集成/E2E）、AI 工具链配置（Claude Code + gstack）以及 `/verify` 自动化流水线。可直接 clone 作为学习参考。

---

## 附录：快速检查清单

新成员入职时，按此清单逐项验证环境：

- [ ] Android Studio 已安装并打开项目无报错
- [ ] JDK 17 已配置，`java -version` 输出正确
- [ ] Android SDK API 35 已安装
- [ ] 模拟器已创建并可以启动
- [ ] `./gradlew :app:compileDebugKotlin` 编译通过
- [ ] `./gradlew :app:testDebugUnitTest` 单元测试通过
- [ ] Claude Code 已安装，`claude` 命令可用
- [ ] gstack 已安装，`~/.claude/skills/gstack/bin/` 存在
- [ ] 在 Claude Code 中执行 `/verify` 可以正常运行
- [ ] Python >= 3.10 已安装，`e2e-tests/.venv` 已创建
- [ ] `npm install -g appium` 已执行，`appium` 命令可用
- [ ] GitHub Actions CI 配置已就绪（`.github/workflows/android-ci.yml`）
- [ ] GitHub Secrets 已添加（`KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`）

---

## 附录：项目配置文件原文

以下为本项目实际使用的三个核心配置文件原文，供新成员直接参考。

### CLAUDE.md（项目根目录）

```markdown
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

**必须要遵循Agent.md**
```

### Agent.md（项目根目录）

```markdown
## Build & Environment

### JDK 版本要求

本项目必须使用 **JDK 17** 编译。JDK 25 会导致 Kotlin 编译器崩溃（`IllegalArgumentException: 25.0.2`）。

编译命令：

```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:compileDebugKotlin
```

### Login API 响应格式

登录接口 (`GET /api/dip-hub/v1/login`) 的响应体可能是：
- **JSON 对象**：`{"access_token": "xxx", "refresh_token": "yyy"}`
- **纯字符串**：直接返回 token 值

代码已做兼容处理：先用 `JsonParser.parseString()` 判断类型，JSON 对象则提取 `access_token` 字段，否则整个 body 作为 token。

### 项目结构

- `data/api/` — Retrofit API 接口定义
- `data/repository/` — 数据仓库层
- `data/model/` — 数据模型
- `data/local/datastore/` — 本地存储（Token 等）
- `di/` — Hilt 依赖注入模块
- `ui/screens/` — Compose UI 页面
- `ui/navigation/` — 导航路由
- `e2e-tests/` — Python Appium E2E 测试项目

## Code Change Workflow

**每次修改代码后，必须执行 `/verify` 运行完整验证流水线。** 这不是可选的，是强制的。

流水线包含 7 个步骤，按顺序执行，任何步骤失败立即停止：

| 步骤 | 命令 | 说明 |
|---|---|---|
| 1. 编译 | `./gradlew :app:compileDebugKotlin` | 确保 Kotlin 代码无语法错误 |
| 2. 单元测试 | `./gradlew :app:testDebugUnitTest` | 验证 Repository / ViewModel 逻辑正确 |
| 3. 集成测试 | `./gradlew :app:connectedDebugAndroidTest` | 验证 Hilt DI 和 Android 组件集成 |
| 4. 构建 APK | `./gradlew :app:assembleDebug` | 生成可安装的 debug APK |
| 5. 安装 APK | `adb install -r` | 安装到 emulator 或真机 |
| 6. Appium 服务器 | 检查/启动 4723 端口 | 确保 E2E 测试环境就绪 |
| 7. E2E 测试 | `pytest tests/ -v` | 端到端 UI 自动化测试 |

### 适用场景

- 修改了 Kotlin/Java 源码（`app/src/main/`）
- 修改了测试代码（`app/src/test/`、`app/src/androidTest/`）
- 修改了构建配置（`build.gradle.kts`、`proguard-rules.pro`）
- 修改了 AndroidManifest.xml
- 修改了 E2E 测试代码（`e2e-tests/`）
- 修改了 Compose UI 元素的 `contentDescription` 或 `testTag`

### 不需要执行的场景

- 仅修改了文档文件（`.md`）
- 仅修改了 `.gitignore`
- 仅浏览/阅读代码，未做任何修改

### 失败处理

如果 `/verify` 在某步骤失败：
1. **不要跳过**该步骤继续后续步骤
2. 分析失败原因并修复代码
3. 修复后重新执行 `/verify`，从步骤 1 开始
```

### verify SKILL.md（~/.claude/skills/verify/）

```markdown
---
name: verify
version: 1.0.0
description: |
  Full pipeline validation for KWeaver DIP Android app. Runs compile, unit tests,
  instrumentation tests, build APK, install, and E2E tests. Fails fast on first error.
  Use after code changes to validate everything passes.
triggers:
  - verify
  - run all tests
  - full pipeline
  - validate changes
allowed-tools:
  - Bash
  - Read
  - TaskCreate
  - TaskUpdate
  - TaskList
---

## /verify — Full Pipeline Validation

Run the full validation pipeline for the KWeaver DIP Android app.
Execute each step in order; stop immediately if any step fails.

### Environment

```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8"
ANDROID_HOME="C:/Users/Yabo.sui/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
PROJECT_DIR="/c/Users/Yabo.sui/StudioProjects/kweaver-app"
```

### Pipeline Steps

Execute these steps **sequentially**. For each step, run the command and
check the exit code. If it fails, report the failure and STOP.
Do not continue to the next step.

Use TaskCreate/TaskUpdate to track progress so the user can see
what's happening.

#### Step 1: Compile

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" \
  ./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

On failure: report the compilation errors.

#### Step 2: Unit Tests

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" \
  ./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

On failure: report the failing test names and errors.

#### Step 3: Instrumentation Tests

**Precondition**: An Android emulator must be running or a device connected.

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" \
  ./gradlew :app:connectedDebugAndroidTest 2>&1 | tail -20
```

On failure: report the failing test names and errors.
If no emulator/device is connected, skip this step with a warning
and continue.

#### Step 4: Build Debug APK

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" \
  ./gradlew :app:assembleDebug 2>&1 | tail -10
```

On failure: report the build error.

#### Step 5: Install APK

**Precondition**: An Android emulator must be running or a device connected.

```bash
/c/Users/Yabo.sui/AppData/Local/Android/Sdk/platform-tools/adb.exe \
  install -r $PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk 2>&1
```

On failure: report the install error.
If no emulator/device is connected, skip with a warning and continue.

#### Step 6: Ensure Appium Server Running

Check if Appium is already running on port 4723:

```bash
curl -s http://127.0.0.1:4723/status 2>&1
```

If not running (connection refused or empty response), start it:

```bash
export ANDROID_HOME='C:\Users\Yabo.sui\AppData\Local\Android\Sdk'
export ANDROID_SDK_ROOT='C:\Users\Yabo.sui\AppData\Local\Android\Sdk'
appium --address 127.0.0.1 --port 4723 &
sleep 5
curl -s http://127.0.0.1:4723/status 2>&1
```

If Appium fails to start, skip E2E tests with a warning.

#### Step 7: Run E2E Tests

```bash
cd $PROJECT_DIR/e2e-tests && \
  .venv/Scripts/python -m pytest tests/ -v 2>&1
```

On failure: report the failing test names.

### Final Report

After all steps complete (or a step fails), print a summary:

```
Pipeline Results:
  1. Compile:          PASS/FAIL/SKIP
  2. Unit Tests:       PASS/FAIL/SKIP
  3. Instrumentation:  PASS/FAIL/SKIP
  4. Build APK:        PASS/FAIL/SKIP
  5. Install APK:      PASS/FAIL/SKIP
  6. Appium Server:    PASS/FAIL/SKIP
  7. E2E Tests:        PASS/FAIL/SKIP

Result: ALL PASS / FAILED at step N
```

If any step failed, explain what went wrong and suggest how to fix it.
```
