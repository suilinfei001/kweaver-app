# KWeaver DIP Android App: Architecture Refactor + TDD + Feature Implementation

## Context

The KWeaver DIP Android app implements a chat-oriented AI assistant client. Comparing with the web frontend (`reference/kweaver-dip-main/web`), the Android app is missing several feature modules (AI Store, Project Management, etc.) and has limited test coverage (6 unit test files, 1 integration test, 2 E2E test files). The user wants to:

1. Refactor architecture to add Domain layer (UseCases) while keeping MVVM
2. Follow TDD: write tests first, then implement
3. Cover all three test layers: Unit + Integration + E2E
4. Implement missing features from the web version

## Phase 1: Architecture Foundation — Add Domain Layer

### Goal
Add a `domain/` package with UseCase classes, keeping existing MVVM structure intact. ViewModels become thin coordinators, business logic moves to UseCases.

### Files to Create

```
app/src/main/java/com/kweaver/dip/domain/
├── usecase/
│   ├── auth/
│   │   └── LoginUseCase.kt          # Login + JWT parsing + token storage
│   ├── chat/
│   │   ├── CreateSessionUseCase.kt   # Session key creation
│   │   └── SendMessageUseCase.kt     # Message sending + SSE stream processing
│   ├── digitalhuman/
│   │   ├── ListDigitalHumansUseCase.kt
│   │   ├── SaveDigitalHumanUseCase.kt  # Validation + create/update
│   │   └── DeleteDigitalHumanUseCase.kt
│   ├── session/
│   │   └── ListSessionsUseCase.kt
│   ├── skill/
│   │   └── ListSkillsUseCase.kt
│   └── plan/
│       └── ListPlansUseCase.kt
└── di/
    └── UseCaseModule.kt              # Hilt module providing all UseCases
```

### Key Files to Modify
- `app/src/main/java/com/kweaver/dip/di/RepositoryModule.kt` — extract Repository interfaces
- ViewModels (ChatViewModel, DigitalHumanEditViewModel, etc.) — delegate to UseCases
- `app/src/main/java/com/kweaver/dip/di/` — add UseCaseModule

### Pattern

Each UseCase follows:
```kotlin
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(sessionKey: String, message: String): Flow<SseEvent> =
        chatRepository.streamChat(sessionKey, message, emptyList())
}
```

### Priority UseCases (by complexity reduction)
1. `SendMessageUseCase` + `CreateSessionUseCase` — extracts ChatViewModel streaming logic
2. `SaveDigitalHumanUseCase` — extracts form validation from DigitalHumanEditViewModel
3. `LoginUseCase` — extracts JWT parsing from AuthRepository
4. CRUD UseCases for remaining modules (simpler, more mechanical)

---

## Phase 2: TDD — Write Tests First (All Three Layers)

### 2A. Unit Tests

**New test files to create:**

```
app/src/test/java/com/kweaver/dip/
├── domain/usecase/
│   ├── LoginUseCaseTest.kt
│   ├── CreateSessionUseCaseTest.kt
│   ├── SendMessageUseCaseTest.kt
│   ├── SaveDigitalHumanUseCaseTest.kt
│   ├── ListDigitalHumansUseCaseTest.kt
│   ├── DeleteDigitalHumanUseCaseTest.kt
│   ├── ListSessionsUseCaseTest.kt
│   ├── ListSkillsUseCaseTest.kt
│   └── ListPlansUseCaseTest.kt
├── data/repository/
│   ├── ChatRepositoryTest.kt
│   ├── DigitalHumanRepositoryTest.kt
│   ├── SessionRepositoryTest.kt
│   ├── SkillRepositoryTest.kt
│   ├── PlanRepositoryTest.kt
│   └── GuideRepositoryTest.kt
└── data/api/
    └── AuthInterceptorTest.kt
```

**Existing tests to enhance:**
- Add edge cases to HomeViewModelTest, ChatViewModelTest, LoginViewModelTest
- Add empty state, loading state, concurrent operation tests

### 2B. Integration Tests

**New test files:**
```
app/src/androidTest/java/com/kweaver/dip/
├── ChatIntegrationTest.kt       # SSE streaming with real API
├── DigitalHumanIntegrationTest.kt  # CRUD with real API
├── SessionIntegrationTest.kt    # Session list/detail
└── HiltTestRunner.kt            # (existing, may need updates)
```

### 2C. E2E Tests

