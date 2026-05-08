## Build & Environment

### JDK 版本要求

本项目必须使用 **JDK 17** 编译。JDK 25 会导致 Kotlin 编译器崩溃（`IllegalArgumentException: 25.0.2`）。

编译命令：

```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:compileDebugKotlin
```

### 项目结构

- `data/api/` — Retrofit API 接口定义（待添加）
- `data/repository/` — 数据仓库层（待添加）
- `data/model/` — 数据模型（待添加）
- `data/local/datastore/` — 本地存储（待添加）
- `di/` — Hilt 依赖注入模块（待添加）
- `ui/screens/` — Compose UI 页面（待添加）
- `ui/navigation/` — 导航路由（待添加）
- `e2e-tests/` — Python Appium E2E 测试项目

## UI Design Workflow

**当需要实现 UI 布局（Compose Screen、组件、页面）时，必须先调用 `/frontend-design` 技能生成设计方案，再进行编码。**

这确保 UI 设计质量一致，避免直接写代码导致的设计缺陷。

适用场景：
- 新建 Compose Screen 页面
- 设计复杂的 Compose 布局组件
- 重新设计现有页面的布局

## Test-Driven Development

**新增或修改功能时，必须同步新增或修改对应的测试代码。** 这不是可选的，是强制的。

测试覆盖要求：

| 功能变更 | 必须更新的测试 |
|---|---|
| Repository 层 | `app/src/test/` 单元测试 |
| ViewModel 层 | `app/src/test/` 单元测试 |
| UseCase 层 | `app/src/test/` 单元测试 |
| 新增 Screen/页面 | `e2e-tests/pages/` Page Object + `e2e-tests/tests/` E2E 测试 |
| 新增导航路由 | `e2e-tests/tests/` E2E 导航测试 |
| 修改已有页面交互 | 更新对应的 E2E 测试 |

原则：
- 每个新功能必须有对应的测试，不能只写功能代码不写测试
- 修改已有功能时，同步更新受影响的测试用例
- E2E 测试使用 Page Object Model 模式，在 `e2e-tests/pages/` 中维护页面对象
- UI 元素必须设置 `contentDescription` 或 `testTag`，确保 E2E 测试可定位

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
