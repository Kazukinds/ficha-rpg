# Eclipse — keep activities / receivers
-keep public class com.fichaeclipse.widgets.** { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends android.webkit.WebView {
    public *;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**
# Suppress warnings for missing annotations
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.**
