package com.hububnet.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.hububnet.ruok.AlertSelector;

public class HububNotificationPanel extends HububTabPanel{
	private static HububNotificationPanel _instance;
	static int MAXNOTES = 100;
	HububNotificationNote[] _notes = new HububNotificationNote[MAXNOTES];
	int _notesIndex = 0;
	boolean _processingHistory = false;
	//HububWidgett _vertWidget;
	HububNotificationNote _mostRecentNote = null;
	AlertSelector _alertSelector = null;
	boolean _foundOwnAlert = false;
	/*
	public class Note extends HububWidgett implements HububButtonListener{
		HububServices _services;
		HububImage _pic;
		HububLabel _name;
		HububLabel _time;
		HububButton _delete;
		HububButton _act;
		HububButton _devEnd; 	// for Development testing
		HububLabel _shortDesc;
		HububWidgett _header;
		HububWidgett _buttons;
		HububWidgett _eventListener;
		HububNotificationPanel _notificationPanel = null;
		Note _masterNote = null;
		boolean _myEmergency = false;
		String _monitorButtonColor;
		
		public Note(HububServices services, HububNotificationPanel notificationPanel){
			this.setPadding(10);
			this.setBorder(true);
			//this.setGaps(20, 0);
			services.reset();
			_services = services;
			_notificationPanel = notificationPanel;
			String deviceID = services.getAttrib("DeviceID");
			Hubub.Logger("HububNotificationPanel.Note: Constructor: services: " +services +", deviceID: " +deviceID);
			_header = new HububWidgett();
			_buttons = new HububWidgett();
			
			_pic = new HububImage("profile." +services.getEntityID());
			_pic.setImageSize(0, 75);
			_pic.resizeEnabled(false);
			
			_shortDesc = new HububLabel(services.getAttrib("ShortDesc") +" (" 
					+services.getAttrib("Status") +")", false);
			_shortDesc.setFontStyle(Typeface.BOLD);
			_shortDesc.setFontSize(15);
			
			String fDate = null;
			String stime = services.getAttrib("Time");
			long time = Long.parseLong(stime);
			SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss aa");
			Date date = new Date(time);
			fDate = dtf.format(date);
			Hubub.Logger("HububNotificationPanel.Note: Constructor: fDate: " +fDate);
			_time = new HububLabel(fDate, false);
			
			services.getServices();
			HububService hubServ = services.nextService();
			String servName = hubServ.getName();
			hubServ.getInputs();
			String fname = hubServ.getParm("FirstName");
			String lname = hubServ.getParm("LastName");
			_name = new HububLabel(fname +" " +lname, false);
			
			_header.addVertWidget(_time);
			_header.addVertWidget(_name);
			_header.addVertWidget(_shortDesc);
			_delete = new HububButton("Delete", "delete");
			_delete.setFontSize(14);
			_delete.setListener(this);
			
			_act = new HububButton("Monitor", "act");
			_act.setFontSize(14);
			_act.setListener(this);
			_myEmergency = (_services.getAttrib("EntityID").equals(HububCookies.getCookie("EntityID")));
			_buttons.addHorizWidget(_delete);
			if(_myEmergency){
				if(deviceID.startsWith("Browser")){
					_delete.setNameTag("DevEnd", "devend");
				}
				else _delete.setNameTag("Leave", "leave");
			}
			_buttons.addHorizWidget(_act);
			//_buttons.setAlignX(_buttons.getWidth()/2);
			_header.addVertWidget(_buttons);
			
			this.addHorizWidget(_pic);
			this.addHorizWidget(_header);
		}
		
		public void loadImage(){
			_pic.reLoadImage();
		}
				
		public boolean matchesDeleteTimer(String timer){
			_services.reset();
			return(_services.getAttrib("Time").equals(timer));
		}
		
		public HububButton getAct(){
			return _act;
		}
		
		public HububButton getDelete(){
			return _delete;
		}
		
		*/

		/* HububButtonListener Protocol */
	/*
		public void buttonPressed(HububButton button) {
			_services.reset();
			if(button == _delete){
				Hubub.getInstance().deleteFromStream(_services.getAttrib("Time"), _services.getAttrib("StreamName"));
			}
			else if(button == _act){
				if(_masterNote != null){
					_masterNote.buttonPressed(_masterNote.getAct());
				}
				else{
					Hubub.Logger("Hubub: buttonPressed: _act: _masterNote: " +_masterNote +", willNotDraw(): " +this.willNotDraw());
					Hubub.getInstance().processEmergency(_services).setNoteListener(this);
					
				}

			}
		}
		
		public void updateStatus(){
			_shortDesc.setText(_services.getAttrib("ShortDesc") +" (" +_services.getAttrib("Status") +")");
		}

	}
	*/
	
