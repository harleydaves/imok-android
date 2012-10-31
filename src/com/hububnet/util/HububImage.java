package com.hububnet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububURLConnection;
import com.hububnet.HububURLConnectionListener;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;

public class HububImage extends HububWidgett implements HububURLConnectionListener, OnClickListener{
	String _imageName = null;
	HububImageView _image;
	String _imageURL;
	HububServices _services;
	boolean _resizeEnabled = true;
	boolean _allowCache = false;
	private float _imageWidth = 0;
	private float _imageHeight = 0;
	private boolean _resized = false;
	private int _origWidth = 0;
	private int _origHeight = 0;

	public class ImageLoader extends Thread{
		HububImageView _image;
		String _imageURL;
		float _imageWidth;
		float _imageHeight;

		public ImageLoader(HububImageView image, String imageURL, float imageWidth, float imageHeight){
			_image = image;
			_imageURL = imageURL;
			_imageWidth = imageWidth;
			_imageHeight = imageHeight;
		}

		public void run(){
			try{
				final Bitmap bitmap = HububImage.getBitmapFromURL(_imageURL);
				Runnable runnable = new Runnable(){
					public void run(){
						_image.setImageBitmap(bitmap);
						_image.setImageSize((int)_imageWidth, (int)_imageHeight);
					}
				};
				DroidHubub.getInstance().runOnUiThread(runnable);
			}catch(Exception e){
				Hubub.Debug("1", "Exception: " +e.getMessage());
				e.printStackTrace();
			}

		}
	}

	public HububImage(){
		super();
		//_gapAbove = 10;
		//_gapBelow = 15;
		_image = new HububImageView();
		_image.setOnClickListener(this);
		this.addView(_image);
	}

	public HububImage(String imageName){
		this();
		_imageName = imageName;
	}

	public void setImagePath(String imagePath){
		_imageName = imagePath;
	}

	public void setImageURL(String imageURL){
		_imageURL = imageURL;
	}

	public void setImageSize(int width, int height){
		_imageWidth = width;
		_imageHeight = height;
	}

	public void resizeEnabled(boolean resizeEnabled){
		_resizeEnabled = resizeEnabled;
	}

	public void reLoadImage(){
		try{
			if(_imageURL != null){
				//_image.setImageBitmap(getBitmapFromURL(_imageURL));
				ImageLoader imageLoader = new ImageLoader(_image, _imageURL, _imageWidth, _imageHeight);
				imageLoader.start();
				//_image.setImageSize((int)this._imageWidth, (int)this._imageHeight);
				return;
			}
			_services = new HububServices();
			HububService service = _services.addServiceCall("GetImage");
			service.getInputs();
			service.setParm("Name", _imageName);
			Hubub.Debug("2", "services.toString: " +_services.toString());
			//String imageURL = this.createImageURL();
			//Hubub.Logger("HububImage: reLoadImage: imageURL: " +imageURL);
			//_image.setUrl(imageURL);
			//_image.send(_services);
			String servicesAsString = _services.toString();
			byte[] postData = null;
			try{
				postData = servicesAsString.getBytes("UTF-8");
			}catch(UnsupportedEncodingException e){
				Hubub.Debug("1", " Error: " +e.getMessage() +", services: " +_services);
				Hubub.Logger(Hubub.getStackTrace(e));
			}
			//String url = "http://" +Hubub.getHububBaseURL() +"HububGetImageFromPost";
			String url = (Hubub._HububTranPort.equals("443"))?"https://":"http://";
			url += Hubub.getBaseAddress() +":" +Hubub._HububTranPort +"/HububSrv/HububGetImageFromPost";
			Hubub.Debug("2", "url: " +url);
			HububURLConnection urlCon = new HububURLConnection(url, _services, this);
			urlCon.setPostData(postData);
			urlCon.send();
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}
	}

	public void setAllowCache(boolean allowCache){
		_allowCache = allowCache;
	}

	/*
	public String createImageURL(){
		if(!_allowCache) _services.setTag("" +new Date().getTime());
		return "http://" +Hubub.getHububBaseURL() +"HububUpload?" +URLUTF8Encoder.encode(_services.toString()) +"ConnectionType=mds-public;deviceside=true";
	}
	 */
	public void enableEdit(boolean edit){
		super.enableEdit(edit);
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	/* HububURLConnectionListener Protocol */
	public void connectionDidFail(HububURLConnection urlConnection) {
		// TODO Auto-generated method stub

	}

	public void responseReceived(HububURLConnection urlConnection) {
		try{
			byte[] data = urlConnection.getResponse();
			Hubub.Debug("2", "data.length: " +data.length);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			_image.setImageBitmap(bitmap);
			//this.setImageSize(50, 50);
			Hubub.Debug("2", "width: " +bitmap.getWidth() +", height: " +bitmap.getHeight());
			if(_imageWidth != 0 || _imageHeight != 0){
				int finalWidth = (int) _imageWidth;
				int finalHeight = (int) _imageHeight;
				float width = bitmap.getWidth();
				float height = bitmap.getHeight();
				float ratio = 0;
				if(finalHeight == 0){
					ratio = _imageWidth/width;
					finalWidth =  (int)_imageWidth;
					finalHeight = (int)(ratio * height);
				}
				else if(finalWidth == 0){
					ratio = _imageHeight/height;
					finalWidth =  (int)_imageHeight;
					finalHeight = (int)(ratio * height);

				}
				_image.setImageSize(finalWidth, finalHeight);
			}
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}
	}

	/* OnClickListener Protocol */
	public void onClick(View arg0) {
		Hubub.Debug("2", "...");
		if(!_resizeEnabled) return;
		if(!_resized){
			_origWidth = _image.getWidth();
			_origHeight = _image.getHeight();
			_image.setImageSize(_origWidth*2, _origHeight*2);
			_resized = true;
		}
		else{
			_image.setImageSize(_origWidth, _origHeight);
			_resized = false;
		}
	}
}
