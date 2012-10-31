package com.hububnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import org.xml.sax.SAXParseException;

import com.hububnet.docs.HububServices;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class HububURLConnection implements Runnable{
	byte[] _postData = null;
	//Bitmap _bitmap = null;
	String _url;
	HububServices _services;
	int _delaySecs = 30;
	int _tries = 0;
	HububURLConnectionListener _listener;
	byte[] _responseData;

	public HububURLConnection(String url, HububServices services, HububURLConnectionListener listener){
		_url = url;
		_services = services;
		_listener = listener;
	}

	private void setDelaySecs(int tries){
		switch(tries % 4){
		case 0:
			_delaySecs = 30; break;
		case 1:
			_delaySecs = 20; break;
		case 2:
			_delaySecs = 10; break;
		case 3:
			_delaySecs = 5; break;
		default:
			_delaySecs = 30; break;
		}
	}

	public void run() {
		int rc = -1;
		HttpURLConnection httpConn = null;
		InputStream is = null;
		OutputStream os = null;

		while(Hubub._KeepRunning){
			boolean error = false;
			try {
				_tries ++;
				Hubub.Debug("2", "this: " +this +", _url: " +_url +", tries: " +_tries +", delaySecs: " +_delaySecs);
				URL url = new URL(_url);
				httpConn = (HttpURLConnection) url.openConnection();
				httpConn.setDoInput(true);
				httpConn.setConnectTimeout(_delaySecs *1000);
				//httpConn.setRequestProperty("Content-Language", "en-US");
				httpConn.setDoOutput(true);

				//try{
				os = httpConn.getOutputStream();
				//}catch(IOException e){
				//	Hubub.Logger("caught, ignore...");
				//}
				os.write(_postData);
				os.flush();

				rc = httpConn.getResponseCode();
				if(rc != HttpURLConnection.HTTP_OK){
					Hubub.Debug("2", "responseCode: " +rc);
					Hubub.Debug("1","responseMessage: " +httpConn.getResponseMessage());
					Thread.sleep(1000);// Let the network take a breath and try again...
					throw new IOException("Bad return code from write...");
				}
				is = httpConn.getInputStream();

				int len = (int)httpConn.getContentLength();
				int actual = 0;
				int bytesRead = 0;
				_responseData = new byte[len];
				while((bytesRead < len) && (actual != -1)){
					actual = is.read(_responseData, bytesRead, len - bytesRead);
					bytesRead += actual;
				}
				Hubub.Debug("2", "this: " +this +", got response... " +", tries: " +_tries +", listener: " +_listener);
				Hubub.Debug("2", "responseString:" +new String(_responseData));
				if(_responseData.length == 0){	// A response of zero length is not valid...
					error = true;
					try {
						Hubub.Debug("2", "Rest for a few secconds..");
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					continue;
				}
				if(_listener != null){
					final HububURLConnection me = this;
					Runnable runnable = new Runnable(){
						public void run(){
							//Hubub.Debug("2", "Runnable: run...");
							_listener.responseReceived(me);
						}
					};
					DroidHubub.getInstance().runOnUiThread(runnable);
				}
				//}catch(SocketTimeoutException e){
				//	Hubub.Debug("1", "SocketTimeoutException: " +e.getMessage() +" rc: " +rc);
				//	Hubub.Logger(Hubub.getStackTrace(e));
				//	if(_tries >= 4) _delaySecs = 10;
				//	error = true;

			//}catch(SAXParseException e){
				
			}catch(UnknownHostException e){
				Hubub.Debug("1", "UnknownHostException: " +e.getMessage() +" rc: " +rc +", tries: " +_tries +", delaySecs: " +_delaySecs);
				try {
					Hubub.Debug("2", "Rest for a few secconds..");
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				this.setDelaySecs(_tries);
				error = true;
				
			}catch (IOException e) {
				Hubub.Debug("1", "IOException: " +e.getMessage() +" rc: " +rc +", tries: " +_tries +", delaySecs: " +_delaySecs);
				Hubub.Logger(Hubub.getStackTrace(e));
				if(e.getMessage().contains("unreachable")){ // In this case, give the network a short rest before trying again...
					try {
						Hubub.Debug("2", "Rest for a few secconds..");
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				//if(_tries >= 4) _delaySecs = 10;
				this.setDelaySecs(_tries);
				error = true;
			}catch(Exception e){
				Hubub.Debug("1", "Exception :" +e.getMessage());
				Hubub.Logger(Hubub.getStackTrace());

			} finally{
				try {
					if(os != null) os.close();
					if(is != null) is.close();
					if(httpConn != null) httpConn.disconnect();
				} catch (IOException e) {
					Hubub.Debug("1", "finally: close Error: " +e.getMessage());
					Hubub.Logger(Hubub.getStackTrace(e));
				}
			}
			if(!error) break;
		}
	}

	public byte[] getResponse(){
		return _responseData;
	}

	public void setPostData(byte[] postData){
		_postData = postData;
	}

	public void send(){
		new Thread(this).start();

	}
}
