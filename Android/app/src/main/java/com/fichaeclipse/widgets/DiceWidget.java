package com.fichaeclipse.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.util.Random;

public class DiceWidget extends AppWidgetProvider {
    public static final String ACTION_ROLL = "com.fichaeclipse.widgets.ACTION_ROLL";
    public static final String EXTRA_SIDES = "sides";
    private static final Random RNG = new Random();

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) pushState(ctx, mgr, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (ACTION_ROLL.equals(intent.getAction())) {
            int sides = intent.getIntExtra(EXTRA_SIDES, 20);
            int roll = 1 + RNG.nextInt(sides);
            SharedPreferences sp = ctx.getSharedPreferences("fe", Context.MODE_PRIVATE);
            sp.edit().putInt("dice_last", roll).putInt("dice_sides", sides).apply();
            AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, DiceWidget.class));
            for (int id : ids) pushState(ctx, mgr, id);
        }
    }

    private void pushState(Context ctx, AppWidgetManager mgr, int id) {
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_dice);
        SharedPreferences sp = ctx.getSharedPreferences("fe", Context.MODE_PRIVATE);
        int last = sp.getInt("dice_last", 0);
        int sides = sp.getInt("dice_sides", 20);
        v.setTextViewText(R.id.dice_num, last > 0 ? String.valueOf(last) : "—");
        v.setTextViewText(R.id.dice_meta, last > 0 ? ("d" + sides) : "Toque");

        int[] btnIds = {R.id.btn_d4, R.id.btn_d6, R.id.btn_d8, R.id.btn_d10, R.id.btn_d12, R.id.btn_d20};
        int[] sidesMap = {4, 6, 8, 10, 12, 20};
        for (int i = 0; i < btnIds.length; i++) {
            Intent click = new Intent(ctx, DiceWidget.class).setAction(ACTION_ROLL).putExtra(EXTRA_SIDES, sidesMap[i]);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pi = PendingIntent.getBroadcast(ctx, 100 + sidesMap[i], click, flags);
            v.setOnClickPendingIntent(btnIds[i], pi);
        }
        mgr.updateAppWidget(id, v);
    }
}
