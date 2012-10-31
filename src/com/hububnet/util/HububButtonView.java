package com.hububnet.util;

import com.hububnet.Hubub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.Button;

public class HububButtonView extends Button{
	Paint _borderPaint = new Paint(Color.BLACK);
	int _width = 0;
	int _height = 0;
	boolean _sizeSet = false;
	int _buttonColor = -1;
	int _radius = 15;	// button corner radius

	public HububButtonView(Context context) {
		super(context);
		//Hubub.Logger("HububButtonView: constructor: isFocusable: " +this.isFocusable() +", isFocusableInTouchMode: " +this.isFocusableInTouchMode());
		this.setSingleLine();
	}

	public void setSize(int width, int height){
		_width = width;
		_height = height;
		_sizeSet = true;
	}
	
	public void setButtonColor(int color){
		_buttonColor = color;
		this.setSingleLine(false);

	}
	
	public void setRadius(int radius){
		_radius = radius;
	}


	protected void onDraw(Canvas canvas) {
		if(_buttonColor != -1){
			Paint paint = new Paint();
			paint.setColor(_buttonColor);
			float left = getLeft();
			float top = getTop();
			float right = left + getWidth();
			float bottom = top + getHeight();
			RectF rect = new RectF(left+2, top+2, right-2, bottom-2);
			RectF borderRect = new RectF(left, top, right, bottom);
			canvas.drawRoundRect(borderRect, _radius, _radius, _borderPaint);

			canvas.drawRoundRect(rect, _radius, _radius, paint);
		}
		super.onDraw(canvas);
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		if(_sizeSet){
			setMeasuredDimension(resolveSize(_width, widthMeasureSpec),
					resolveSize(_height, heightMeasureSpec));

		}
		else
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