	/*
	 * End of Note Class Definition...
	 */
	
	private HububNotificationPanel(){
		super();
		_alertSelector = AlertSelector.getInstance();
		ViewGroup.LayoutParams lp = _hubPanel.getLayoutParams();
		lp.width = Hubub._DisplayMetrics.widthPixels;
		_hubPanel.setLayoutParams(lp);
	}
	
	
	public static HububNotificationPanel getInstance(){
		if(_instance == null) _instance = new HububNotificationPanel();
		return _instance;
	}
	
	public void reset(){
		_processingHistory = false;
		_notes[0] = null;
		_mostRecentNote = null;
		_foundOwnAlert = false;
		_alertSelector.setNoticeCount(0);
	}
	
	public void onTabSelected(){
		super.onTabSelected();
		Hubub.Logger("HububNotificationPanel: onTabSelected: willNotDraw(): " +this.willNotDraw());
		_hubPanel.invalidate();
	}

	
	public void setProcessingHistory(boolean processingHistory){
		Hubub.Logger("HububNotificationPanel: setProcessingHistory: processingHistory: " +processingHistory
				+", _mostRecentNote: " +_mostRecentNote);
		_processingHistory = processingHistory;
		if(!_processingHistory && _mostRecentNote != null){
			_mostRecentNote.buttonPressed(_mostRecentNote.getAct());
		}
	}
	
	public void deleteNotice(String deleteTime){
		HububNotification notification = HububNotification.getInstance();
		if(notification.getVisibility() == View.VISIBLE)
			notification.buttonPressed(notification.getDeferBtn());
		HububNotificationNote note = null;
		Hubub.Logger("HububNotificationPanel: deleteNotice: deleteTime: " +deleteTime);
		for(int i=0; i<_notesIndex; i++){
			if(_notes[i].matchesDeleteTimer(deleteTime)){
				Hubub.Logger("HububNotifications: deleteNotice: noticeMatched...");
				//_notes[i].setVisible(false);
				_hubPanel.removeView(_notes[i]);
				while(i < _notesIndex-1){
					_notes[i] = _notes[i+1];
					i++;
				}
				_notesIndex--;
				_notes[_notesIndex] = null;
				//break;
			}
		}
		if(_hubPanel.getChildCount() > 0){
			note = (HububNotificationNote)_hubPanel.getChildAt(0);
		//if((note = (Note)_vertWidget.getField(0)) != null){
			//_hubPanel.setVerticalScroll(0);
			note.setFocus();
		}
		_alertSelector.setNoticeCount(_notesIndex);

		//_vertWidget.setVerticalScroll(0);
		invalidate();
	}
	
	public void setStreamConnection(HububServices services){
		services.reset();
		String deviceID = services.getAttrib("DeviceID");
		if(HububCookies.getCookie("DeviceID").equals(deviceID)){
			Hubub.getInstance().processEmergency(services);
			_mostRecentNote = null;
			_foundOwnAlert = true;
			return;
		}
		Hubub.Logger("HububNotificationPanel: setStreamConnection..._notesIndex: " +_notesIndex +" _processingHistory: " +_processingHistory);
		HububNotificationNote masterNote = new HububNotificationNote(services, this);
		//_vertWidget.addVertWidget(masterNote);
		_hubPanel.addVertWidgetTop(masterNote);
		masterNote.loadImage();
		_notes[_notesIndex++] = masterNote;
		_notes[_notesIndex] = null;
		_alertSelector.setNoticeCount(_notesIndex);
		services.reset();
		if(!_processingHistory){
			HububNotificationNote noteCopy = new HububNotificationNote(services, this);	// A second copy for the popup
			noteCopy._masterNote = masterNote;
			noteCopy.loadImage();
			HububNotification.getInstance().alert(noteCopy);
		}
		else if(!_foundOwnAlert && services.getAttrib("Status").equals("Act")){
			_mostRecentNote = masterNote;
		}
	}


	@Override
	public void releaseInstance() {
		// TODO Auto-generated method stub
		
	}


}
