package com.bfyycdi.commhleper;

import java.util.regex.*;

import com.bfyycdi.commhleper.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.InputType;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class SetDataActivity extends Activity {
	// 声明View
	EditText startTimeEditText;
	EditText endTimeEditText;
	EditText ringTimeEditText;
	Spinner answerWaySpinner;
	CheckBox enquireCheckBox;
	CheckBox pauseCheckBox;
	EditText pauseEditText;
	EditText messageEditText;
	Button saveSetButton;
	Button cancelButton;
	CursorFactory userFactory;
	SQLiteDatabase userDB;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_data);
		userDB = openOrCreateDatabase("userDB", Context.MODE_WORLD_WRITEABLE,
				null);
		findView();
		int myIntentInt = getIntentInt();
		setText(myIntentInt);
		setEditTextState(myIntentInt);
		pauseEditText.clearFocus();
		buttonAnswer();
	}

	private void setEditTextState(int myIntentInt) {
		pauseEditText.setInputType(InputType.TYPE_NULL);
		pauseCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							pauseEditText
									.setInputType(InputType.TYPE_CLASS_NUMBER);
						} else {
							pauseEditText.setInputType(InputType.TYPE_NULL);
						}
					}
				});
		pauseEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (!pauseCheckBox.isChecked()) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								pauseEditText.getWindowToken(), 0);
					}
				}
			}

		});

	}

	private void setText(int myIntentInt) {
		if (myIntentInt == -1)
			return;
		Cursor myResult = userDB.query("USERDB", null, "ROWID=" + myIntentInt,
				null, null, null, null);
		myResult.moveToFirst();
		startTimeEditText.setText(myResult.getString(0));
		endTimeEditText.setText(myResult.getString(1));
		ringTimeEditText.setText(myResult.getString(2));
		answerWaySpinner.setSelection(myResult.getInt(3));
		enquireCheckBox.setChecked((myResult.getInt(4) == 0) ? false : true);
		pauseCheckBox.setChecked((myResult.getInt(5) == 0) ? false : true);
		pauseEditText.setText(myResult.getString(6));
		messageEditText.setText(myResult.getString(7));
	}

	private int getIntentInt() {
		Intent intent = getIntent();
		return intent.getIntExtra("itemIndex", -1);
	}

	private void buttonAnswer() {
		saveSetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (checkNull())
					return;
				if (checkFormatWrong())
					return;
				if (checkTimeWrong())
					return;
				addItem(getIntentInt());
				userDB.close();
				finish();
			}

		});
		cancelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				finish();
			}

		});
	}

	protected boolean checkFormatWrong() {
		Pattern pat = Pattern.compile("^(([01]?\\d)|(2[0-3]{1})):[0-5]?\\d$");
		Matcher mat1 = pat.matcher(startTimeEditText.getText().toString());
		Matcher mat2 = pat.matcher(endTimeEditText.getText().toString());
		boolean res1 = mat1.find();
		boolean res2 = mat2.find();
		if (!(res1 && res2))
			Toast.makeText(SetDataActivity.this, "亲，您输入的时间格式不正确额!",
					Toast.LENGTH_LONG).show();
		return !(res1 && res2);
	}

	private boolean checkNull() {
		if (startTimeEditText.getText().toString().equals("")) {
			Toast.makeText(SetDataActivity.this, "亲，您还没用输入起始时间额！",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		if (endTimeEditText.getText().toString().equals("")) {
			Toast.makeText(SetDataActivity.this, "亲，您还没用输入终止时间额！",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		if (ringTimeEditText.getText().toString().equals("")) {
			Toast.makeText(SetDataActivity.this, "亲，您还没用输入响铃时间额！",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		if (messageEditText.getText().toString().equals("")) {

			return true;
		}
		return false;
	}

	private boolean checkTimeWrong() {
		Pattern pat = Pattern.compile(":");
		String[] strS = pat.split(startTimeEditText.getText().toString());
		String[] strE = pat.split(endTimeEditText.getText().toString());
		int numS = Integer.parseInt(strS[0]) * 100 + Integer.parseInt(strS[1]);
		int numE = Integer.parseInt(strE[0]) * 100 + Integer.parseInt(strE[1]);
		if (numE < numS) {
			Toast.makeText(SetDataActivity.this, "亲，您输入的时间不符合逻辑额！",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	private void findView() {
		startTimeEditText = (EditText) findViewById(R.id.startTimeEditText);
		endTimeEditText = (EditText) findViewById(R.id.endTimeEditText);
		ringTimeEditText = (EditText) findViewById(R.id.ringTimeEditText);
		answerWaySpinner = (Spinner) findViewById(R.id.answerWaySpinner);
		enquireCheckBox = (CheckBox) findViewById(R.id.enquireCheckBox);
		messageEditText = (EditText) findViewById(R.id.messageEditText);
		saveSetButton = (Button) findViewById(R.id.saveSetButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		pauseCheckBox = (CheckBox) findViewById(R.id.noAnswerCheckBox);
		pauseEditText = (EditText) findViewById(R.id.noAnswerEditText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		MenuItem suggest = menu.getItem(0);
		MenuItem about = menu.getItem(1);
		MenuItem help = menu.getItem(2);
		suggest.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				AlertDialog.Builder enquire = new AlertDialog.Builder(
						SetDataActivity.this);
				enquire.setTitle("直接发送邮件？");
				enquire.setMessage("点击确认打开邮件程序并发送邮件至bfyycdi@gmail.com，或者记下此地址手动发送邮件？");
				enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						Uri uri = Uri.parse("mailto:bfyycdi@gmail.com");
						Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
						startActivity(intent);
					}
				});
				enquire.setNegativeButton("取消", new Dialog.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				enquire.show();
				return true;
			}

		});
		about.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				startActivity(new Intent(SetDataActivity.this, About.class));
				return false;
			}

		});
		help.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				howToUse();
				return false;
			}
		});

		return true;
	}

	private void howToUse() {
		final Dialog firstUseDialog = new Dialog(SetDataActivity.this);
		Window window = firstUseDialog.getWindow();
		window.setFlags(WindowManager.LayoutParams.ALPHA_CHANGED,
				WindowManager.LayoutParams.ALPHA_CHANGED);
		firstUseDialog.setTitle("自动应答");
		firstUseDialog.setContentView(R.layout.first_use_dialog);
		Button okButton = (Button) firstUseDialog
				.findViewById(R.id.howToUseOkbutton);
		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				firstUseDialog.cancel();
			}

		});
		firstUseDialog.show();
	}

	public void addItem(int i) {
		ContentValues cv = new ContentValues();
		cv.put("STARTTIME", startTimeEditText.getText().toString());
		cv.put("ENDTIME", endTimeEditText.getText().toString());
		cv.put("RINGTIME", ringTimeEditText.getText().toString());
		cv.put("ANSWERWAY", answerWaySpinner.getSelectedItemPosition());
		cv.put("ENQUIRE", (enquireCheckBox.isChecked()) ? 1 : 0);
		cv.put("SUREPAUSE", (pauseCheckBox.isChecked()) ? 1 : 0);
		cv.put("PAUSETIME", pauseEditText.getText().toString());
		cv.put("MESSAGE", messageEditText.getText().toString());
		cv.put("SETMARK", 1);
		if (i == -1) {
			Cursor result = userDB.query("USERDB", null, null, null, null,
					null, null);
			if (result.moveToFirst()) {
				result.moveToLast();
				cv.put("ROWID", result.getInt(8) + 1);
			} else {
				cv.put("ROWID", 0);
			}
			userDB.insert("USERDB", "null", cv);
		} else {
			userDB.update("USERDB", cv, "ROWID=" + i, null);
		}

	}
}
