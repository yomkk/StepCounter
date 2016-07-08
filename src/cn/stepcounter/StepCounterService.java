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
	private SensorManager sensorManager;// 传感器服务
	private StepDetector detector;// 传感器监听对象
	private PowerManager powerManager;// 电源管理服务
	private WakeLock wakeLock;// 屏幕灯
	public static Boolean FLAG = false;// 服务运行标志
	private String stepnum, stepnum_temp;
	private boolean threadrun = false;
	private NotificationManager manager;// 通知管理
	private static Intent Notificationintent, broadcastIntent;
	private PendingIntent pendingIntent;
	private NotificationCompat.Builder notificationBuilder;
	private LocalBroadcastManager localBroadcastManager;
	private StepDBHelper stepDBHelper;
	private SQLiteDatabase database;
	private String str_selectstep, str_updatestep, str_insertstep, str_UserName, str_Date;
	private Thread thread = new Thread() {// 子线程用于监听当前步数的变化

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
		 * 初始化通知栏
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
	 * 传感器和电源管理
	 */
	void mSensor() {
		// 创建监听器类，实例化监听对象
		detector = new StepDetector(this);
		// 获取传感器的服务，初始化传感器
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		// 注册传感器，注册监听器
		sensorManager.registerListener(detector, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		// 电源管理服务
		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		// wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
		// | PowerManager.ACQUIRE_CAUSES_WAKEUP, "S");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S");
		wakeLock.acquire();
	}

	/**
	 * 同步通知栏步数
	 */
	void mNotification(String step) {
		notificationBuilder.setContentText("走了" + step + "步");
		manager.notify(1, notificationBuilder.build());
	}
}
