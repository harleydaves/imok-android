package com.hububnet.reg;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.android.FbDialog;
import com.facebook.android.R;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.util.*;

public class HububRegDialog extends HububDialog implements HububButtonListener{
	static final int FB_BLUE = 0xFF6D84B4;
	static final int MARGIN = 4;
	static final int PADDING = 2;
	static final float[] DIMENSIONS_DIFF_LANDSCAPE = {20, 60};
	static final float[] DIMENSIONS_DIFF_PORTRAIT = {40, 60};

	private LinearLayout _content;
	private LinearLayout _innerContent;
	private TextView _title;
	private String _tag;	// used to distinguish multiple instances of dialogs...
	private TextView _topText;
	private HububWidgett _buttons;
	private HububButton _lButton;	// left Button
	private HububButton _rButton;	// right Button
	private HububButton _buttonPressed;
	private ScrollView _scrollView;
	private WebView _webView;
	private HububRegDialog.Listener _listener;
    private ProgressDialog _spinner;
    private HububWidgett _widgets;
    private String _loadURL;
	
	public interface Listener{
		public void dialogAction(HububRegDialog regDialog, Object selectedObject);
	}

	public HububRegDialog() {
		super(DroidHubub.getInstance());
		_buttons = new HububWidgett();
		_lButton = new HububButton("Yes","yes");
		_lButton.setListener(this);
		_buttons.addHorizWidget(_lButton);
		_rButton = new HububButton("No", "no");
		_rButton.setListener(this);
		_buttons.addHorizWidget(_rButton);
		_buttons.setVisibility(View.INVISIBLE);
		
		_scrollView = new ScrollView(DroidHubub.getInstance());
		_scrollView.setScrollbarFadingEnabled(false);
		_scrollView.setFocusableInTouchMode(true);
		
        _webView = new WebView(getContext());
        _webView.setVerticalScrollBarEnabled(false);
        _webView.setHorizontalScrollBarEnabled(false);
        _webView.setWebViewClient(new HububRegDialog.MyWebViewClient());
        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        
        _innerContent = new LinearLayout(this.getContext());
		_innerContent.setOrientation(LinearLayout.VERTICAL);
		_innerContent.addView(_webView);
		
        _scrollView.addView(_innerContent);
        
        _spinner = new ProgressDialog(getContext());
        _spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        _spinner.setMessage("Loading...");

		_topText = new TextView(DroidHubub.getInstance());
		_topText.setTextColor(Color.BLACK);
		_topText.setTextSize(18);
		_topText.setPadding(MARGIN + PADDING, MARGIN, MARGIN, 0);
		_topText.setVisibility(View.GONE);
		
		_widgets = new HububWidgett();
		_widgets.setVisibility(View.GONE);


	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mSpinner = new ProgressDialog(getContext());
		//mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//mSpinner.setMessage("Loading...");

		_content = new LinearLayout(getContext()){
			protected void onLayout (boolean changed, int left, int top, int right, int bottom){
				ViewGroup.LayoutParams lp = _scrollView.getLayoutParams();
				lp.height = _content.getMeasuredHeight() - _title.getMeasuredHeight() - 
					_topText.getMeasuredHeight() - _buttons.getMeasuredHeight();
				if(_widgets.getMeasuredHeight() > 0) lp.height = 0;
				_scrollView.setLayoutParams(lp);
				super.onLayout(changed, left, top, right, bottom);
				if(_lButton.getTag().length() == 0) _lButton.setVisibility(View.GONE);
				if(_rButton.getTag().length() == 0) _rButton.setVisibility(View.GONE);
				_buttons.measure(this.getMeasuredWidth(), this.getMeasuredHeight());
				//Hubub.Debug("2", "onLayout override... this.getMeasuredWidth: " +this.getMeasuredWidth() +", _buttons.getMeasuredWidth: " +_buttons.getMeasuredWidth());
				
				int childLeft = this.getMeasuredWidth()/2 - _buttons.getMeasuredWidth()/2;
				int childTop = this.getMeasuredHeight() - _buttons.getMeasuredHeight();
				_buttons.layout(childLeft, childTop, childLeft + _buttons.getMeasuredWidth(), childTop + _buttons.getMeasuredHeight());
			}
		};
		_content.setOrientation(LinearLayout.VERTICAL);
		_content.setBackgroundColor(FB_BLUE);
		setUpTitle();
		//setUpWebView();
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		final float scale =
			getContext().getResources().getDisplayMetrics().density;
		int orientation =
			getContext().getResources().getConfiguration().orientation;
		float[] dimensions =
			(orientation == Configuration.ORIENTATION_LANDSCAPE)
			? DIMENSIONS_DIFF_LANDSCAPE : DIMENSIONS_DIFF_PORTRAIT;
		addContentView(_content, new LinearLayout.LayoutParams(
				display.getWidth() - ((int) (dimensions[0] * scale + 0.5f)),
				display.getHeight() - ((int) (dimensions[1] * scale + 0.5f))));


		_content.addView(_topText);
		_content.addView(_scrollView);
		_content.addView(_widgets);
		_content.addView(_buttons);

	}

	private void setUpTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Drawable icon = getContext().getResources().getDrawable(
				R.drawable.ruokicon);
		icon.setBounds(0, 0, 30, 30);
		_title = new TextView(getContext());
		_title.setText("IMOK");
		_title.setTextColor(Color.WHITE);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setBackgroundColor(FB_BLUE);
		_title.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
		_title.setCompoundDrawablePadding(MARGIN + PADDING);
		_title.setCompoundDrawables(
				icon, null, null, null);
		_content.addView(_title);
	}
	
	public LinearLayout getInnerContent(){
		_buttons.setVisibility(View.VISIBLE);
		return _innerContent;
	}
	
	public HububWidgett getWidgets(){
		_widgets.setVisibility(View.VISIBLE);
		_buttons.setVisibility(View.VISIBLE);
		return _widgets;
	}

	public void setTag(String tag){
		_tag = tag;
	}

	public String getTag(){
		return _tag;
	}
	
	public void setTopText(String text){
		_topText.setText(Html.fromHtml(text));
		_topText.setVisibility(View.VISIBLE);
	}
	
	public void setListener(HububRegDialog.Listener listener){
		_listener = listener;
	}
	
	public void setLButton(String name, String tag){
		_lButton.setNameTag(name, tag);
	}

	public void setRButton(String name, String tag){
		_rButton.setNameTag(name, tag);
	}
	
	public void showButtons(){
		_buttons.setVisibility(View.VISIBLE);
	}
	
	public void loadURL(String url){
		_loadURL = url;
		_webView.loadUrl(url);
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "button.tag: " +button.getTag());
		_buttonPressed = button;
		if(_listener != null) _listener.dialogAction(this, button);
		else{
			if(button.getTag().equals("dismiss")) this.dismiss();
		}
		
	}
	
	// Inner Classes
	private class MyWebViewClient extends WebViewClient{
	       @Override
	        public void onPageStarted(WebView view, String url, Bitmap favicon) {
	            Hubub.Debug("2", "Webview loading URL: " + url);
	            super.onPageStarted(view, url, favicon);
	            _buttons.setVisibility(View.INVISIBLE);
	            _spinner.show();
	        }

	        @Override
	        public void onPageFinished(WebView view, String url) {
	            super.onPageFinished(view, url);
	            _spinner.dismiss();
	            _buttons.setVisibility(View.VISIBLE);
	        }
	        public void onReceivedError (WebView view, int errorCode, String description, String failingUrl){
	        	Hubub.Debug("2", "...");
	        	view.loadUrl(_loadURL);
	        }
	}
	


}
