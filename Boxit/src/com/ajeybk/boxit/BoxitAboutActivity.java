package com.ajeybk.boxit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class BoxitAboutActivity extends Activity {

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);              
    setContentView(R.layout.aboutview);
    Button okBtn = (Button)findViewById(R.id.okbutton);
    OnClickListener okListener = new OnClickListener() {
        public void onClick(View v) {
        	finish();
        }
    };
    okBtn.setOnClickListener(okListener);
    WebView webView = (WebView) findViewById(R.id.web_view);
    webView.loadUrl("file:///android_asset/help.html");
  }
}