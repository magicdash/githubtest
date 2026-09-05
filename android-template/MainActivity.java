package com.zumilo.finanzas;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;

public class MainActivity extends BridgeActivity {
  private WebView mainWebView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mainWebView = getBridge().getWebView();
    configureWebView(mainWebView);
    mainWebView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        markNativeApp(view);
      }
    });

    mainWebView.setWebChromeClient(new BridgeWebChromeClient(getBridge()) {
      @Override
      public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return createPopup(view, resultMsg);
      }

      @Override
      public void onCloseWindow(WebView window) {
        closePopup(window);
      }
    });
  }

  private void configureWebView(WebView webView) {
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setSupportMultipleWindows(true);
    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setAcceptCookie(true);
    cookieManager.setAcceptThirdPartyCookies(webView, true);
  }

  private void markNativeApp(WebView webView) {
    webView.evaluateJavascript("(function(){window.__ZUMILO_NATIVE_ANDROID__=true;document.documentElement.setAttribute('data-zumilo-native','android');var s=document.getElementById('zumilo-native-hide-install');if(!s){s=document.createElement('style');s.id='zumilo-native-hide-install';s.textContent='.installPair,.installGuide,.androidInstall,.iosInstall{display:none!important}';document.head.appendChild(s);}var buttons=document.querySelectorAll('button');buttons.forEach(function(b){var t=(b.innerText||b.textContent||'').trim().toLowerCase();if(t.indexOf('instalar app')>=0||t.indexOf('instalar en android')>=0||t.indexOf('instalar en iphone')>=0){b.style.setProperty('display','none','important');}});})();", null);
  }

  private boolean createPopup(WebView opener, Message resultMsg) {
    final WebView popup = new WebView(MainActivity.this);
    popup.setBackgroundColor(Color.WHITE);
    configureWebView(popup);

    popup.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
      }
    });

    popup.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message childResultMsg) {
        return createPopup(view, childResultMsg);
      }

      @Override
      public void onCloseWindow(WebView window) {
        closePopup(window);
      }
    });

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.MATCH_PARENT,
      FrameLayout.LayoutParams.MATCH_PARENT
    );
    addContentView(popup, params);

    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
    transport.setWebView(popup);
    resultMsg.sendToTarget();
    return true;
  }

  private void closePopup(WebView window) {
    if (window == null || window == mainWebView) return;
    ViewGroup parent = (ViewGroup) window.getParent();
    if (parent != null) parent.removeView(window);
    window.stopLoading();
    window.destroy();
  }
}
