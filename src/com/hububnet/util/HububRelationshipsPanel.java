package com.hububnet.util;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.docs.Rowset;
import com.hububnet.reg.HububRegDialog;

public class HububRelationshipsPanel extends HububTabPanel implements HububChoiceBox.Listener, HububButtonListener, 
InvokerListener, HububCheckBox.Listener, HububRegDialog.Listener{

	private static HububRelationshipsPanel _instance;
	HububChoiceBox _relationship;
	HububWidgett _header;
	HububNVPair _search;
	HububButton _refresh;
	HububButton _contacts;
	HububServices _services;
	ArrayList<Rel> _rels = new ArrayList<Rel>();
	int _yesCount = 0;
	boolean _isBack = false;
	HububLabel _separator;
	String _choice;
	String _origChoice = null;
	private static int MAXALERTS = 7;

	public class Rel extends HububWidgett implements HububCheckBox.Listener{
		HububImage _image;
		HububLabel _firstName;
		HububLabel _lastName;
		HububCheckBox _rel;
		String _entityID;
		String _relationship;
		boolean _remove;
		private int PADDING = 8;

		public Rel(String relationship, Rowset rowset){
			super();
			this.setBorder(true);
			this.setPadding(PADDING);
			_relationship = relationship;
			_entityID = rowset.getParm("EntityID");
			_remove = rowset.getParm("Rel").equals("Yes");

			_image = new HububImage("profile." +_entityID);
			_image.setImageSize(0, Hubub.getScaledHeight(50));
			_image.resizeEnabled(false);
			_image.reLoadImage();

			HububWidgett names = new HububWidgett();
			HububLabelView firstName = new HububLabelView();
			firstName.setTextColor(Color.BLACK);
			firstName.setText(rowset.getParm("FirstName"));

			HububLabelView lastName = new HububLabelView();
			lastName.setText(rowset.getParm("LastName"));
			lastName.setTextColor(Color.BLACK);

			names.addVertWidget(firstName);
			names.addVertWidget(lastName);

			_rel = new HububCheckBox();
			_rel.setLabel((_remove)?"Remove":"Add");
			_rel.setListener(this);

			this.addHorizWidget(_image);
			this.addHorizWidget(names);
			this.addHorizWidget(_rel);
		}

		public String getEntityID(){
			return _entityID;
		}

		public boolean hasChanged(){
			return _rel.hasChanged();
		}

		public String getRelValue(){
			return _rel.getLabel();
		}

		public void setCheckBoxListener(HububCheckBox.Listener listener){
			_rel.setListener(listener);
		}


		protected void onLayout (boolean changed, int left, int top, int right, int bottom){
			super.onLayout(changed, left, top, right, bottom);
			int l = Hubub._DisplayMetrics.widthPixels -PADDING -_rel.getMeasuredWidth();
			int t = _rel.getTop();
			_rel.layout(l, t, l+_rel.getMeasuredWidth(), t +_rel.getMeasuredHeight());
			l = _image.getLeft();
			t = this.getMeasuredHeight()/2 - _image.getMeasuredHeight()/2;
			_image.layout(l, t, l +_image.getMeasuredWidth(), t + _image.getMeasuredHeight());
		}

		protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(resolveSize(Hubub._DisplayMetrics.widthPixels, widthMeasureSpec),
					resolveSize(this.getMeasuredHeight(), heightMeasureSpec));
		}

		/* HububCheckBox.Listener Protocol*/
		public void onClick(HububCheckBox checkBox) {
			Hubub.Debug("2", "this: " +this +", checkBox: " +checkBox);

		}
	}

	private HububRelationshipsPanel(){
		super();
		try{
			ViewGroup.LayoutParams lp = _hubPanel.getLayoutParams();
			lp.width = Hubub._DisplayMetrics.widthPixels;
			_hubPanel.setLayoutParams(lp);
			_separator = new HububLabel("Not in the Group...");
			_separator.setFontStyle(Typeface.BOLD_ITALIC);

			_header = new HububWidgett();
			_relationship = new HububChoiceBox();
			_relationship.setListener(this);
			_relationship.addItem("I'm Alerting...", "Alert-->");
			_relationship.addItem("Alerting Me...", "-->Alert");
			_relationship.addItem("I Notify...", "-->Notify");
			_relationship.addItem("I Alarm...", "-->Alarm");

			_search = new HububNVPair("Search: 3 or More Letters");
			_search.edit(true);

			_refresh = new HububButton("Refresh", "refresh");
			_refresh.setListener(this);

			_contacts = new HububButton("Contacts", "contacts");
			_contacts.setListener(this);

			HububWidgett topOfHeader = new HububWidgett();
			topOfHeader.addHorizWidget(_relationship);
			topOfHeader.addHorizWidget(_refresh);
			topOfHeader.addHorizWidget(_contacts);
			topOfHeader.setAlignX(Hubub._DisplayMetrics.widthPixels/2);
			_header.addVertWidget(topOfHeader);
			_header.addVertWidget(_search);
			this.addVertWidgetTop(_header, 0);
			_header.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);

			lp = _scrollView.getLayoutParams();
			lp.height = Hubub._DisplayMetrics.heightPixels - _header.getMeasuredHeight() 
			- HububBanner.getInstance().getMeasuredHeight() - Hubub.STATUSBAR_HEIGHT;
			_scrollView.setLayoutParams(lp);

		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}
	}

	public static HububRelationshipsPanel getInstance(){
		if(_instance == null) _instance = new HububRelationshipsPanel();
		return _instance;
	}

	public void onTabSelected(){
		super.onTabSelected();
		//if(_origChoice == null){
		//}
		this._refresh.click();
		//HububAlert.getInstance().alert("Manage your Alert Group here. Enter a search string and hit 'Refresh' to locate other users...");
		if(HububCookies.getCookie("GroupTutorial") == null){
			HububRegDialog dialog = new HububRegDialog();
			dialog.setListener(this);
			dialog.setLButton("Stop Showing", "stop");
			dialog.setRButton("Close", "dismiss");
			dialog.showButtons();
			dialog.show();
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/Manage%20Your%20Alert%20Group%20Here.htm");
		}

		Hubub._InputMethodMgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	/* HububTabPanel Protocol */
	public void releaseInstance() {
		// TODO Auto-generated method stub

	}


	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}
	/*
	 * Utility function to parse relationship choices. Side effect: sets _isBack
	 */
	private String filterChoice(String choice){
		String retcode = null;
		_isBack = choice.startsWith("-");
		if(_isBack){
			retcode = choice.substring(3);
		}
		else{
			int index = choice.indexOf("-->");
			retcode = choice.substring(0, index);
		}
		return retcode;
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "button.tag: " +button.getTag());
		try{
			if(button == _refresh){
				_origChoice = _relationship.getCurrentTag();
				_choice = this.filterChoice(_origChoice);
				Hubub.Debug("2", "_choice: " +_choice);
				HububServices services = new HububServices();
				HububService hubServ = services.addServiceCall("SetGetRelationships");
				if(_isBack) hubServ.setTag("Back");
				hubServ.getInputs();
				hubServ.setParm("Relationship", _choice);
				String search = _search.getValue();
				if(search.length() > 0 && search.length() < 3){
					HububAlert.getInstance().alert("Search string must be three or more letters...");
					return;
				}
				if(search.length() > 0){
					hubServ.setParm("Search", search);
				}
				Rowset rowset = hubServ.addRowset("Relationships");
				for(int i=0; i<_rels.size(); i++){
					if(_rels.get(i).hasChanged()){
						rowset.addRow();
						rowset.setParm("EntityID", _rels.get(i).getEntityID());
						rowset.setParm("Action", _rels.get(i).getRelValue());
					}
				}
				Hubub.Debug("2", "services: " +services);
				Invoker invoker = new Invoker();
				invoker.send(services, this);
				HububWorking.getInstance().working();
			}
			else if(button == _contacts){
				//DroidHubub.getInstance().displayTab(HububContactPicker.getInstance());
				String[] emailList = {""};
				String subject = "Like to Add You to My IMOK Alert Group...";
				String message = HububCookies.getCookie("EmailPromo") +
				HububCookies.getCookie("FirstName") +" " +HububCookies.getCookie("LastName");

				HububSendMail.getInstance().send(emailList, subject, message);
				//Thread.sleep(3000);
				//HububAlert.getInstance().alert("Can't find the person you're looking for on IMOK? Just select those you would like to invite from your contacts..." );

			}
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}

	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		Hubub.Debug("2", "services: " +services);
		try{
			_services = services;
			_services.getServices();
			HububService hubServ = _services.nextService();
			hubServ.getOutputs();
			Rowset rowset = hubServ.getRowset("Relationships");
			_rels.clear();
			_hubPanel.removeAllViews();
			_relationship.edit();
			//_search.edit(true);
			_relationship.setSelectionByTag(_origChoice);
			_yesCount = 0;
			boolean firstNo = false;
			while(rowset.nextRow()){
				Rel newRel = new Rel(_origChoice, rowset);
				newRel.setCheckBoxListener(this);
				_rels.add(newRel);
				String rel = rowset.getParm("Rel");
				if(rel.equals("Yes")){
					_yesCount++;
				}else{
					if(!firstNo){
						firstNo = true;
						_hubPanel.addVertWidget(_separator);
						_separator.setText("Not in my " +_choice +" Group...");
					}
				}
				_hubPanel.addVertWidget(newRel);
			}
			HububCookies.setCookie("NumAlerts", "" +_yesCount);
			HububWorking.getInstance().doneWorking();
			if(_separator.getParent() == null && _search.getValue().length() > 0){
				Hubub.Debug("2", "Search produced no results...");
				//HububAlert.getInstance().alert("Your search produced no results. Taking you to the Contacts screen where you can selectively notify people of your interest to connect...");
				//DroidHubub.getInstance().displayTab(HububContactPicker.getInstance());
				_contacts.click();
			}
			_search.reset();
			_search.setVisible(!_isBack);
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}

	}

	/* HububChoiceBox.Listener Protocol */
	public void onItemSelected(int itemIndex, HububChoiceBox choiceBox) {
		Hubub.Debug("2", "itemIndex: " +itemIndex +", choiceBox: " +choiceBox);
		_refresh.click();
	}

	/* HububCheckBox.Listener Protocol */
	public void onClick(HububCheckBox checkBox) {
		Hubub.Debug("2", "checkBox: " +checkBox +", _yesCount: " +_yesCount);
		if(checkBox.getLabel().equals("Add")){
			if(checkBox.isChecked()) _yesCount++;
			else _yesCount--;
		}
		else{	// Remove
			if(checkBox.isChecked()) _yesCount--;
			else _yesCount++;
		}
		if(_yesCount > MAXALERTS){
			checkBox.toggle();
			_yesCount--;
			HububAlert.getInstance().alert("You are limited to no more than " +MAXALERTS +" people in your alert group...");
		}		
	}

	/* HububRegDialog.Listener Protocol */
	public void dialogAction(HububRegDialog regDialog, Object selectedObject) {
		Hubub.Debug("2", "RelationshipViewController: dialogAction...");
		HububButton button = (HububButton)selectedObject;
		if(button.getTag().equals("stop")){
			HububCookies.setCookie("GroupTutorial","stop");
		}
		regDialog.dismiss();
	}

}
