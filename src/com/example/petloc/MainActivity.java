package com.example.petloc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.czm1989.widget.CZMPopupMenu;
import com.example.petloc.R;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements
		OnGetGeoCoderResultListener {
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	BaiduMap mBaiduMap = null;
	MapView mMapView = null;
	CZMPopupMenu menu;
	double lastPetLatitude;
	double lastPetLongitude;
	MyHandler handler;
	RefreshThread refreshThread;
	Button sendButton;
	private ProgressDialog progressDialog = null;
	private ProgressDialog mProgressDialog = null;
	long progressTime;
	
	protected static final int refresh_map = 0x101;
	final Handler handler2 = new Handler();
	
	private MyRunnable myRunnable = new MyRunnable();
	class MyRunnable implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			SMSSender.send("DW");
			handler2.postDelayed(myRunnable, 60000);
	}
	}
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext （setContentView方法之前实现  ）
		//SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		CharSequence titleLable = "定位界面";
		setTitle(titleLable);
		 
		
		GlobalResource.instance();

		handler = new MyHandler();
		
		
		((TextView)findViewById(R.id.top_text_view)).setText(titleLable);
		findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
		registerForContextMenu(findViewById(R.id.function_button));
		findViewById(R.id.function_button).setOnClickListener(new Button.OnClickListener()
		{

			@Override
			public void onClick(View v) {
				//openContextMenu(v);
				if (menu == null)
		        {
					menu = MainActivity.this.createPopupMenu(MainActivity.this);
				}
				MainActivity.this.showPopupMenu(MainActivity.this.getWindow().getDecorView(), menu);
			}
			
		});
		
		findViewById(R.id.send_message).setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v) {
				if(GlobalResource.instance().phoneNumberList.get(0).equals("")){
					Toast.makeText(MainActivity.this, "请您先设置初始号码", Toast.LENGTH_LONG).show();		
				}else{
				//SMSSender.send("DW");		
				handler2.postDelayed(myRunnable, 60000);
				Toast.makeText(MainActivity.this, "位置搜索中.....", Toast.LENGTH_LONG).show();
				//progressDialog = ProgressDialog.show(MainActivity.this, null , "正在获取宠物位置信息,请稍候......"); 
				ProgressDialog mProgressDialog=new ProgressDialog(MainActivity.this); 
				mProgressDialog.setMessage("正在获取位置信息"); 
				mProgressDialog.show();
				}
			}
		});
		
/*		try {
			mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {          
	            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	                // TODO Auto-generated method stub
	                // Cancel task.
	                if (keyCode == KeyEvent.KEYCODE_BACK) {
	        			try {
	        				if(mProgressDialog != null){				
	        					mProgressDialog.dismiss(); //关闭进度条
	        					mProgressDialog = null;					
	        				}
	        			} catch (Exception e) {
	        				// TODO: handle exception
	        				Log.d("progressDialog2" , "progressDialog error:" + e.toString());
	        			}
	                }
	                return false;
	            }
	        });
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("progressDialog222" , "progressDialog error:" + e.toString());
		}*/
		
				
		// 地图初始化	
		initView();
