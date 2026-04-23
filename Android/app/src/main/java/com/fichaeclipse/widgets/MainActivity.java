package com.fichaeclipse.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String APP_URL = "file:///android_asset/www/index.html";
    private WebView webView;
    private FrameLayout splash;
    private FrameLayout offline;

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
}
