## Build & Environment

### JDK 版本要求

本项目必须使用 **JDK 17** 编译。JDK 25 会导致 Kotlin 编译器崩溃（`IllegalArgumentException: 25.0.2`）。

编译命令（详细步骤见下方流水线）：

```bash
# 设置 JDK 17 环境变量后执行 Gradle 命令
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

流水线包含 5 个本地验证步骤（步骤 6-7 需要设备连接和 Appium 环境）：

| 步骤 | 命令 | 说明 |
|---|---|---|
| 1. 编译 | `JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:compileDebugKotlin` | 确保 Kotlin 代码无语法错误（JDK 17 必须） |
| 2. 单元测试 | `JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:testDebugUnitTest` | 验证 Repository / ViewModel 逻辑正确 |
| 3. 集成测试 | `JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:connectedDebugAndroidTest` | 验证 Hilt DI 和 Android 组件集成 |
| 4. 构建 APK | `JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:assembleDebug` | 生成可安装的 debug APK |
| 5. 安装 APK | `"C:\Users\Yabo.sui\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk` | 安装到 Android 设备 |

**环境要求**：
- JDK 17（项目必须使用，`jdk-17.0.18+8`）
- Android SDK（ADB 路径：`C:\Users\Yabo.sui\AppData\Local\Android\Sdk\platform-tools`）
- Android 设备连接（通过 `adb devices` 确认）

**E2E 测试环境**（需要额外配置）：
- Appium 服务器运行在 4723 端口
- 设备需要 `WRITE_SECURE_SETTINGS` 权限
- 运行命令：`cd e2e-tests; py -m pytest tests/ -v --udid <设备ID> --no-reset`

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

## Logcat 日志排查

当需要在设备上排查运行时问题时，使用以下命令：

### 获取 APP PID

```powershell
adb.exe shell pidof com.kweaver.dip
```

### 查看应用日志

```powershell
# 抓取指定 PID 的所有日志
adb.exe logcat -d --pid=<PID>

# 过滤特定关键字（如类名、日志TAG）
adb.exe logcat -d --pid=<PID> | Select-String -Pattern "SseChatService|ChatViewModel|doSend|SSE|Chunk"

# 过滤特定日志级别（D = Debug，V = Verbose，E = Error）
adb.exe logcat -d --pid=<PID> | Select-String -Pattern "D\/com\.kweaver"
```

### 实时监控日志

```powershell
# 实时监控指定 PID 日志
adb.exe logcat --pid=<PID>

# 监控所有应用日志（谨慎使用，输出量大）
adb.exe logcat -d
```

### 清除日志缓冲区

```powershell
# 清除所有日志缓冲区（重新开始抓取干净日志）
adb.exe logcat -c
```

### 查看日志缓冲区大小

```powershell
adb.exe logcat -g
```

### 常用排查场景

| 场景 | 命令 |
|---|---|
| 查看 OkHttp SSE 响应数据 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "okhttp.OkHttpClient"` |
| 查看应用 Debug 日志 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "D\/com\.kweaver"` |
| 查找应用崩溃异常 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "FATAL|AndroidRuntime|Exception"` |
| 查看 SSE 原始数据 | `adb.exe logcat -d --pid=<PID> \| Select-String -Pattern "data:"` |
| 清空后重新测试 | `adb.exe logcat -c` 然后重新执行操作并抓取新日志 |

**注意**：执行 `adb.exe logcat -c` 清除日志后，**必须重新打开 APP 并执行操作**，才能抓到新日志。

### 示例：排查流式输出问题

```powershell
# 1. 获取 PID
adb.exe shell pidof com.kweaver.dip

# 2. 清除旧日志
adb.exe logcat -c

# 3. 重新打开 APP，发送消息

# 4. 抓取日志（<PID> 替换为实际 PID）
adb.exe logcat -d --pid=<PID>

# 5. 过滤关键词
adb.exe logcat -d --pid=<PID> | Select-String -Pattern "SseChatService|ChatViewModel|SSE"
```
