package cn.stepcounter;

import cn.stepcounter.R;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private Button btn_reg;
	private String str_UserName, str_password, str_create;
	private EditText userNameEditText, passWordEditText;
	private DBHelper dbHelper;
	private StepDBHelper stepDBHelper;
	private SQLiteDatabase database, database2;
	private String str_insertUser = "insert into User_table (id,UserName,Password,step_length,weight) values(?,?,?,?,?)";
	private String str_selectUser = "select * from User_table where UserName=?";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		init();
	}

	void init() {
		dbHelper = new DBHelper(RegisterActivity.this, "Data.db", null, 1);
		database = dbHelper.getWritableDatabase();
		stepDBHelper = new StepDBHelper(RegisterActivity.this, "stepNum.db", null, 1);
		database2 = stepDBHelper.getWritableDatabase();
		userNameEditText = (EditText) findViewById(R.id.editText_Reg_UserName);
		passWordEditText = (EditText) findViewById(R.id.editText_editText_Reg_PassWord);
		btn_reg = (Button) findViewById(R.id.btn_Reg_Register);
		btn_reg.setOnClickListener(onClickListener);
	}

	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		database.close();
		database2.close();
		dbHelper.close();
		stepDBHelper.close();
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.btn_Reg_Register:
				str_UserName = userNameEditText.getText().toString();
				str_password = passWordEditText.getText().toString();
				Cursor cursor = database.rawQuery(str_selectUser, new String[] { str_UserName });
				if (str_UserName.isEmpty())
					Toast.makeText(RegisterActivity.this, "用户名不能为空！", 0).show();
				else if (cursor.moveToNext()) {
					Toast.makeText(RegisterActivity.this, "该用户名已存在！", 0).show();
				} else {
					Toast.makeText(RegisterActivity.this, "注册成功，请以新注册的用户信息登陆！", 0).show();
					database.execSQL(str_insertUser, new String[] { null, str_UserName, str_password, "60", "60" });
					str_create = "create table " + "User_"+str_UserName + " (Date text,step_number integer)";
					database2.execSQL(str_create);
					cursor.close();
					finish();
				}
				break;

			default:
				break;
			}
		}
	};
}
