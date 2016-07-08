package cn.stepcounter;

import cn.stepcounter.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	Button btn_login, btn_register;
	Intent intent;
	DBHelper dbHelper;
	SQLiteDatabase database;
	private EditText userNameEditText, passWordEditText;
	private String str_UserName, str_password;
	String str_selectUser = "select * from User_table where UserName=?";
	String tempString;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
		dbHelper.close();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (StepCounterService.FLAG) {
			intent = new Intent(LoginActivity.this, StepCounterService.class);
			stopService(intent);
		}
	}

	void init() {
		dbHelper = new DBHelper(LoginActivity.this, "Data.db", null, 1);
		database = dbHelper.getWritableDatabase();
		editor = getSharedPreferences("loginUser", MODE_PRIVATE).edit();

		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (Button) findViewById(R.id.btn_register);
		userNameEditText = (EditText) findViewById(R.id.editText_UserName);
		passWordEditText = (EditText) findViewById(R.id.editText_PassWord);
		btn_login.setOnClickListener(onClickListener);
		btn_register.setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_login:
				str_UserName = userNameEditText.getText().toString();
				str_password = passWordEditText.getText().toString();
				Cursor cursor = database.rawQuery(str_selectUser, new String[] { str_UserName });
				if (str_UserName.isEmpty())
					Toast.makeText(LoginActivity.this, "用户名不能为空！", 0).show();
				if (!cursor.moveToNext())
					Toast.makeText(LoginActivity.this, "该用户名不存在！", 0).show();
				else {
					tempString = cursor.getString(2);
					Log.i("read", tempString);
					Log.i("input", str_password);
					if (tempString.equals(str_password)) {
						intent = new Intent(LoginActivity.this, StepCounterActivity.class);
						editor.putString("UserName", str_UserName);
						editor.commit();
						cursor.close();
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(LoginActivity.this, "密码有误！", 0).show();
					}
				}

				break;
			case R.id.btn_register:
				intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};
}
