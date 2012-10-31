package com.hububnet.util;

import com.hububnet.Hubub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class HububWidgett extends AbsolutePanel implements HububWidgettListener{
	int _maxHeight = 0;
	int _maxWidth = 0;
	int _maxAlign = 0;
	int _gapAbove = 0; // Space in pixels above this widget
	int _gapBelow = 0; // Space in pixels below this widget
	int _padding = 0;
	int _topPadding = 0;
	int _alignX = 0;	// Override to alignX
	int _fieldIndex = -1;
	int _centerOn = -1;
	//Manager _prevManager = null;
	boolean _border = false;
	ShapeDrawable _drawable;

	static int VERTGAP = 0;
	boolean _enableEdit = true;
	protected String _tag = "";	// A way to distinguish HububWidgets
	HububWidgettListener _widgetListener = null;
	int _horizRoom = 350;	// the horizontal room for this widget
	int _vertRoom = 350;	// the vertical room for this widget
	boolean _isVertical = true;	// indicates the orientation of this widget
	int _backgroundColor = -1;

	public static boolean isHububWidget(View widget){
		return (widget instanceof HububWidgett);
	}

	public HububWidgett(){
		super();
		//this.setWillNotDraw(false);
		//this.setFocusable(true);
		//this.setFocusableInTouchMode(true);
	}

	public HububWidgett(long style){
		super();
	}

	public HububWidgett(Context context){
		super(context);
	}

	public void setBorder(boolean border){
		Hubub.Logger("HububWidgett: this: " +this +", setBorder: border: " +border +", willNotDraw(): " +this.willNotDraw());
		_border = border;
		this.setWillNotDraw(!border);
	}

	public void setWillNotDraw(boolean willNotDraw){
		//if(this instanceof HububNotificationPanel.Note){
		Hubub.Logger("HububWidgett: this: " +this +", setWillNotDraw: " +willNotDraw +", willNotDraw(): " +this.willNotDraw());
		//}
		super.setWillNotDraw(willNotDraw);
		Hubub.Logger("HububWidgett: this: " +this +", After setWillNotDraw: " +willNotDraw +", willNotDraw(): " +this.willNotDraw());

	}

	public void setBackgroundColor(int backgroundColor){
		_backgroundColor = backgroundColor;
		super.setBackgroundColor(backgroundColor);
	}

	public void setPadding(int padding){
		_padding = padding;
	}

	public void setTopPadding(int topPadding){
		_topPadding = topPadding;
	}

	public int getTopPadding(){
		return _topPadding;
	}

	public void setCenterOn(int centerOn){
		_centerOn = centerOn;
	}

	public boolean setFocus(){
		return requestFocus();
	}

	protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect){
		Hubub.Logger("HububWidgett: this: " +this +", onFocusChanged: " +this +", gainFocus: " +gainFocus);
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	//protected void onFocus(int direction){
	//	super.onFocus(direction);
	//	invalidate();
	//}

	public void setVisible(boolean visible){
		this.setVisibility((visible)?View.VISIBLE:View.GONE);
	}

	public void sizeToFit(){
		int maxWidth = 0;
		int maxHeight = 0;
		int width;
		int height;
		for(int i=0; i<this.getChildCount(); i++){
			//Iterator<Widget> it = getChildren().iterator();
			//while(it.hasNext()){

			View widget = getChildAt(i);
			width = widget.getLeft() + widget.getWidth();
			maxWidth = (width > maxWidth)?width:maxWidth;
			height = widget.getTop() + widget.getHeight();
			maxHeight = (height > maxHeight)?height:maxHeight;
		}
		//setPixelSize(maxWidth, maxHeight);
		this.layout(this.getLeft(), this.getTop(), maxWidth, maxHeight);
		_maxWidth = maxWidth + _padding;
		_maxHeight = maxHeight + _padding;
		Hubub.Logger("HububWidgett: sizeToFit: this.getWidth: " +this.getWidth());
		//if(getOffsetWidth() == 299) new Exception().printStackTrace();
	}

	public void setWidgetListener(HububWidgettListener widgetListener){
		_widgetListener = widgetListener;
	}

	protected void sizeToFitVertical(){
		int maxHeight = 0;
		int height;
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			height = widget.getTop() + widget.getHeight();
			maxHeight = (height > maxHeight)?height:maxHeight;
		}
		this.setMeasuredDimension(this.getWidth(), maxHeight);
	}

	public void setAlignX(int alignX){
		_alignX = alignX;
	}

	public String getTag(){
		return _tag;
	}

	public void setTag(String tag){
		_tag = tag;
	}

	public int getAlignX(){
		return (_alignX > 0)?_alignX:this.getMeasuredWidth()/2;
	}

	//public void setFontSize(int size){
	//	Font font = this.getFont();
	//	font = font.derive(font.getStyle(), size);
	//	this.setFont(font);
	//}

	//public void setFontStyle(int style){
	//	Font font = this.getFont();
	//	this.setFont(font.derive(style, font.getHeight()));
	//}

	public int getAlignY(){
		return this.getMeasuredHeight()/2;
	}

	public void addVertWidgetTop(View widget){
		this.addView(widget, 0);
	}

	public void addVertWidgetTop(View widget, int index){
		this._isVertical = true;
		this.addView(widget, index);
	}

	public void addVertWidget(View widget){
		//boolean isHububWidget = (((Object)widget).getClass().getSuperclass() == HububWidgett.class);
		/*
		try{
			boolean isHububWidget = HububWidgett.isHububWidget(widget);
			//if((widget.getClass() == HububDevice.class))
			//	HububConsole.getInstance().write("HububWidgett: addVertWidget widgetClass: ..." + widget.getClass().getName());
			int gapAbove = (isHububWidget)?((HububWidgett)widget)._gapAbove:0;
			int gapBelow = (isHububWidget)?((HububWidgett)widget)._gapBelow:0;
			//if((widget.getClass() == HububDevice.class))
			//	HububConsole.getInstance().write("HububWidgett: addVertWidget before add()...");
			add(widget);
			//if((widget.getClass() == HububDevice.class))
			//	HububConsole.getInstance().write("HububWidgett: addVertWidget after add()...");
			setPositionChild(widget, 0, _maxHeight + VERTGAP + gapAbove);
			_maxHeight += widget.getHeight() +VERTGAP + gapBelow;
			int retval;
			if(isHububWidget){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				retval = ((HububWidgett)widget).getAlignX();			
			}
			else{
				//GWT.log("HububWidgett: addVertWidget: got a Regular Widget...", null);
				retval = widget.getLeft() + widget.getWidth()/2;
			}
			_maxAlign = (retval > _maxAlign)?retval:_maxAlign;
			//if((widget.getClass() == HububDevice.class))
			//	HububConsole.getInstance().write("HububWidgett: addVertWidget finished...");
		}catch(Exception e){
			System.out.println("HububWidgett: Exception: ");
			e.printStackTrace();
			//HububConsole.getInstance().printStackTrace("HububWidgett: addVertWidget: widget name..." +widget.getClass().getName(), e);
			//HububConsole.getInstance().write("HububWidgett: addVertWidget: caught Exception...e: " +e);
		}
		 */
		this._isVertical = true;
		this.addView(widget);
		this.setMeasuredDimension(50, 100);
	}

	public void addHorizWidget(View widget){
		this._isVertical = false;
		this.addView(widget);
	}

	/* This should be overridden to be useful */
	public void hububWidgetEventHasHappened(){

	}

	public void reLayout(){
		//Iterator<Widget> it = getChildren().iterator();
		int retval = 0;
		boolean isHububWidget = false;
		int gapAbove = 0;
		int gapBelow = 0;
		_maxHeight = _padding;
		_maxWidth = 0;
		_maxAlign = 0;

		int childCount = this.getChildCount();
		Hubub.Logger("HububWidgett: " +this +" reLayout: childCount: " +childCount);
		for(int i = 0; i<childCount; i++){
			View widget = this.getChildAt(i);
			gapAbove = 0;
			gapBelow = 0;
			isHububWidget = HububWidgett.isHububWidget(widget);
			if(isHububWidget){
				gapAbove = ((HububWidgett)widget)._gapAbove;
				gapBelow = ((HububWidgett)widget)._gapBelow;
			}
			int y = _maxHeight + VERTGAP + gapAbove;
			Hubub.Logger("HububWidgett: " +this +" relayout: widget: " +widget +", widget.getMeasuredWidth: " +widget.getMeasuredWidth() +", widget.getMeasuredHeight: " +widget.getMeasuredHeight());
			widget.layout(_padding, y, _padding + widget.getMeasuredWidth(), y + widget.getMeasuredHeight());
			//layoutChild(widget, _horizRoom, _vertRoom);
			//setPositionChild(widget, _padding, _maxHeight + VERTGAP + gapAbove);
			_maxHeight = widget.getTop() + widget.getMeasuredHeight()+gapBelow;
			_maxWidth = (widget.getMeasuredWidth()+_padding > _maxWidth)?widget.getMeasuredWidth()+_padding:_maxWidth;
			//GWT.log("HububWidgett: reLayout: _maxHeight: " +_maxHeight, null);
			//if(!widgetVisible) widget.setVisible(false);
			//int retval;
			if(isHububWidget){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				retval = ((HububWidgett)widget).getAlignX()+_padding;			
			}
			else{
				//GWT.log("HububWidgett: addVertWidget: got a Regular Widget...", null);
				retval = widget.getWidth()/2 +_padding;
			}
			_maxAlign = (retval > _maxAlign)?retval:_maxAlign;
		}
		//sizeToFitVertical();
		_maxHeight += _padding;
		_maxWidth += _padding;
	}

	public void reLayoutHoriz(){
		int retval = 0;
		boolean isHububWidget = false;
		int gapAbove = 0;
		int gapBelow = 0;
		_maxWidth = _padding;
		_maxHeight = 0;
		_maxAlign = 0;
		int childCount = this.getChildCount();
		//Hubub.Logger("HububWidgett: " +this +" reLayoutHoriz: childCount: " +childCount);
		for(int i=0; i<childCount; i++){
			View widget = this.getChildAt(i);
			gapAbove = 0;
			gapBelow = 0;
			isHububWidget = HububWidgett.isHububWidget(widget);
			if(isHububWidget){
				gapAbove = ((HububWidgett)widget)._gapAbove;
				gapBelow = ((HububWidgett)widget)._gapBelow;
			}
			//Hubub.Logger("HububWidgett: reLayoutHoriz: getWidth(): " +getWidth() +" getHeight(): " +getHeight());
			int x = _maxWidth + VERTGAP + gapAbove;
			widget.layout(x, _topPadding, x + widget.getWidth(), _topPadding + widget.getHeight());
			//layoutChild(widget, _horizRoom, _vertRoom);
			//setPositionChild(widget, _maxWidth + VERTGAP + gapAbove, _topPadding);
			_maxWidth = widget.getLeft() + widget.getWidth() + gapBelow;
			_maxHeight = (widget.getHeight()+_topPadding > _maxHeight)?widget.getHeight() +_topPadding:_maxHeight;
			//if(!widgetVisible) widget.setVisible(false);
			if(isHububWidget){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				retval = ((HububWidgett)widget).getAlignY() +_topPadding;			
			}
			else{
				//GWT.log("HububWidgett: addVertWidget: got a Regular Widget...", null);
				//retval = getWidgetTop(widget) + widget.getOffsetHeight()/2;
				retval = widget.getHeight()/2 +_topPadding;
			}
			_maxAlign = (retval > _maxAlign)?retval:_maxAlign;
		}
		_maxHeight += _padding;
		_maxWidth += _padding;
	}


	public void align(){
		int retval = 0;
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			if(HububWidgett.isHububWidget(widget)){
				retval = ((HububWidgett)widget).getAlignX();			
				//GWT.log("HububWidgett: align: got a HububWidget...retval: " +retval, null);
			}
			else{
				retval = widget.getWidth()/2;
				//GWT.log("HububWidgett: align: got a Regular Widget... retval: " +retval, null);
			}
			int x = _maxAlign - retval;
			int y = widget.getTop();
			widget.layout(x, y, x + widget.getWidth(), y + widget.getHeight());
			//setPositionChild(widget, _maxAlign - retval, widget.getTop());
		}
	}

	public void alignCenter(){
		//Iterator<Widget> it = getChildren().iterator();
		int retval = 0;
		//while(it.hasNext()){
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			if(HububWidgett.isHububWidget(widget)){
				retval = ((HububWidgett)widget).getAlignX();			
				//GWT.log("HububWidgett: align: got a HububWidget...retval: " +retval, null);
			}
			else{
				retval =  widget.getWidth()/2;
				//GWT.log("HububWidgett: align: got a Regular Widget... retval: " +retval, null);
			}
			int x = this.getWidth()/2 - retval;
			int y = widget.getTop();
			widget.layout(x, y, x + widget.getWidth(), y + widget.getHeight());
			//setPositionChild(widget, this.getWidth()/2 - retval, widget.getTop());
		}

	}

	public void alignY(){
		int retval = 0;
		for(int i=0; i<this.getChildCount(); i++){
			View widget = getChildAt(i);
			if(HububWidgett.isHububWidget(widget)){
				retval = ((HububWidgett)widget).getAlignY();			
				//GWT.log("HububWidgett: align: got a HububWidget...retval: " +retval, null);
			}
			else{
				retval = widget.getHeight()/2;
				//GWT.log("HububWidgett: align: got a Regular Widget... retval: " +retval, null);
			}
			int x = widget.getLeft();
			int y = _maxAlign = retval;
			widget.layout(x, y, x + widget.getWidth(), y + widget.getHeight());
			//this.setPositionChild(widget, widget.getLeft(), _maxAlign - retval);
		}
	}

	public void align(HububWidgett widget){ // Use to realign a single widget after dynamic change
		int x = _maxAlign - widget.getAlignX();
		int y = widget.getTop();
		widget.layout(x, y, x + widget.getWidth(), y + widget.getHeight());
		//setPositionChild(widget, _maxAlign - widget.getAlignX(), widget.getTop());

	}

	public HububWidgett setGaps(int above, int below){
		_gapAbove = above;
		_gapBelow = below;
		return this;
	}

	protected int getGapAbove(){
		return _gapAbove;
	}

	protected int getGapBelow(){
		return _gapBelow;
	}

	public void edit(){
		//Iterator<Widget> it = getChildren().iterator();
		//while(it.hasNext()){
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			//if(((Object)widget).getClass().getSuperclass() == HububWidgett.class){
			if(HububWidgett.isHububWidget(widget)){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				((HububWidgett)widget).edit(true);			
			}
		}
	}

	public void reset(){
		//Iterator<Widget> it = getChildren().iterator();
		//while(it.hasNext()){
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			//if(((Object)widget).getClass().getSuperclass() == HububWidgett.class){
			if(HububWidgett.isHububWidget(widget)){
				((HububWidgett)widget).reset();			
			}
		}
	}

	public void enableEdit(boolean edit){
		_enableEdit = edit;
	}

	public void edit(boolean edit){
		//Iterator<Widget> it = getChildren().iterator();
		//while(it.hasNext()){
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			//if(((Object)widget).getClass().getSuperclass() == HububWidgett.class){
			if(HububWidgett.isHububWidget(widget)){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				((HububWidgett)widget).edit(edit);			
			}
		}

	}

	public void cancelEdit(){
		//Iterator<Widget> it = getChildren().iterator();
		//while(it.hasNext()){
		for(int i=0; i<this.getChildCount(); i++){
			View widget = this.getChildAt(i);
			//if(((Object)widget).getClass().getSuperclass() == HububWidgett.class){
			if(HububWidgett.isHububWidget(widget)){
				//GWT.log("HububWidgett: addVertWidget: got a HububWidget...", null);
				((HububWidgett)widget).edit(false);			
			}
		}
	}

	/* HububWidgettListener Protocol */
	public void widgetHasChanged(HububWidgett widget) {
		// TODO Auto-generated method stub

	}

	protected void getMaxAlign(){
		if(_centerOn != -1){
			_maxAlign = _centerOn;
			return;
		}
		int retval = 0;
		int align = 0;
		int childCount = this.getChildCount();
		for(int i=0; i<childCount; i++){
			View view = this.getChildAt(i);
			if (view.getVisibility() != GONE) {
				if(view instanceof HububWidgett){
					align = (_isVertical)?((HububWidgett)view).getAlignX():((HububWidgett)view).getAlignY();
				}
				else{
					align = (_isVertical)?view.getMeasuredWidth()/2:view.getMeasuredHeight()/2;
				}
				retval = (align > retval)?align:retval;
			}
		}
		_maxAlign = retval;
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		//Hubub.Logger("HububWidgett: " +this +" onLayout: tag: " +getTag() +", left: " +left +", top: " +top +", right: " +right +", bottom: " +bottom
		//		+", _isVertical: " +this._isVertical +", childCount: " +this.getChildCount());
		//this.setWillNotDraw(false);
		int viewCursor = _padding;
		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				int childLeft = 0;
				int childTop = 0;
				if(child instanceof HububWidgett){
					if(_isVertical){
						childLeft = _padding + _maxAlign -((HububWidgett)child).getAlignX();
						childTop = viewCursor + ((HububWidgett)child).getGapAbove();
					}
					else{
						childTop = _padding + _maxAlign -((HububWidgett)child).getAlignY();
						childLeft = viewCursor + ((HububWidgett)child).getGapAbove();
					}
				}
				else{
					if(_isVertical){
						childLeft = _padding +_maxAlign -child.getMeasuredWidth()/2;
						childTop = viewCursor;
					}
					else{
						childTop = _padding +_maxAlign -child.getMeasuredHeight()/2;
						childLeft = viewCursor;
					}
				}

				viewCursor = ((_isVertical)?childTop +child.getMeasuredHeight():childLeft+ child.getMeasuredWidth());
				child.layout(childLeft, childTop,
						childLeft + child.getMeasuredWidth(),
						childTop + child.getMeasuredHeight());

			}
		}
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		//Hubub.Debug("2", "" +this +" onMeasure...");
		int count = getChildCount();

		int maxHeight = _padding + _topPadding;
		int maxWidth = 0;
		//int viewCursor = _padding + _topPadding;

		// Find out how big everyone wants to be
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		this.getMaxAlign();

		// Find rightmost and bottom-most child
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				int childRight;
				int childBottom;

				//AbsoluteLayout.LayoutParams lp
				//        = (AbsoluteLayout.LayoutParams) child.getLayoutParams();

				childRight = _maxAlign + ((_isVertical)?child.getMeasuredWidth():child.getMeasuredHeight());
				if(child instanceof HububWidgett){
					if(_isVertical){
						childRight = _maxAlign + child.getMeasuredWidth() -((HububWidgett)child).getAlignX() + ((HububWidgett)child).getTopPadding();
						childBottom = ((HububWidgett)child).getGapAbove() +child.getMeasuredHeight() + ((HububWidgett)child).getGapBelow();
					}
					else{
						childBottom = _maxAlign + child.getMeasuredHeight() -((HububWidgett)child).getAlignY() + ((HububWidgett)child).getTopPadding();
						childRight = ((HububWidgett)child).getGapAbove() +child.getMeasuredWidth() + ((HububWidgett)child).getGapBelow();
					}
				}
				else{
					if(_isVertical){
						childRight = _maxAlign +child.getMeasuredWidth()/2;
						childBottom = child.getMeasuredHeight();
					}
					else{
						childBottom = _maxAlign +child.getMeasuredHeight()/2;
						childRight = child.getMeasuredWidth();
					}
				}
				if(_isVertical){
					maxWidth = Math.max(maxWidth, childRight);
					maxHeight += childBottom; 
				}
				else{
					maxHeight = Math.max(maxHeight, childBottom);
					maxWidth += childRight; 
				}
			}
		}

		// Account for padding too
		maxWidth += 2*_padding;
		maxHeight += _padding;

		// Check against minimum height and width
		maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
		maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

		setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
				resolveSize(maxHeight, heightMeasureSpec));
		//Hubub.Debug("2", "this:" +this +", maxWidth: " +maxWidth +", maxHeight: " +maxHeight);
	}

	protected void onDraw(Canvas canvas) {
		//Hubub.Logger("HububWidgett: this: " +this +", onDraw: _border: " +_border +", canvas: " +canvas);
		if(_border){
			//super.onDraw(canvas);
			Paint paint = new Paint();
			paint.setColor(Color.LTGRAY);
			Paint borderPaint = new Paint();
			borderPaint.setColor(Color.BLACK);
			float left = 0;//getLeft();
			float top = 0;//getTop();
			float right = left + getMeasuredWidth();
			float bottom = top + getMeasuredHeight();
			RectF rect = new RectF(left+2, top+2, right-2, bottom-2);
			RectF borderRect = new RectF(left, top, right, bottom);
			//Hubub.Logger("HububWidgett:  borderRect: " +borderRect +", rect: " +rect);
			canvas.drawRoundRect(borderRect, 8, 8, borderPaint);

			canvas.drawRoundRect(rect, 8, 8, paint);
		}
		super.onDraw(canvas);
	}
}
