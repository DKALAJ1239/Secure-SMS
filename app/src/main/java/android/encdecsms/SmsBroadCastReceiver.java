package android.encdecsms;
import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

public class SmsBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();

        // Specify the bundle to get object based on SMS protocol "pdus"
        Object[] object = (Object[]) bundle.get("pdus");
        SmsMessage[] sms = new SmsMessage[object.length];

        String msgContent = "";
        String originNum = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < object.length; i++)
        {
            sms[i] = SmsMessage.createFromPdu((byte[]) object[i]);

            // get the received SMS content
            msgContent = sms[i].getDisplayMessageBody();

            // get the sender phone number
            originNum = sms[i].getDisplayOriginatingAddress();

            // aggregate the messages together when long message are fragmented
            sb.append(msgContent);

            // abort broadcast to cellphone inbox
            abortBroadcast();
        }

        EncDecSMSActivity.intent = new Intent(context, DisplaySMSActivity.class);
        EncDecSMSActivity.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        EncDecSMSActivity.intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // fill the sender's phone number into Intent
        EncDecSMSActivity.intent.putExtra("originNum", originNum);

        // fill the entire message body into Intent
        EncDecSMSActivity.intent.putExtra("msgContent", new String(sb));

        Log.d("success", "Message received!\nNum: " + originNum + "\nContent: " + new String(sb));
    }
}
