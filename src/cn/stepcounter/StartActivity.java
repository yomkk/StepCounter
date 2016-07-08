package cn.stepcounter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartActivity extends Activity {
	Intent intent = new Intent();
	String str_UserName;
	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("loginUser", MODE_PRIVATE);
		str_UserName = sharedPreferences.getString("UserName", "");
		if (StepCounterService.FLAG || !str_UserName.isEmpty()) {// 程序已经启动，直接跳转到运行界面
			intent.setClass(StartActivity.this, StepCounterActivity.class);
			startActivity(intent);
		} else {
			intent.setClass(StartActivity.this, LoginActivity.class);
			startActivity(intent);
		}
		finish();
	}
}
