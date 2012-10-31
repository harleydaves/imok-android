package com.hububnet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.google.android.c2dm.C2DMessaging;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububServices;


public class HububStreamListener extends Thread {

	boolean _keepRunning;
	String _command = "0";
	Socket _sc = null;
	InputStream _is = null;
	OutputStream _os = null;
	boolean _running = false;
	int _port = 0;

	static HububStreamListener _instance;

	public class EventDispatcherObject implements Runnable{
		HububServices _services;
		public EventDispatcherObject(HububServices services){
			_services = services;
		}

		public void run() {
			HububStreamConnector.getInstance().processStreamMessage(_services);
		}

	}

	private HububStreamListener(){
		_instance = this;
		_keepRunning = true;
		_port = Integer.parseInt(Hubub._HububPushStreamPort);
		this.start();	// Start the thread
		Hubub.Debug("2", "Constructor: about to wait...");
		HububWorking.getInstance().working();
		try {
			synchronized(this){
				this.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	// Wait for it to release me...
		Hubub.Debug("2", "Constructor: released from wait...");
		HububWorking.getInstance().doneWorking();
	}

	public static HububStreamListener getInstance(){
		if(_instance == null){
			_instance = new HububStreamListener();
		}
		return _instance;
	}

	public void close(){
		_keepRunning = false;
		this.interrupt();
		Hubub.Debug("2", "just interrupted threa...");
		this.closeSocket();
	}

	private void closeSocket(){
		try{
			if(_os != null){
				_os.close();
				_os = null;
			}
			if(_is != null){
				_is.close();
				_is = null;
			}
			if(_sc != null){
				_sc.close();
				_sc = null;
			}
		}catch(IOException e){
			Hubub.Debug("1", "IOException: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void waitOnMe(){
		if(_running) return;
		Hubub.Debug("2", "wait for Listener to start...");
		try {
			synchronized(this){
				this.wait();
			}
		} catch (InterruptedException e) {
			Hubub.Debug("1", "InteruptedException: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
		}	// Wait for it to release me...

	}

	public void run(){
		int tries = 0;
		while(_keepRunning){
			_sc = null;
			_is = null;
			_os = null;
			int conn = 0;
			try {
				String url = Hubub.getHububBaseAddress();
				url = Hubub.getBaseAddress() +":" + Hubub._HububPushStreamPort;
				Hubub.Debug("2", "baseaddress: " +Hubub.getBaseAddress() +", port: " +_port);
				String connectParms = (Hubub.HUBUBSIMULATOR)?";deviceside=true":";deviceside=false;ConnecionType=mds-public";
				//_sc = (SocketConnection)Connector.open("socket://" +url +connectParms +";ConnectionTimeout=240000",
				//		Connector.READ_WRITE, true);
				_sc = new Socket(Hubub.getBaseAddress(), _port);
				//_sc.setSocketOption(SocketConnection.KEEPALIVE, 2);
				//_sc.setSocketOption(SocketConnection.LINGER, 10 * 60);
				//_sc.setSocketOption(SocketConnection.DELAY, 0);
				//_sc.setSocketOption(SocketConnection.LINGER, 300);
				_sc.setSoLinger(true, 300);
				//_sc.setSocketOption(SocketConnection.KEEPALIVE, 0);
				//_sc.setSocketOption(SocketConnection.RCVBUF, 128);
				//_sc.setSocketOption(SocketConnection.SNDBUF, 128);
				_os = _sc.getOutputStream();
				_is = _sc.getInputStream();
				//_is = Connector.openInputStream("socket://whitestone.dyndns.biz:8080");
			} catch (IOException e) {
				conn = -1;
				Hubub.Debug("1", "IOException: error: " +e.getMessage());
				Hubub.Logger(Hubub.getStackTrace(e));
				try {
					Hubub.Debug("2", "Rest for a few seconds...");
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					Hubub.Debug("1", "InterruptedException: " +e.getMessage());
					Hubub.Logger(Hubub.getStackTrace(e));
				}
				continue;
			}

			if(_keepRunning){
				Hubub.Debug("2", "conn: " +conn );
				byte[] outbuf = new byte[1000];
				byte[] inbuf = new byte[64*1024];
				byte[] msgbuf = new byte[64*1024];
				byte[] ackbuf = new byte[9];

				String entityID = HububCookies.getCookie("EntityID");
				while(Hubub._KeepRunning){	// don't start until you have a valid EntityID...
					entityID = HububCookies.getCookie("EntityID");
					if(entityID != null && !entityID.equals("")){
						break;
					}
					try {
						Hubub.Debug("2", "wait for entityID cookie...");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Hubub.Debug("1", "InterruptedException: " +e.getMessage());
						Hubub.Logger(Hubub.getStackTrace(e));
					}
				}
				if(!Hubub._KeepRunning){
					synchronized(this){
						this.notifyAll();
					}
					break;
				}

				/* Register device for push notifications... */
				//Hubub.Debug("2", "get registrationId...");
				//String registrationId = C2DMessaging.getRegistrationId(DroidHubub.getInstance());
				//if(registrationId != null && !"".equals(registrationId)){
				//	Hubub.Debug("2", "Already registered. registrationId is " + registrationId);
				//	HububCookies.setCookie("PushToken", registrationId);
				//}else{
				//	Hubub.Debug("2", "No existing registrationId. Registering..");
				//	if(HububCookies.getCookie("PushToken") == null)
				//		C2DMessaging.register(DroidHubub.getInstance(), "hububnet@gmail.com");
				//}

				int cursor = this.prepareMessageBuf(outbuf, _command, tries);
				String testString = new String(outbuf, 0, cursor);
				Hubub.Debug("2", "testString: " +testString +", testLength: " +cursor);

				//String entityName = entityID +"/" +HububCookies.getCookie("DeviceID");
				//Hubub.Debug("2", "entitID cookie found!! entityName: " +entityName);
				//byte[] data = null;
				int bytesRead = 0;
				//data = ("GET /hubub" +_command +entityName +" HTTP/1.1").getBytes();
				//int cursor = 0;
				//outbuf[cursor++] = (byte) Integer.parseInt(_command);
				//outbuf[cursor++] = (byte) ((data.length >> 8) & 0xff);	// high order byte of length
				//outbuf[cursor++] = (byte) (data.length & 0xff);	// low order byte of length
				//for(int i=0; i<data.length; i++){	// copy to outbuf and add http request cr/nl
				//	outbuf[cursor++] = data[i];
				//}
				//outbuf[cursor++] = 13;
				//outbuf[cursor++] = 10;
				Hubub.Debug("2", "outBuff.toString: " +new String(outbuf,0, cursor));
				try {//
					_os.write(outbuf, 0, cursor);
					_os.flush();
					//_os.close(); // Apparently in Android, this also closes InputStream... Seems wasteful to keep it open
					int timeout = 0;
					switch(tries % 5){
					case(0): timeout = 3000;
					break;
					
					case(1): timeout = 3000;
					break;
					
					case(2): timeout = 3000;
					break;
					
					case(3): timeout = 5000;
					break;
					
					case(4): timeout = 10000;
					break;
					
					default: timeout = 3000;
					break;
					}
					
					Hubub.Debug("2", "timeout: " +timeout);
					//timeout = 50;	// set only for testing, comment out for production...
					_sc.setSoTimeout(timeout);
					try{
						bytesRead = HububUtil.readInputStream(_is, ackbuf);
					}catch(SocketTimeoutException e){
						Hubub.Debug("2", "Read timeout... try again, tries: " +tries);
						tries++;
						this.closeSocket();
						try {
							if(timeout < 1000) Thread.sleep(5000);	// Do this only if testing...
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						continue;

					}
					Hubub.Debug("2", "after read, bytesRead: " +bytesRead);
					String test = new String(ackbuf, 0, bytesRead);
					Hubub.Debug("2", "after read: bytesRead: " +bytesRead +", msg: " +test);
					if(bytesRead != 9){
						this.closeSocket();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
					//_os.close();
				} catch (IOException e) {
					Hubub.Debug("1", "IOException: " +e.getMessage());
					Hubub.Logger(Hubub.getStackTrace(e));
				}
				synchronized(this){
					this.notifyAll();
				}
				_running = true;
				Hubub.Debug("2", "Hjust NOTIFIED...");

				/*
				 * now start processing push channel
				 */
				int lengthCursor = 0;
				int msgCursor = 0;
				int length = 0;
				int tmpLength = 0;
				while(_keepRunning){
					Hubub.Debug("2", "in loop, about to read bytes...");
					try {
						_sc.setSoTimeout(0);	// make sure this call blocks
					} catch (SocketException e1) {
						tries++;
						this.closeSocket();
						e1.printStackTrace();
						continue;
					}
					try {
						bytesRead = HububUtil.readInputStream(_is, inbuf);
					} catch (SocketTimeoutException e1) {
						// Should never be called since timeout set to zero...
						Hubub.Debug("1", "Should never have been called...");
						tries++;
						this.closeSocket();
						e1.printStackTrace();
						continue;
					}
					if(bytesRead <= 0){	// server closed its end, reconnect...
						Hubub.Debug("2", "bytesRead = ZERO");
						break;	// get out and take corrective action...
					}
					Hubub.Debug("2", "bytesRead: " +bytesRead +", inbuf: " + new String(inbuf,0, bytesRead));
					// Now process the input buffer that can have multiple and fragmented messages
					cursor = 0;

					while(_keepRunning){
						if(lengthCursor == 0){
							if(cursor >= bytesRead){
								Hubub.Debug("2", "lengthCursor = 0 get new batch...");
								break;
							}
							tmpLength = inbuf[cursor++] &0xff;
							length = (tmpLength << 8) &0xff00;	//High order byte of length
							lengthCursor++;
						}
						if(lengthCursor == 1){
							if(cursor >= bytesRead){
								Hubub.Debug("2", "lengthCursor = 1 get new batch...");
								break;
							}
							tmpLength = inbuf[cursor++] &0xff;
							length |= tmpLength &0xff;	// Low order byte of length
							lengthCursor++;
						}
						boolean breakNextWhile = false;
						while(msgCursor < length){
							if(cursor >= bytesRead){
								Hubub.Debug("2", "msgCursor < length get new batch...");
								breakNextWhile = true;
								break;
							}
							msgbuf[msgCursor++] = inbuf[cursor++];
						}
						if(breakNextWhile) break;
						String msgString = null;
						try {
							msgString = new String(msgbuf, 0, length, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							Hubub.Debug("1", "UnsupportedEncodingException..." +e.getMessage());
							Hubub.Logger(Hubub.getStackTrace(e));
						}
						Hubub.Debug("2", "in loop: after read... msgLength: " +length +", msg: " +msgString);
						lengthCursor = 0;
						msgCursor = 0;
						HububServices services = new HububServices();
						services.parse(msgString);
						EventDispatcherObject dispObj = new EventDispatcherObject(services);
						//Application.getApplication().invokeLater(dispObj);
						DroidHubub.getInstance().runOnUiThread(dispObj);
					}
				}
				if(bytesRead <= 0){
					Hubub.Debug("2", "socket problem processing... _keepRunning: " +_keepRunning);
					if(_keepRunning){
						this.closeSocket();
						try {
							Thread.sleep(1000); // Giver server time to regroup...
						} catch (InterruptedException e) {
							Hubub.Debug("1", "run: ThreadSleep Error: " +e.getMessage());
							Hubub.Logger(Hubub.getStackTrace(e));
						} 
						_command = "1";
						//break;
					}
				}
			}
		}
		Hubub.Debug("2", "HububStreamListener: exiting...");
	}

	private int prepareMessageBuf(byte[] msgBuf, String command, int tries){
		int length = 0;
		String entityID = HububCookies.getCookie("EntityID");
		String deviceID = HububCookies.getCookie("DeviceID");
		String entityName = entityID +"/" +deviceID;
		byte[] outbuf = new byte[1000];
		if((tries % 2) == 1){	//Use the full http approach...
			String firstline = "GET /peter" +command +entityName +" HTTP/1.1";
			String[] chunks = {
					firstline,
					"Host: 74.54.77.66",
					"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_0_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A405 Safari/7534.48.3",
					"Accept-Language: en-us",
					"x-wsb-billing: 5e80b0ee-a2ad-4bf7-b509-f3de4030d63a",
					"Accept: text/html,application/xhtml+xml,application/xml;q=0.900,*/*;q=0.001",
					"Accept-Charset: *;q=0.001",
					"Accept-Encoding: gzip,deflate,identity,*;q=0.001",
					"X-Forwarded-For: 1.42.51.126",
					"Cache-Control: max-age=259200",
					"Connection: keep-alive"
			};
			for(int i=0; i<chunks.length; i++){
				byte[] data = chunks[i].getBytes();
				System.arraycopy(data, 0, msgBuf, length, data.length);
				length += data.length;
				msgBuf[length++] = 13;
				msgBuf[length++] = 10;
			}
			msgBuf[length++] = 13;	// End with another CR/LF
			msgBuf[length++] = 10;
		}
		else{	// Use the short, binary request
			byte[] data = entityName.getBytes();
			msgBuf[length++] = (byte) Integer.parseInt(_command);
			msgBuf[length++] = (byte) ((data.length >> 8) & 0xff);	// high order byte of length
			outbuf[length++] = (byte) (data.length & 0xff);	// low order byte of length
			for(int i=0; i<data.length; i++){	// copy to outbuf and add http request cr/nl
				msgBuf[length++] = data[i];
			}

		}
		return length;
	}
}
