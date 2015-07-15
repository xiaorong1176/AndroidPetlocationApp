package com.example.petloc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver{
	public final String flagString = "[petloc]:";
	
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(
    			"android.provider.Telephony.SMS_RECEIVED"));
		{
			StringBuilder sBuilder =  new StringBuilder();
			Bundle bundle =  intent.getExtras();
			Log.d(GlobalResource.TAG, "receive message");
			if (bundle != null)
			{
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
				{
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				for (SmsMessage message : messages)
    			{
    				sBuilder.append(message.getDisplayMessageBody());
    			}
				String message = sBuilder.toString();
				Log.d(GlobalResource.TAG, "receive message:" + message);
				if (message.length() >= flagString.length() && message.substring(0, flagString.length()).equals(flagString))
				{
					this.abortBroadcast();
					
					GlobalResource globalResource = GlobalResource.instance();
					// 格式为(31.53893,104.713855)
					message = message.substring(flagString.length(), message.length());
					int sta = message.indexOf("(");
					int end = message.indexOf(")", sta);
					if (sta >= 0 && end >= 0) {
						String[] posStrings = message.substring(sta + 1, end).split(",");
						if (posStrings.length == 2) {
							globalResource.petLatitude = Double.valueOf(posStrings[0]);
							globalResource.petLongitude = Double.valueOf(posStrings[1]);
							globalResource.saveConfig();
							Log.d(GlobalResource.TAG, "petLatitude:" + globalResource.petLatitude + ", petLongitude:" + globalResource.petLongitude);
						} else {
							Log.d(GlobalResource.TAG, "Error message1:" + message);
						}
					} else {
						Log.d(GlobalResource.TAG, "Error message2:" + message);
					}
					
//					//Pattern pattern = Pattern.compile("\\lan=(\\w*)");
//					lat_string = message.substring(message.indexOf("(")+1, message.lastIndexOf(")"));
//					lon_string = message.substring(message.indexOf("{")+1, message.lastIndexOf("}"));
//					sum_string = lat_string + "\n" + lon_string;
//					MainActivity.lat_message = lat_string;
//					MainActivity.lon_message = lon_string;
					//FirstActivity.messageText.setText("短信获取成功，请进行宠物定位。短信内容如下：\n" + sum_string);
					//Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}

			}		
		}
	}
}
