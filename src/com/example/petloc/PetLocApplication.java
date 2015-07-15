package com.example.petloc;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class PetLocApplication extends Application {
	public void onCreate(){
		super.onCreate();
		/*
		 * 注意：在SDK各功能组件使用之前都需要调用
         *  SDKInitializer.initialize(getApplicationContext());
         * 因此将该方法放在Application的初始化方法中
		 */
		SDKInitializer.initialize(this);
	}
}
