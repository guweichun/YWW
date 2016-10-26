package com.ywangwang.yww;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ywangwang.yww.lib.CustomToast;
import com.ywangwang.yww.lib.SessionKey;
import com.ywangwang.yww.lib.SharedPreferencesConfig;
import com.ywangwang.yww.net.Heartbeat;
import com.ywangwang.yww.net.Net;
import com.ywangwang.yww.net.Operaton;
import com.ywangwang.yww.net.TcpManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class UserInfoFragment extends Fragment {
	private static final String TAG = "UserInfoFragment";
	public static final String BROADCAST_USER_INFO_ACTION = "com.ywangwang.yww.MainActivity.UserInfoFragment.broadcast";
	public static final String BROADCAST_RECEIVE_NEW_MESSAGE = "RECEIVE_NEW_MESSAGE";
	public static final String BROADCAST_CONNECT_SOCKET_SUCCESS = "CONNECT_SOCKET_SUCCESS";

	public static final int TIMEOUT = -1;

	public static final int LAYOUT_LOGIN = 0;
	public static final int LAYOUT_REGISTER = 1;
	public static final int LAYOUT_USER_INFO = 2;

	private static final int LOGIN_SUCCESS = 1;
	private static final int LOGIN_FAIL = 2;
	private static final int REGISTER_SUCCESS = 3;
	private static final int REGISTER_FAIL = 4;
	private static final int GET_GXJ_SUCCESS = 5;
	private static final int GET_GXJ_FAIL = 6;

	private CustomToast toast;
	private boolean gotoWaterCodeFragment = false;
	private boolean manualLogout = false;
	SessionKey sessionKey = new SessionKey();
	private long lastConnectTime = 0L;

	private RelativeLayout layoutLogin, layoutRegister, layoutUserInfo;
	private EditText edtTxtUsernameLogin, edtTxtPasswordLogin, edtTxtUsernameRegister, edtTxtPasswordRegister;
	private CheckBox chkBoxSavePassword, chkBoxAutoLogin;
	private Button btnLogin, btnRegister, btnGetGxj;// , btnLogout;
	private TextView tvConnectStatus;

	private ListView lvDevice;
	private List<Map<String, Object>> listDevice;
	private SimpleAdapter saDevice;
	private Client gotClient = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, TAG + "-->>onCreate");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSaveInstanceState");
		outState.putSerializable("gotClient", gotClient);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, TAG + "-->>onCreateView");
		SharedPreferencesConfig.read(getActivity());
		TcpManager.getInstance(getActivity(), GlobalInfo.serverAddress);

		View view = inflater.inflate(R.layout.fragment_user_info, container, false);

		tvConnectStatus = (TextView) view.findViewById(R.id.tvConnectStatus);

		layoutLogin = (RelativeLayout) view.findViewById(R.id.includeLogin);
		layoutRegister = (RelativeLayout) view.findViewById(R.id.includeRegister);
		layoutUserInfo = (RelativeLayout) view.findViewById(R.id.includeUserInfo);

		chkBoxSavePassword = (CheckBox) layoutLogin.findViewById(R.id.chkBoxSavePassword);
		chkBoxAutoLogin = (CheckBox) layoutLogin.findViewById(R.id.chkBoxAutoLogin);
		chkBoxSavePassword.setChecked(GlobalInfo.savePassword);
		chkBoxAutoLogin.setChecked(GlobalInfo.autoLogin);
		chkBoxSavePassword.setOnClickListener(ButtonListener);
		chkBoxAutoLogin.setOnClickListener(ButtonListener);
		edtTxtUsernameLogin = (EditText) layoutLogin.findViewById(R.id.edtTxtUsername);
		edtTxtPasswordLogin = (EditText) layoutLogin.findViewById(R.id.edtTxtPassword);
		btnLogin = (Button) layoutLogin.findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(ButtonListener);
		btnLogin.setText("��¼");
		layoutLogin.findViewById(R.id.tvChangeToRegister).setOnClickListener(ButtonListener);
		layoutLogin.findViewById(R.id.rdoBtnLocal).setOnClickListener(ButtonListener);
		layoutLogin.findViewById(R.id.rdoBtnRemote).setOnClickListener(ButtonListener);
		if (GlobalInfo.serverAddress.equals("www.ywangwang.com")) {
			((RadioButton) layoutLogin.findViewById(R.id.rdoBtnRemote)).setChecked(true);
		}

		edtTxtUsernameRegister = (EditText) layoutRegister.findViewById(R.id.edtTxtUsername);
		edtTxtPasswordRegister = (EditText) layoutRegister.findViewById(R.id.edtTxtPassword);
		layoutRegister.findViewById(R.id.tvChangeToLogin).setOnClickListener(ButtonListener);
		btnRegister = (Button) layoutRegister.findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(ButtonListener);
		btnRegister.setText("ע��");

		layoutUserInfo.findViewById(R.id.btnLogout).setOnClickListener(ButtonListener);
		btnGetGxj = (Button) layoutUserInfo.findViewById(R.id.btnGetGxj);
		layoutUserInfo.findViewById(R.id.btnGetGxj).setOnClickListener(ButtonListener);
		((TextView) layoutUserInfo.findViewById(R.id.tvInfo)).setText("�豸�б�");

		TextView tvChangeLogin = (TextView) layoutLogin.findViewById(R.id.tvChangeToRegister);
		tvChangeLogin.setText("ע�����û�");
		tvChangeLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // �»���
		tvChangeLogin.getPaint().setAntiAlias(true);// �����
		TextView tvChangeRegister = (TextView) layoutRegister.findViewById(R.id.tvChangeToLogin);
		tvChangeRegister.setText("��ע���û���˵�½");
		tvChangeRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // �»���
		tvChangeRegister.getPaint().setAntiAlias(true);// �����

		lvDevice = (ListView) view.findViewById(R.id.lvDevice);
		// lvDevice.setVisibility(View.GONE);
		listDevice = new ArrayList<Map<String, Object>>();
		saDevice = new SimpleAdapter(getActivity(), listDevice, R.layout.device_list_item, new String[] { "tvDeviceInfo", "tvDeviceStatus" }, new int[] { R.id.tvDeviceInfo, R.id.tvDeviceStatus });
		lvDevice.setAdapter(saDevice);
		lvDevice.setOnItemClickListener(itemClickListener);
		if (null != savedInstanceState) {
			gotClient = (Client) savedInstanceState.getSerializable("gotClient");
		}

		int showLayout = -1;
		Bundle bundle = getArguments();
		if (bundle != null) {
			showLayout = bundle.getInt("show", LAYOUT_LOGIN);
			gotoWaterCodeFragment = bundle.getBoolean("getWaterCode", false);
		}
		if (showLayout == -1) {
			switchLayout(LAYOUT_USER_INFO);
		} else {
			switchLayout(showLayout);
		}
		toast = new CustomToast(getActivity());
		toast.setText(null, 16f);

		handlerAutoConnect.post(runnableAutoConnect);
		getActivity().registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_USER_INFO_ACTION));
		return view;
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getExtras().getString(BROADCAST_RECEIVE_NEW_MESSAGE, null) != null) {
				MoMessage moMsg = MoMessage.analyzeJsonData(intent.getExtras().getString(BROADCAST_RECEIVE_NEW_MESSAGE));
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
							newMsg.what = GET_GXJ_SUCCESS;
							newMsg.obj = client;
						} else {
							newMsg.what = GET_GXJ_FAIL;
							newMsg.obj = "���ݽ���ʧ��";
						}
						handler.sendMessage(newMsg);
					} else if (moMsg.cmd == MoMessage.GET_GXJ_FAIL) {
						Message newMsg = Message.obtain();
						newMsg.arg1 = moMsg.sessionKey;
						newMsg.what = GET_GXJ_FAIL;
						newMsg.obj = moMsg.info;
						handler.sendMessage(newMsg);
					}
				}
			} else if (intent.getExtras().getBoolean(BROADCAST_CONNECT_SOCKET_SUCCESS, false) == true) {
				tvConnectStatus.setVisibility(View.GONE);
				lastConnectTime = System.currentTimeMillis();
				Log.d(TAG, "BROADCAST_CONNECT_SOCKET_SUCCESS");
				if (GlobalInfo.online == false && manualLogout == false && GlobalInfo.password.length() > 0 && GlobalInfo.autoLogin == true) {
					// AutoLogin
					Log.d(TAG, "AutoLogin");
					login(GlobalInfo.username, GlobalInfo.password);
				}
			}
		}
	};
	OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			if (gotClient != null) {
				if (gotClient.gxjOnline[position] == Client.OFFLINE) {
					String msg = "�豸���ߣ��޷�Զ�̿��ƣ�\n�豸��ţ�" + String.format("%012X", gotClient.gxjIds[position]);
					AlertDialog.Builder builer = new Builder(getActivity());
					builer.setTitle("�豸��Ϣ");
					builer.setMessage(msg);
					builer.setPositiveButton("ȷ��", null);
					builer.show();
				} else {
					startActivity(new Intent(getActivity(), DeviceControl.class).putExtra("deviceId", gotClient.gxjIds[position]));
				}
			}
		}
	};

	void addWaterCodes(Client client) {
		listDevice.clear();
		gotClient = client;
		if (client != null && client.gxjIds.length > 0) {
			for (int i = 0; i < client.gxjIds.length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("tvDeviceInfo", "���߻�(" + String.format("%012X", client.gxjIds[i]) + ")");
				if (client.gxjOnline[i] == Client.ONLINE) {
					map.put("tvDeviceStatus", "����");
				} else {
					map.put("tvDeviceStatus", "����");
				}
				listDevice.add(map);
			}
		}
		saDevice.notifyDataSetChanged();
	}

	Handler handlerAutoConnect = new Handler();
	Runnable runnableAutoConnect = new Runnable() {
		@Override
		public void run() {
			handlerAutoConnect.removeCallbacks(runnableAutoConnect);
			handlerAutoConnect.postDelayed(runnableAutoConnect, 2500);
			if (TcpManager.isConnect()) {
				tvConnectStatus.setVisibility(View.GONE);
				lastConnectTime = System.currentTimeMillis();
				if (Heartbeat.checkTimeout() == Heartbeat.SEND_ACK) {
					TcpManager.sendMSG("ACK");
				} else if (Heartbeat.checkTimeout() == Heartbeat.TIMEOUT) {
					TcpManager.reconnect();
				}
			} else {
				// �Ͽ����ӳ���5����
				if ((System.currentTimeMillis() - lastConnectTime) > 5 * 1000L) {
					tvConnectStatus.setText("�޷����ӵ�������");
					tvConnectStatus.setVisibility(View.VISIBLE);
				}
				GlobalInfo.online = false;
				if (Net.isNetworkAvailable(getActivity())) {
					TcpManager.connect();
				}
			}
		}
	};

	private void switchLayout(int layout) {
		switch (layout) {
		case LAYOUT_LOGIN:
			layoutLogin.setVisibility(View.VISIBLE);
			layoutRegister.setVisibility(View.GONE);
			layoutUserInfo.setVisibility(View.GONE);
			edtTxtUsernameLogin.setText(GlobalInfo.username);
			if (chkBoxSavePassword.isChecked() == true) {
				edtTxtPasswordLogin.setText(GlobalInfo.password);
			} else {
				edtTxtPasswordLogin.setText("");
			}
			break;
		case LAYOUT_REGISTER:
			layoutLogin.setVisibility(View.GONE);
			layoutRegister.setVisibility(View.VISIBLE);
			layoutUserInfo.setVisibility(View.GONE);
			break;
		case LAYOUT_USER_INFO:
			if (GlobalInfo.online == true) {
				layoutLogin.setVisibility(View.GONE);
				layoutRegister.setVisibility(View.GONE);
				addWaterCodes(gotClient);
				layoutUserInfo.setVisibility(View.VISIBLE);
			} else {
				layoutLogin.setVisibility(View.VISIBLE);
				layoutRegister.setVisibility(View.GONE);
				layoutUserInfo.setVisibility(View.GONE);
				edtTxtUsernameLogin.setText(GlobalInfo.username);
				if (chkBoxSavePassword.isChecked() == true) {
					edtTxtPasswordLogin.setText(GlobalInfo.password);
				} else {
					edtTxtPasswordLogin.setText("");
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, TAG + "-->>onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, TAG + "-->>onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, TAG + "-->>onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, TAG + "-->>onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, TAG + "-->>onDestroyView");
	}

	@Override
	public void onDestroy() {
		handlerAutoConnect.removeCallbacksAndMessages(null);
		handler.removeCallbacksAndMessages(null);
		sessionKey.cleanSessionKey();
		getActivity().unregisterReceiver(broadcastReceiver);
		super.onDestroy();
		Log.i(TAG, TAG + "-->>onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, TAG + "-->>onDetach");
	}

	OnClickListener ButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnLogin:
				if (TcpManager.isConnect() == false)
					return;
				final String username = edtTxtUsernameLogin.getText().toString().trim();
				final String password = edtTxtPasswordLogin.getText().toString().trim();
				if (username == null || username.length() <= 0) {
					edtTxtUsernameLogin.requestFocus();
					edtTxtUsernameLogin.setError("�û�������Ϊ�գ�");
					return;
				}
				if (password == null || password.length() <= 0) {
					edtTxtPasswordLogin.requestFocus();
					edtTxtPasswordLogin.setError("���벻��Ϊ�գ�");
					return;
				}
				btnLogin.setText("���ڵ�¼...");
				btnLogin.setEnabled(false);
				login(username, password);
				break;
			case R.id.btnRegister:
				toast.setText("�ݲ�֧��ע��").show();
				break;
			case R.id.tvChangeToRegister:
				switchLayout(LAYOUT_REGISTER);
				break;
			case R.id.tvChangeToLogin:
				switchLayout(LAYOUT_LOGIN);
				break;
			case R.id.btnLogout:
				logout();
				break;
			case R.id.btnGetGxj:
				addWaterCodes(null);
				btnGetGxj.setText("���ڻ�ȡ...");
				btnGetGxj.setEnabled(false);
				Message msg2 = Message.obtain();
				msg2.arg1 = sessionKey.generateNewSessionKey();
				Operaton operaton2 = new Operaton(getActivity());
				int result2 = operaton2.getGxj(GlobalInfo.username, GlobalInfo.password, msg2.arg1);
				Log.d(TAG, "login-->" + result2);
				if (result2 == msg2.arg1) {
					msg2.what = GET_GXJ_FAIL;
					msg2.arg2 = TIMEOUT;
					msg2.obj = "��ȡ��ʱ";
					handler.sendMessageDelayed(msg2, 10 * 1000L);
				} else {
					msg2.what = GET_GXJ_FAIL;
					msg2.obj = "��ȡ��Ϣ����ʧ��";
					handler.sendMessage(msg2);
				}
				break;
			case R.id.chkBoxSavePassword:
				chkBoxAutoLogin.setChecked(false);
				break;
			case R.id.chkBoxAutoLogin:
				chkBoxSavePassword.setChecked(true);
				break;
			case R.id.rdoBtnLocal:
				switchServer((String) ((RadioButton) v).getText());
				break;
			case R.id.rdoBtnRemote:
				switchServer((String) ((RadioButton) v).getText());
				break;
			default:
				break;
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
			btnLogin.setText("��¼");
			btnLogin.setEnabled(true);
			btnRegister.setText("ע��");
			btnRegister.setEnabled(true);
			btnGetGxj.setText("��ȡ�豸�б�");
			btnGetGxj.setEnabled(true);
			switch (msg.what) {
			case LOGIN_SUCCESS:
				toast.setText("��¼�ɹ���\n" + (User) msg.obj).show();
				loginOrRegisterSuccess((User) msg.obj);
				handler.removeMessages(LOGIN_FAIL);
				manualLogout = false;
				btnGetGxj.callOnClick();
				break;
			case LOGIN_FAIL:
				toast.setText("��½ʧ�ܣ�-->" + (String) msg.obj).show();
				if (msg.arg2 == TIMEOUT) {
					TcpManager.reconnect();
				}
				break;
			case REGISTER_SUCCESS:
				toast.setText("ע��ɹ���\n" + (User) msg.obj).show();
				loginOrRegisterSuccess((User) msg.obj);
				handler.removeMessages(REGISTER_FAIL);
				break;
			case REGISTER_FAIL:
				toast.setText("ע��ʧ�ܣ�-->" + (String) msg.obj).show();
				break;
			case GET_GXJ_SUCCESS:
				toast.setText("��ȡ�豸�б�ɹ���\n" + (Client) msg.obj).show();
				addWaterCodes((Client) msg.obj);
				handler.removeMessages(GET_GXJ_FAIL);
				// loginOrRegisterSuccess((User) msg.obj);
				break;
			case GET_GXJ_FAIL:
				toast.setText("��ȡ�豸�б�ʧ�ܣ�-->" + (String) msg.obj).show();
				if (msg.arg2 == TIMEOUT) {
					TcpManager.reconnect();
				}
				break;
			case MoMessage.LOGIN_KEY_ERR:
				new AlertDialog.Builder(getActivity()).setTitle("��ʾ").setMessage((String) msg.obj).setPositiveButton("ȷ��", null).show();
				logout();
				break;
			default:
				break;
			}
		}
	};

	private void loginOrRegisterSuccess(User user) {
		GlobalInfo.username = user.username;
		GlobalInfo.password = user.password;
		GlobalInfo.online = true;
		GlobalInfo.savePassword = chkBoxSavePassword.isChecked();// ��������
		GlobalInfo.autoLogin = chkBoxAutoLogin.isChecked();// �Զ���¼

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(GlobalInfo.S_P_NAME_CONFIG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(GlobalInfo.S_P_KEY_USERNAME, GlobalInfo.username);
		if (chkBoxSavePassword.isChecked() == true) {
			editor.putString(GlobalInfo.S_P_KEY_PASSWORD, GlobalInfo.password);
		} else {
			editor.remove(GlobalInfo.S_P_KEY_PASSWORD);
		}
		editor.putBoolean("savePassword", GlobalInfo.savePassword);
		editor.putBoolean("autoLogin", GlobalInfo.autoLogin);
		editor.commit();
		switchLayout(LAYOUT_USER_INFO);
	}

	private void switchServer(String address) {
		GlobalInfo.serverAddress = address;
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(GlobalInfo.S_P_NAME_CONFIG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(GlobalInfo.S_P_KEY_SERVER_ADDRESS, GlobalInfo.serverAddress);
		editor.commit();
		TcpManager.setServerAddress(GlobalInfo.serverAddress);
		TcpManager.reconnect();
	}

	private void login(String username, String password) {
		Message msg = Message.obtain();
		msg.arg1 = sessionKey.generateNewSessionKey();
		Operaton operaton = new Operaton(getActivity());
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
		manualLogout = true;
		Operaton operaton1 = new Operaton(getActivity());
		operaton1.logout(sessionKey.generateNewSessionKey());
		if (chkBoxSavePassword.isChecked() == false) {
			GlobalInfo.password = "";
		}
		switchLayout(LAYOUT_LOGIN);
	}
}
