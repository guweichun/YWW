package com.ywangwang.yww;

import java.util.ArrayList;

import com.ywangwang.yww.lib.CustomToast;
import com.ywangwang.yww.lib.SharedPreferencesConfig;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	final String TAG = "MainActivity";

	private TextView tvConnectStatus;
	private ArrayList<Fragment> fragmentArrayList;

	private Button[] btnTabs;
	private int currentTabIndex = 0;
	public static CustomToast toast = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initFragment();
		registerReceiver(broadcastReceiver, new IntentFilter(GlobalInfo.BROADCAST_ACTION));
		SharedPreferencesConfig.read(this);
		startService(new Intent(this, TcpService.class));
		if ((GlobalInfo.password.length() == 0 || GlobalInfo.autoLogin == false) && ConnectionHelper.getLoginStatus() != ConnectionHelper.LOGIN_SUCCESS) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}
		toast = new CustomToast(this);
		toast.setText(null, 16f);
	}

	private void initView() {
		tvConnectStatus = (TextView) findViewById(R.id.tvConnectStatus);
		btnTabs = new Button[4];
		btnTabs[0] = (Button) findViewById(R.id.btnUserList);
		btnTabs[1] = (Button) findViewById(R.id.btnDevice);
		btnTabs[2] = (Button) findViewById(R.id.btnShopping);
		btnTabs[3] = (Button) findViewById(R.id.btnSettings);
		for (Button btn : btnTabs) {
			btn.setOnClickListener(clickListener);
		}
		// btnTabs[0].setSelected(true);
	}

	private void initFragment() {
		fragmentArrayList = new ArrayList<Fragment>();
		fragmentArrayList.add(new UserListFragment());
		fragmentArrayList.add(new DeviceFragment());
		fragmentArrayList.add(new ShoppingFragment());
		fragmentArrayList.add(new SettingsFragment());
	}

	private void changeTab(int index) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// // �жϵ�ǰ��Fragment�Ƿ�Ϊ�գ���Ϊ��������
		// if (null != mCurrentFrgment) {
		// ft.hide(mCurrentFrgment);
		// }
		// ��������Fragment(�����Ƕδ���ֻ���ص�ǰ�ģ��ں������л���ʱ�����е�Fragment������ʾ�������������ʾ����)
		for (int i = 0; i < fragmentArrayList.size(); i++) {
			if (i == index) {
				continue;
			}
			Fragment mFragment = getFragmentManager().findFragmentByTag(fragmentArrayList.get(i).getClass().getName());
			if (null != mFragment) {
				ft.hide(mFragment);
			}
			btnTabs[i].setSelected(false);
		}
		// �ȸ���Tag��FragmentTransaction�����ȡ֮ǰ��ӵ�Fragment
		Fragment fragment = getFragmentManager().findFragmentByTag(fragmentArrayList.get(index).getClass().getName());
		if (null == fragment) {
			// ��fragmentΪ�գ���֮ǰδ��Ӵ�Fragment����Ӽ�����ȡ��
			fragment = fragmentArrayList.get(index);
		}
		// mCurrentFrgment = fragment;
		// �жϴ�Fragment�Ƿ��Ѿ���ӵ�FragmentTransaction������
		if (!fragment.isAdded()) {
			ft.add(R.id.fragmentContainer, fragment, fragment.getClass().getName());
		} else {
			ft.show(fragment);
		}
		ft.commit();
		btnTabs[index].setSelected(true);
		currentTabIndex = index;
	}

	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnUserList:
				changeTab(0);
				break;
			case R.id.btnDevice:
				changeTab(1);
				break;
			case R.id.btnShopping:
				changeTab(2);
				break;
			case R.id.btnSettings:
				changeTab(3);
				break;
			default:
				break;
			}
		}
	};
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_UPDATE_CONNECT_STATUS, false) == true) {
				if (ConnectionHelper.getConnectionStatus() == ConnectionHelper.CONNECT_FAIL) {
					tvConnectStatus.setVisibility(View.VISIBLE);
				} else {
					tvConnectStatus.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		// if (GlobalInfo.manualLogout == true || GlobalInfo.password.length() == 0) {
		// startService(new Intent(this, TcpService.class));
		// }
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
		super.onNewIntent(intent);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onPostCreate");
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onPostResume() {
		Log.d(TAG, "onPostResume");
		super.onPostResume();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		if (ConnectionHelper.getConnectionStatus() == ConnectionHelper.CONNECT_FAIL) {
			tvConnectStatus.setVisibility(View.VISIBLE);
		} else {
			tvConnectStatus.setVisibility(View.GONE);
		}
		changeTab(currentTabIndex);
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		outState.putInt("tabNum", currentTabIndex);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState");
		if (savedInstanceState != null) {
			currentTabIndex = savedInstanceState.getInt("tabNum", 0);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		// ���ϡ�moveTaskToBack(false);������ע�͵���super.onBackPressed();�����ڰ����ؼ���ʹ�ø�Activity��������
		moveTaskToBack(true);
		// super.onBackPressed();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
}
