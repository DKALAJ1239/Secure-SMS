package android.encdecsms;
import java.security.Key;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EncDecSMSActivity extends Activity
{
    /** Called when the activity is first created. */
    EditText recNum;
    EditText secretKey;
    EditText msgContent;
    Button send;
    Button cancel;
    Button refresh;
    public static Intent intent;

    @Override
    public void onStart()
    {
        super.onStart();
        intent = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        intent = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Check if SEND_SMS permission is not granted
        if (ContextCompat.checkSelfPermission(EncDecSMSActivity.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            // Request permission to SEND_SMS
            if (ActivityCompat.shouldShowRequestPermissionRationale(EncDecSMSActivity.this,
                    Manifest.permission.SEND_SMS))
            {
                ActivityCompat.requestPermissions(EncDecSMSActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            }
            else
            {
                ActivityCompat.requestPermissions(EncDecSMSActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }

        recNum = (EditText) findViewById(R.id.recNum);
        secretKey = (EditText) findViewById(R.id.secretKey);
        msgContent = (EditText) findViewById(R.id.msgContent);
        send = (Button) findViewById(R.id.Send);
        cancel = (Button) findViewById(R.id.back);
        refresh = (Button) findViewById(R.id.refresh);

        // finish the activity when click Cancel button
        cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });

        // refresh to check for received messages
        refresh.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Check if message has been received and start new activity with given intent
                if (intent != null)
                {
                    startActivity(intent);
                }
                else
                {
                    // If intent is null then display No new messages! message
                    Toast.makeText(EncDecSMSActivity.this, "No new messages!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // encrypt the message and send when click Send button
        send.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String recNumString = recNum.getText().toString();
                String secretKeyString = secretKey.getText().toString();
                String msgContentString = msgContent.getText().toString();

                // check for the validity of the user input
                // key length should be 16 characters as defined by AES-128-bit
                if (recNumString.length() > 0
                        && secretKeyString.length() > 0
                        && msgContentString.length() > 0
                        && secretKeyString.length() == 16) {
                    // encrypt the message
                    byte[] encryptedMsg = encryptSMS(secretKeyString, msgContentString);

                    // convert the byte array to hex format in order for
                    // transmission
                    String msgString = byte2hex(encryptedMsg);

                    // send the message through SMS
                    sendSMS(recNumString, msgString);

                    // Display sent message
                    Toast.makeText(EncDecSMSActivity.this, "SMS Sent", Toast.LENGTH_LONG).show();

                    // finish
                    finish();
                }
                else
                    Toast.makeText(getBaseContext(),
                            "Please enter phone number, secret key and the message. Secret key must be 16 characters!",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void sendSMS(String recNumString, String encryptedMsg)
    {
        try
        {
            // get a SmsManager
            SmsManager smsManager = SmsManager.getDefault();

            // Message may exceed 160 characters
            // need to divide the message into multiples
            ArrayList<String> parts = smsManager.divideMessage(encryptedMsg);

            smsManager.sendMultipartTextMessage(recNumString, null, parts,
                    null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // utility function
    public static String byte2hex(byte[] b)
    {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs += ("0" + stmp);
            else
                hs += stmp;
        }
        return hs.toUpperCase();
    }

    // encryption function
    public static byte[] encryptSMS(String secretKeyString, String msgContentString)
    {
        try
        {
            byte[] returnArray;

            // generate AES secret key from user input
            Key key = generateKey(secretKeyString);

            // specify the cipher algorithm using AES
            Cipher c = Cipher.getInstance("AES");

            // specify the encryption mode
            c.init(Cipher.ENCRYPT_MODE, key);

            // encrypt
            returnArray = c.doFinal(msgContentString.getBytes());

            return returnArray;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            byte[] returnArray = null;
            return returnArray;
        }
    }

    private static Key generateKey(String secretKeyString) throws Exception
    {
        // generate secret key from string
        Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");
        return key;
    }

    /* Handles granting permissions */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode)
        {
            case 1: {
                // Check if user has given permission to SEND_SMS
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // If permission is granted then display "Permission granted" text
                    if (ContextCompat.checkSelfPermission(EncDecSMSActivity.this,
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    }
                    // If permission is not granted then display "Permission denied!" text
                    else
                    {
                        Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
