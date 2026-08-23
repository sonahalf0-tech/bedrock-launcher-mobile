# Bedrock Launcher Mobile (Android) 🎮

Полнофункциональный мобильный клиент-лаунчер для **Minecraft Bedrock Edition (Android)** с поддержкой мгновенного переключения версий, изолированными профилями, менеджером аддонов/текстур и оптимизатором FPS.

---

## 🚀 Основные возможности

1. **Каталог и менеджер версий:**
   - Импорт любых версий APK Minecraft Bedrock (от старых 0.14/1.12 до 1.20/1.21).
   - Автоматический анализ архитектуры (`arm64-v8a`, `armeabi-v7a`), версии и размера файла.
   - Быстрое переключение активной версии в один клик.

2. **Мгновенное переключение (Fast Switcher):**
   - **Shizuku API (Рекомендуется):** позволяет менять версии Bedrock в фоне за 2-3 секунды без Root-прав и без всплывающих окон подтверждения.
   - **Root доступ (`su`):** мгновенная установка с флагом даунгрейда (`pm install -r -d`).
   - **Стандартный PackageInstaller:** классическая установка с подтверждением системы.

3. **Изоляция профилей (Profile Sandbox):**
   - У каждого профиля — своя изолированная директория с мирами (`minecraftWorlds`), ресурс-паками (`resource_packs`), поведением (`behavior_packs`) и `options.txt`.
   - Ваши миры больше не сломаются при переходе со старой версии на новую и обратно!

4. **Менеджер аддонов, текстур и карт:**
   - Прямой импорт и распаковка `.mcpack`, `.mcaddon`, `.mcworld`, `.zip`.
   - Включение/отключение пакетов для выбранного профиля.

5. **FPS & Graphics Tweaker (`options.txt`):**
   - Разблокировка 60 / 90 / 120 / MAX FPS прямо из главного меню.
   - Настройка дальности прорисовки (чанков), угла обзора (FOV) и VSync.

---

## 🛠️ Стек технологий

- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material Design 3 (Dark Neon Theme)
- **База данных:** Room (SQLite)
- **Фоновые задачи:** Kotlin Coroutines & StateFlow
- **Управление пакетами:** Shizuku API + Android PackageInstaller + SAF
- **Работа с архивами:** Zip4j

---

## 📁 Структура проекта

```
BedrockLauncherMobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/bedrock/launcher/
│   │   │   ├── BedrockLauncherApp.kt       # Application класс
│   │   │   ├── domain/model/               # Модели: BedrockVersion, GameProfile, BedrockAddon
│   │   │   ├── data/
│   │   │   │   ├── local/                  # Room: Database, Entity, DAO
│   │   │   │   ├── repository/             # LauncherRepository
│   │   │   │   └── util/                   # ApkInspector, OptionsTxtParser, StorageHelper, AddonImporter
│   │   │   ├── installer/                  # ShizukuInstaller, RootInstaller, ProfileSwitcher, LaunchManager
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt         # Главная активность
│   │   │       ├── theme/                  # Цветовая палитра и Material3 стили
│   │   │       ├── navigation/             # Bottom Navigation & AppNavigation
│   │   │       ├── components/             # Reusable UI (GlassCard, GamerButton, StatusBadge)
│   │   │       └── screens/
│   │   │           ├── home/               # Главный экран (Кнопка ИГРАТЬ, FPS, активная версия)
│   │   │           ├── versions/           # Каталог версий и импорт APK
│   │   │           ├── profiles/           # Управление профилями и изоляцией миров
│   │   │           ├── mods/               # Аддоны, ресурс-паки и карты (.mcpack/.mcaddon)
│   │   │           └── settings/           # Настройка метода установки (Shizuku/Root)
│   │   ├── res/                            # Ресурсы, темы, строки, XML провайдеры
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📦 Сборка и запуск

1. Откройте проект `BedrockLauncherMobile` в **Android Studio** (Hedgehog, Iguana или новее).
2. Дождитесь синхронизации Gradle.
3. Соберите APK через меню: `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`.
4. Либо через терминал:
   ```bash
   ./gradlew assembleDebug
   ```
5. Установите полученный APK на Android-устройство и запустите лаунчер!
