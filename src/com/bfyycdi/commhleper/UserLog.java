package com.bfyycdi.commhleper;

import java.util.*;

import android.net.Uri;
import android.os.Bundle;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class UserLog extends Activity {
	ListView logList;
	SQLiteDatabase userDB;
	LogAdapter logAdapter;
	List<Map<String, Object>> logData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userDB = openOrCreateDatabase("userDB", Context.MODE_PRIVATE, null);
		setContentView(R.layout.activity_user_log);
		logData = getData();
		logList = (ListView) findViewById(R.id.logList);
		logAdapter = new LogAdapter(this);
		logList.setAdapter(logAdapter);
		registerForContextMenu(logList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("应答日志");
		menu.add(0, Menu.FIRST, Menu.NONE, "拨打此号码");
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "向此号码发送短信");
		menu.add(0, Menu.FIRST + 2, Menu.NONE, "删除选中记录");
		menu.add(0, Menu.FIRST + 3, Menu.NONE, "删除所有记录");

	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case Menu.FIRST:
			callPhone(info.position);
			break;
		case Menu.FIRST + 1:
			sendSMS(info.position);
			break;
		case Menu.FIRST + 2:
			deleteChecked(info.position);
			break;
		case Menu.FIRST + 3:
			deleteAll();
			break;
		}
		return true;
	}

	private void sendSMS(int position) {
		Cursor result = userDB.query("USERLOG", null, "ROWID=" + position,
				null, null, null, null);
		result.moveToFirst();
		String telNum = result.getString(1);
		Uri uri = Uri.parse("smsto:" + telNum);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		startActivity(intent);
	}

	private void callPhone(int position) {
		Cursor result = userDB.query("USERLOG", null, "ROWID=" + position,
				null, null, null, null);
		result.moveToFirst();
		String telNum = result.getString(1);
		Uri uri = Uri.parse("tel:" + telNum);
		Intent intent = new Intent(Intent.ACTION_CALL, uri);
		startActivity(intent);
	}

	private void deleteAll() {
		AlertDialog.Builder enquire = new AlertDialog.Builder(UserLog.this);
		enquire.setTitle("确认删除？");
		enquire.setMessage("您确定要删除所有记录吗？");
		enquire.setPositiveButton("确定", new Dialog.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				userDB.delete("USERLOG", null, null);
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

	private void deleteChecked(int position) {
		delelteDataItem(position);
		refreshListView();
	}

	private void refreshListView() {
		logData = getData();
		logAdapter.notifyDataSetChanged();
	}

	private void delelteDataItem(int position) {
		userDB.delete("USERLOG", "ROWID=" + position, null);
		Cursor itemAfterDeleteItem = userDB.query("USERLOG", null, "ROWID>"
				+ position, null, null, null, null);
		if (itemAfterDeleteItem.moveToFirst()) {
			int nowItemId = itemAfterDeleteItem.getInt(4);
			do {
				ContentValues cv = new ContentValues();
				cv.put("ROWID", nowItemId - 1);
				userDB.update("USERLOG", cv, "ROWID=" + nowItemId, null);
				nowItemId++;
			} while (itemAfterDeleteItem.moveToNext());
		}
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Cursor result = userDB.query("USERLOG", null, null, null, null, null,
				null);
		Map<String, Object> map;
		String info = "";
		if (result.moveToFirst()) {
			do {
				info = result.getString(0) + "   " + result.getString(3)
						+ result.getString(1) + result.getString(2);
				map = new HashMap<String, Object>();
				map.put("text", info);
				list.add(map);
			} while (result.moveToNext());
		}
		result.close();
		return list;
	}

	public final class ViewHolder {
		public TextView logItem;
	}

	public class LogAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public LogAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return logData.size();
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View converView, ViewGroup parent) {
			ViewHolder holder = null;
			if (converView == null) {
				holder = new ViewHolder();
				converView = mInflater.inflate(R.layout.log_list, null);
				holder.logItem = (TextView) converView
						.findViewById(R.id.logItem);
				converView.setTag(holder);
			} else {
				holder = (ViewHolder) converView.getTag();
			}
			holder.logItem.setText((String) logData.get(position).get("text"));
			return converView;
		}
	}
}
