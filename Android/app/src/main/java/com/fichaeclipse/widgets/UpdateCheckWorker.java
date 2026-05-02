package com.fichaeclipse.widgets;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class UpdateCheckWorker extends Worker {

    public static final String CHANNEL_ID = "eclipse_updates";
    public static final String UNIQUE_NAME = "eclipse_update_check";
    private static final String PREFS = "eclipse_ota_state";
    private static final String KEY_LAST_NOTIFIED_TAG = "last_notified_tag";
    private static final String OTA_REPO = "Kazukinds/eclipse";
    public static final int NOTIF_ID = 4711;
    public static final String EXTRA_START_OTA = "start_ota";

    public UpdateCheckWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    public static void schedule(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                UpdateCheckWorker.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, req);
    }

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            NotificationChannel ch = nm.getNotificationChannel(CHANNEL_ID);
            if (ch == null) {
                ch = new NotificationChannel(CHANNEL_ID, "Atualizações",
                        NotificationManager.IMPORTANCE_HIGH);
                ch.setDescription("Avisa quando nova versão está disponível");
                nm.createNotificationChannel(ch);
            }
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            JSONObject rel = fetchLatestRelease();
            if (rel == null) return Result.retry();
            String tag = rel.optString("tag_name", "");
            int latestCode = parseVersionCode(tag);
            int curCode = currentVersionCode();
            if (latestCode > curCode && latestCode > 0) {
                SharedPreferences sp = getApplicationContext()
                        .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String last = sp.getString(KEY_LAST_NOTIFIED_TAG, "");
                if (!tag.equals(last)) {
                    notifyUpdate(tag, rel.optString("name", ""));
                    sp.edit().putString(KEY_LAST_NOTIFIED_TAG, tag).apply();
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private int currentVersionCode() {
        try {
            Context c = getApplicationContext();
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            int byTag = parseVersionCode(pi.versionName);
            return byTag > 0 ? byTag : pi.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseVersionCode(String tag) {
        if (tag == null) return 0;
        String t = tag.replaceAll("^v", "");
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(?:\\.(\\d+))?").matcher(t);
        if (!m.find()) return 0;
        int major = Integer.parseInt(m.group(1));
        int minor = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        return major * 1000 + minor * 10;
    }

    private JSONObject fetchLatestRelease() {
        HttpURLConnection conn = null;
        try {
            URL u = new URL("https://api.github.com/repos/" + OTA_REPO + "/releases/latest");
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "FichaEclipseApp");
            int code = conn.getResponseCode();
            if (code != 200) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void notifyUpdate(String tag, String name) {
        Context ctx = getApplicationContext();
        ensureChannel(ctx);
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra(EXTRA_START_OTA, true);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, piFlags);

        String title = "Nova versão " + tag + " disponível";
        String text = (name == null || name.isEmpty())
                ? "Toque para atualizar Ficha Eclipse"
                : name;

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        try {
            NotificationManagerCompat.from(ctx).notify(NOTIF_ID, b.build());
        } catch (SecurityException ignored) {}
    }
}
