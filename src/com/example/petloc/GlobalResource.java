package com.example.petloc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.util.Log;

public class GlobalResource {

	public static final String TAG = "petloc";
	@SuppressLint("SdCardPath")
	public static final String configPath = "/data/data/com.example.petloc/files/settings.properties";
	public List<String> phoneNumberList;
	public double petLatitude;
	public double petLongitude;
	public int refreshTime;

	private volatile static GlobalResource instance_;

	private GlobalResource()
	{
		phoneNumberList = new ArrayList<String>();
//		petLatitude = 31.53893;
//		petLongitude = 104.713855;
		petLatitude = 30.66347;
    	petLongitude = 104.072295;
		refreshTime = 0;
		readConfig();
	}

	public static GlobalResource instance()
	{
		if (instance_ == null)
		{
			synchronized (GlobalResource.class)
			{
				if (instance_ == null)
				{
					instance_ = new GlobalResource();
				}
			}
		}
		return instance_;
	}

	public boolean readConfig()
	{
		this.phoneNumberList.clear();
		// 保证里面有值，以后使用时不用做判断
		this.phoneNumberList.add("");

		Properties properties = new Properties();
		try {
			File f = new File(configPath);
			if (!f.exists()) {
				f.createNewFile();
				Log.d(TAG, "config file not exist!");
				return false;
			} else {
				FileInputStream s = new FileInputStream(configPath);
				properties.load(s);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, e.toString());
			return false;
		}
		
		if ((String)properties.get("phoneNumber") != null) {
			String phoneNumberString = (String)properties.get("phoneNumber");
			Log.d(TAG, "readConfig: phoneNumber = " + phoneNumberString);
			String[] phoneNumberStrings = phoneNumberString.split(",");
			this.phoneNumberList.clear();
			for (int i = 0; i < phoneNumberStrings.length; i++) {
				phoneNumberList.add(phoneNumberStrings[i]);
			}
			if (this.phoneNumberList.size() == 0) {
				this.phoneNumberList.add("");
			}
		}
		if ((String)properties.get("petLatitude") != null) {
			this.petLatitude = Double.valueOf((String)properties.get("petLatitude"));
		}
		if ((String)properties.get("petLongitude") != null) {
			this.petLongitude = Double.valueOf((String)properties.get("petLongitude"));
		}
		if ((String)properties.get("refreshTime") != null) {
			this.refreshTime = Integer.valueOf((String)properties.get("refreshTime"));
		}
		return true;
	}

	public boolean saveConfig()
	{
		try {
			FileOutputStream s = new FileOutputStream(configPath, false);
			Properties properties = new Properties();
			File f = new File(configPath);
			if (!f.exists()) {
				f.createNewFile();
			}
			String phoneNumberString = "";
			for (int i = 0; i < phoneNumberList.size(); i++) {
				if (i > 0) {
					phoneNumberString += ",";
				}
				phoneNumberString += phoneNumberList.get(i);
			}
			Log.d(TAG, "saveConfig: phoneNumber = " + phoneNumberString);
			properties.setProperty("phoneNumber", String.valueOf(phoneNumberString));
			
			properties.setProperty("petLatitude", String.valueOf(this.petLatitude));
			properties.setProperty("petLongitude", String.valueOf(this.petLongitude));
			properties.setProperty("refreshTime", String.valueOf(this.refreshTime));
			
			properties.store(s, null);
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, e.toString());
			return false;
		}
		return true;
	}

}
