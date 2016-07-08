package cn.stepcounter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.stepcounter.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class StepCounterActivity extends Activity {

	private TextView tv_show_step;// 步数
	private TextView tv_distance;// 行程
	private TextView tv_calories;// 卡路里
	private TextView tv_velocity;// 速度
	private WeekStatisticsView weekStatisticsView;
	private int[] Hisstep = { 0, 0, 0, 0, 0, 0, 0 };
	private Double distance = 0.0;// 路程：米
	private Double distance_temp[] = { 0.0, 0.0, 0.0 };
	private Double calories = 0.0;// 热量：卡路里
	private Double velocity = 0.0;// 速度：米每秒
	private int step_length = 0; // 步长
	private int weight = 0; // 体重
	private int total_step = 0; // 走的总步数
	public static Boolean FLAG = true;//
	private IntentFilter broadcastIntentFilter = new IntentFilter();
	private StepNumReceiver stepNumReceiver = new StepNumReceiver();
	private LocalBroadcastManager localBroadcastManager;
	private String str_UserName;;
	/*
	 * DB
	 */
	private DBHelper dbHelper;
	String str_selectstep, str_Date;
	private StepDBHelper stepDBHelper;
	private SQLiteDatabase database, database2;
	private String str_selectUser = "select * from User_table where UserName=?";
	/*
	 * viewPager
	 */
	private ViewPager viewPager;
	private List<View> viewlist;
	private LayoutInflater inflater;
	private View view1, view2;
	private MyViewPagerAdapter adapter;
	SharedPreferences getUserPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Activity", "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.viewpage); // 设置当前屏幕
		getUserPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
		str_UserName = getUserPreferences.getString("UserName", "");
		Log.i("UserName", str_UserName);
		if (!StepCounterService.FLAG) {
			Intent service = new Intent(this, StepCounterService.class);
			service.putExtra("UserName", str_UserName);
			startService(service);
		}
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_step, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(StepCounterActivity.this, SettingsActivity.class);
			intent.putExtra("UserName", str_UserName);
			startActivity(intent);
			finish();
			break;

		case R.id.menu_exit:
			Intent service = new Intent(StepCounterActivity.this, StepCounterService.class);
			stopService(service);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {

		super.onResume();
		Log.d("Activity", "onResuame.");
		init();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("Activity", "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("Activity", "onStop");
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		Log.d("Activity", "onDestroy");
		localBroadcastManager.unregisterReceiver(stepNumReceiver);
		database.close();
		dbHelper.close();
		database2.close();
		stepDBHelper.close();
	}

	/**
	 * 初始化界面
	 */
	private void init() {
		// viewPager
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		inflater = LayoutInflater.from(this);
		view1 = inflater.inflate(R.layout.main, null);
		view2 = inflater.inflate(R.layout.weekstatistics, null);
		viewlist = new ArrayList<View>();
		viewlist.add(view1);
		viewlist.add(view2);
		adapter = new MyViewPagerAdapter(viewlist);
		viewPager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
				case 1:
					freshStatices();
					break;

				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO 自动生成的方法存根

			}
		});
		// DB
		dbHelper = new DBHelper(StepCounterActivity.this, "Data.db", null, 1);
		database = dbHelper.getWritableDatabase();
		stepDBHelper = new StepDBHelper(StepCounterActivity.this, "stepNum.db", null, 1);
		database2 = stepDBHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(str_selectUser, new String[] { str_UserName });
		cursor.moveToLast();
		Log.i(str_UserName, String.valueOf(cursor.getCount()));
		step_length = Integer.parseInt(cursor.getString(3));
		weight = Integer.parseInt(cursor.getString(4));

		// LocalBroadcast
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		broadcastIntentFilter.addAction("cn.stepcounter.StepCounterService.LOCALBROAD");
		localBroadcastManager.registerReceiver(stepNumReceiver, broadcastIntentFilter);
		// getview
		tv_show_step = (TextView) view1.findViewById(R.id.show_step);
		tv_distance = (TextView) view1.findViewById(R.id.distance);
		tv_calories = (TextView) view1.findViewById(R.id.calories);
		tv_velocity = (TextView) view1.findViewById(R.id.velocity);
		weekStatisticsView = (WeekStatisticsView) view2.findViewById(R.id.weekStatistics);

		freshUI();
		freshStatices();
	}

	/**
	 * 刷新界面
	 */
	void freshUI() {
		countStep(); // 调用步数方法
		countDistance(); // 调用距离方法，看一下走了多远
		if (distance != 0.0) {
			// 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
			calories = weight * distance * 0.001;
			// 速度velocity
			velocity = (distance - distance_temp[0]) / 1.5;
		} else {
			calories = 0.0;
			velocity = 0.0;
		}
		tv_show_step.setText(total_step + "");// 显示当前步数
		tv_distance.setText(formatDouble(distance));// 显示路程
		tv_calories.setText(formatDouble(calories));// 显示卡路里
		tv_velocity.setText(formatDouble(velocity));// 显示速度
	}

	void freshStatices() {
		str_selectstep = "select * from " + "User_" + str_UserName + " where Date=?";
		Cursor cursor2;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -6);
		for (int i = 0; i < 6; i++) {
			// text, baseX, baseY, textPaint
			str_Date = new SimpleDateFormat("MM-dd").format(calendar.getTime());
			cursor2 = database2.rawQuery(str_selectstep, new String[] { str_Date });
			cursor2.moveToLast();
			if (cursor2.getCount() != 0) {
				Hisstep[i] = Integer.parseInt(cursor2.getString(1));
			} else
				Hisstep[i] = 0;
			calendar.add(Calendar.DATE, 1);
		}
		Hisstep[6] = total_step;
		weekStatisticsView.setnumber(Hisstep);
		weekStatisticsView.invalidate();
	}

	/**
	 * 计算并格式化doubles数值，保留两位有效数字
	 * 
	 * @return 返回当前路程
	 */
	private String formatDouble(Double doubles) {
		DecimalFormat format = new DecimalFormat("####.##");
		String distanceStr = format.format(doubles);
		return distanceStr.equals(getString(R.string.zero)) ? getString(R.string.double_zero) : distanceStr;
	}

	/**
	 * 计算行走的距离
	 */
	private void countDistance() {
		distance_temp[0] = distance_temp[1];
		distance_temp[1] = distance_temp[2];
		distance_temp[2] = distance;
		distance = StepDetector.CURRENT_SETP * step_length * 0.01;
	}

	/**
	 * 实际的步数
	 */
	private void countStep() {
		total_step = StepDetector.CURRENT_SETP;
	}

	class StepNumReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			freshUI();
		}
	}

	public class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));// 删除页卡
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			container.addView(mListViews.get(position), 0);// 添加页卡
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();// 返回页卡的数量
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}
}
