package com.czm1989.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

public class CZMPopupMenu extends PopupWindow {

	public CZMPopupMenu(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CZMPopupMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CZMPopupMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public void setAdapterAndListener(int width,
			SimpleAdapter simpleAdapter,
			OnItemClickListener onItemClickListener) {
		// TODO Auto-generated method stub
		layout.setOrientation(LinearLayout.VERTICAL);
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		list.setOnItemClickListener(onItemClickListener);
		//list_.setSelector(new ColorDrawable(Color.TRANSPARENT));
		list.setAdapter(simpleAdapter);
		layout.addView(list);
		
		setContentView(layout);
		setWidth(width);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		
		layout.setFocusableInTouchMode(true);
		layout.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_MENU && isShowing()){
					dismiss();
					return true;
				}
				return false;
			}
		});
	}
	
	protected void init(Context context)
	{
		layout = new LinearLayout(context);
		list = new ListView(context);
	}

	protected ListView list = null;
	protected LinearLayout layout = null;
}
