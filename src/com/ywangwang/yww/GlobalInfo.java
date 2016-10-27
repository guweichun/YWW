package com.ywangwang.yww;

import android.graphics.Color;

public class GlobalInfo {
	public static final String SERVER = "www.ywangwang.com";

	public static final String BROADCAST_ACTION = "com.ywangwang.yww.BROADCAST";
	public static final String BROADCAST_SERVICE_ACTION = "com.ywangwang.yww.TcpService.BROADCAST";
	public static final String BROADCAST_DATA_SEND_ACTION = "com.ywangwang.yww.DataSendManager.BROADCAST";
	public static final String BROADCAST_LOGIN = "LOGIN";
	public static final String BROADCAST_LOGOUT = "LOGOUT";
	public static final String BROADCAST_LOGIN_SUCCESS = "LOGIN_SUCCESS";
	public static final String BROADCAST_LOGIN_FAIL = "LOGIN_FAIL";
	public static final String BROADCAST_RECEIVE_NEW_MESSAGE = "RECEIVE_NEW_MESSAGE";
	public static final String BROADCAST_CONNECT_SOCKET_SUCCESS = "CONNECT_SOCKET_SUCCESS";
	public static final String BROADCAST_SWITCH_SERVER = "SWITCH_SERVER";
	public static final String BROADCAST_UPDATE_CONNECT_STATUS = "UPDATE_CONNECT_STATUS";
	public static final String BROADCAST_GET_DEVICE_LIST = "GET_DEVICE_LIST";
	public static final String BROADCAST_GET_DEVICE_LIST_SUCCESS = "GET_DEVICE_LIST_SUCCESS";
	public static final String BROADCAST_GET_DEVICE_LIST_FAIL = "GET_DEVICE_LIST_FAIL";

	public static final int DEFAULT_COLOR = Color.parseColor("#DFDFDF");
	public static final int DEFAULT_DARKEN_COLOR = Color.parseColor("#DDDDDD");
	public static final int COLOR_BLUE = Color.parseColor("#33B5E5");
	public static final int COLOR_VIOLET = Color.parseColor("#AA66CC");
	public static final int COLOR_GREEN = Color.parseColor("#46C01B");
	// public static final int COLOR_GREEN = Color.parseColor("#99CC00");
	public static final int COLOR_ORANGE = Color.parseColor("#FFBB33");
	public static final int COLOR_RED = Color.parseColor("#FF4444");

	public static final String S_P_NAME_CONFIG = "config";

	public static final int MIN_WATER_TEMPRATURE = 40;// ���ˮ��
	public static final int MAX_WATER_TEMPRATURE = 100;// ���ˮ��
	public static final int MIN_WATER_AMOUNT = 50;// ����ˮ��
	public static final int MAX_WATER_AMOUNT = 2500;// ���ˮ��

	public static final int OUT_WATER_COUNT_DOWN_TIME = 120;// ��ˮ����ʱ�趨����λ��

	public static final String S_P_KEY_DEBUG = "debug";
	public static final String S_P_KEY_DEBUG_TIMES = "debugTimes";
	public static boolean debug = false; // DEBUGģʽ���
	public static int debugTimes = 0; // ʣ��DEBUG����
	public static boolean testMode = false; // ���ģʽģʽ���

	public static int setTemperature = 0; // �趨�¶�0��40~100
	public static int setWaterAmount = 0; // �趨��ˮ��10~2500mL
	public static String setMode = "��ѡ��"; // �趨��ˮģʽ

	public static final int COOL_WATER = 10; // ��ˮ
	public static final int RoomTemperatureValue = 25; // �����¶�ֵ
	public static final int MilkTemperatureValue = 45; // �����¶�ֵ
	public static final int HoneyTemperatureValue = 55; // �����¶�ֵ
	public static final int BoilingTemperatureValue = 100; // ��ˮ�¶�ֵ
	public static final int WaterAmount150Value = 150; // �趨��ˮ��150
	public static final int WaterAmount260Value = 260; // �趨��ˮ��260
	public static final int WaterAmount300Value = 300; // �趨��ˮ��300
	public static boolean selectCoolWater = false;// ѡ���ˮ
	public static boolean selectRoomTemperatureWater = false;// ѡ����ˮ

