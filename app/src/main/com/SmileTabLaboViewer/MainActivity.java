package com.SmileTabLaboViewer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        
        setContentView(createLayout());

       
        webView = findViewById(R.id.webView);
        initializeWebView();

        
        findViewById(R.id.button_top).setOnClickListener(v -> loadPage("https://wiki3.jp/SmileTabLabo/"));
        findViewById(R.id.button_menu).setOnClickListener(v -> loadPage("https://wiki3.jp/SmileTabLabo/page/2"));
        findViewById(R.id.button_pagelist).setOnClickListener(v -> loadPage("https://wiki3.jp/SmileTabLabo/pageList"));
        findViewById(R.id.button_screenshot).setOnClickListener(v -> takeScreenshot());
    }

    private LinearLayout createLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFFF9F9F9);

        // ヘッダー作成
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFF009688);
        header.setPadding(10, 10, 10, 10);

   
        Button buttonTop = createButton("トップ", R.id.button_top);
        Button buttonMenu = createButton("メニュー", R.id.button_menu);
        Button buttonPagelist = createButton("一覧", R.id.button_pagelist);
        Button buttonScreenshot = createButton("スクショ", R.id.button_screenshot);

        header.addView(buttonTop);
        header.addView(buttonMenu);
        header.addView(buttonPagelist);
        header.addView(buttonScreenshot);

        layout.addView(header);

 
        WebView webView = new WebView(this);
        webView.setId(R.id.webView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        webView.setLayoutParams(params);
        layout.addView(webView);

        return layout;
    }

    private Button createButton(String text, int id) {
        Button button = new Button(this);
        button.setId(id);
        button.setText(text);
        button.setBackgroundColor(0xFFFFFFFF);
        button.setTextColor(0xFF009688);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        params.setMargins(5, 0, 5, 0); // ボタン間に5dpの余白を設定
        button.setLayoutParams(params);
        return button;
    }

    private void initializeWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("https://wiki3.jp/SmileTabLabo/")) {
                    injectBasicCSS(view);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("https://wiki3.jp/SmileTabLabo/")) {
                    injectFullCSSAndRemoveAds(view); 
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (isBlockedUrl(url)) {
                    return true; 
                }
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void loadPage(String url) {
        webView.loadUrl(url);
    }

    private void injectBasicCSS(WebView view) {
        String basicCss =
            "document.body.style.backgroundColor = '#f9f9f9';" +
            "document.body.style.fontFamily = 'Arial, sans-serif';";
        view.evaluateJavascript("javascript:(function() {" + basicCss + "})()", null);
    }

    private void injectFullCSSAndRemoveAds(WebView view) {
        String fullCssAndJs =
            "document.body.style.backgroundColor = '#f9f9f9';" +
            "document.body.style.fontFamily = 'Arial, sans-serif';" +
            "document.querySelectorAll('.ad-banner, .ad-box, iframe, [id*=\"ad\"], [class*=\"ad\"]').forEach(e => e.remove());" +
            "document.querySelectorAll('a').forEach(e => e.style.color = '#009688');" +
            "document.querySelectorAll('a[href*=\"SmileTabLabo/page/2\"], a[href*=\"SmileTabLabo/pagelist\"]').forEach(e => e.remove());" +
            "setInterval(() -> {" +
            "    document.querySelectorAll('.ad-banner, .ad-box, iframe, [id*=\"ad\"], [class*=\"ad\"]').forEach(e => e.remove());" +
            "}, 1000);";

        view.evaluateJavascript("javascript:(function() {" + fullCssAndJs + "})()", null);
    }

    private boolean isBlockedUrl(String url) {
        String[] blockedUrls = {
            "https://cat.jp2.as.criteo.com/",
            "https://trace.popin.cc/ju/",
            "https://sin3-ib.adnxs.com/",
            "https://cr.adsappier.com/",
            "https://static-content-1.smadex.com/",
            "https://adclick.g.doubleclick.net"
        };
        for (String blocked : blockedUrls) {
            if (url.startsWith(blocked)) {
                return true;
            }
        }
        return false;
    }

    private void takeScreenshot() {
        webView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(webView.getDrawingCache());
        webView.setDrawingCacheEnabled(false);

        try {
            File screenshotDir = new File("/sdcard/DCIM/Screenshot/");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = timeStamp + ".png";
            File screenshotFile = new File(screenshotDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }

            runOnUiThread(() -> Toast.makeText(this, "スクリーンショットを保存しました: " + screenshotFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "スクリーンショット保存中にエラー: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true; //
        }
        return super.onKeyDown(keyCode, event);
    }
}