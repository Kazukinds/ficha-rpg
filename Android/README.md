# Ficha Eclipse — Widgets Android Nativos

5 widgets nativos pro home screen Android:
1. **Dados** — roll d20 (completo neste MVP)
2. **Sessão** — cronômetro
3. **Progressão** — XP ring
4. **Notas** — bloco rápido
5. **Iniciativa** — tracker combate

## Pré-requisitos
- Android Studio Hedgehog+ (2023.1)
- JDK 17
- Android SDK 34

## Build
```bash
cd android-widgets
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/app-debug.apk
```

Ou abre no Android Studio → `Run`.

## Instalar
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Depois long-press home screen → **Widgets** → Ficha Eclipse → arrasta.

## Estender (4 widgets restantes)

Pra cada widget novo, crie 4 arquivos copiando pattern do `DiceWidget`:

1. `java/.../XxxWidget.java` — `AppWidgetProvider` subclass
2. `res/layout/widget_xxx.xml` — layout RemoteViews
3. `res/xml/widget_xxx_info.xml` — metadados (size, preview)
4. `AndroidManifest.xml` — registra `<receiver>`

Exemplo mínimo Timer:
```java
public class TimerWidget extends AppWidgetProvider {
  public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids){
    for(int id:ids){
      RemoteViews v=new RemoteViews(ctx.getPackageName(),R.layout.widget_timer);
      long el=ctx.getSharedPreferences("fe",0).getLong("timer_el",0);
      v.setTextViewText(R.id.t_display,fmt(el));
      mgr.updateAppWidget(id,v);
    }
  }
}
```

## Limitações widgets Android
- **Sem WebView** — só RemoteViews (TextView, ImageView, Button, ListView, ProgressBar)
- **Interatividade limitada** — clicks abrem Activity ou disparam BroadcastReceiver
- Update mínimo 30min via sistema; forçar via `AppWidgetManager.updateAppWidget()`

## Sync com web app
Widgets leem/escrevem `SharedPreferences` com keys iguais ao `localStorage` do PWA.
Sync manual via export/import JSON (futuro: Firebase/backend).
