package com.bfyycdi.commhleper;

import android.content.*;
import android.os.Bundle;
import android.telephony.*;

public class StartBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Intent intent = new Intent(arg0, Listenservice.class);
		intent.putExtra("answerWay", arg1.getAction().toString());
		TelephonyManager tm = (TelephonyManager) arg0
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (arg1.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = arg1.getExtras();
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			String to = messages[0].getOriginatingAddress();
			intent.putExtra("telNum", to);
			arg0.startService(intent);
		}
		if (arg1.getAction()
				.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
				Bundle bundle = arg1.getExtras();
				String telNum = bundle.getString("incoming_number");
				intent.putExtra("telNum", telNum);
				arg0.startService(intent);
			}

		}
		if (arg1.getAction().equals("android.intent.action.DATE_CHANGED")) {
			arg0.startService(intent);
		}

	}

}
