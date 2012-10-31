package com.hububnet.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.hububnet.Hubub;


public class HububUtil {

	public static int readInputStream(InputStream is, byte[] b) throws SocketTimeoutException{
		int read = 0;
		int blength = b.length;
		int ch = 0;	// the byte that will be read
		
		while(true){
			try {
				if(read >= blength){
					return read;
				}
				ch = is.read();
				if(ch == -1) return -1;
				if(ch == 0 && read == 1 && b[0] == 0){	// keepalive from server, ignore it.
					Hubub.Logger("HububUtil: readInputStream: keepalive received...");
					read = 0;
					continue;	
				}
				b[read++] = (byte) ch;
				if(is.available() == 0) return read;
			}catch(SocketTimeoutException e){
				Hubub.Debug("1", "SocketTimeoutException : " +e.getMessage());
				throw new SocketTimeoutException();
			}catch(SocketException e){
				String msg = e.getMessage();
				Hubub.Debug("1", "SocketException: " +msg);
				if(msg == null || msg.equals("Socket closed")){
					return -1;
				}
				Hubub.Logger(Hubub.getStackTrace(e));
				continue;
			} catch (IOException e) {
				String msg = e.getMessage();
				Hubub.Debug("1", "HububUtil: readInputStream: exception: " +msg +", className: " +e.getClass().getName());
				if(msg == null || msg.equals("Connection closed") || msg.equals("Stream closed")){
					return -1;
				}
				Hubub.Logger(Hubub.getStackTrace(e));
				continue;
			}
		}
	}
	
	/*
	public static void getWebData(final String url, final WebDataCallback callback) throws IOException
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				HttpConnection connection = null;
				InputStream inputStream = null;

				Hubub.Logger("HububUtil: getWebData: run: started...");
				try
				{
					//String encodedURL = URLUTF8Encoder.encode(url);
					//Hubub.Logger("HububUtil: getWebData: encodedURL: " +encodedURL);
					connection = (HttpConnection) Connector.open(url+";ConnectionTimeout=" + 30*1000 +";deviceside=false");//, Connector.READ, true);
					//connection.setRequestMethod(HttpConnection.GET);
					connection.setRequestProperty("Content-Language", "en-US");
					inputStream = connection.openInputStream();
					byte[] responseData = new byte[10000];
					int length = 0;
					StringBuffer rawResponse = new StringBuffer();
					while (-1 != (length = inputStream.read(responseData)))
					{
						rawResponse.append(new String(responseData, 0, length));
						//Hubub.Logger("HububUtil: getWebData: run: rawResponse: " +rawResponse.toString());
					}
					int responseCode = connection.getResponseCode();
					if (responseCode != HttpConnection.HTTP_OK)
					{
						Hubub.Logger("HububUtil: run: connection Failed: responseCode: " +responseCode);
						throw new IOException("HTTP response code: "
								+ responseCode);
					}

					final String result = rawResponse.toString();
					UiApplication.getUiApplication().invokeLater(new Runnable()
					{
						public void run()
						{
							callback.callback(result);
						}
					});
				}
				catch (final Exception ex)
				{
					UiApplication.getUiApplication().invokeLater(new Runnable()
					{
						public void run()
						{
							callback.callback("Exception (" + ex.getClass() + "): " + ex.getMessage());
						}
					});
				}
				finally
				{
					try
					{
						inputStream.close();
						inputStream = null;
						connection.close();
						connection = null;
					}
					catch(Exception e){}
				}
				Hubub.Logger("HububUtil: getWebData: run: thread Ending...");
			}
		});
		t.start();
	}
	*/
}
