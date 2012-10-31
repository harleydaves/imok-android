package com.hububnet;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.util.HububPhone;
import com.hububnet.util.HububWorking;

public class C2DMReceiver extends C2DMBaseReceiver implements InvokerListener{

	public C2DMReceiver() {
		super("hububnet@gmail.com");
		Hubub.Debug("2", "C2DMReceiver: constructor...");
	}

	public C2DMReceiver(String senderId) {
		super(senderId);
		Hubub.Logger("C2DMReceiver: constructor: senderId: " +senderId);
		// TODO Auto-generated constructor stub
	}

	/* C2DMBaseReceiver Protocol */
	@Override
	public void onError(Context context, String errorId) {
		try{
			Hubub.Logger("C2DMReceiver: onError: errorId: " +errorId);
			// Usually caused by Service_Not_Available, sleep for a bit and retry...
			Thread.sleep(5000);
			C2DMessaging.register(this, "hububnet@gmail.com");
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		try{
			//HububCookies cookies = HububCookies.getInstance();
			//Hubub.Logger("C2DMReceiver: onMessage: cookies: " +cookies);

			Bundle extras = intent.getExtras();
			String payload = (String)extras.get("payload");
			Hubub.Logger("C2DMReceiver: onMessage... Intent: " +intent +", extras: " +extras);

			if(payload != null){
				Hubub.Logger("C2DMReceiver: onMessage: payload: " +payload);
			}

			Ringtone ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM));
			ringtone.play();

			Intent hububIntent = new Intent("com.hububnet.action.NOTIFY");
			hububIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			this.startActivity(hububIntent);

			Thread.sleep(5000); // Let alarm ring for this period of time...
			//ringtone.stop();

		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void onRegistered(Context context, String registrationId) throws IOException {
		try{
			Hubub.Debug("2", "C2DMReceiver: onRegistered and got key: " + registrationId);
			//HububCookies.setCookie("PushToken", registrationId);

			// Now update the server
			//HububServices services = new HububServices();
			//HububService service = services.addServiceCall("UpdateDevice");
			//service.getInputs();
			//service.setParm("DeviceID", HububCookies.getCookie("DeviceID"));
			//service.setParm("PushToken", registrationId);
			//Invoker invoker = new Invoker();
			//invoker.sendAsEntityID("0");
			//invoker.send(services, this);
			final String pushToken = registrationId;
			Runnable runnable = new Runnable(){
				public void run(){
					Hubub.Debug("2", "Runnable: run...");
					HububWorking.getInstance().doneWorking();
					HububPhone.getInstance().updatePushToken(pushToken);
				}
			};
			DroidHubub.getInstance().runOnUiThread(runnable);

		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void onResponseReceived(HububServices services) {
		Hubub.Logger("C2DMReceiver: onResponseReceived: services: " +services);

	}

	//public void onRegistrered(Context context, String registration) {
	//	Hubub.Logger("C2DMReceiver: registered and got key: " + registration);
	//}

}
