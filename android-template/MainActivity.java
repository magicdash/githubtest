package com.zumilo.finanzas;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    WebView mainWebView = getBridge().getWebView();
    mainWebView.getSettings().setJavaScriptEnabled(true);
    mainWebView.getSettings().setDomStorageEnabled(true);
    mainWebView.getSettings().setSupportMultipleWindows(true);
    mainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

    mainWebView.setWebChromeClient(new BridgeWebChromeClient(getBridge()) {
      @Override
      public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        final WebView popup = new WebView(MainActivity.this);
        popup.setBackgroundColor(Color.WHITE);
        popup.getSettings().setJavaScriptEnabled(true);
        popup.getSettings().setDomStorageEnabled(true);
        popup.getSettings().setSupportMultipleWindows(true);
        popup.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        popup.setWebViewClient(new WebViewClient() {
          @Override
          public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
            return false;
          }
        });

        popup.setWebChromeClient(new WebChromeClient() {
          @Override
          public void onCloseWindow(WebView window) {
            ViewGroup parent = (ViewGroup) window.getParent();
            if (parent != null) parent.removeView(window);
            window.destroy();
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

      @Override
      public void onCloseWindow(WebView window) {
        if (window != mainWebView) {
          ViewGroup parent = (ViewGroup) window.getParent();
          if (parent != null) parent.removeView(window);
          window.destroy();
        }
      }
    });
  }
}
