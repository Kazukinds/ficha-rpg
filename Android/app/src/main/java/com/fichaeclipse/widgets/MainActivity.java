package com.fichaeclipse.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import androidx.core.content.FileProvider;
import java.io.File;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.provider.DocumentsContract;
import androidx.documentfile.provider.DocumentFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String APP_URL = "file:///android_asset/www/index.html";
    private static final int REQ_NOTIF_PERM = 9100;
    private WebView webView;
    private FrameLayout splash;
    private FrameLayout offline;
    private boolean pendingOtaTrigger = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#09090B"));
        getWindow().setNavigationBarColor(Color.parseColor("#09090B"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#09090B"));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        webView = buildWebView();
        root.addView(webView);

        splash = buildSplash();
        root.addView(splash);

        offline = buildOfflinePanel();
        root.addView(offline);
        offline.setVisibility(View.GONE);

        setContentView(root);

        // Carrega bundled local — funciona offline sempre
        webView.loadUrl(APP_URL);

        // Background update check + notificação fora do app
        UpdateCheckWorker.ensureChannel(this);
        UpdateCheckWorker.schedule(this);
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF_PERM);
            }
        }

        handleOtaIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOtaIntent(intent);
    }

    private void handleOtaIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra(UpdateCheckWorker.EXTRA_START_OTA, false)) {
            pendingOtaTrigger = true;
            // Cancela notif uma vez aberta
            android.app.NotificationManager nm = (android.app.NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(UpdateCheckWorker.NOTIF_ID);
        }
    }

    private WebView buildWebView() {
        WebView wv = new WebView(this);
        wv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        wv.setBackgroundColor(Color.parseColor("#09090B"));

        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setLoadsImagesAutomatically(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setCacheMode(isOnline() ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ELSE_NETWORK);
        s.setUserAgentString(s.getUserAgentString() + " FichaEclipseApp/1.0");
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);

        // JS ↔ Java bridge (OTA update + versão nativa)
        wv.addJavascriptInterface(new UpdateBridge(), "EclipseNative");
        // Storage bridge — SAF folder picker + ler/escrever arquivos na pasta escolhida
        wv.addJavascriptInterface(new StorageBridge(), "EclipseStorage");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme();
                if (scheme != null && (scheme.equals("mailto") || scheme.equals("tel") ||
                        scheme.equals("whatsapp") || scheme.equals("intent") ||
                        scheme.equals("market") || scheme.startsWith("http") && isExternalHost(u.getHost()))) {
                    if (scheme.startsWith("http") && !isExternalHost(u.getHost())) return false;
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, u);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) { /* ignore */ }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (splash != null) {
                    splash.animate().alpha(0f).setDuration(350).withEndAction(() -> splash.setVisibility(View.GONE)).start();
                }
                offline.setVisibility(View.GONE);
                if (pendingOtaTrigger) {
                    pendingOtaTrigger = false;
                    view.postDelayed(() -> view.evaluateJavascript(
                            "window.otaCheckAndInstall&&window.otaCheckAndInstall(true)", null), 800);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame() && !isOnline()) {
                    offline.setVisibility(View.VISIBLE);
                }
            }
        });

        wv.setWebChromeClient(new WebChromeClient());

        wv.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                try {
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                    req.setMimeType(mimeType);
                    req.addRequestHeader("User-Agent", userAgent);
                    req.setDescription("Ficha Eclipse");
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    String name = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType);
                    req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (dm != null) dm.enqueue(req);
                } catch (Exception ignored) {}
            }
        });

        return wv;
    }

    private boolean isExternalHost(String host) {
        if (host == null) return false;
        return !host.endsWith("github.io") && !host.endsWith("kazukinds.github.io")
                && !host.endsWith("fichaeclipse") && !host.endsWith("localhost");
    }

    private FrameLayout buildSplash() {
        FrameLayout s = new FrameLayout(this);
        s.setBackgroundColor(Color.parseColor("#09090B"));
        s.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.gravity = android.view.Gravity.CENTER;
        box.setLayoutParams(bp);
        box.setGravity(android.view.Gravity.CENTER);

        TextView brand = new TextView(this);
        brand.setText("FICHA ECLIPSE");
        brand.setTextColor(Color.parseColor("#C8F542"));
        brand.setTextSize(14);
        brand.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        brand.setLetterSpacing(0.35f);
        brand.setGravity(android.view.Gravity.CENTER);

        TextView loading = new TextView(this);
        loading.setText("Carregando…");
        loading.setTextColor(Color.parseColor("#A0A0B0"));
        loading.setTextSize(12);
        loading.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = dp(12);
        loading.setLayoutParams(lp2);

        box.addView(brand);
        box.addView(loading);
        s.addView(box);
        return s;
    }

    private FrameLayout buildOfflinePanel() {
        FrameLayout o = new FrameLayout(this);
        o.setBackgroundColor(Color.parseColor("#09090B"));
        o.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.gravity = android.view.Gravity.CENTER;
        int pad = dp(24);
        box.setPadding(pad, pad, pad, pad);
        box.setLayoutParams(bp);
        box.setGravity(android.view.Gravity.CENTER);

        TextView t = new TextView(this);
        t.setText("Sem internet");
        t.setTextColor(Color.parseColor("#F0F0F5"));
        t.setTextSize(20);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setGravity(android.view.Gravity.CENTER);

        TextView s2 = new TextView(this);
        s2.setText("Tentando carregar do cache local.\nToque pra tentar de novo.");
        s2.setTextColor(Color.parseColor("#A0A0B0"));
        s2.setTextSize(13);
        s2.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        s2.setLayoutParams(lp);

        TextView btn = new TextView(this);
        btn.setText("TENTAR NOVAMENTE");
        btn.setTextColor(Color.parseColor("#09090B"));
        btn.setTextSize(12);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setLetterSpacing(0.2f);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setPadding(dp(24), dp(12), dp(24), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#C8F542"));
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);
        btn.setClickable(true);
        LinearLayout.LayoutParams lpb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpb.topMargin = dp(18);
        lpb.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        btn.setLayoutParams(lpb);
        btn.setOnClickListener(v -> {
            if (isOnline()) {
                offline.setVisibility(View.GONE);
                webView.reload();
            }
        });

        box.addView(t);
        box.addView(s2);
        box.addView(btn);
        o.addView(box);
        return o;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @SuppressWarnings("deprecation")
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Evita recriar activity ao rotacionar — WebView mantém estado JS.
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            ViewGroup p = (ViewGroup) webView.getParent();
            if (p != null) p.removeView(webView);
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    // ═══ JS ↔ Java Bridge — OTA update Github releases ═══
    public class UpdateBridge {
        @JavascriptInterface
        public String appVersion() {
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                return pi.versionName + "|" + pi.versionCode;
            } catch (Exception e) {
                return "0|0";
            }
        }

        @JavascriptInterface
        public void downloadAndInstall(String apkUrl) {
            runOnUiThread(() -> _doDownload(apkUrl));
        }
    }

    private void _doDownload(String url) {
        try {
            Uri uri = Uri.parse(url);
            String fname = "Eclipse-update.apk";
            File outDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
            if (!outDir.exists()) outDir.mkdirs();
            final File outFile = new File(outDir, fname);
            if (outFile.exists()) outFile.delete();

            DownloadManager.Request req = new DownloadManager.Request(uri);
            req.setTitle("Eclipse — atualização");
            req.setDescription("Baixando nova versão");
            req.setMimeType("application/vnd.android.package-archive");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationUri(Uri.fromFile(outFile));

            final DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm == null) return;
            final long id = dm.enqueue(req);

            // Progress polling → envia pro JS
            new Thread(() -> {
                while (true) {
                    DownloadManager.Query q = new DownloadManager.Query().setFilterById(id);
                    Cursor c = dm.query(q);
                    if (c != null && c.moveToFirst()) {
                        int statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int totalIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                        int soFarIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int status = statusIdx >= 0 ? c.getInt(statusIdx) : -1;
                        long total = totalIdx >= 0 ? c.getLong(totalIdx) : 0;
                        long soFar = soFarIdx >= 0 ? c.getLong(soFarIdx) : 0;
                        final int pct = total > 0 ? (int) (soFar * 100 / total) : 0;
                        runOnUiThread(() -> {
                            if (webView != null)
                                webView.evaluateJavascript("window.__otaProgress&&window.__otaProgress(" + pct + ")", null);
                        });
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            c.close();
                            runOnUiThread(() -> _installApk(outFile));
                            return;
                        }
                        if (status == DownloadManager.STATUS_FAILED) {
                            c.close();
                            runOnUiThread(() -> {
                                if (webView != null)
                                    webView.evaluateJavascript("window.__otaError&&window.__otaError('download falhou')", null);
                            });
                            return;
                        }
                    }
                    if (c != null) c.close();
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
            }).start();
        } catch (Exception e) {
            if (webView != null)
                webView.evaluateJavascript("window.__otaError&&window.__otaError('" + e.getMessage().replace("'", "") + "')", null);
        }
    }

    private void _installApk(File apk) {
        try {
            Uri contentUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentUri = FileProvider.getUriForFile(this,
                        "com.fichaeclipse.widgets.fileprovider", apk);
            } else {
                contentUri = Uri.fromFile(apk);
            }
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(contentUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(install);
            if (webView != null)
                webView.evaluateJavascript("window.__otaReady&&window.__otaReady()", null);
        } catch (Exception e) {
            if (webView != null)
                webView.evaluateJavascript("window.__otaError&&window.__otaError('install falhou')", null);
        }
    }

    // ═══ Storage Bridge — SAF folder picker + file IO ═══
    private static final String PREFS = "eclipse_storage";
    private static final String KEY_FOLDER_URI = "folder_uri";
    private static final int REQ_PICK_FOLDER = 9001;

    private SharedPreferences _prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private Uri _folderUri() {
        String s = _prefs().getString(KEY_FOLDER_URI, null);
        return s == null ? null : Uri.parse(s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_FOLDER) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri treeUri = data.getData();
                try {
                    final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(treeUri, flags);
                } catch (Exception ignored) {}
                _prefs().edit().putString(KEY_FOLDER_URI, treeUri.toString()).apply();
                String name = _folderDisplayName(treeUri);
                if (webView != null) {
                    webView.evaluateJavascript("window.__storagePicked&&window.__storagePicked(" + JSONObject.quote(name) + ")", null);
                }
            } else {
                if (webView != null) {
                    webView.evaluateJavascript("window.__storagePicked&&window.__storagePicked(null)", null);
                }
            }
        }
    }

    private String _folderDisplayName(Uri treeUri) {
        try {
            DocumentFile df = DocumentFile.fromTreeUri(this, treeUri);
            if (df != null && df.getName() != null) return df.getName();
        } catch (Exception ignored) {}
        return treeUri.getLastPathSegment() != null ? treeUri.getLastPathSegment() : "pasta";
    }

    public class StorageBridge {
        @JavascriptInterface
        public void pickFolder() {
            runOnUiThread(() -> {
                try {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    startActivityForResult(i, REQ_PICK_FOLDER);
                } catch (Exception e) {
                    if (webView != null) webView.evaluateJavascript(
                            "window.__storagePicked&&window.__storagePicked(null)", null);
                }
            });
        }

        @JavascriptInterface
        public String currentFolderName() {
            Uri u = _folderUri();
            if (u == null) return "";
            return _folderDisplayName(u);
        }

        @JavascriptInterface
        public boolean hasFolder() {
            return _folderUri() != null;
        }

        @JavascriptInterface
        public void clearFolder() {
            Uri u = _folderUri();
            if (u != null) {
                try {
                    getContentResolver().releasePersistableUriPermission(u,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception ignored) {}
            }
            _prefs().edit().remove(KEY_FOLDER_URI).apply();
        }

        @JavascriptInterface
        public String listFiles() {
            Uri u = _folderUri();
            if (u == null) return "[]";
            try {
                DocumentFile tree = DocumentFile.fromTreeUri(MainActivity.this, u);
                if (tree == null || !tree.isDirectory()) return "[]";
                JSONArray arr = new JSONArray();
                for (DocumentFile f : tree.listFiles()) {
                    if (!f.isFile()) continue;
                    String n = f.getName();
                    if (n == null) continue;
                    if (!n.toLowerCase().endsWith(".json")) continue;
                    JSONObject o = new JSONObject();
                    o.put("name", n);
                    o.put("size", f.length());
                    o.put("modified", f.lastModified());
                    arr.put(o);
                }
                return arr.toString();
            } catch (Exception e) {
                return "[]";
            }
        }

        @JavascriptInterface
        public String readFile(String name) {
            Uri u = _folderUri();
            if (u == null || name == null) return "";
            try {
                DocumentFile tree = DocumentFile.fromTreeUri(MainActivity.this, u);
                if (tree == null) return "";
                DocumentFile f = tree.findFile(name);
                if (f == null || !f.isFile()) return "";
                InputStream is = getContentResolver().openInputStream(f.getUri());
                if (is == null) return "";
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int r;
                while ((r = is.read(buf)) > 0) bos.write(buf, 0, r);
                is.close();
                return bos.toString("UTF-8");
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public boolean writeFile(String name, String content) {
            Uri u = _folderUri();
            if (u == null || name == null) return false;
            try {
                DocumentFile tree = DocumentFile.fromTreeUri(MainActivity.this, u);
                if (tree == null) return false;
                DocumentFile existing = tree.findFile(name);
                if (existing != null) existing.delete();
                DocumentFile f = tree.createFile("application/json", name);
                if (f == null) return false;
                OutputStream os = getContentResolver().openOutputStream(f.getUri());
                if (os == null) return false;
                os.write(content.getBytes("UTF-8"));
                os.flush();
                os.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean deleteFile(String name) {
            Uri u = _folderUri();
            if (u == null || name == null) return false;
            try {
                DocumentFile tree = DocumentFile.fromTreeUri(MainActivity.this, u);
                if (tree == null) return false;
                DocumentFile f = tree.findFile(name);
                if (f == null) return false;
                return f.delete();
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean renameFile(String oldName, String newName) {
            Uri u = _folderUri();
            if (u == null || oldName == null || newName == null) return false;
            try {
                DocumentFile tree = DocumentFile.fromTreeUri(MainActivity.this, u);
                if (tree == null) return false;
                DocumentFile f = tree.findFile(oldName);
                if (f == null) return false;
                return f.renameTo(newName);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
