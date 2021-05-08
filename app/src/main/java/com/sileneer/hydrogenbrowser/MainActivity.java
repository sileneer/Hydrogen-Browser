package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import me.jingbin.progress.WebProgress;

public class MainActivity extends BaseActivity {

    protected static AutoCompleteTextView addressBar;
    protected static WebView webView;
    private ImageView back;
    private ImageView forward;
    private ImageView refresh;
    private ImageView home;
    private ImageView menuImage;
    protected String inputFromAddressBar;
    protected String urlFromInput;
    protected String currentUrl;

    private WebProgress progressBar;

    protected static int searchEnginesIndex;
    protected static SharedPreferences sharedPref_searchEngines;
    protected static SharedPreferences.Editor editor_searchEngines;

    protected static String homepageUrl;
    protected static SharedPreferences sharedPref_homepage;
    protected static SharedPreferences.Editor editor_homepage;

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

    protected static final String[] searchEngines = {"Google", "Baidu", "Bing", "DuckDuckGo"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref_searchEngines = MainActivity.this.getSharedPreferences(
                "search engines", Context.MODE_PRIVATE);
        editor_searchEngines = sharedPref_searchEngines.edit();
        searchEnginesIndex = sharedPref_searchEngines.getInt("search engines", 0);

        sharedPref_homepage = MainActivity.this.getSharedPreferences(
                "homepage", Context.MODE_PRIVATE);
        editor_homepage = sharedPref_homepage.edit();
        homepageUrl = sharedPref_homepage.getString("homepage", "www.google.com");

        webView = findViewById(R.id.show);
        addressBar = findViewById(R.id.url);
        back = findViewById(R.id.back);
        forward = findViewById(R.id.forward);
        refresh = findViewById(R.id.refresh);
        home = findViewById(R.id.home);
        menuImage = findViewById(R.id.menu);
        progressBar = findViewById(R.id.progress_bar);

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(menuImage);
            }
        });

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(homepageUrl);

        changeAddressBarHint(searchEnginesIndex);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.
                this, android.R.layout.simple_dropdown_item_1line, data);
        addressBar.setAdapter(adapter);
        addressBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard(MainActivity.this);
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
                        urlFromInput = searchPrefix[searchEnginesIndex] + inputFromAddressBar;
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
            changeStatueOfWebToolsButton();
            currentUrl = webView.getUrl();
            addressBar.setText(currentUrl);
            progressBar.hide();
            super.onPageFinished(view, url);
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
                MainActivity.this.finish();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        ad.show();
    }

    protected static void hideKeyboard(Activity context) {
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
                    default:
                        return false;
                }
            }
        });
    }

    protected static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    protected static void changeAddressBarHint(int searchEnginesIndex){
        String searchEngine = searchEngines[searchEnginesIndex];
        String addressBarHint = "Search by "+searchEngine+" or input URL here";
        addressBar.setHint(addressBarHint);
    }

    protected static void loadOpenSource() {
        webView.loadUrl("https://github.com/lzh7522/Hydrogen-Browser");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}