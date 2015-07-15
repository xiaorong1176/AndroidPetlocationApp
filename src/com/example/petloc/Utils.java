package com.example.petloc;

import com.example.petloc.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Utils {
	
	public static void showPhoneSettingDlg(final Activity activity)
	{
		LayoutInflater factory = LayoutInflater.from(activity);
		//得到自定义对话框
        final View dialog_view = factory.inflate(R.layout.activity_phone_setting, null);
        ((EditText)dialog_view.findViewById(R.id.phoneNumber)).setText(GlobalResource.instance().phoneNumberList.get(0));
        //创建对话框
        AlertDialog dlg = new AlertDialog.Builder(activity)
        .setTitle("设置")
        .setView(dialog_view)//设置自定义对话框的样式
        .setPositiveButton("确定", //设置"确定"按钮
        new DialogInterface.OnClickListener() //设置事件监听
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
            	EditText phoneNumberEditText = (EditText)dialog_view.findViewById(R.id.phoneNumber);
            	GlobalResource.instance().phoneNumberList.set(0, phoneNumberEditText.getText().toString());
            	GlobalResource.instance().saveConfig();
            }
        })
        .setNegativeButton("取消", //设置“取消”按钮
        new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
            	//点击"取消"按钮之后
            }
        })
        .create();//创建
        dlg.show();//显示
	}
	
	/*
	 * 发送短信设置及制定协议：点击确定后短信自动发出，点击取消不发送
	 */
	public static void showTimeSettingDlg(final Activity activity){
		LayoutInflater factory = LayoutInflater.from(activity);
		final View dialog_view = factory.inflate(R.layout.activity_time_setting, null);
		
		switch (GlobalResource.instance().refreshTime) {
		case 10:
			((RadioButton)dialog_view.findViewById(R.id.ten_seconds)).setChecked(true);
			break;

		case 30:
			((RadioButton)dialog_view.findViewById(R.id.thirty_seconds)).setChecked(true);
			break;

		case 60:
			((RadioButton)dialog_view.findViewById(R.id.one_minute)).setChecked(true);
			break;

		default:
			((RadioButton)dialog_view.findViewById(R.id.one_time)).setChecked(true);
			break;
		}
		
		//创建对话框
		AlertDialog dlg = new AlertDialog.Builder(activity)
		.setTitle("选择时间")
		.setView(dialog_view)//设置自定义对话框的样式
		.setPositiveButton("确定", //设置"确定"按钮
		new DialogInterface.OnClickListener(){ //设置事件监听
			public void onClick(DialogInterface dialog, int whichButton)
			{
				//判断发送定位请求的时间，点击确定按钮后发送短信
				RadioGroup timeGroup = (RadioGroup)dialog_view.findViewById(R.id.setting_time);
				int selectId = timeGroup.getCheckedRadioButtonId();
				int refreshTime = 0;
				switch (selectId) {
				case R.id.ten_seconds:
					refreshTime = 10;
					break;
				case R.id.thirty_seconds:
					refreshTime = 30;
					break;
				case R.id.one_minute:
					refreshTime = 60;
					break;
				case R.id.one_time:
					refreshTime = 0;
					break;
				}
				
				// 协议先定成简单地发个数字，发多了它还得做字符串匹配，麻烦；
				// 该数字直接表示几秒发一次，
				// 如果是0，则表示只发一次后不再发送；
				// 如果是-1，则表示不修改当前刷新时间，但是需要马上给我发一次。
				Log.d(GlobalResource.TAG, "refreshTime = " + refreshTime);
				GlobalResource.instance().refreshTime = refreshTime;
				GlobalResource.instance().saveConfig();
				SMSSender.send("" + refreshTime);
			}
			})
		.setNegativeButton("取消", //设置“取消”按钮
        new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
            	//点击"取消"按钮之后不进行定位
            	
            }
        })
			.create();//创建
		dlg.show();//显示
		}
	
}
