package com.example.petloc;

import java.util.List;

import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {

	public static boolean send(String content) {

		/*
		 * 向指定号码发送短信
		 */
		try {
			List<String> phone_numberList;
			GlobalResource phone_Resource = GlobalResource.instance();
			phone_numberList = phone_Resource.phoneNumberList;
			// 这里做成所有号码都发送了，方便以后扩展时不用改
			for (String phone_number : phone_numberList) {
				SmsManager smsManager = SmsManager.getDefault();
				//List<String> divideContents = smsManager.divideMessage("[petloc]:" + content); 
				List<String> divideContents = smsManager.divideMessage(content);
				for (String text : divideContents) {
					smsManager.sendTextMessage(phone_number, null, text, null, null);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(GlobalResource.TAG, e.toString());
			return false;
		}
		return true;
		
	}
}
