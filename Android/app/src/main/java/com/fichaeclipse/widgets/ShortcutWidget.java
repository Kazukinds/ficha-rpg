package com.fichaeclipse.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * Base for the 4 "shortcut" widgets (Iniciativa, Nível, Timer, Notas).
 * Each renders a labeled card that opens MainActivity when tapped.
 * Subclasses override getLabel() + getIcon() to customize.
 */
public abstract class ShortcutWidget extends AppWidgetProvider {

    protected abstract String getLabel();
    protected abstract String getHint();
    protected abstract String getAccentColor(); // hex

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(ctx, mgr, id);
    }

    protected void render(Context ctx, AppWidgetManager mgr, int id) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_shortcut);
        rv.setTextViewText(R.id.sw_label, getLabel());
        rv.setTextViewText(R.id.sw_hint, getHint());
        rv.setTextColor(R.id.sw_label, android.graphics.Color.parseColor(getAccentColor()));

        Intent launch = new Intent(ctx, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(ctx, id, launch, flags);
        rv.setOnClickPendingIntent(R.id.sw_root, pi);

        mgr.updateAppWidget(id, rv);
    }

    public static class Iniciativa extends ShortcutWidget {
        protected String getLabel() { return "INICIATIVA"; }
        protected String getHint() { return "Rolar iniciativa do grupo"; }
        protected String getAccentColor() { return "#22D3EE"; }
    }

    public static class Nivel extends ShortcutWidget {
        protected String getLabel() { return "NÍVEL"; }
        protected String getHint() { return "Ver progressão e XP"; }
        protected String getAccentColor() { return "#C8F542"; }
    }

    public static class Timer extends ShortcutWidget {
        protected String getLabel() { return "TIMER"; }
        protected String getHint() { return "Contagem de tempo"; }
        protected String getAccentColor() { return "#FBBF24"; }
    }

    public static class Notas extends ShortcutWidget {
        protected String getLabel() { return "NOTAS"; }
        protected String getHint() { return "Abrir anotações da ficha"; }
        protected String getAccentColor() { return "#A78BFA"; }
    }
}
