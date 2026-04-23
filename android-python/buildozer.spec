[app]

title = Eclipse
package.name = eclipse
package.domain = com.kazuki.fichaeclipse

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,html,js,svg,json,webmanifest
source.include_patterns = assets/*,assets/**/*

version = 3.4

requirements = python3,kivy,pyjnius,android

orientation = portrait
fullscreen = 0

# Permissões — internet opcional (offline funciona), storage pra pasta Documents
android.permissions = INTERNET,READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,MANAGE_EXTERNAL_STORAGE

android.api = 34
android.minapi = 24
android.ndk_api = 24
android.archs = arm64-v8a,armeabi-v7a
android.allow_backup = True

# Icons
icon.filename = assets/www/icons/icon-512.png
presplash.filename = assets/www/icons/icon-512.png

android.manifest.intent_filters =

# Entry point
android.entrypoint = org.kivy.android.PythonActivity

# Output APK nome
android.release_artifact = apk

[buildozer]

log_level = 2
warn_on_root = 1
