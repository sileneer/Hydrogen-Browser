package com.sileneer.hydrogenbrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sileneer.hydrogenbrowser.common.base.BaseActivity;
import com.sileneer.hydrogenbrowser.common.utils.ActivityCollector;
import com.sileneer.hydrogenbrowser.common.utils.Utils;
import com.sileneer.hydrogenbrowser.settings.SettingsActivity;

import me.jingbin.progress.WebProgress;

public class MainActivity extends BaseActivity {

    protected AutoCompleteTextView addressBar;
    protected WebView webView;
    private ImageView back;
    private ImageView forward;
    private ImageView refresh;
    private ImageView home;
    private ImageView menuImage;
    private TextView multiTabButton;
    protected String inputFromAddressBar;
    protected String urlFromInput;
    protected String currentUrl;

    private WebProgress progressBar;

    private SharedPreferences sharedPref;

    public String homepageUrl;

    MyWebViewClient webViewClient = new MyWebViewClient();
    MyWebChromeClient webChromeClient = new MyWebChromeClient();

    private static final String[] data = new String[]{
            "www.baidu.com", "baidu.com",
            "www.google.com", "google.com",
            "www.youtube.com", "youtube.com",
            "www.bilibili.com", "bilibili.com",
            "www.zhihu.com", "zhihu.com"
    };

    private static final String[] searchPrefix = new String[]{
            "http://www.google.com/search?q=",
            "http://www.baidu.com/s?wd=",
            "https://www.bing.com/search?q=",
            "https://duckduckgo.com/?q="
    };

    public static final String[] searchEngines = {"Google", "Baidu", "Bing", "DuckDuckGo"};

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("config", 0);

        homepageUrl = sharedPref.getString("homepage", "www.google.com");

        webView = findViewById(R.id.webview);
        addressBar = findViewById(R.id.url);
        back = findViewById(R.id.back);
        forward = findViewById(R.id.forward);
        refresh = findViewById(R.id.refresh);
        home = findViewById(R.id.home);
        menuImage = findViewById(R.id.menu);
        progressBar = findViewById(R.id.progress_bar);
        multiTabButton = findViewById(R.id.multi_tab_button);

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(menuImage);
            }
        });

        multiTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMultiTabMenu(multiTabButton);
            }
        });

        initWebView();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.
                this, android.R.layout.simple_dropdown_item_1line, data);
        addressBar.setAdapter(adapter);

        addressBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    addressBar.setText(webView.getUrl());
                    addressBar.selectAll();
                } else {
                    String title = webView.getTitle();
                    if (!TextUtils.isEmpty(title)) {
                        addressBar.setText(title);
                    }
                }
            }
        });


        addressBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Utils.hideKeyboard(MainActivity.this);
                    inputFromAddressBar = addressBar.getText().toString();
                    if (!(inputFromAddressBar.startsWith("http://") || inputFromAddressBar.startsWith("https://"))) {
                        urlFromInput = "http://" + inputFromAddressBar;
                    }
                    boolean isSearched = true;
                    String[] suffix = {".com", ".net", ".sg", ".cn"};
                    for (String s : suffix) {
                        if (inputFromAddressBar.contains(s)) {
                            isSearched = false;
                            break;
                        }
                    }
                    if (isSearched) {
                        urlFromInput = searchPrefix[sharedPref.getInt("search engines", 0)] + inputFromAddressBar;
                    }
                    webView.loadUrl(urlFromInput);
                    addressBar.clearFocus();
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
                pageGoBack();
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageGoForward();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(webView.getUrl());
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(homepageUrl);
            }
        });
    }

    private void initWebView() {
        changeAddressBarHint();

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(homepageUrl);
        webView.getSettings().setSupportMultipleWindows(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");

        changeAddressBarHint();
        homepageUrl = sharedPref.getString("homepage", "www.google.com");

        Intent intent = getIntent();
        String url = null;
        try {
            url = intent.getData().toString();
        } catch (Exception e) {
            url = intent.getStringExtra("url");
        } finally {
            openWebpage(url);
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            changeStatueOfWebToolsButton();
            currentUrl = webView.getUrl();
            addressBar.setText(currentUrl);
            progressBar.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            changeStatueOfWebToolsButton();
            progressBar.hide();

            if (!addressBar.isFocused()) {
                String title = webView.getTitle();
                if (!TextUtils.isEmpty(title)) {
                    addressBar.setText(title);
                }
            }
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setWebProgress(newProgress);
        }
    }

    protected void changeStatueOfWebToolsButton() {

        if (webView.canGoBack()) {
            back.setEnabled(true);
        } else {
            back.setEnabled(false);
        }

        if (webView.canGoForward()) {
            forward.setEnabled(true);
        } else {
            forward.setEnabled(false);
        }

    }

    public boolean pageGoBack() {
        webView.goBack();
        return true;
    }

    public boolean pageGoForward() {
        webView.goForward();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            pageGoBack();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            ConfirmExit();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);

    }

    public void ConfirmExit() {
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("Warning");
        ad.setMessage("Are you sure to exit?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                ActivityCollector.finishAll();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        ad.show();
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
                    default:
                        return false;
                }
            }
        });
    }

    private void showMultiTabMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(0, 1, Menu.NONE, "Tab 1");
        popupMenu.getMenuInflater().inflate(R.menu.multi_tab, popupMenu.getMenu());
        popupMenu.show();
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public void changeAddressBarHint() {
        String searchEngine = searchEngines[sharedPref.getInt("search engines", 0)];
        String addressBarHint = "Search by " + searchEngine + " or input URL";
        addressBar.setHint(addressBarHint);
    }

    public void openWebpage(String url) {
        if (url != null)
            webView.loadUrl(url);
    }
}