package cn.stepcounter;

import cn.stepcounter.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 应用程序的设置界面
 */
public class SettingsActivity extends Activity {
	private String str_selectUser = "select * from User_table where UserName=?";
	private String str_updateUser1 = "update User_table set step_length=?, weight=? where UserName=?";
	private String str_updateUser2 = "update User_table set Password=?,step_length=?, weight=? where UserName=?";
	private String str_deleteUser = "delete from User_table where UserName=?";
	private String str_deletestep;
	private TextView tv_step_length_vlaue;
	private TextView tv_weight_value;
	private SeekBar sb_step_length;
	private SeekBar sb_weight;
	private EditText et_password;
	private int step_length = 0;
	private int weight = 0;
	private AlertDialog.Builder switchDialog, deleteDialog;
	private DBHelper dbHelper;
	private StepDBHelper stepDBHelper;
	private SQLiteDatabase database, database2;
	private String tempString;
	private String str_UserName;
	private Intent getUserIntent, backIntent, loginIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);
		init();
		listener();
	}

	/**
	 * SeekBar的拖动监听
	 */
	private void listener() {
		sb_step_length.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				step_length = progress * 5 + 30;
				tv_step_length_vlaue.setText(step_length + getString(R.string.cm));
			}
		});

		sb_weight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				weight = progress + 20;
				tv_weight_value.setText(weight + getString(R.string.kg));
			}
		});
	}

	private void init() {

		dbHelper = new DBHelper(SettingsActivity.this, "Data.db", null, 1);
		database = dbHelper.getWritableDatabase();
		stepDBHelper = new StepDBHelper(SettingsActivity.this, "stepNum.db", null, 1);
		database2 = stepDBHelper.getWritableDatabase();
		getUserIntent = getIntent();
		backIntent = new Intent(SettingsActivity.this, StepCounterActivity.class);
		backIntent.putExtra("UserName", str_UserName);
		loginIntent = new Intent(SettingsActivity.this, LoginActivity.class);
		str_UserName = getUserIntent.getStringExtra("UserName");
		Cursor cursor = database.rawQuery(str_selectUser, new String[] { str_UserName });
		cursor.moveToLast();
		step_length = Integer.parseInt(cursor.getString(3));
		weight = Integer.parseInt(cursor.getString(4));
		/**
		 * 布局控件
		 */
		tv_step_length_vlaue = (TextView) this.findViewById(R.id.step_lenth_value);
		tv_weight_value = (TextView) this.findViewById(R.id.weight_value);
		sb_step_length = (SeekBar) this.findViewById(R.id.step_lenth);
		sb_weight = (SeekBar) this.findViewById(R.id.weight);
		et_password = (EditText) findViewById(R.id.password_edit);
		sb_step_length.setProgress((step_length - 30) / 5); // 步长按钮在进度条上占得比例
		sb_weight.setProgress(weight - 20);
		tv_step_length_vlaue.setText(step_length + getString(R.string.cm));
		tv_weight_value.setText(weight + getString(R.string.kg));
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save:
			tempString = et_password.getText().toString();
			if (tempString.isEmpty())
				database.execSQL(str_updateUser1, new String[] { String.valueOf(step_length), String.valueOf(weight), str_UserName });
			else {
				database.execSQL(str_updateUser2, new String[] { tempString, String.valueOf(step_length), String.valueOf(weight), str_UserName });
			}
			startActivity(backIntent);
			Toast.makeText(SettingsActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
			this.finish();
			break;
		case R.id.cancle:
			startActivity(backIntent);
			this.finish();
			break;
		case R.id.switch_account:
			switchDialog = new AlertDialog.Builder(SettingsActivity.this);
			switchDialog.setTitle("提醒！");
			switchDialog.setMessage("确定切换账户？");
			switchDialog.setCancelable(true);
			switchDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(SettingsActivity.this, "ok", 0).show();
					SharedPreferences.Editor editor = getSharedPreferences("loginUser", MODE_PRIVATE).edit();
					editor.clear();
					editor.commit();
					startActivity(loginIntent);
					if (StepCounterService.FLAG) {
						Intent intent = new Intent(SettingsActivity.this, StepCounterService.class);
						stopService(intent);
					}
					finish();
				}
			});
			switchDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(SettingsActivity.this, "cancel", 0).show();
				}
			});
			switchDialog.show();
			break;
		case R.id.delete_account:
			deleteDialog = new AlertDialog.Builder(SettingsActivity.this);
			deleteDialog.setTitle("警告！");
			deleteDialog.setMessage("确定删除此账户？（删除无法恢复）");
			deleteDialog.setCancelable(true);
			deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(SettingsActivity.this, "ok", 0).show();
					SharedPreferences.Editor editor = getSharedPreferences("loginUser", MODE_PRIVATE).edit();
					editor.clear();
					editor.commit();
					str_deletestep = "drop table " + "User_" + str_UserName;
					database.execSQL(str_deleteUser, new String[] { str_UserName });
					database2.execSQL(str_deletestep);
					startActivity(loginIntent);
					if (StepCounterService.FLAG) {
						Intent intent = new Intent(SettingsActivity.this, StepCounterService.class);
						stopService(intent);
					}
					finish();
				}
			});
			deleteDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(SettingsActivity.this, "cancel", 0).show();
				}
			});
			deleteDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startActivity(backIntent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
		dbHelper.close();
	}
}
