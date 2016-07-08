package cn.stepcounter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class StepCounterService extends Service {
	private SensorManager sensorManager;// ����������
	private StepDetector detector;// ��������������
	private PowerManager powerManager;// ��Դ�������
	private WakeLock wakeLock;// ��Ļ��
	public static Boolean FLAG = false;// �������б�־
	private String stepnum, stepnum_temp;
	private boolean threadrun = false;
	private NotificationManager manager;// ֪ͨ����
	private static Intent Notificationintent, broadcastIntent;
	private PendingIntent pendingIntent;
	private NotificationCompat.Builder notificationBuilder;
	private LocalBroadcastManager localBroadcastManager;
	private StepDBHelper stepDBHelper;
	private SQLiteDatabase database;
	private String str_selectstep, str_updatestep, str_insertstep, str_UserName, str_Date;
	private Thread thread = new Thread() {// ���߳����ڼ�����ǰ�����ı仯

		@Override
		public void run() {
			super.run();
			Log.d("service", "thread Start");
			while (threadrun) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stepnum_temp = stepnum;
				stepnum = String.valueOf(StepDetector.CURRENT_SETP);
				if (stepnum != stepnum_temp)
					mNotification(stepnum);
				localBroadcastManager.sendBroadcast(broadcastIntent);
			}
			Log.d("service", "thread Stop");
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Service", "onBind");

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		str_UserName = "";
		mSensor();
		init();
		Log.i("service", "onCreate");
		thread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (str_UserName.isEmpty())
			str_UserName = intent.getStringExtra("UserName");
		Log.d("Service", str_UserName);
		Calendar calendar = Calendar.getInstance();
		str_Date = new SimpleDateFormat("MM-dd").format(calendar.getTime());
		str_selectstep = "select * from " + "User_" + str_UserName + " where Date=?";
		Cursor cursor = database.rawQuery(str_selectstep, new String[] { str_Date });
		if (cursor.getCount() == 0) {
			str_insertstep = "insert into " + "User_" + str_UserName + " (Date,step_number) values(?,?)";
			database.execSQL(str_insertstep, new String[] { str_Date, "0" });
		} else {
			cursor.moveToLast();
			detector.setStepNum(cursor.getInt(1));
		}
		return super.onStartCommand(intent, flags, startId);
	}

	void init() {
		threadrun = true;
		FLAG = true;
		stepnum = stepnum_temp = "0";
		broadcastIntent = new Intent();
		broadcastIntent.setAction("cn.stepcounter.StepCounterService.LOCALBROAD");
		stepDBHelper = new StepDBHelper(StepCounterService.this, "stepNum.db", null, 1);
		database = stepDBHelper.getWritableDatabase();

		/**
		 * ��ʼ��֪ͨ��
		 */
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notificationintent = new Intent(this, StepCounterActivity.class);
		Notificationintent.putExtra("UserName", str_UserName);
		pendingIntent = PendingIntent.getActivity(this, 0, Notificationintent, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(getBaseContext().getResources().getText(R.string.app_name));
		notificationBuilder.setSmallIcon(R.drawable.ic_launch);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		notificationBuilder.setContentIntent(pendingIntent);

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		if (detector != null) {
			sensorManager.unregisterListener(detector);
		}
		FLAG = false;
		if (wakeLock != null) {
			wakeLock.release();
		}
		manager.cancelAll();
		Calendar calendar = Calendar.getInstance();
		str_Date = new SimpleDateFormat("MM-dd").format(calendar.getTime());
		str_updatestep = "update " + "User_" + str_UserName + " set step_number=? where Date=?";
		String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + "User_" + str_UserName + "';";
		Cursor cursor = database.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			int count = cursor.getInt(0);
			if (count > 0) {
				database.execSQL(str_updatestep, new String[] { String.valueOf(stepnum), str_Date });
				Log.i("####", String.valueOf(stepnum));
			}
		}

		database.close();
		stepDBHelper.close();
		cursor.close();
		threadrun = false;
		Log.i("service", "onDestroy");

		detector.setStepNum(0);
	}

	/**
	 * �������͵�Դ����
	 */
	void mSensor() {
		// �����������࣬ʵ������������
		detector = new StepDetector(this);
		// ��ȡ�������ķ��񣬳�ʼ��������
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		// ע�ᴫ������ע�������
		sensorManager.registerListener(detector, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		// ��Դ�������
		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		// wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
		// | PowerManager.ACQUIRE_CAUSES_WAKEUP, "S");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S");
		wakeLock.acquire();
	}

	/**
	 * ͬ��֪ͨ������
	 */
	void mNotification(String step) {
		notificationBuilder.setContentText("����" + step + "��");
		manager.notify(1, notificationBuilder.build());
	}
}
