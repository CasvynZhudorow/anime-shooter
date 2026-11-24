# Anime Shooter

A vertical shoot'em up (shmup) for Android with anime-style visuals, built with LibGDX + Kotlin + Ashley ECS.

## Features

- **3 Playable Ships** with distinct playstyles:
  - **Interceptor** (Cyan): Fast movement, piercing shots, Rapid Burst ultimate
  - **Balanced** (Green): Homing shots, Homing Missiles ultimate
  - **Heavy** (Orange): Wide spread shots, Nova Ring ultimate

- **Weapon Progression**: 5 upgrade levels per ship, each changing shot pattern
- **Ultimate System**: Charge meter filled by pickups, 5s duration, 30s cooldown
- **5 Stages** with 3 waves + boss each, boss rush on stages 4-5
- **Enemy Variety**: Straight shooters, Spread shooters, Multi-phase bosses
- **Pickups**: Weapon upgrades, Ultimate charge, Health, Score bonuses
- **No Monetization**: Completely free, offline, no ads, no IAP
- **Min API 21** (Android 5.0 Lollipop)

## Screenshots

*Add screenshots here after building*

## Building

### Prerequisites
- JDK 17+
- Android SDK (API 34) for local builds
- Git

### Local Development (Desktop)
```bash
./gradlew :desktop:run
```

### Local Android Build
```bash
./gradlew :android:assembleDebug
```

### Signed Release Build
Requires keystore configured in `android/keystore.jks` or environment variables:
```bash
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password
./gradlew :android:bundleRelease
```

## GitHub Actions CI/CD

### Continuous Integration (ci.yml)
Runs on every push/PR to main/develop:
- Builds debug APK
- Runs unit tests
- Runs Android Lint
- Runs instrumented tests on API 21 & 30 emulators

### Release (release.yml)
Triggers on version tags (v*):
- Decodes keystore from `KEYSTORE_BASE64` secret
- Builds signed release AAB
- Creates GitHub Release with AAB artifact

### Required Secrets (Repo Settings → Secrets → Actions)
| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore.jks |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Generate keystore:
```bash
keytool -genkeypair -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias starfall
base64 -w0 keystore.jks  # Copy output to KEYSTORE_BASE64 secret
```

## Project Structure

```
anime-shooter/
├── core/                 # Shared game logic (ECS, systems, screens)
│   └── src/main/kotlin/com/animeshooter/
│       ├── ecs/          # Components
│       ├── systems/      # IteratingSystems
│       ├── entities/     # EntityFactory
│       ├── models/       # ModelGenerator (placeholders)
│       ├── screens/      # ScreenAdapter implementations
│       └── GameMain.kt   # Game entry point
├── android/              # Android application module
│   ├── src/main/
│   │   ├── java/com/animeshooter/MainActivity.kt
│   │   ├── assets/models/    # .g3db models go here
│   │   └── res/
│   └── build.gradle.kts
├── desktop/              # Desktop launcher (LWJGL3)
│   └── src/main/kotlin/com/animeshooter/DesktopLauncher.kt
├── assets/prompts/       # AI asset generation prompts
├── .github/workflows/    # CI/CD pipelines
├── gradle/libs.versions.toml  # Version catalog
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

## Architecture

### Ashley ECS (Entity Component System)
- **Components** (`ecs/Components.kt`): Data-only (Transform, Velocity, Player, Weapon, Ultimate, Enemy, Projectile, Pickup, Health, Collision, Model, Particle, ScreenShake, Stage, Input)
- **Systems** (`systems/Systems.kt`): IteratingSystems in priority order:
  1. Movement (0)
  2. PlayerMovement (1)
  3. Weapon (2)
  4. EnemyAI (3)
  5. BossAI (4)
  6. Projectile (5)
  7. Pickup (6)
  8. Ultimate (7)
  9. Collision (10)
  10. Cleanup (100)

### 2.5D Rendering
- `ModelBatch` with `DefaultShaderProvider` for 3D models
- `OrthographicCamera` fixed at 540x960 (portrait)
- `ModelInstance` per entity, transformed each frame
- `SpriteBatch` for particles and UI overlay

### Screen Management
- LoadingScreen → MainMenuScreen → ShipSelectScreen → GameScreen → GameOverScreen / StageClearScreen

### Input Handling
- **Touch**: Drag to move
- **Keyboard** (desktop): WASD/Arrows for movement, Space for ultimate, ESC/P for pause

## AI Asset Pipeline

See `assets/prompts/models.md` for detailed prompts and export pipeline.

### Placeholder Models
All models generated programmatically via `ModelGenerator.kt` using `ModelBuilder`:
- Color-coded: Cyan=Interceptor, Green=Balanced, Orange=Heavy, Red=Enemies, Gold=Bosses
- Ready for drop-in replacement when AI assets arrive

### Converting AI Models
```bash
# Place .glb files in android/assets/models/
# Convert to .g3db:
fbx-conv -f model.glb model.g3db
```

## License

MIT License - see LICENSE file for details.

## Credits

- LibGDX framework
- Ashley ECS
- AI-generated assets (Meshy/Tripo/CSM prompts in assets/prompts/)