	public static final String S_P_KEY_USERNAME = "username";
	public static final String S_P_KEY_PASSWORD = "password";
	public static final String S_P_SAVE_PASSWORD = "savePassword";
	public static final String S_P_AUTO_LOGIN = "autoLogin";
	public static String username = "";// �û���
	public static String password = "";// �û�����
	public static Boolean online = false;// ��¼״̬
	public static Boolean savePassword = true;// ��������
	public static Boolean autoLogin = true;// �Զ���¼

	public static final String S_P_KEY_SERVER_ADDRESS = "serverAddress";
	public static String serverAddress = "192.168.0.123";// ��������ַ

	public static Boolean isBoundWaterCode = false;// �Ƿ�󶨵�ȡˮ����
	public static final String KEY_IS_BOUND_WATER_CODE = "isBoundWaterCode";
	public static final String KEY_NUMBER = "boundWaterCode_number";
	public static final String KEY_TYPE = "boundWaterCode_type";
	public static final String KEY_STATUS = "boundWaterCode_status";
	public static final String KEY_BOUND_DEVICE_ID = "boundWaterCode_boundDeviceID";
	public static final String KEY_PERIOD_VALIDITY = "boundWaterCode_periodValidity";
	public static final String KEY_ACTIVATION_TIME = "boundWaterCode_activationTime";

	// public static final int FILTER_RECOMMEND_DAYS_PP = 90; // PP���Ƽ�ʹ������
	// public static final int FILTER_RECOMMEND_DAYS_FC = 90; // ǰ�ÿ�������̿(front_carbon)�Ƽ�ʹ������
	// public static final int FILTER_RECOMMEND_DAYS_RO = 720; // RO����͸�Ƽ�ʹ������
	// public static final int FILTER_RECOMMEND_DAYS_BC = 90; // ���ÿ�������̿(behind_carbon)�Ƽ�ʹ������
	// public static long filterInstallTimePP = 0L; // PP�ް�װʱ��
	// public static long filterInstallTimeFC = 0L; // ǰ�ÿ�������̿(front_carbon)��װʱ��
	// public static long filterInstallTimeRO = 0L; // RO����͸��װʱ��
	// public static long filterInstallTimeBC = 0L; // ���ÿ�������̿(behind_carbon)��װʱ��
	public static final int[] FILTER_RECOMMEND_DAYS = { 90, 90, 720, 90 }; // ��оʹ������
	public static long[] filterInstallTime = { 0L, 0L, 0L, 0L }; // ��о��װʱ��

	public static long id = 0L;
	public static int loginKey = 0;
	public static boolean unableConnectToServer = false;
//	public static boolean manualLogout = false;

	public static Client client = null;

	public void reset() {
	}

	public static class SubDevice {
		public boolean used = false;
		public int add = 0;
		public int deviceType = 0;
		public int itemId = 0;

		public String getDeviceType() {
			switch (this.deviceType) {
			case 0:
				return "����";
			case 1:
				return "̨��ʽ��ˮ��";
			case 2:
				return "̨��ʽ��ˮ��";
			case 3:
				return "���߻�";
			default:
				break;
			}
			return "����";
		}
	}

	public static class GxjOutWaterDetails {
		public int averageTDS = 0;// ƽ��TDS
		public int waterAmount = 0; // ��ˮ����λ���� mL
		public int temperature = 0;// �趨�¶ȣ�10=��ˮ��25=����ˮ
		public long time = 0L; // ��ˮʱ��

		public void clear() {
			this.averageTDS = 0;
			this.waterAmount = 0;
			this.temperature = 0;
			this.time = 0L;
		}
	}

	public static class JsqDataStatistics {
		public int averageTDSIn = 0; // ��ˮƽ��TDS
		public int averageTDSOut = 0; // �ճ�ˮƽ��TDS
		public float totalWaterIn = 0f; // ��ˮ������λ�� L
		public float totalWaterOut = 0f; // ��ˮ������λ�� L
		public int totalFilterWaterTimes = 0; // ����ˮ����
		public long time = 0L; // ����ˮ����

		public void clear() {
			this.averageTDSIn = 0;
			this.averageTDSOut = 0;
			this.totalWaterIn = 0f;
			this.totalWaterOut = 0f;
			this.totalFilterWaterTimes = 0;
			this.time = 0L;
		}

		// public void addData(JsqDataStatistics newData) {
		// this.averageTDSIn += newData.averageTDSIn;
		// this.averageTDSOut += newData.averageTDSOut;
		// this.totalWaterIn += newData.totalWaterIn;
		// this.totalWaterOut += newData.totalWaterOut;
		// this.totalFilterWaterTimes += newData.totalFilterWaterTimes;
		// }
	}

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
