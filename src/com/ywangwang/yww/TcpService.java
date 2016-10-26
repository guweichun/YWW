package com.ywangwang.yww;

import com.ywangwang.yww.lib.CustomToast;
import com.ywangwang.yww.lib.SessionKey;
import com.ywangwang.yww.net.Heartbeat;
import com.ywangwang.yww.net.Net;
import com.ywangwang.yww.net.Operaton;
import com.ywangwang.yww.net.TcpManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class TcpService extends Service {
	private static final String TAG = "TcpService";

	private static final int TIMEOUT = -1;

	private static final int LOGIN_SUCCESS = 1;
	private static final int LOGIN_FAIL = 2;
	private static final int REGISTER_SUCCESS = 3;
	private static final int REGISTER_FAIL = 4;
	private static final int GET_DEVICE_LIST_SUCCESS = 5;
	private static final int GET_DEVICE_LIST_FAIL = 6;

	private long lastConnectTime = 0L;
	private SessionKey sessionKey = new SessionKey();
	public static CustomToast toast = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		TcpManager.getInstance(this, GlobalInfo.serverAddress);
		lastConnectTime = System.currentTimeMillis();
		handlerAutoConnect.post(runnableAutoConnect);
		registerReceiver(broadcastReceiver, new IntentFilter(GlobalInfo.BROADCAST_SERVICE_ACTION));
		toast = new CustomToast(this);
		toast.setText(null, 16f);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		handlerAutoConnect.removeCallbacksAndMessages(null);
		handler.removeCallbacksAndMessages(null);
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind()");
		return null;
	}

	Handler handlerAutoConnect = new Handler();
	Runnable runnableAutoConnect = new Runnable() {
		@Override
		public void run() {
			handlerAutoConnect.removeCallbacks(runnableAutoConnect);
			handlerAutoConnect.postDelayed(runnableAutoConnect, 2500);
			if (TcpManager.isConnect()) {
				lastConnectTime = System.currentTimeMillis();
				if (GlobalInfo.unableConnectToServer == true) {
					GlobalInfo.unableConnectToServer = false;
					sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_UPDATE_CONNECT_STATUS, true));
				}
				if (Heartbeat.checkTimeout() == Heartbeat.SEND_ACK) {
					TcpManager.sendMSG("ACK");
				} else if (Heartbeat.checkTimeout() == Heartbeat.TIMEOUT) {
					TcpManager.reconnect();
				}
			} else {
				// �Ͽ����ӳ���5����
				if ((System.currentTimeMillis() - lastConnectTime) > 5 * 1000L && GlobalInfo.unableConnectToServer == false) {
					GlobalInfo.unableConnectToServer = true;
					sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_UPDATE_CONNECT_STATUS, true));
				}
				GlobalInfo.online = false;
				if (Net.isNetworkAvailable(TcpService.this)) {
					TcpManager.connect();
				}
			}
		}
	};
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().getStringArray(GlobalInfo.BROADCAST_LOGIN) != null) {
				String username = (intent.getExtras().getStringArray(GlobalInfo.BROADCAST_LOGIN))[0];
				String password = (intent.getExtras().getStringArray(GlobalInfo.BROADCAST_LOGIN))[1];
				login(username, password);
			} else if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_LOGOUT, false) == true) {
				logout();
			} else if (intent.getExtras().getString(GlobalInfo.BROADCAST_SWITCH_SERVER) != null) {
				GlobalInfo.online = false;
				TcpManager.setServerAddress(intent.getExtras().getString(GlobalInfo.BROADCAST_SWITCH_SERVER));
				TcpManager.reconnect();
			} else if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_GET_DEVICE_LIST, false) == true) {
				Message msg2 = Message.obtain();
				msg2.arg1 = sessionKey.generateNewSessionKey();
				Operaton operaton2 = new Operaton(TcpService.this);
				int result2 = operaton2.getGxj(GlobalInfo.username, GlobalInfo.password, msg2.arg1);
				if (result2 == msg2.arg1) {
					msg2.what = GET_DEVICE_LIST_FAIL;
					msg2.arg2 = TIMEOUT;
					msg2.obj = "��ȡ��ʱ";
					handler.sendMessageDelayed(msg2, 10 * 1000L);
				} else {
					msg2.what = GET_DEVICE_LIST_FAIL;
					msg2.obj = "��ȡ��Ϣ����ʧ��";
					handler.sendMessage(msg2);
				}
			} else if (intent.getExtras().getString(GlobalInfo.BROADCAST_RECEIVE_NEW_MESSAGE, null) != null) {
				MoMessage moMsg = MoMessage.analyzeJsonData(intent.getExtras().getString(GlobalInfo.BROADCAST_RECEIVE_NEW_MESSAGE));
				if (moMsg == null) {
					return;
				}
				toast.setText(moMsg.toString()).show();
				if (moMsg.cmd == MoMessage.LOGIN_KEY_ERR) {
					Message newMsg = Message.obtain();
					newMsg.arg1 = sessionKey.getSessionKey();
					newMsg.what = MoMessage.LOGIN_KEY_ERR;
					newMsg.obj = moMsg.info;
					handler.sendMessage(newMsg);
				} else if (moMsg.sessionKey == sessionKey.getSessionKey()) {
					if (moMsg.cmd == MoMessage.LOGIN_SUCCESS) {
						User user = User.analyzeJsonData(moMsg.jsonData);
						Message newMsg = Message.obtain();
						newMsg.arg1 = moMsg.sessionKey;
						if (user != null) {
							newMsg.what = LOGIN_SUCCESS;
							GlobalInfo.id = moMsg.id;
							newMsg.obj = user;
						} else {
							newMsg.what = LOGIN_FAIL;
							newMsg.obj = "���ݽ���ʧ��";
						}
						handler.sendMessage(newMsg);
					} else if (moMsg.cmd == MoMessage.LOGIN_FAIL) {
						Message newMsg = Message.obtain();
						newMsg.arg1 = moMsg.sessionKey;
						newMsg.what = LOGIN_FAIL;
						newMsg.obj = moMsg.info;
						handler.sendMessage(newMsg);
					} else if (moMsg.cmd == MoMessage.GET_GXJ_SUCCESS) {
						Client client = Client.analyzeJsonData(moMsg.jsonData);
						Message newMsg = Message.obtain();
						newMsg.arg1 = moMsg.sessionKey;
						if (client != null) {
							newMsg.what = GET_DEVICE_LIST_SUCCESS;
							newMsg.obj = client;
						} else {
							newMsg.what = GET_DEVICE_LIST_FAIL;
							newMsg.obj = "���ݽ���ʧ��";
						}
						handler.sendMessage(newMsg);
					} else if (moMsg.cmd == MoMessage.GET_GXJ_FAIL) {
						Message newMsg = Message.obtain();
						newMsg.arg1 = moMsg.sessionKey;
						newMsg.what = GET_DEVICE_LIST_FAIL;
						newMsg.obj = moMsg.info;
						handler.sendMessage(newMsg);
					}
				}
			} else if (intent.getExtras().getBoolean(GlobalInfo.BROADCAST_CONNECT_SOCKET_SUCCESS, false) == true) {
				GlobalInfo.unableConnectToServer = false;
				sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_UPDATE_CONNECT_STATUS, true));
				lastConnectTime = System.currentTimeMillis();
				Log.d(TAG, "BROADCAST_CONNECT_SOCKET_SUCCESS");
				if (GlobalInfo.online == false && GlobalInfo.manualLogout == false && GlobalInfo.password.length() > 0 && GlobalInfo.autoLogin == true) {
					// AutoLogin
					Log.d(TAG, "AutoLogin");
					login(GlobalInfo.username, GlobalInfo.password);
				}
			}
		}
	};
	@SuppressLint({ "HandlerLeak" })
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// ����ỰKEY�����һ�β�ͬ���ͺ��Դ�msg
			if (sessionKey.getSessionKey() != msg.arg1) {
				return;
			}
			sessionKey.cleanSessionKey();
			switch (msg.what) {
			case LOGIN_SUCCESS:
				toast.setText("��¼�ɹ���\n" + (User) msg.obj).show();
				handler.removeMessages(LOGIN_FAIL);
				String[] user = { ((User) msg.obj).username, ((User) msg.obj).password };
				sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_LOGIN_SUCCESS, user));
				break;
			case LOGIN_FAIL:
				toast.setText("��½ʧ�ܣ�-->" + (String) msg.obj).show();
				sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_LOGIN_FAIL, true));
				if (msg.arg2 == TIMEOUT) {
					GlobalInfo.online = false;
					TcpManager.reconnect();
				}
				break;
			case GET_DEVICE_LIST_SUCCESS:
				toast.setText("��ȡ�豸�б�ɹ���\n" + (Client) msg.obj).show();
				GlobalInfo.client = (Client) msg.obj;
				sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_GET_DEVICE_LIST_SUCCESS, true));
				handler.removeMessages(GET_DEVICE_LIST_FAIL);
				break;
			case GET_DEVICE_LIST_FAIL:
				toast.setText("��ȡ�豸�б�ʧ�ܣ�-->" + (String) msg.obj).show();
				sendBroadcast(new Intent(GlobalInfo.BROADCAST_ACTION).putExtra(GlobalInfo.BROADCAST_GET_DEVICE_LIST_FAIL, true));
				if (msg.arg2 == TIMEOUT) {
					GlobalInfo.online = false;
					TcpManager.reconnect();
				}
				break;
			case MoMessage.LOGIN_KEY_ERR:
				// new AlertDialog.Builder(getActivity()).setTitle("��ʾ").setMessage((String) msg.obj).setPositiveButton("ȷ��", null).show();
				// logout();
				break;
			default:
				break;
			}
		}
	};

	private void login(String username, String password) {
		Message msg = Message.obtain();
		msg.arg1 = sessionKey.generateNewSessionKey();
		Operaton operaton = new Operaton(TcpService.this);
		int result = operaton.login(username, password, msg.arg1);
		if (result == msg.arg1) {
			msg.what = LOGIN_FAIL;
			msg.arg2 = TIMEOUT;
			msg.obj = "��¼��ʱ";
			handler.sendMessageDelayed(msg, 10 * 1000L);
		} else {
			msg.what = LOGIN_FAIL;
			msg.obj = "��¼��Ϣ����ʧ��";
			handler.sendMessage(msg);
		}
	}

	private void logout() {
		GlobalInfo.online = false;
		GlobalInfo.manualLogout = true;
		Operaton operaton1 = new Operaton(TcpService.this);
		operaton1.logout(sessionKey.generateNewSessionKey());
		startActivity(new Intent(TcpService.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
