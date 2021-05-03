package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.Stack;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    protected static AutoCompleteTextView url;
    protected static WebView webView;
    private ImageView back;
    private ImageView forward;
    private ImageView refresh;
    private ImageView home;
    private ImageView menuImage;
    protected String input;
    protected String strUrl;
    protected String forwardUrl;

    protected AutoCompleteTextView getUrl() {
        return url;
    }

    protected boolean isForward = false; // whether can forward to last page
    protected int isForwardCount = 0; // -1 means that we are loading the last page, 0 means that we are loading a new page

    private static final String[] data = new String[]{
            "www.baidu.com", "baidu.com",
            "www.google.com", "google.com",
            "www.youtube.com", "youtube.com",
            "www.bilibili.com", "bilibili.com",
            "www.zhihu.com", "zhihu.com"
    };
    MyWebViewClient webViewClient = new MyWebViewClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        webView = findViewById(R.id.show);
        url = findViewById(R.id.url);
        back = findViewById(R.id.back);
        forward = findViewById(R.id.forward);
        refresh = findViewById(R.id.refresh);
        home = findViewById(R.id.home);
        menuImage = findViewById(R.id.menu);

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(menuImage);
            }
        });

        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        strUrl = "https://www.google.com";
        webView.loadUrl(strUrl);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.
                this, android.R.layout.simple_dropdown_item_1line, data);
        url.setAdapter(adapter);
        url.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard(MainActivity.this);
                    input = url.getText().toString();
                    if (!(input.startsWith("http://") || input.startsWith("https://"))) {
                        strUrl = "http://" + input;
                    }
                    boolean isSearched = true;
                    String[] suffix = {".com", ".net", ".sg", ".cn"};
                    for (String s : suffix) {
                        if (input.contains(s)) {
                            isSearched = false;
                            break;
                        }
                    }
                    if (isSearched) {
                        strUrl = "http://www.google.com/search?q=" + input;
                        webView.loadUrl(strUrl);
                    }
                    webView.loadUrl(strUrl);
                    url.clearFocus();
                    return true;
                }
                return false;
            }
        });

        back.setEnabled(false);
        forward.setEnabled(false);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageGoBack(webView, webViewClient);
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageGoForward(webView, webViewClient);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String strUrl = url.getText().toString();
//                webView.loadUrl(strUrl);
                webView.loadUrl(webViewClient.getCurrentUrl());
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("http://www.baidu.com");
            }
        });
    }


    protected void changeStatueOfWebToolsButton() {

        if (webView.canGoBack()) {
            back.setEnabled(true);
        } else {
            back.setEnabled(false);
        }

        if (isForward) {
            forward.setEnabled(true);
        } else {
            forward.setEnabled(false);
        }

    }

    public boolean pageGoBack(WebView web, MyWebViewClient client) {
        final String url = client.popLastPageUrl();
        if (url != null) {
            web.loadUrl(url);
            isForward = true;
            isForwardCount = -1;
            return true;
        }
        finish();
        return false;
    }

    public boolean pageGoForward(WebView web, MyWebViewClient client) {
        web.loadUrl(forwardUrl);
        forwardUrl = null;
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            return pageGoBack(webView, webViewClient);
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // hide keyboard
        imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        SettingsActivity.actionStart(MainActivity.this);
                        return true;
                    case R.id.about:
                        AboutActivity.actionStart(MainActivity.this);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    class MyWebViewClient extends WebViewClient {
        private final Stack<String> mUrls = new Stack<>();
        private boolean mIsLoading;
        private String mUrlBeforeRedirect;
        private String currentUrl;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (isForwardCount == 0) {
                isForward = false;
            } else {
                isForwardCount++;
            }

            changeStatueOfWebToolsButton();

            if (mIsLoading && mUrls.size() > 0) {
                mUrlBeforeRedirect = mUrls.pop();
            }
            recordUrl(url);
            currentUrl = url;
            this.mIsLoading = true;
            MainActivity.url.setText(currentUrl);
        }

        public String getCurrentUrl() {
            return currentUrl;
        }

        private void recordUrl(String url) {
            if (!TextUtils.isEmpty(url) && !url.equalsIgnoreCase(getLastPageUrl())) {
                mUrls.push(url);
            } else if (!TextUtils.isEmpty(mUrlBeforeRedirect)) {
                mUrls.push(mUrlBeforeRedirect);
                mUrlBeforeRedirect = null;
            }
        }

        private synchronized String getLastPageUrl() {
            return mUrls.size() > 0 ? mUrls.peek() : null;
        }

        public String popLastPageUrl() {
            forwardUrl = currentUrl;
            if (mUrls.size() >= 2) {
                mUrls.pop();
                return mUrls.pop();
            }
            return null;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (this.mIsLoading) {
                this.mIsLoading = false;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String strUrl) {
            if (strUrl == null) return false;

            try {
                if (strUrl.startsWith("weixin://") //微信
                        || strUrl.startsWith("alipays://") //支付宝
                        || strUrl.startsWith("mailto://") //邮件
                        || strUrl.startsWith("tel://")//电话
                        || strUrl.startsWith("dianping://")//大众点评
                        || strUrl.startsWith("baiduboxapp://")
                    //其他自定义的scheme
                ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
                    startActivity(intent);
                    return true;
                }
            } catch (Exception e) {
                return true;
            }

            webView.loadUrl(strUrl);
            return true;
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
                strUrl = "http://www.google.com/search?q=" + input;
                webView.loadUrl(strUrl);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}