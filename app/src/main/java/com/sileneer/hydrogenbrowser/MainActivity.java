package com.sileneer.hydrogenbrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    protected static String homepageUrl;

    MyWebViewClient webViewClient = new MyWebViewClient();

    private static final String[] data = new String[]{
            "www.baidu.com", "baidu.com",
            "www.google.com", "google.com",
            "www.youtube.com", "youtube.com",
            "www.bilibili.com", "bilibili.com",
            "www.zhihu.com", "zhihu.com"
    };

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
        homepageUrl = "https://www.google.com";
        webView.loadUrl(homepageUrl);

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
        public void onPageFinished(WebView view, String url) {
            changeStatueOfWebToolsButton();
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
            System.out.println("***************");
            ConfirmExit();//按了返回键，但已经不能返回，则执行退出确认
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void ConfirmExit() {//退出确认

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
//                    case R.id.about:
//                        AboutActivity.actionStart(MainActivity.this);
//                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}