**New test files:**
```
e2e-tests/
├── pages/
│   ├── digital_human_page.py    # Page object for DH operations
│   ├── session_page.py          # Page object for session list
│   └── settings_page.py         # Page object for settings
├── tests/
│   ├── test_digital_human.py    # DH CRUD E2E
│   ├── test_chat_flow.py        # Full chat flow E2E
│   ├── test_session_history.py  # Session history E2E
│   └── test_navigation.py       # Navigation between all screens
```

---

## Phase 3: Implement Missing Features

### 3A. AI Store Module (highest priority missing feature)

**New files:**
```
app/src/main/java/com/kweaver/dip/
├── data/
│   ├── api/DipHubApi.kt         # Add store endpoints
│   ├── model/AppModels.kt       # Application, AppList, etc.
│   └── repository/AppRepository.kt
├── domain/usecase/
│   ├── ListAppsUseCase.kt
│   ├── InstallAppUseCase.kt
│   └── UninstallAppUseCase.kt
├── ui/screens/store/
│   ├── AppStoreScreen.kt        # Browse and install apps
│   ├── MyAppsScreen.kt          # Installed apps with pin
│   ├── AppStoreViewModel.kt
│   └── MyAppsViewModel.kt
└── di/
    └── UseCaseModule.kt         # Add store UseCases
```

**API endpoints to add** (from web: `/dip-hub/v1/applications/`):
- `GET /applications` — list installed apps
- `GET /applications/store` — list store apps
- `POST /applications/{appKey}/install`
- `DELETE /applications/{appKey}/uninstall`
- `POST /applications/{appKey}/pin`
- `DELETE /applications/{appKey}/unpin`

**Navigation changes:**
- Add "Store" tab to bottom navigation (5 tabs total)
- Add routes: `/store/my-apps`, `/store/app-store`

**Tests (TDD — write before implementation):**
- Unit: AppStoreViewModelTest, MyAppsViewModelTest, AppRepositoryTest
- Integration: AppIntegrationTest
- E2E: test_app_store.py

### 3B. Initial Configuration (Guide Enhancement)

Enhance existing GuideScreen to match web's Initial Configuration:
- Server setup wizard on first launch
- User preference configuration

### 3C. Future Features (lower priority, defer)
- Project Management (complex, involves TipTap-like document editor)
- Business Network (requires external integration knowledge)
- System Workbench (admin features)

---

## Execution Order

### Step 1: Repository Tests (TDD foundation)
Write tests for all 6 untested repositories, then run to see current state.

### Step 2: Add Domain Layer + UseCase Tests
Create `domain/usecase/` package, write UseCase tests first, then implement UseCases.

### Step 3: Refactor ViewModels
Update ViewModels to use UseCases instead of calling Repositories directly. Run existing ViewModel tests to verify no regressions.

### Step 4: Integration Tests
Write and run integration tests against real API.

### Step 5: E2E Tests
Write new page objects and E2E test files. Run full E2E suite.

### Step 6: AI Store Feature (TDD)
Write tests → implement models → implement repository → implement UseCase → implement ViewModel → implement UI screens → add navigation.

### Step 7: Verify
Run `/verify` (full pipeline: compile → unit tests → integration tests → build APK → install → E2E tests).

---

## Verification

After each step, run:
```bash
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:compileDebugKotlin
JAVA_HOME="C:/Users/Yabo.sui/.jdks/jdk-17.0.18+8" ./gradlew :app:testDebugUnitTest
```

Final verification via `/verify` (7-step pipeline per Agent.md).

## Critical Files Reference

| File | Purpose |
|------|---------|
| `app/src/main/java/com/kweaver/dip/di/NetworkModule.kt` | DI for network layer |
| `app/src/main/java/com/kweaver/dip/di/RepositoryModule.kt` | DI for repositories |
| `app/src/main/java/com/kweaver/dip/data/api/DipStudioApi.kt` | Studio API endpoints |
| `app/src/main/java/com/kweaver/dip/data/api/DipHubApi.kt` | Hub API endpoints (auth + store) |
| `app/src/main/java/com/kweaver/dip/ui/navigation/KWeaverNavGraph.kt` | Navigation routes |
| `app/src/main/java/com/kweaver/dip/ui/navigation/BottomNavBar.kt` | Bottom tab bar |
| `app/src/main/java/com/kweaver/dip/data/api/SseClient.kt` | SSE streaming client |
| `app/src/main/java/com/kweaver/dip/data/local/datastore/TokenDataStore.kt` | Token persistence |
| `reference/kweaver-dip-main/web/apps/dip/src/apis/` | Web API definitions (reference) |
