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

Run the full validation pipeline for the KWeaver DIP Android app. Execute each step in order; stop immediately if any step fails.

### Environment

```
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8"
ANDROID_HOME="C:/Users/Yabo.sui/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
PROJECT_DIR="/c/Users/Yabo.sui/StudioProjects/kweaver-app"
```

### Pipeline Steps

Execute these steps **sequentially**. For each step, run the command and check the exit code. If it fails, report the failure and STOP. Do not continue to the next step.

Use TaskCreate/TaskUpdate to track progress so the user can see what's happening.

#### Step 1: Compile

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

On failure: report the compilation errors.

#### Step 2: Unit Tests

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

On failure: report the failing test names and errors.

#### Step 3: Instrumentation Tests

**Precondition**: An Android emulator must be running or a device connected.

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:connectedDebugAndroidTest 2>&1 | tail -20
```

On failure: report the failing test names and errors.

If no emulator/device is connected, skip this step with a warning and continue.

#### Step 4: Build Debug APK

```bash
cd $PROJECT_DIR && JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:assembleDebug 2>&1 | tail -10
```

On failure: report the build error.

#### Step 5: Install APK

**Precondition**: An Android emulator must be running or a device connected.

```bash
/c/Users/Yabo.sui/AppData/Local/Android/Sdk/platform-tools/adb.exe install -r $PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk 2>&1
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
cd $PROJECT_DIR/e2e-tests && .venv/Scripts/python -m pytest tests/ -v 2>&1
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
