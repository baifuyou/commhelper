package com.bfyycdi.commhleper;

import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.android.internal.telephony.ITelephony;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.os.*;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.*;
import android.view.WindowManager;

public class Listenservice extends Service {
	SQLiteDatabase userDB;
	int nowSetSite;
	SharedPreferences todayData;
	SharedPreferences.Editor todayDataEditor;
	String telNum;
	Runnable answer;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		final Intent intent2 = intent;
		final int startId2 = startId;
		answer = new Runnable() {

			public void run() {
				startDO(intent2, startId2);
			}

		};
		Thread thread = new Thread(null, answer, "Background");
		thread.start();
		stopService(new Intent(this, Listenservice.class));
	}

	protected void startDO(Intent intent, int startId) {
		int timeNum = getSystemTime();
		todayData = getSharedPreferences("todayData", Activity.MODE_PRIVATE);
		todayDataEditor = todayData.edit();
		String answerWay = intent.getStringExtra("answerWay");
		telNum = intent.getStringExtra("telNum");
		userDB = openOrCreateDatabase("userDB", Context.MODE_WORLD_WRITEABLE,
				null);
		Cursor myResult = userDB.query("USERDB", null, "SETMARK=1", null, null,
				null, null);
		if (checkRealAnswer(answerWay)) {
			if (checkSetExist(myResult, timeNum)) {
				if (checkPauseSet(myResult, timeNum)) {
					stopService(new Intent(this, Listenservice.class));
				} else {
					if (checkAnswerWay(myResult, answerWay)) {
						if (checkPhoneState(myResult, answerWay)) {
							sendSMS(myResult);
							saveLog(answerWay);
							createNotification(timeNum, answerWay);
							if (myResult.getInt(5) == 1) {
								saveTodayData(timeNum, myResult);
							}
						}

						stopService(new Intent(this, Listenservice.class));
					} else {

						stopService(new Intent(this, Listenservice.class));
					}
				}
			} else {
				stopService(new Intent(this, Listenservice.class));
			}
		} else {
			clearYesterdayData();
			stopService(new Intent(this, Listenservice.class));
		}
	}

	private void createNotification(int timeNum, String answerWay) {
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setTicker("已自动应答");
		builder.setSmallIcon(R.drawable.appicon2);
		Intent intent = new Intent(Listenservice.this, UserLog.class);
		PendingIntent mPI = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);
		String action;
		if (answerWay.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			action = "的一个来电被挂断";
		} else {
			action = "发来一条短信";
		}
		String contentText = getTelName() + telNum + action + ",已回复。";
		SimpleDateFormat mysdf = new SimpleDateFormat("HH:mm");
		String nowTime = mysdf.format(new Date());
		builder.setContentText(contentText);
		builder.setContentTitle(nowTime + "新的应答");
		Notification noti = builder.build();
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(R.drawable.appicon2, noti);
	}

	private void saveLog(String answerWay) {
		Cursor logResult = userDB.query("USERLOG", null, null, null, null,
				null, null);
		// 设置logId
		int logId;
		if (logResult.moveToFirst()) {
			logResult.moveToLast();
			logId = logResult.getInt(4) + 1;
		} else {
			logId = 0;
		}
		// 获取当前是来电还是短信
		String action;
		if (answerWay.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			action = "的一个来电被挂断";
		} else {
			action = "发来一条短信";
		}
		// 获取来电或者短信号码的姓名
		String name = getTelName();
		SimpleDateFormat mysdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
		String nowDate = mysdf.format(new Date());
		ContentValues cv = new ContentValues();
		cv.put("TIME", nowDate);
		cv.put("TELNUM", telNum);
		cv.put("ANSWERWAY", action);
		cv.put("NAME", name);
		cv.put("ROWID", logId);
		userDB.insert("USERLOG", null, cv);
		logResult.close();
		userDB.close();
	}

	private String getTelName() {
		ContentResolver myResolver = Listenservice.this.getContentResolver();

		// 遍历通讯录
		Cursor cursor = myResolver.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);
		while (cursor.moveToNext()) {
			// 取得联系人的名字索引
			int nameIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			String contact = cursor.getString(nameIndex);

			// 取得联系人的ID索引值
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			// 查询该位联系人的电话号码，类似的可以查询email，photo
			Cursor phone = myResolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);// 第一个参数是确定查询电话号，第三个参数是查询具体某个人的过滤值
			// 一个人可能有多个号码
			while (phone.moveToNext()) {
				String strPhoneNumber = phone
						.getString(phone
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				if (strPhoneNumber.equals(telNum))
					return contact;
			}
			phone.close();
		}
		cursor.close();

		return "";
	}

	private boolean checkPhoneState(Cursor myResult, String answerWay) {
		TelephonyManager telePhone = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (answerWay == "android.provider.Telephony.SMS_RECEIVED")
			return true;
		myResult.moveToPosition(nowSetSite);
		try {
			Thread.sleep(myResult.getInt(2) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (telePhone.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
			return false;
		return true;
	}

	private void saveTodayData(int timeNum, Cursor myResult) {
		myResult.moveToPosition(nowSetSite);
		todayDataEditor.putInt(telNum, myResult.getInt(6) + timeNum);
		todayDataEditor.commit();
	}

	private void sendSMS(Cursor myResult) {
		myResult.moveToPosition(nowSetSite);
		final Cursor finalMyResult = myResult;
		if (myResult.getInt(4) == 1) {
			Looper.prepare();
			AlertDialog.Builder enquire = new AlertDialog.Builder(
					Listenservice.this);
			enquire.setTitle("确认发送？");
			enquire.setMessage("您确定要挂断电话并发送短信吗？");
			enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					doSendSMS(finalMyResult);
				}
			});
			enquire.setNegativeButton("取消", new Dialog.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			AlertDialog ad = enquire.create();
			ad.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
			ad.getWindow()
					.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			ad.setCanceledOnTouchOutside(false);
			ad.show();
			Looper.loop();
			return;
		}
		doSendSMS(myResult);

	}

	private void doSendSMS(Cursor myResult) {
		// 挂断电话
		myResult.moveToPosition(nowSetSite);
		ITelephony phone = null;
		TelephonyManager telePhone = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = telePhone.getClass().getDeclaredMethod(
					"getITelephony");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		getITelephonyMethod.setAccessible(true);
		try {
			phone = (ITelephony) getITelephonyMethod.invoke(telePhone);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		try {
			phone.endCall();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// 发送短信
		PendingIntent mPI = PendingIntent.getBroadcast(Listenservice.this, 0,
				new Intent("SEND_SMS_ACTION"), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(telNum, null, myResult.getString(7), mPI, null);
		// 添加系统短信记录
		synchronized (this) {
			ContentValues cv = new ContentValues();
			cv.put("address", telNum);
			cv.put("date", String.valueOf(System.currentTimeMillis()));
			cv.put("read", 1);
			cv.put("status", 0);
			cv.put("type", 2);
			cv.put("body", myResult.getString(7)
					+ "（这条短信由自动应答助手自动发出，括号内的内容对方不可见。）");
			Uri uri = Uri.parse("content://sms");
			Listenservice.this.getContentResolver().insert(uri, cv);
		}
	}

	private boolean checkAnswerWay(Cursor myResult, String answerWay) {
		myResult.moveToPosition(nowSetSite);
		if (myResult.getInt(3) == 2)
			return true;
		if (myResult.getInt(3) == 0
				&& answerWay
						.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
			return true;
		if (myResult.getInt(3) == 1
				&& answerWay.equals("android.provider.Telephony.SMS_RECEIVED"))
			return true;
		return false;
	}

	private boolean checkPauseSet(Cursor myResult, int nowtime) {
		myResult.moveToPosition(nowSetSite);
		if (myResult.getInt(5) == 1) {
			if (todayData.getInt(telNum, 0) > nowtime) {
				return true;
			} else
				return false;
		}
		return false;
	}

	private void clearYesterdayData() {
		todayDataEditor.clear();
	}

	private boolean checkSetExist(Cursor myResult, int nowTime) {
		if (myResult.moveToFirst())
			do {
				if (timeToInt(myResult.getString(0)) <= nowTime
						&& nowTime < timeToInt(myResult.getString(1))) {
					nowSetSite = myResult.getPosition();
					return true;
				}
			} while (myResult.moveToNext());
		return false;
	}

	private int timeToInt(String string) {
		Pattern pat = Pattern.compile(":");
		String[] timeStr = pat.split(string);
		return Integer.parseInt(timeStr[0]) * 60 + Integer.parseInt(timeStr[1]);
	}

	private boolean checkRealAnswer(String answerWay) {
		if (answerWay.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
				|| answerWay.equals("android.provider.Telephony.SMS_RECEIVED"))
			return true;
		return false;
	}

	private int getSystemTime() {
		Calendar nowCala = Calendar.getInstance();
		int timeNum = nowCala.get(Calendar.HOUR_OF_DAY) * 60
				+ nowCala.get(Calendar.MINUTE);
		return timeNum;
	}

}
