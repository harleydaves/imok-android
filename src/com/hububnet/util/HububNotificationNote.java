package com.hububnet.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

//import Driod.Hubub.Util.HububNotificationPanel.Note;
import android.graphics.Typeface;

public class HububNotificationNote extends HububWidgett implements HububButtonListener{
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
	HububNotificationNote _masterNote = null;
	boolean _myEmergency = false;
	String _monitorButtonColor;

	public HububNotificationNote(HububServices services, HububNotificationPanel notificationPanel){
		super();
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
		_pic.setImageSize(0, Hubub.getScaledHeight(75));
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



	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububNotificationNote: buttonPressed: button: " +button);
		try{
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
			HububNotification.getInstance().hide();
		}catch(Exception e){
			Hubub.Logger("HububNotficationNote: Exception: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void updateStatus(){
		_shortDesc.setText(_services.getAttrib("ShortDesc") +" (" +_services.getAttrib("Status") +")");
	}

}
