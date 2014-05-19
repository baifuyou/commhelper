package com.bfyycdi.commhleper;

import java.util.*;

import com.bfyycdi.commhleper.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends Activity {
	private List<Map<String, Object>> userData;
	private Button addButton;
	UserData userDBHelper;
	SQLiteDatabase userDB;
	ListView list;
	MyAdapter adapter;
	SharedPreferences useMark;
	SharedPreferences.Editor useMarkEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list = (ListView) findViewById(R.id.list);
		userData = getData();
		adapter = new MyAdapter(this);
		list.setAdapter(adapter);
		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				startActivityForResult(new Intent(MainActivity.this,
						SetDataActivity.class).putExtra("itemIndex", -1), 2);
			}

		});
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(MainActivity.this,
						SetDataActivity.class);
				intent.putExtra("itemIndex", arg2);
				startActivityForResult(intent, 2);
			}

		});
		registerForContextMenu(list);
		firstUsePrompt();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshListView();
	}

	private void firstUsePrompt() {
		useMark = getSharedPreferences("useMark", Activity.MODE_PRIVATE);
		boolean firstUseMark = useMark.getBoolean("firstUseMark", true);
		if (firstUseMark) {
			howToUse();
		}
	}

	private void howToUse() {
		final Dialog firstUseDialog = new Dialog(MainActivity.this);
		useMarkEditor = useMark.edit();
		Window window = firstUseDialog.getWindow();
		window.setFlags(WindowManager.LayoutParams.ALPHA_CHANGED,
				WindowManager.LayoutParams.ALPHA_CHANGED);
		firstUseDialog.setTitle("自动应答");
		firstUseDialog.setContentView(R.layout.first_use_dialog);
		Button okButton = (Button) firstUseDialog
				.findViewById(R.id.howToUseOkbutton);
		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				useMarkEditor.putBoolean("firstUseMark", false);
				useMarkEditor.commit();
				firstUseDialog.cancel();
			}

		});
		firstUseDialog.show();
	}

	public List<Map<String, Object>> getData() {
		userDBHelper = new UserData(MainActivity.this, "userDB", null, 1);
		userDB = userDBHelper.getWritableDatabase();
		Cursor myResult = userDB.query("USERDB", null, null, null, null, null,
				null);
		List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
		Map<String, Object> mapData;

		if (myResult.moveToFirst())
			do {
				mapData = new HashMap<String, Object>();
				mapData.put("dataTime",
						myResult.getString(0) + "-" + myResult.getString(1));
				mapData.put("dataMessage", myResult.getString(7));
				mapData.put("setMark", myResult.getString(9));
				listData.add(mapData);
			} while (myResult.moveToNext());
		myResult.close();
		return listData;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		MenuItem suggest = menu.getItem(0);
		MenuItem about = menu.getItem(1);
		MenuItem help = menu.getItem(2);
		MenuItem log = menu.getItem(3);
		suggest.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {

				AlertDialog.Builder enquire = new AlertDialog.Builder(
						MainActivity.this);
				enquire.setTitle("直接发送邮件？");
				enquire.setMessage("点击确认打开Gmail并发送邮件至bfyycdi@gmail.com，或者记下此地址手动发送邮件？");
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
				startActivity(new Intent(MainActivity.this, About.class));
				return false;
			}

		});
		help.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				howToUse();
				return false;
			}
		});
		log.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem arg0) {
				startActivity(new Intent(MainActivity.this, UserLog.class));
				return false;
			}

		});

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(userData.get(info.position).get("dataTime")
				.toString());
		menu.add(0, Menu.FIRST, Menu.NONE, "删除设定");
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "开始所有设定");
		menu.add(0, Menu.FIRST + 2, Menu.NONE, "停止所有设定");
		menu.add(0, Menu.FIRST + 3, Menu.NONE, "删除所有设定");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 如果是返回键,直接返回到桌面
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder enquire = new AlertDialog.Builder(
					MainActivity.this);
			enquire.setTitle("确认退出？");
			enquire.setMessage("现在退出程序？退出程序后依然可以提供服务！");
			enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					System.exit(0);
				}
			});
			enquire.setNegativeButton("取消", new Dialog.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			enquire.show();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case Menu.FIRST:
			deleteItem(info.position);
			break;
		case Menu.FIRST + 1:
			startAllItem();
			break;
		case Menu.FIRST + 2:
			stopAllItem();
			break;
		case Menu.FIRST + 3:
			deleteAllItem();
			break;
		}
		return true;
	}

	private void stopAllItem() {
		ContentValues cv = new ContentValues();
		cv.put("SETMARK", 0);
		userDB.update("USERDB", cv, "SETMARK=" + 1, null);
		refreshListView();
	}

	private void deleteAllItem() {
		AlertDialog.Builder enquire = new AlertDialog.Builder(MainActivity.this);
		enquire.setTitle("确认删除？");
		enquire.setMessage("您确定要删除所有设定吗？");
		enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				userDB.delete("USERDB", null, null);
				refreshListView();
			}
		});
		enquire.setNegativeButton("取消", new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		enquire.show();
	}

	private void startAllItem() {
		ContentValues cv = new ContentValues();
		cv.put("SETMARK", 1);
		userDB.update("USERDB", cv, "SETMARK=" + 0, null);
		refreshListView();
	}

	private void deleteItem(int position) {
		final int nowSite = position;
		AlertDialog.Builder enquire = new AlertDialog.Builder(MainActivity.this);
		enquire.setTitle("确认删除？");
		enquire.setMessage("您确定要删除当前设定吗？");
		enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				deleteDataItem(nowSite);
				refreshListView();
			}
		});
		enquire.setNegativeButton("取消", new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		enquire.show();
	}

	protected void deleteDataItem(int nowSite) {
		userDB.delete("USERDB", "ROWID=" + nowSite, null);
		Cursor ItemAfterDeleteItem = userDB.query("USERDB", null, "ROWID>"
				+ nowSite, null, null, null, null);
		if (ItemAfterDeleteItem.moveToFirst()) {
			int nowItemId = ItemAfterDeleteItem.getInt(8);
			do {
				ContentValues cv = new ContentValues();
				cv.put("ROWID", nowItemId - 1);
				userDB.update("USERDB", cv, "ROWID=" + nowItemId, null);
				nowItemId++;
			} while (ItemAfterDeleteItem.moveToNext());
		}
	}

	private void refreshListView() {
		userData = getData();
		adapter.notifyDataSetChanged();
	}

	public final class ViewHolder {
		public TextView dataTime;
		public TextView dataMessage;
		public CheckBox openCheckBox;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return userData.size();
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View converView, ViewGroup parent) {
			ViewHolder holder = null;
			final int itemId = position;
			if (converView == null) {
				holder = new ViewHolder();
				converView = mInflater.inflate(R.layout.itemview, null);
				holder.dataTime = (TextView) converView
						.findViewById(R.id.dataTime);
				holder.dataMessage = (TextView) converView
						.findViewById(R.id.dataMessage);
				holder.openCheckBox = (CheckBox) converView
						.findViewById(R.id.openCheckBox);
				converView.setTag(holder);
			} else {
				holder = (ViewHolder) converView.getTag();
			}
			holder.dataTime.setText((String) userData.get(position).get(
					"dataTime"));
			holder.dataMessage.setText((String) userData.get(position).get(
					"dataMessage"));
			holder.openCheckBox.setOnCheckedChangeListener(null);
			holder.openCheckBox.setChecked(Integer.parseInt(userData
					.get(position).get("setMark").toString()) == 1 ? true
					: false);
			holder.openCheckBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							ContentValues cv = new ContentValues();
							cv.put("SETMARK", arg1 ? 1 : 0);
							userDB.update("USERDB", cv, "ROWID=" + itemId, null);
						}

					});
			return converView;
		}

	}

	public class UserData extends SQLiteOpenHelper {

		public UserData(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE USERDB" + "(STARTTIME TEXT NOT NULL," + // 0
					"ENDTIME TEXT NOT NULL," + // 1
					"RINGTIME INTEGER NOT NULL," + // 2
					"ANSWERWAY INEGER NOT NULL," + // 3
					"ENQUIRE INEGER NOT NULL," + // 4
					"SUREPAUSE INEGER NOT NULL," + // 5
					"PAUSETIME INEGER NOT NULL," + // 6
					"MESSAGE TEXT NOT NULL," + // 7
					"ROWID INEGER NOT NULL," + // 8
					"SETMARK INEGER NOT NULL)");// 9
			db.execSQL("CREATE TABLE USERLOG" + "(TIME TEXT NOT NULL," + // 0
					"TELNUM TEXT NOT NULL," + // 1
					"ANSWERWAY TEXT NOT NULL," + // 2
					"NAME TEXT NOT NULL," + // 3
					"ROWID INTEGER NOT NULL)");// 4
			insertDefault(db);
		}

		private void insertDefault(SQLiteDatabase db) {
			ContentValues cv = new ContentValues();
			cv.put("STARTTIME", "9:00");
			cv.put("ENDTIME", "11:30");
			cv.put("RINGTIME", 15);
			cv.put("ANSWERWAY", 0);
			cv.put("ENQUIRE", 0);
			cv.put("SUREPAUSE", 0);
			cv.put("PAUSETIME", 360);
			cv.put("MESSAGE", "主人现在不方便接听你的电话，我将通知主人尽快回复。");
			cv.put("ROWID", 0);
			cv.put("SETMARK", 0);
			db.insert("USERDB", null, cv);
			cv.clear();
			cv.put("STARTTIME", "14:00");
			cv.put("ENDTIME", "17:25");
			cv.put("RINGTIME", 15);
			cv.put("ANSWERWAY", 0);
			cv.put("ENQUIRE", 0);
			cv.put("SUREPAUSE", 0);
			cv.put("PAUSETIME", 360);
			cv.put("MESSAGE", "主人现在不方便接听你的电话，我将通知主人尽快回复。");
			cv.put("ROWID", 1);
			cv.put("SETMARK", 0);
			db.insert("USERDB", null, cv);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}
}
