# Ficha Eclipse — APK Android

App nativo (WebView wrapper) do Ficha Eclipse PWA.

## Pré-requisitos
- Android Studio Hedgehog+ (2023.1)
- JDK 17
- Android SDK 34

## Build
```bash
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/Eclipse.apk
```

Ou abre no Android Studio → `Run`.

## Instalar
```bash
adb install -r app/build/outputs/apk/release/Eclipse.apk
```

## Sync com web
Antes de buildar, rode `sync-assets.sh` pra copiar webroot pra `app/src/main/assets/www/`.

## Componentes
- `MainActivity.java` — WebView wrapper
- `UpdateCheckWorker.java` — checa novas versões em background, notifica via channel
