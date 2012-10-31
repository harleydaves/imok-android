package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.docs.HububCookies;

import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.ViewGroup;

public class HububContactPicker extends HububTabPanel implements HububNVPair.Listener, HububButtonListener, Runnable{
	private static HububContactPicker _instance;
	private static String[] EmailTypes = {"?", "H", "W", "O", "M"};
	private Cursor _cursor;
	private HububNVPair _search;
	private HububButton _dismissBtn;
	private HububButton _doneBtn;

	/* Begin Inner Classes*/
	public class PickItem extends HububWidgett{
		HububLabel _name;
		HububLabel _email;
		HububCheckBox _checkBox;
		HububWidgett _nameAddr;


		public PickItem(String name, String email, String emailType){
			_name = new HububLabel(name, false);
			_name.setAlignX(1);
			_name.setFontStyle(Typeface.BOLD);

			//_email = new HububLabel(emailType +": " +email, false);
			_email = new HububLabel(email, false);
			_email.setAlignX(1);

			_checkBox = new HububCheckBox();
			_checkBox.setLabel("Notify");

			_nameAddr = new HububWidgett();
			_nameAddr.addVertWidget(_name);
			_nameAddr.addVertWidget(_email);

			this.addHorizWidget(_nameAddr);
			this.addHorizWidget(_checkBox);
		}

		public boolean isChecked(){
			return _checkBox.isChecked();
		}

		public String getEmailAddr(){
			//String label = _email.getLabel();
			//return label.substring(label.indexOf(": ") +2, label.length());
			return _email.getLabel();
		}

		public void showOnMatch(String match){
			Hubub.Debug("2", "_name.getLabel: " +_name.getLabel());
			if(match.length() == 0 || _name.getLabel().toLowerCase().contains(match.toLowerCase()) || 
					_email.getLabel().toLowerCase().contains(match.toLowerCase())){
				this.setVisibility(View.VISIBLE);
			}
			else
				this.setVisibility(View.GONE);
		}

		protected void onLayout (boolean changed, int left, int top, int right, int bottom){
			super.onLayout(changed, left, top, right, bottom);
			int l = Hubub._DisplayMetrics.widthPixels -_checkBox.getMeasuredWidth();
			int t = _checkBox.getTop();
			_checkBox.layout(l, t, l+_checkBox.getMeasuredWidth(), t +_checkBox.getMeasuredHeight());
		}

		protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(resolveSize(Hubub._DisplayMetrics.widthPixels, widthMeasureSpec),
					resolveSize(this.getMeasuredHeight(), heightMeasureSpec));
		}
	}

	//public class SubsetContacts implements Runnable{
	//	private String _subset;

	//	public SubsetContacts(String subset){
	//		_subset = subset;
	//	}

	//	public void run() {
	//		try{
	//			int childCount = _hubPanel.getChildCount();
	//			for(int i=0; i<childCount; i++){
	//				PickItem pItem = (PickItem)_hubPanel.getChildAt(i);
	//				pItem.showOnMatch(_subset);
	//				Runnable runnable = new Runnable(){
	////					public void run(){
	//						finishSubset();
	//					}
	//				};
	//				DroidHubub.getInstance().runOnUiThread(runnable);
	//			}
	//		}catch(Exception e){
	//			Hubub.Debug("2", Hubub.getStackTrace(e));
	//		}

	//	}
	//}

	/* End Inner Classes */
	private HububContactPicker(){
		super();
		try{
			ViewGroup.LayoutParams lp = _hubPanel.getLayoutParams();
			lp.width = Hubub._DisplayMetrics.widthPixels;
			_hubPanel.setLayoutParams(lp);

			_search = new HububNVPair("Search");
			_search.setListener(this);
			_search.setDropKeyboardOnUnFocus(true);
			_search.edit(true);

			_dismissBtn = new HububButton("Dismiss", "dismiss");
			_dismissBtn.setListener(this);
			_doneBtn = new HububButton("Email", "done");
			_doneBtn.setListener(this);

			HububWidgett header = new HububWidgett();
			header.addHorizWidget(_search);
			header.addHorizWidget(_dismissBtn);
			header.addHorizWidget(_doneBtn);
			this.addVertWidgetTop(header, 0);
			header.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);

			lp = _scrollView.getLayoutParams();
			lp.height = Hubub._DisplayMetrics.heightPixels - header.getMeasuredHeight() 
			- HububBanner.getInstance().getMeasuredHeight() - Hubub.STATUSBAR_HEIGHT;
			_scrollView.setLayoutParams(lp);



		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}
	}

	public static HububContactPicker getInstance(){
		if(_instance == null) _instance = new HububContactPicker();
		return _instance;
	}

	public void onTabSelected(){
		super.onTabSelected();
		Hubub.Debug("2", "...");
		this.getContacts();
		HububAlert.getInstance().alert("Notify Contacts via email who you would like to add to your Alert Group...");
	}

	public void getContacts(){
		HububWorking.getInstance().working();
		new Thread(this).start();	// do the query in separate thread in case DB is busy...
	}

	public void displayContacts(Cursor c){
		Hubub.Debug("2", "cursor count: " +c.getCount());
		while(c.moveToNext()){
			//Hubub.Debug("2", " col0: " +c.getString(0) +", col1: " +c.getString(1) +", col2: " +c.getString(2) +", col3: " +c.getString(3));
			_hubPanel.addVertWidget(new PickItem(c.getString(1), c.getString(2), EmailTypes[c.getInt(3)]));
		}
		HububWorking.getInstance().doneWorking();

	}

	public void run(){
		try{
			//String[] dummy = new String[]{Data._ID, Data.DISPLAY_NAME, Email.NUMBER, Phone.TYPE};
			final Cursor c = DroidHubub.getInstance().getContentResolver().query(Data.CONTENT_URI,
					new String[] {Data._ID, Data.DISPLAY_NAME, Phone.NUMBER, Phone.TYPE},
					//Data.CONTACT_ID + "=?" + " AND " +
					Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
					//new String[] {String.valueOf(contactId)}, null);
					null, Data.DISPLAY_NAME);


			Hubub.Debug("2", "cursor count: " +c.getCount());
			Runnable runnable = new Runnable(){
				public void run(){
					displayContacts(c);
				}
			};
			DroidHubub.getInstance().runOnUiThread(runnable);
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}

	}

	/* HububTabPanel Protocol */
	public void releaseInstance() {
		// TODO Auto-generated method stub

	}

	/* HububEditText.NextCharListener Protocol */
	public void nextCharEntered(String text) {
		Hubub.Debug("2", "text: " +text);

	}

	public void finishSubset(){
		HububWorking.getInstance().doneWorking();
	}

	/* HububNVPair.Listener Protocol */
	public void onUnFocus(HububNVPair nvPair) {
		Hubub.Debug("2", "nvPair.getValue: " +nvPair.getValue());
		//HububWorking.getInstance().working();
		//Runnable runnable = new SubsetContacts(nvPair.getValue());
		//new Thread(runnable).start();	// do the subsetting in separate thread in case DB is busy...

		int childCount = _hubPanel.getChildCount();
		for(int i=0; i<childCount; i++){
			PickItem pItem = (PickItem)_hubPanel.getChildAt(i);
			pItem.showOnMatch(nvPair.getValue());
			this.invalidate();
		}
		//HububWorking.getInstance().doneWorking();
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "button tag: " +button.getTag());
		int itemsChecked = 0;
		if(button == _dismissBtn){
			DroidHubub.getInstance().displayTab(HububRelationshipsPanel.getInstance());
			_hubPanel.removeAllViews();
		}
		else if(button == _doneBtn){
			int childCount = _hubPanel.getChildCount();
			for(int i=0; i<childCount; i++){
				PickItem pItem = (PickItem)_hubPanel.getChildAt(i);
				if(pItem.isChecked()){
					itemsChecked++;
				}
			}
			if(itemsChecked == 0){
				HububAlert.getInstance().alert("No contacts selected...");
				return;
			}
			String[] emailList = new String[itemsChecked];
			itemsChecked = 0;
			for(int i=0; i<childCount; i++){
				PickItem pItem = (PickItem)_hubPanel.getChildAt(i);
				if(pItem.isChecked()){
					emailList[itemsChecked++] = pItem.getEmailAddr();
				}
			}
			for(int i=0; i<itemsChecked; i++){
				Hubub.Debug("2", "item: " +i +", email: " +emailList[i]);
			}

			String subject = "Like to Add You to My IMOK Alert Group...";
			String message = HububCookies.getCookie("EmailPromo") +
			HububCookies.getCookie("FirstName") +" " +HububCookies.getCookie("LastName");

			HububSendMail.getInstance().send(emailList, subject, message);
			DroidHubub.getInstance().displayTab(HububRelationshipsPanel.getInstance());
			_hubPanel.removeAllViews();

		}

	}
}