//		mMapView = (MapView) findViewById(R.id.bmapView);
//		mBaiduMap = mMapView.getMap();
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);		
		lastPetLatitude = 0;
		lastPetLongitude = 0;

		Log.d(GlobalResource.TAG, "initial position:(" + lastPetLatitude + "," + lastPetLongitude + ")");
		LatLng ptCenter = new LatLng(lastPetLatitude, lastPetLongitude);
		// 反Geo搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption()
				.location(ptCenter));

		refreshThread = new RefreshThread();
		refreshThread.start();

		
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		mSearch.destroy();
		refreshThread.close();
		super.onDestroy();
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mBaiduMap.clear();
		mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));
		if(result.getAddress().equals("")){		
		}else{
		Toast.makeText(MainActivity.this, result.getAddress(),
				Toast.LENGTH_LONG).show();
		}
		//给出具体位置信息提示
		TextView address = (TextView)findViewById(R.id.address);
		address.setBackgroundColor(Color.argb(100, 100, 255, 100));
		address.setText("位置坐标为:" + result.getAddress());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		dealSelectBottomMenu(this, item);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
		if (this.menu == null)
        {
			this.menu = createPopupMenu(this);
		}
        showPopupMenu(this.getWindow().getDecorView(), this.menu);
        return false;
    }

	public CZMPopupMenu createPopupMenu(final Activity activity)
	{
		final CZMPopupMenu menu = new CZMPopupMenu(activity);
		List<Map<String, Object>> menu_items = new ArrayList<Map<String, Object>>();
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("item_name", "寻找位置");
			menu_items.add(map);
		}
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("item_name", "设置号码");
			menu_items.add(map);
		}
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("item_name", "设置刷新时间");
			menu_items.add(map);
		}
		menu.setAdapterAndListener((int)activity.getResources().getDimension(R.dimen.menu_item_width),
				new SimpleAdapter(activity, menu_items, R.layout.popup_menu_item, new String[]{"item_name"}, new int[]{R.id.item_name}),
				new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				// TODO Auto-generated method stub
				menu.dismiss();
				switch (index)
				{
				case 0:
				{
					// 处理 寻找我的宠物 消息
						if(GlobalResource.instance().phoneNumberList.get(0).equals("")){
							Toast.makeText(MainActivity.this, "请您先设置初始号码", Toast.LENGTH_LONG).show();
						}else{
						//SMSSender.send("DW");
						//handler2.postDelayed(myRunnable, 60000);
						Toast.makeText(MainActivity.this, "位置搜索中.....", Toast.LENGTH_LONG).show();
						}
				}
					break;
					
				case 1:
				{
					// 处理 设置号码 消息
					Utils.showPhoneSettingDlg(MainActivity.this);
				}
					break;
					
				case 2:
				{
					// 处理 设置刷新时间 消息
					Utils.showTimeSettingDlg(MainActivity.this);
				}
					break;
				}
			}
			
		});
		return menu;
	}

	public void dealSelectBottomMenu(final Activity activity, MenuItem item)
	{
		int item_id = item.getItemId();
		switch (item_id)
		{
		case R.id.action_find_pet:
		{
			// 处理 寻找我的宠物 消息
			if(GlobalResource.instance().phoneNumberList.get(0).equals("")){
				Toast.makeText(MainActivity.this, "请您先设置初始号码", Toast.LENGTH_LONG).show();
			}else{
			SMSSender.send("DW");
			Toast.makeText(MainActivity.this, "位置搜索中......", Toast.LENGTH_LONG).show();
			}
		}
			break;
			
		case R.id.action_set_phone:
		{
			// 处理 设置号码 消息
			Utils.showPhoneSettingDlg(this);
		}
			break;
			
		case R.id.action_set_time:
		{
			// 处理 设置刷新时间 消息
			Utils.showTimeSettingDlg(this);
		}
			break;
		}
	}
	
	public void showPopupMenu(View view, CZMPopupMenu menu)
	{
		if (menu == null) return;
        if (menu.isShowing())
        {
        	menu.dismiss();
        }
        else
        {
        	menu.showAtLocation(view, Gravity.CENTER_VERTICAL, 0, (int)view.getResources().getDimension(R.dimen.menu_adjust_y));
        }
	}
	
	class RefreshThread extends Thread {
		
		private boolean isClosed = false;
		
		public void run() {
			while (!isClosed)
			{
				MainActivity.this.checkPosition();
				try {
					for (int i = 0; i < 1; i++)
					{
						sleep(1000);
						if (isClosed) 
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void close()
		{
			isClosed = true;
		}
		
	}

	@SuppressLint("HandlerLeak")
	public class MyHandler extends Handler {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message arg0) {
			// TODO Auto-generated method stub
			super.handleMessage(arg0);
			switch (arg0.what) {
			case MainActivity.refresh_map:
			{
				List<Object> params = (List<Object>)arg0.obj;
				Double latitude = (Double)params.get(0);
				Double longitude = (Double)params.get(1);
				MainActivity.this.refreshMap(latitude, longitude);
			}
			try {
				if(mProgressDialog != null){				
					mProgressDialog.dismiss(); //关闭进度条
					mProgressDialog = null;					
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("progressDialog" , "progressDialog error:" + e.toString());
			}
				break;
			}
		}
				
	}
	

	
	private void checkPosition() {
		if (this.lastPetLatitude != GlobalResource.instance().petLatitude
				|| this.lastPetLongitude != GlobalResource.instance().petLongitude) {
			Log.d(GlobalResource.TAG, "position changed");

			try {
				List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
				StringBuilder paramStringBuilder = new StringBuilder();
				paramsList.add(new BasicNameValuePair("coords", "" + GlobalResource.instance().petLongitude + "," + GlobalResource.instance().petLatitude));
				paramsList.add(new BasicNameValuePair("from", "1"));
				paramsList.add(new BasicNameValuePair("to", "5"));
				paramsList.add(new BasicNameValuePair("ak", "49000baf3ed42b909ee3b977e805f5c9"));
				HttpEntity entity = new UrlEncodedFormEntity(paramsList, HTTP.UTF_8);
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					paramStringBuilder.append(line);
				}
				
				URL url = new URL("http://api.map.baidu.com/geoconv/v1/?" + paramStringBuilder.toString());
				HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
				InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
				BufferedReader buffer = new BufferedReader(in);
				String resultData = "";
				String inputLine = null;
				while ((inputLine = buffer.readLine()) != null) {
					resultData += inputLine + "\n";
				}
				Log.d(GlobalResource.TAG, resultData);
				if (resultData != null && !resultData.equals("")) {
					JSONObject response = new JSONObject(new JSONTokener(resultData));
					if (response.optInt("status") == 0
							&& response.has("result")) {
						JSONArray arr = response.optJSONArray("result");
						if (arr.length() > 0)
						{
							JSONObject position = (JSONObject)arr.get(0);
							double latitude = position.optDouble("y");
							double longitude = position.getDouble("x");
							Log.d(GlobalResource.TAG, "(" + GlobalResource.instance().petLatitude + ", " + GlobalResource.instance().petLongitude + ") -> (" + latitude + ", " + longitude + ")");
							List<Object> params = new ArrayList<Object>();
							params.add(latitude);
							params.add(longitude);
							
							lastPetLatitude = GlobalResource.instance().petLatitude;
							lastPetLongitude = GlobalResource.instance().petLongitude;
							handler.obtainMessage(MainActivity.refresh_map, 0, 0, params).sendToTarget();
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(MainActivity.this, "坐标转换失败", 1000).show();
			}
		}
	}

	@SuppressLint("ShowToast")
	private void refreshMap(double latitude, double longitude) {

		Log.d(GlobalResource.TAG, "refresh map");
		LatLng ptCenter = new LatLng(latitude, longitude);
		// 反Go搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption()
				.location(ptCenter));
		mMapView.invalidate();
		
	}
	
	private void initView()
	{
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
	}
	
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   

	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}

	
	
}

