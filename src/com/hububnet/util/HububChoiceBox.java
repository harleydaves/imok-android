package com.hububnet.util;

import java.util.ArrayList;

import android.widget.ArrayAdapter;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

public class HububChoiceBox extends HububWidgett implements HububSpinnerExt.Listener{
	HububSpinnerExt _spinner;
	private ArrayList<String> _names = new ArrayList<String>();
	private ArrayList<String> _tags = new ArrayList<String>();
	int _currentItem = 0;
	Listener _listener;
	
	public interface Listener{
		public void onItemSelected(int itemIndex, HububChoiceBox choiceBox);
	}
	
	public HububChoiceBox(){
		super();
		_spinner = new HububSpinnerExt();
		_spinner.setListener(this);
		this.addVertWidget(_spinner);
	}
	
	public void setListener(Listener listener){
		_listener = listener;
	}
	
	@SuppressWarnings("unchecked")
	public void addItem(String name, String tag){
		_names.add(name);
		_tags.add(tag);
		ArrayAdapter adapter = new ArrayAdapter(DroidHubub.getInstance(), android.R.layout.simple_spinner_item, _names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spinner.setAdapter(adapter);
		//_spinner.setAdapter(new ArrayAdapter(DroidHubub.getInstance(),
				//android.R.layout.simple_list_item_multiple_choice, _names) );
		//		android.R.layout.simple_spinner_item, _names) );
	}

	public String getCurrentTag(){
		return _tags.get(_currentItem);
	}
	
	public void setSelectionByTag(String tag){
		for(int i=0; i<_tags.size(); i++){
			if(_tags.get(i).equals(tag)){
				_spinner.setSelection(i);
				_currentItem = i;
				break;
			}
		}
	}

	
	/* HububSpinnerExt.Listener Protocol */
	public void onItemSelected(int itemIndex, HububSpinnerExt spinnerExt) {
		Hubub.Debug("2", "itemIndex: " +itemIndex +", tag: " +_tags.get(itemIndex) +", _currentItem: " +_currentItem);
		if(_currentItem != itemIndex){
			_currentItem = itemIndex;
			if(_listener != null){
				_listener.onItemSelected(itemIndex, this);
			}
		}
		//_currentItem = itemIndex;
		
	}


}
