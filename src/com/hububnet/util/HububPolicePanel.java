package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.docs.HububService;
import com.hububnet.docs.Rowset;

import com.hububnet.ruok.HububEmergencyPanel;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class HububPolicePanel extends HububWidgett implements HububButtonListener{
	TextView _policeView;
	TextView _fireView;
	HububButton _dismiss;
	Header _policeHeader;
	Header _fireHeader;
	//TextView _textView;
	ScrollView _scrollView;
	HububWidgett _container;

	private static HububPolicePanel _instance;

	public class Header extends HububWidgett implements HububButtonListener{	// Header with call button and Police or Fire...
		HububButton _call;
		TextView _label;
		String _phoneNumber;

		public Header(String title){
			super();
			_label = new TextView(DroidHubub.getInstance());
			_label.setText(title +":");
			_label.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
			_label.setTextColor(Color.BLACK);
			
			_call = new HububButton("Call", "call");
			_call.setFontStyleSize(Typeface.BOLD, 12);
			_call.setListener(this);
			this.addHorizWidget(_call);
			this.addHorizWidget(_label);
			_call.setVisible(false);
		}

		public void setPhoneNumber(String phoneNumber){
			_phoneNumber = phoneNumber;
			_call.setVisible(true);
		}

		public void reset(){
			_phoneNumber = null;
			_call.setVisible(false);
		}

		/* HububButtonListener Protocol */
		public void buttonPressed(HububButton button) {
			if(_phoneNumber != null){
				String dialNumber = _phoneNumber;
				if(!Hubub.HUBUBSIMULATOR && Hubub.HUBUBDEBUG)
					dialNumber = "1770-335-4975";
				HububPhone.getInstance().initiateCall(dialNumber);
			}		
		}
	}

	private HububPolicePanel(HububEmergencyPanel ep){
		super();
		setBackgroundColor(Color.LTGRAY);
		_container = new HububWidgett();
		
		_dismiss = new HububButton("Dismiss", "dismiss");
		_dismiss.setListener(this);

		_policeView = new TextView(DroidHubub.getInstance());
		_policeView.setBackgroundColor(Color.LTGRAY);
		_policeView.setTextColor(Color.BLACK);

		_fireView = new TextView(DroidHubub.getInstance());
		_fireView.setBackgroundColor(Color.LTGRAY);
		_fireView.setTextColor(Color.BLACK);
		
		_policeHeader = new Header("Police");
		_policeHeader.setAlignX(Hubub._DisplayMetrics.widthPixels/2);
		_fireHeader = new Header("Fire");
		_fireHeader.setAlignX(Hubub._DisplayMetrics.widthPixels/2);

		//ep = HububEmergencyPanel.getInstance();
		ep.getTopButtons().measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		ep.getChatSession().measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);

		_scrollView = new ScrollView(DroidHubub.getInstance());
		_scrollView.setScrollbarFadingEnabled(false);
		_scrollView.setFocusableInTouchMode(true);
		_scrollView.addView(_container);

		this.addVertWidget(_dismiss);
		_container.addVertWidget(_policeHeader);
		_container.addVertWidget(_policeView);
		_container.addVertWidget(_fireHeader);
		_container.addVertWidget(_fireView);

		this.addView(_scrollView);
		ViewGroup.LayoutParams lp = _container.getLayoutParams();
		lp.width = Hubub._DisplayMetrics.widthPixels;
		_container.setLayoutParams(lp);
		
		lp = _scrollView.getLayoutParams();
		lp.height = ep.getChatSession().getMeasuredHeight();
		_scrollView.setLayoutParams(lp);
	}

	public static HububPolicePanel getInstance(HububEmergencyPanel ep){
		if(_instance == null)
			_instance = new HububPolicePanel(ep);
		return _instance;
	}

	public void show(){
		this.setVisible(true);
	}

	public void hide(){
		this.setVisibility(View.GONE);
	}

	public void setService(HububService service){
		Hubub.Logger("HububPolicePanel: setService...");
		String phoneNum;
		
		try{
			_policeHeader.reset();
			_fireHeader.reset();
			String policeText = "";
			String fireText = "";
			service.getOutputs();
			Rowset rowset = service.getRowset("Police");
			if(rowset.size() == 0){
				policeText += "None available within 10 mile radius...\n";
			}
			else{
				rowset.nextRow();
				phoneNum = rowset.getParm("Phone");
				//phoneNum = "1770-335-4975";
				_policeHeader.setPhoneNumber(phoneNum);
				policeText += "" +rowset.getParm("Name") +"\n";
				policeText += "" +rowset.getParm("Addr") +"\n";
				policeText += "" +phoneNum +"\n";
				policeText += "" +rowset.getParm("Dist") +"\n";
				policeText += "";
			}
			_policeView.setText(policeText);

			rowset = service.getRowset("Fire");
			if(rowset.size() == 0){
				fireText += "None available within 10 mile radius...\n";
			}
			else{
				rowset.nextRow();
				phoneNum = rowset.getParm("Phone");
				//phoneNum = "1770-335-4975";
				_fireHeader.setPhoneNumber(phoneNum);
				fireText += "" +rowset.getParm("Name") +"\n";
				fireText += "" +rowset.getParm("Addr") +"\n";
				fireText += "" +phoneNum +"\n";
				fireText += "" +rowset.getParm("Dist") +"\n";		
				fireText += "";
			}
			_fireView.setText(fireText);

			this.show();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		if(button == _dismiss){
			this.hide();
		}
	}
}
